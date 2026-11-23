package br.states;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import br.characters.Alien;
import br.characters.Shot;

// Prova simples (sem JUnit) dos 3 casos planejados para o Level2StateTest (issue #10).
public class Prova10 {
    static Graphics g() { return new BufferedImage(800, 1080, BufferedImage.TYPE_INT_RGB).getGraphics(); }
    static Level2State novo() { Level2State s = new Level2State(); s.init(); return s; }
    static void check(String nome, boolean ok, String detalhe) {
        System.out.println((ok ? "PASSOU " : "FALHOU ") + nome + " | " + detalhe);
    }
    public static void main(String[] a) {
        new StateManager();
        Level2State s = novo();
        s.aliens.get(0).setVisible(false); s.aliens.get(1).setVisible(false);
        s.render(g());
        long inv = s.aliens.stream().filter(x -> !x.isVisible()).count();
        check("T1 remove aliens invisiveis consecutivos no mesmo frame", inv == 0 && s.aliens.size() == 6,
              "tamanho=" + s.aliens.size() + " invisiveis=" + inv);

        s = novo();
        for (Alien al : s.aliens) al.setVisible(false);
        s.render(g());
        check("T2 vitoria no mesmo frame em que o ultimo alien some", s.victory,
              "victory=" + s.victory + " aliens na lista=" + s.aliens.size());

        s = novo();
        s.ship.simpleShot(); s.ship.simpleShot();
        Shot primeiro = s.shots.get(0), segundo = s.shots.get(1);
        primeiro.setVisible(false);
        int y0 = segundo.getY();
        s.render(g());
        check("T3 tiro depois de um tiro removido continua subindo", segundo.getY() < y0 && !s.shots.contains(primeiro),
              "y antes=" + y0 + " y depois=" + segundo.getY() + " tiros=" + s.shots.size());
    }
}
