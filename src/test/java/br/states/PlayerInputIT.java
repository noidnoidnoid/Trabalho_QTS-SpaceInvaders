package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.Game;
import br.characters.Ship;

/**
 * Integration: keyboard -> KeyManager -> StateManager -> Level1State -> Ship.
 *
 * Owner: Roger Egito (220083118).
 *
 * The loop is driven with IntegrationSupport.tick(), which calls the collaborators
 * in the same order as Game.update(). That order matters: the state reads
 * KeyManager's flags before KeyManager refreshes them, so input lands one frame late.
 */
@DisplayName("Integracao: teclado ate a nave na fase 1")
class PlayerInputIT {

    private static final int SPEED = 6;

    private IntegrationSupport game;
    private Ship ship;

    @BeforeEach
    void setUp() {
        game = new IntegrationSupport();
        StateManager.setState(StateManager.LEVEL1);
        ship = IntegrationSupport.readField(game.current(), "ship");
    }

    @AfterEach
    void tearDown() {
        game.close();
    }

    @Test
    @DisplayName("CTI-07: D move a nave 6 px para a direita a partir do segundo frame (latencia de 1 frame)")
    void dMovesRightOneFrameLate() {
        game.press(KeyEvent.VK_D);

        game.tick();
        assertEquals(Game.WIDTH / 2, ship.getX(), "first frame: KeyManager not refreshed yet");

        game.tick();
        assertEquals(Game.WIDTH / 2 + SPEED, ship.getX());
    }

    @Test
    @DisplayName("CTI-08: segurar A leva a nave ate a borda esquerda e para em x = 0")
    void holdingAStopsAtLeftEdge() {
        game.press(KeyEvent.VK_A);

        for (int i = 0; i < 200; i++) game.tick();

        assertEquals(0, ship.getX());
    }

    @Test
    @DisplayName("CTI-09: segurar D leva a nave ate a borda direita e para em x = 700")
    void holdingDStopsAtRightEdge() {
        game.press(KeyEvent.VK_D);

        for (int i = 0; i < 200; i++) game.tick();

        assertEquals(Game.WIDTH - ship.getShipWidth(), ship.getX());
    }

    @Test
    @DisplayName("CTI-10: soltar D para a nave no frame seguinte")
    void releasingDStops() {
        game.press(KeyEvent.VK_D);
        game.tick();
        game.tick();
        int afterOneStep = ship.getX();

        game.release(KeyEvent.VK_D);
        game.tick(); // still moving: the release is read one frame late
        game.tick();
        game.tick();

        assertEquals(afterOneStep + SPEED, ship.getX());
    }

    @Test
    @DisplayName("CTI-11: A e D juntos se anulam")
    void aAndDCancelOut() {
        game.press(KeyEvent.VK_A);
        game.press(KeyEvent.VK_D);

        for (int i = 0; i < 10; i++) game.tick();

        assertEquals(Game.WIDTH / 2, ship.getX());
    }

    @Test
    @DisplayName("CTI-12: segurar ESPACO dispara um tiro por frame, saindo da posicao da nave")
    void holdingSpaceFiresEveryFrame() {
        game.press(KeyEvent.VK_SPACE);

        game.tick();
        game.tick();
        game.tick();

        assertEquals(2, ship.getShots().size(), "frames 2 and 3 fire, frame 1 is the input delay");
        assertEquals(ship.getX(), ship.getShots().get(0).getX());
        assertEquals(ship.getY(), ship.getShots().get(0).getY());
    }
}
