package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.characters.Alien;
import br.characters.Ship;
import br.characters.Shot;

/**
 * Integration: Level1State / Level2State + Ship + Shot + Alien + StateManager.
 *
 * Owner: Roger Egito (220083118).
 *
 * The frames are rendered for real into an off-screen image, so movement, collision
 * and the change of state all run through the same code as in the game. Positions
 * are only prepared (shot placed over an alien, alien moved onto the ship) to avoid
 * waiting hundreds of frames for the situation to happen on its own.
 */
@DisplayName("Integracao: progressao entre fases")
class LevelProgressionIT {

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
    @DisplayName("CTI-13: tiro que acerta o alien da fase 1 abre a fase 2 com 8 aliens")
    void hittingLevel1AlienOpensLevel2() {
        StateManager.setState(StateManager.LEVEL1);
        Level1State level1 = (Level1State) game.current();
        Ship ship = IntegrationSupport.readField(level1, "ship");
        Alien alien = IntegrationSupport.readField(level1, "alien");
        ship.getShots().add(new Shot(alien.getX() + 20, alien.getY() + 20));

        game.render();

        Level2State level2 = assertInstanceOf(Level2State.class, game.current());
        assertEquals(8, level2.aliens.size());
        assertEquals(0, level2.shots.size(), "level 2 starts with a new ship and no shots");
    }

    @Test
    @DisplayName("CTI-14: alien que alcanca a nave na fase 1 leva ao Game Over")
    void alienReachingShipOnLevel1IsGameOver() {
        StateManager.setState(StateManager.LEVEL1);
        Level1State level1 = (Level1State) game.current();
        Ship ship = IntegrationSupport.readField(level1, "ship");
        Alien alien = IntegrationSupport.readField(level1, "alien");
        IntegrationSupport.writeField(alien, "x", ship.getX());
        IntegrationSupport.writeField(alien, "y", ship.getY() - 30);

        game.render();

        assertInstanceOf(GameOverState.class, game.current());
    }

    @Test
    @DisplayName("CTI-15: destruir o ultimo alien da fase 2 leva ao You Win")
    void destroyingLastAlienOnLevel2IsVictory() {
        StateManager.setState(StateManager.LEVEL2);
        Level2State level2 = (Level2State) game.current();
        List<Alien> aliens = level2.aliens;
        aliens.subList(1, aliens.size()).clear();
        Alien last = aliens.get(0);
        level2.ship.getShots().add(new Shot(last.getX() + 20, last.getY() + 20));

        game.render(); // collision marks the alien as destroyed
        assertInstanceOf(Level2State.class, game.current());
        game.render(); // the destroyed alien leaves the list, and no aliens means victory

        assertInstanceOf(YouWinState.class, game.current());
    }

    @Test
    @DisplayName("CTI-16: alien que alcanca a nave na fase 2 leva ao Game Over")
    void alienReachingShipOnLevel2IsGameOver() {
        StateManager.setState(StateManager.LEVEL2);
        Level2State level2 = (Level2State) game.current();
        Alien alien = level2.aliens.get(3);
        IntegrationSupport.writeField(alien, "x", level2.ship.getX());
        IntegrationSupport.writeField(alien, "y", level2.ship.getY() - 30);

        game.render();

        assertInstanceOf(GameOverState.class, game.current());
    }

    @Test
    @DisplayName("CTI-17: do Game Over, ENTER em MENU volta ao menu")
    void gameOverBackToMenu() {
        StateManager.setState(StateManager.GAMEOVER);

        game.tap(java.awt.event.KeyEvent.VK_ENTER);

        assertInstanceOf(MenuState.class, game.current());
    }
}
