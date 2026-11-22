package br.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Starts the web version of the game (br.web.WebLaunch) as a separate Java process.
 *
 * A separate process per test, and not the game inside the test JVM, because:
 * - the game keeps its state in static fields (StateManager, KeyManager, Shot), so a
 *   fresh process is the only clean way to start every test from the menu;
 * - the EXIT option calls System.exit, which would kill the test runner.
 * This also makes the test black box at the process level: it only talks HTTP to
 * the game, through a real browser.
 */
final class GameServerProcess implements AutoCloseable {

    private static final String READY_PREFIX = "Space Invaders web: ";
    private static final Duration STARTUP_TIMEOUT = Duration.ofSeconds(60);

    private final Process process;
    private final int port;
    private final List<String> output = new CopyOnWriteArrayList<>();

    private GameServerProcess(Process process, int port) {
        this.process = process;
        this.port = port;
    }

    static GameServerProcess start() throws IOException, InterruptedException {
        int port = freePort();
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        new File("target/evidencias-sistema").mkdirs();
        ProcessBuilder builder = new ProcessBuilder(java,
                "-Djava.awt.headless=true",
                // GC pauses of the game process, one file per process, to check whether
                // a slow frame measured by the performance tests was a GC pause.
                "-Xlog:gc:file=target/evidencias-sistema/gc-" + port + ".log",
                "-cp", System.getProperty("java.class.path"),
                "br.web.WebLaunch", String.valueOf(port))
                .directory(new File(System.getProperty("user.dir"))) // sprites use paths relative to the project root
                .redirectErrorStream(true);

        GameServerProcess server = new GameServerProcess(builder.start(), port);
        server.waitUntilReady();
        return server;
    }

    private void waitUntilReady() throws InterruptedException, IOException {
        CountDownLatch ready = new CountDownLatch(1);
        Thread reader = new Thread(() -> {
            try (BufferedReader lines = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = lines.readLine()) != null) {
                    output.add(line);
                    if (line.startsWith(READY_PREFIX)) ready.countDown();
                }
            } catch (IOException e) {
                output.add("reader stopped: " + e);
            }
        }, "game-server-output");
        reader.setDaemon(true);
        reader.start();

        if (!ready.await(STARTUP_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
            close();
            throw new IOException("game server did not start in " + STARTUP_TIMEOUT + ". Output: " + output);
        }
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    String url() {
        return "http://localhost:" + port;
    }

    /** Waits for the game to quit on its own. Returns its exit code, or null if it is still running. */
    Integer waitForExit(Duration timeout) throws InterruptedException {
        return process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS) ? process.exitValue() : null;
    }

    List<String> output() {
        return output;
    }

    @Override
    public void close() {
        if (process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly();
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }
}
