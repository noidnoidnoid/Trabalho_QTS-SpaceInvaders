package br.states;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.util.Random;
import br.characters.Alien;
import br.characters.Shot;
import br.input.KeyManager;

// Roda Level2State com teclas aleatorias (semente fixa) e imprime o hash do estado quadro a quadro.
// Usado para comparar a versao antes e depois da inspecao com PMD (docs/inspecao/level2state/).
// Compilar junto com src/br e rodar a partir da raiz, para cada versao da classe:
//   java -Djava.awt.headless=true -Dtiro=6 -cp "out:lib/*" br.states.SimL2     (tiro = 1 em N quadros; usados 6, 60 e 400)
// Resultado (as duas versoes deram hashes iguais):
//   tiro=1/6    partidas=50 quadros=14424 vitorias=50 derrotas=0  hash=5cf0797216e3db10
//   tiro=1/60   partidas=50 quadros=67719 vitorias=11 derrotas=39 hash=c9f144cce0ce8a3c
//   tiro=1/400  partidas=50 quadros=58597 vitorias=0  derrotas=50 hash=b3606c330ac69054
public class SimL2 {
    public static void main(String[] a) throws Exception {
        new StateManager();
        Graphics g = new BufferedImage(800, 1080, BufferedImage.TYPE_INT_RGB).getGraphics();
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        int vit = 0, derr = 0, frames = 0;
        for (int seed = 0; seed < 50; seed++) {
            Random r = new Random(seed);
            Level2State s = new Level2State();
            s.init();
            for (int f = 0; f < 20000 && !s.victory && !s.gameOver; f++) {
                KeyManager.a = r.nextInt(4) == 0; KeyManager.d = r.nextInt(4) == 0; KeyManager.space = r.nextInt(Integer.getInteger("tiro", 6)) == 0;
                s.update(); s.render(g); frames++;
                StringBuilder sb = new StringBuilder();
                sb.append(seed).append(':').append(f).append(' ').append(s.ship.getX()).append(' ').append(s.victory).append(s.gameOver);
                for (Alien al : s.aliens) sb.append(" A").append(al.getX()).append(',').append(al.getY()).append(al.isVisible());
                for (Shot sh : s.shots) sb.append(" S").append(sh.getX()).append(',').append(sh.getY()).append(sh.isVisible());
                md.update(sb.toString().getBytes());
            }
            if (s.victory) vit++; if (s.gameOver) derr++;
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : md.digest()) hex.append(String.format("%02x", b));
        System.out.println("partidas=50 quadros=" + frames + " vitorias=" + vit + " derrotas=" + derr + " hash=" + hex.substring(0, 16));
    }
}
