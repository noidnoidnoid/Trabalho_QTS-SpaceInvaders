package br.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * System tests for the gameplay requirements, through the browser.
 *
 * Owner: Roger Egito (220083118).
 *
 * RF05 A and D move the ship without leaving the screen.
 * RF06 a shot that hits the level 1 alien opens level 2.
 * RF07 an alien that reaches the ship ends the game (Game Over).
 *
 * The ship position is checked on level 2 because that is where the game publishes
 * it (StateSnapshot): Level1State keeps its ship private and belongs to another member.
 */
@DisplayName("Sistema: jogabilidade (Selenium)")
class GameplaySystemIT extends SystemTestBase {

    private static final int SHIP_START_X = 400, SHIP_MAX_X = 700, SHIP_MIN_X = 0;

    @Test
    @DisplayName("CTS-05 (RF06): segurar ESPACO na fase 1 acerta o alien e abre a fase 2 com 8 aliens")
    void shootingTheAlienOpensLevel2() throws Exception {
        reachLevel2();

        page.screenshot("CTS-05-fase-2");
        assertEquals(Integer.valueOf(SHIP_START_X), page.shipX(), "level 2 starts with a new ship at the center");
        assertEquals(Integer.valueOf(8), page.aliens());
    }

    @Test
    @DisplayName("CTS-06 (RF05): D leva a nave ate a borda direita e A ate a esquerda, sem passar delas")
    void shipStaysInsideTheScreen() throws Exception {
        reachLevel2();

        page.press(GamePage.D);
        page.waitUntil(Duration.ofSeconds(5), p -> Integer.valueOf(SHIP_MAX_X).equals(p.shipX()));
        assertStaysAt(SHIP_MAX_X);
        page.release(GamePage.D);
        page.screenshot("CTS-06-nave-na-borda-direita");

        page.press(GamePage.A);
        page.waitUntil(Duration.ofSeconds(5), p -> Integer.valueOf(SHIP_MIN_X).equals(p.shipX()));
        assertStaysAt(SHIP_MIN_X);
        page.release(GamePage.A);
    }

    /** Keeps the key held a little longer and checks the ship did not cross the edge. */
    private void assertStaysAt(int edge) throws InterruptedException {
        Thread.sleep(500); // 30 more frames pushing against the edge
        assertEquals(Integer.valueOf(edge), page.shipX());
    }

    @Test
    @DisplayName("CTS-07 (RF07): sem atirar, o alien desce ate a nave e o jogo acaba em Game Over")
    void alienReachingTheShipIsGameOver() throws Exception {
        startLevel1();

        // The alien descends one row per wall bounce: about 7 bounces, roughly 35 s.
        page.waitForState("GAMEOVER", Duration.ofSeconds(90));

        page.screenshot("CTS-07-game-over");
    }
}
