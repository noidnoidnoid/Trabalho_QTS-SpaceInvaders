import java.awt.Graphics;
import java.awt.image.BufferedImage;
import br.display.Background;
import br.characters.Alien;
import br.states.*;

public class RenderBench {
    public static void main(String[] a) {
        BufferedImage img = new BufferedImage(800, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics g = img.getGraphics();
        for (int i = 0; i < 30; i++) { new Background().print(g); } // warm-up
        int n = 200;
        long t = System.nanoTime();
        for (int i = 0; i < n; i++) { new Background(); }
        System.out.printf("new Background() (reads src/fundo.png): %.2f ms%n", (System.nanoTime() - t) / 1e6 / n);
        Background bg = new Background();
        t = System.nanoTime();
        for (int i = 0; i < n; i++) { bg.print(g); }
        System.out.printf("Background.print() only: %.2f ms%n", (System.nanoTime() - t) / 1e6 / n);
        t = System.nanoTime();
        for (int i = 0; i < n; i++) { new Alien(0, 0); }
        System.out.printf("new Alien() (reads PNG): %.2f ms%n", (System.nanoTime() - t) / 1e6 / n);
        new StateManager();
        StateManager.setState(StateManager.LEVEL2);
        t = System.nanoTime();
        for (int i = 0; i < n; i++) { StateManager.getState().render(g); }
        System.out.printf("Level2State.render() full frame: %.2f ms%n", (System.nanoTime() - t) / 1e6 / n);
        System.exit(0);
    }
}
