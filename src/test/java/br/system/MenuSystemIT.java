package br.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * System tests for the menu requirements, through the browser.
 *
 * Owner: Roger Egito (220083118).
 *
 * RF01 the menu starts on START, and W/S move the selection with wraparound.
 * RF02 ENTER on START opens level 1.
 * RF03 ENTER on HELP opens the help screen, and ENTER on RETURN goes back.
 * RF04 ENTER on EXIT closes the game.
 */
@DisplayName("Sistema: menu principal (Selenium)")
class MenuSystemIT extends SystemTestBase {

    @Test
    @DisplayName("CTS-01 (RF01): menu abre em START e W/S percorrem as opcoes com volta")
    void menuNavigation() throws Exception {
        assertEquals("MENU", page.state());
        assertEquals("START", page.menuOption());

        page.tap(GamePage.S);
        page.waitForMenuOption("HELP");
        page.tap(GamePage.S);
        page.waitForMenuOption("EXIT");
        page.screenshot("CTS-01-menu-exit-selecionado");
        page.tap(GamePage.S);
        page.waitForMenuOption("START"); // wraps from the last option to the first
        page.tap(GamePage.W);
        page.waitForMenuOption("EXIT");  // wraps from the first option to the last
    }

    @Test
    @DisplayName("CTS-02 (RF02): ENTER em START abre a fase 1")
    void startOpensLevel1() throws Exception {
        startLevel1();

        page.screenshot("CTS-02-fase-1");
        assertEquals("LEVEL1", page.state());
    }

    @Test
    @DisplayName("CTS-02b (RF02): o ENTER do teclado numerico tambem seleciona, como no jogo desktop")
    void numpadEnterAlsoSelects() {
        page.tap(GamePage.NUMPAD_ENTER);

        page.waitForState("LEVEL1", Duration.ofSeconds(3));
    }

    @Test
    @DisplayName("CTS-03 (RF03): HELP abre a ajuda e RETURN volta ao menu")
    void helpAndReturn() throws Exception {
        page.tap(GamePage.S);
        page.waitForMenuOption("HELP");
        page.tap(GamePage.ENTER);
        page.waitForState("HELP", Duration.ofSeconds(3));
        page.screenshot("CTS-03-ajuda");

        page.tap(GamePage.ENTER); // the help screen starts on RETURN

        page.waitForState("MENU", Duration.ofSeconds(3));
    }

    @Test
    @DisplayName("CTS-04 (RF04): EXIT encerra o jogo com codigo 0 e a pagina mostra desconectado")
    void exitClosesTheGame() throws Exception {
        page.tap(GamePage.W);
        page.waitForMenuOption("EXIT");

        page.tap(GamePage.ENTER);

        assertEquals(Integer.valueOf(0), server.waitForExit(Duration.ofSeconds(5)), "game process exit code");
        page.waitUntil(Duration.ofSeconds(5), p -> "desconectado".equals(p.connection()));
        page.screenshot("CTS-04-desconectado");
    }
}
