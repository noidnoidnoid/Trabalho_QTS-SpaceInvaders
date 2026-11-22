package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.event.KeyEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.Game;
import br.characters.Ship;

/**
 * Integration: keyboard events -> StateManager -> MenuState / HelpState -> Level1State.
 *
 * Owner: Roger Egito (220083118).
 *
 * Every class is real except the menu's theme player and exit action (see
 * IntegrationSupport). The events go through StateManager.keyReleased, the same
 * entry point Display registers on the window.
 */
@DisplayName("Integracao: navegacao entre menu, ajuda e fase 1")
class MenuNavigationIT {

    private IntegrationSupport game;

    @BeforeEach
    void setUp() {
        game = new IntegrationSupport();
        StateManager.setState(StateManager.MENU); // what Game() does at startup
    }

    @AfterEach
    void tearDown() {
        game.close();
    }

    @Test
    @DisplayName("CTI-01: jogo comeca no menu e a musica toca uma vez")
    void startsAtMenuPlayingTheme() {
        assertInstanceOf(MenuState.class, game.current());
        verify(game.themeSound, times(1)).play();
    }

    @Test
    @DisplayName("CTI-02: ENTER em START abre a fase 1 com a nave no centro e um alien")
    void enterOnStartOpensLevel1() {
        game.tap(KeyEvent.VK_ENTER);

        assertEquals(StateManager.LEVEL1, StateManager.currentState);
        Level1State level1 = assertInstanceOf(Level1State.class, game.current());
        Ship ship = IntegrationSupport.readField(level1, "ship");
        assertEquals(Game.WIDTH / 2, ship.getX());
        assertNotNull(IntegrationSupport.readField(level1, "alien"));
    }

    @Test
    @DisplayName("CTI-03: S + ENTER abre a ajuda, e ENTER em RETURN volta ao menu reiniciando a musica")
    void helpAndBack() {
        game.tap(KeyEvent.VK_S);
        game.tap(KeyEvent.VK_ENTER);
        assertInstanceOf(HelpState.class, game.current());

        game.tap(KeyEvent.VK_ENTER); // HelpState starts on RETURN

        assertInstanceOf(MenuState.class, game.current());
        verify(game.themeSound, times(2)).play();
    }

    @Test
    @DisplayName("CTI-04: W em START vai para EXIT, e ENTER pede para encerrar o jogo")
    void wrapToExitAndConfirm() {
        game.tap(KeyEvent.VK_W);
        game.tap(KeyEvent.VK_ENTER);

        assertEquals(1, game.exitRequests);
        assertInstanceOf(MenuState.class, game.current());
    }

    @Test
    @DisplayName("CTI-05: o menu so reage ao soltar a tecla, segurar ENTER nao seleciona")
    void holdingEnterDoesNotSelect() {
        game.press(KeyEvent.VK_ENTER);

        assertInstanceOf(MenuState.class, game.current());
    }

    @Test
    @DisplayName("CTI-06: teclas da fase (A, D, ESPACO) no menu nao mudam a tela nem a opcao")
    void gameplayKeysDoNothingInMenu() {
        game.tap(KeyEvent.VK_A);
        game.tap(KeyEvent.VK_D);
        game.tap(KeyEvent.VK_SPACE);
        game.frame();
        game.tap(KeyEvent.VK_ENTER); // still on START

        assertEquals(StateManager.LEVEL1, StateManager.currentState);
    }
}
