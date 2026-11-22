import java.awt.event.KeyEvent;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import br.states.*;
import br.web.WebGame;

public class SpikeProbe {
    static long gcMillis() {
        long t = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) t += b.getCollectionTime();
        return t;
    }
    public static void main(String[] a) throws Exception {
        // (a) cost of entering level 2, with sprites already cached
        new br.characters.Alien(0, 0);
        long t0 = System.nanoTime();
        Level2State l2 = new Level2State(); l2.init();
        System.out.printf("Level2State.init() (alien sprite cached): %.1f ms%n", (System.nanoTime() - t0) / 1e6);
        t0 = System.nanoTime();
        new br.characters.Ship();
        System.out.printf("new Ship() (reads PNG every time): %.1f ms%n", (System.nanoTime() - t0) / 1e6);

        // (b) real loop, frames fetched like the page does, GC time sampled each second
        boolean fetch = a.length == 0 || !a[0].equals("nofetch");
        WebGame game = new WebGame();
        game.start();
        Thread.sleep(1000);
        game.key(KeyEvent.VK_ENTER, true); game.key(KeyEvent.VK_ENTER, false);
        Thread.sleep(300);
        game.key(KeyEvent.VK_SPACE, true);
        while (!"LEVEL2".equals(game.snapshot().state())) Thread.sleep(5);
        game.key(KeyEvent.VK_SPACE, false);
        Thread fetcher = new Thread(() -> {
            try { while (true) { game.frameJpeg(); Thread.sleep(33); } } catch (Exception e) {}
        });
        fetcher.setDaemon(true);
        if (fetch) fetcher.start();
        System.out.println("fetching frames like the page: " + fetch);
        long gc = gcMillis();
        for (int s = 0; s < 20; s++) {
            Thread.sleep(1000);
            long now = gcMillis();
            System.out.printf("s=%2d state=%s slowest=%.1f ms gcTimeThisSecond=%d ms%n",
                s, game.snapshot().state(), game.slowestFrameMicros() / 1000.0, now - gc);
            gc = now;
        }
        System.exit(0);
    }
}
