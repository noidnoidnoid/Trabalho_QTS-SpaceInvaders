package br.system;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Non-functional system tests: Performance efficiency (ISO/IEC 25010), measured
 * through the browser on the running game.
 *
 * Owner: Roger Egito (220083118).
 *
 * RNF01 time behavior: during play the game keeps its 60 FPS target. We accept an
 *       average of at least 57 FPS (5% below the target) over 10 s on level 2, the
 *       heaviest screen (8 aliens).
 * RNF01b smoothness: over 10 s on level 2, sampling the slowest frame of each second,
 *       (a) the median of those samples fits the 16.7 ms frame budget and (b) at most
 *       one second has a frame above 33.3 ms (two budgets, one dropped frame).
 *       History of this criterion, kept on purpose:
 *       - Added after the first runs, because the average FPS passed while the slowest
 *         frame took ~50 ms: the loop catches up after a late frame and the average
 *         hides the stutter.
 *       - First version: no frame above 33.3 ms at all. After the fixes for issues #11
 *         and #12 the per-second slowest frames dropped to 3-10 ms, but an isolated
 *         ~35 ms spike still showed up in about 1 of 8 runs. GC was ruled out by
 *         measurement (longest stop-the-world pause 4.6 ms, see
 *         docs/ai/roger/evidencias/2026-11-22-investigacao-cts10.txt). The spike only
 *         appears with Chrome and Selenium running on the same machine, so it is most
 *         likely the OS taking the game thread off the CPU, which the game cannot control.
 *       - Revised to (a) + (b). The revised version still fails the code before the
 *         fix, where every sampled second was between 17 and 28 ms.
 * RNF02 response time: a key released in the browser changes what the page shows in
 *       at most 200 ms (measured from the release, since the menu reacts on release).
 *       Our threshold: 200 ms is a common rule of thumb for "feels immediate" in menus.
 *       It is a judgment call, not a standard.
 *
 * The measurements are written to target/evidencias-sistema/desempenho.txt and the
 * assertions keep the thresholds above, so a slow machine fails the test instead of
 * hiding the problem.
 */
@DisplayName("Sistema: desempenho (Selenium)")
class PerformanceSystemIT extends SystemTestBase {

    private static final double MIN_AVERAGE_FPS = 57.0;
    private static final long MAX_KEY_RESPONSE_MS = 200;
    private static final double FRAME_BUDGET_MS = 1000.0 / 60;     // 16.7 ms
    private static final double MAX_FRAME_MS = 2 * FRAME_BUDGET_MS; // 33.3 ms
    private static final int MAX_SECONDS_ABOVE_LIMIT = 1;

    @Test
    @DisplayName("CTS-08 (RNF01): fase 2 mantem media >= 57 FPS por 10 s")
    void level2KeepsFrameRate() throws Exception {
        reachLevel2();

        List<Integer> fps = new ArrayList<>();
        List<Double> slowest = new ArrayList<>();
        long end = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < end && "LEVEL2".equals(page.state())) {
            Integer sample = page.fps();
            if (sample != null) fps.add(sample);
            slowest.add(page.slowestFrameMs());
            Thread.sleep(500);
        }

        double average = fps.stream().mapToInt(Integer::intValue).average().orElse(0);
        double worstFrame = slowest.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN);
        record(String.format(Locale.ROOT,
                "CTS-08 RNF01 fase 2: %d amostras de FPS %s, media %.1f, min %d, frame mais lento do jogo %.1f ms (orcamento 16.7 ms)",
                fps.size(), fps, average, fps.isEmpty() ? 0 : Collections.min(fps), worstFrame));

        assertAll(
                () -> assertTrue(fps.size() >= 15, "level 2 ended before 10 s of samples: " + fps.size()),
                () -> assertTrue(average >= MIN_AVERAGE_FPS,
                        String.format(Locale.ROOT, "average %.1f FPS is below %.1f. Samples: %s", average, MIN_AVERAGE_FPS, fps)));
    }

    @Test
    @DisplayName("CTS-10 (RNF01b, issues #11 e #12): fase 2 com mediana do pior frame por segundo <= 16,7 ms e no maximo 1 segundo acima de 33,3 ms")
    void level2IsSmooth() throws Exception {
        reachLevel2();

        List<Double> slowest = new ArrayList<>();
        long end = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < end && "LEVEL2".equals(page.state())) {
            slowest.add(page.slowestFrameMs()); // each sample covers the previous second
            Thread.sleep(1000);
        }

        List<Double> sorted = new ArrayList<>(slowest);
        Collections.sort(sorted);
        int n = sorted.size();
        double median = n == 0 ? Double.NaN
                : n % 2 == 1 ? sorted.get(n / 2) : (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2;
        long secondsAboveLimit = slowest.stream().filter(ms -> ms > MAX_FRAME_MS).count();
        record(String.format(Locale.ROOT,
                "CTS-10 RNF01b fase 2: frame mais lento por segundo (ms) %s, mediana %.1f (limite %.1f), segundos acima de %.1f ms: %d (limite %d)",
                slowest, median, FRAME_BUDGET_MS, MAX_FRAME_MS, secondsAboveLimit, MAX_SECONDS_ABOVE_LIMIT));

        assertAll(
                () -> assertTrue(n >= 8, "level 2 ended before enough samples: " + n),
                () -> assertTrue(median <= FRAME_BUDGET_MS,
                        String.format(Locale.ROOT, "median slowest frame %.1f ms is above the %.1f ms budget. Per second: %s",
                                median, FRAME_BUDGET_MS, slowest)),
                () -> assertTrue(secondsAboveLimit <= MAX_SECONDS_ABOVE_LIMIT,
                        String.format(Locale.ROOT, "%d seconds had a frame above %.1f ms. Per second: %s",
                                secondsAboveLimit, MAX_FRAME_MS, slowest)));
    }

    @Test
    @DisplayName("CTS-09 (RNF02): tecla no navegador muda a tela em ate 200 ms")
    void keyResponseTime() throws Exception {
        List<Long> responses = new ArrayList<>();
        String[] cycle = { "HELP", "EXIT", "START" };

        for (int i = 0; i < 12; i++) {
            String expected = cycle[i % cycle.length];
            // The menu reacts when the key is released, so the clock starts at the release.
            // The first version started before a tap() that holds the key for 50 ms, which
            // added that pause to every sample and made the test fail at random.
            page.press(GamePage.S);
            long start = System.nanoTime();
            page.release(GamePage.S);
            page.waitForMenuOption(expected);
            responses.add((System.nanoTime() - start) / 1_000_000);
        }

        List<Long> sorted = new ArrayList<>(responses);
        Collections.sort(sorted);
        long median = sorted.get(sorted.size() / 2);
        long worst = sorted.get(sorted.size() - 1);
        record(String.format(Locale.ROOT, "CTS-09 RNF02 resposta a tecla (ms): %s, mediana %d, pior %d", responses, median, worst));

        assertTrue(worst <= MAX_KEY_RESPONSE_MS, "slowest response " + worst + " ms. All: " + responses);
    }

    private static void record(String line) throws IOException {
        Path file = Path.of("target", "evidencias-sistema", "desempenho.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, line + System.lineSeparator(),
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        System.out.println(line);
    }
}
