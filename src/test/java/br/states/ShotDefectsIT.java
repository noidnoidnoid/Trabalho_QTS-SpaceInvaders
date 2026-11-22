package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.characters.Alien;
import br.characters.Ship;
import br.characters.Shot;

/**
 * Integration: lifecycle of shots across Ship, Shot, Alien and the level states.
 *
 * Owner: Roger Egito (220083118).
 *
 * These cases describe the expected behavior of a shot. Both failed against the
 * original br.characters.Shot and pass after the fixes for issues #8 and #9.
 */
@DisplayName("Integracao: ciclo de vida dos tiros")
class ShotDefectsIT {

    private IntegrationSupport game;

    @BeforeEach
    void setUp() {
        game = new IntegrationSupport();
    }

    @AfterEach
    void tearDown() {
        game.close();
    }

    @Test
    @DisplayName("CTI-18 (issue #8): tiro que sai pelo topo da tela e removido da lista")
    void shotLeavingTheScreenIsRemoved() {
        StateManager.setState(StateManager.LEVEL1);
        Ship ship = IntegrationSupport.readField(game.current(), "ship");
        // x = 0: the level 1 alien starts at x = 400 moving right, so it never
        // crosses this column during the 200 frames below.
        ship.getShots().add(new Shot(0, ship.getY()));

        for (int i = 0; i < 200; i++) game.render(); // 200 * 7 px is far past the top edge

        assertTrue(ship.getShots().isEmpty(),
                "shot is still in the list at y = " + (ship.getShots().isEmpty() ? "-" : ship.getShots().get(0).getY()));
    }

    @Test
    @DisplayName("CTI-19 (issue #9): acertar um alien nao apaga os outros tiros em voo")
    void hittingOneAlienKeepsOtherShots() {
        StateManager.setState(StateManager.LEVEL2);
        Level2State level2 = (Level2State) game.current();
        Alien target = level2.aliens.get(3);
        Shot hit = new Shot(target.getX() + 20, target.getY() + 20);
        Shot inFlight = new Shot(level2.ship.getX(), 700); // below the alien row, hits nothing yet
        level2.ship.getShots().add(hit);
        level2.ship.getShots().add(inFlight);

        game.render(); // hit collides with the target
        game.render(); // destroyed shots and aliens are cleaned up here
        // A third frame is needed: the cleanup loop skips the element after each
        // remove(e), so the shot right after a removed one survives one extra frame.
        // Checking after two frames hid the defect (both bugs cancelled out).
        game.render();

        assertTrue(level2.ship.getShots().contains(inFlight), "the shot that hit nothing disappeared");
        assertEquals(7, level2.aliens.size(), "exactly one alien destroyed");
    }
}
