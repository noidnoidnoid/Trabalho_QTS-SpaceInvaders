package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.audio.AudioPlayer;
import br.characters.Ship;

/**
 * Unit tests for br.states.StateSnapshot, what the web adapter shows to the browser.
 *
 * Owner: Roger Egito (220083118).
 */
@DisplayName("StateSnapshot - estado publicado para a versao web")
class StateSnapshotTest {

    private State[] originalStates;
    private int originalCurrentState;

    @BeforeEach
    void saveStateManager() {
        originalStates = StateManager.states.clone();
        originalCurrentState = StateManager.currentState;
    }

    @AfterEach
    void restoreStateManager() {
        System.arraycopy(originalStates, 0, StateManager.states, 0, originalStates.length);
        StateManager.currentState = originalCurrentState;
    }

    private static void show(int index, State state) {
        StateManager.states[index] = state;
        StateManager.currentState = index;
    }

    @ParameterizedTest(name = "choice {0} -> {1}")
    @CsvSource({ "0, START", "1, HELP", "2, EXIT" })
    @DisplayName("no menu: nome da tela e opcao selecionada")
    void menuShowsSelectedOption(int choice, String option) {
        MenuState menu = new MenuState(mock(AudioPlayer.class), () -> {});
        menu.choice = choice;
        show(StateManager.MENU, menu);

        assertEquals(new StateSnapshot("MENU", option, null, null, null), StateSnapshot.capture());
    }

    @Test
    @DisplayName("no menu com choice fora do intervalo: opcao nula")
    void menuWithInvalidChoice() {
        MenuState menu = new MenuState(mock(AudioPlayer.class), () -> {});
        menu.choice = 3;
        show(StateManager.MENU, menu);

        assertEquals(new StateSnapshot("MENU", null, null, null, null), StateSnapshot.capture());
    }

    @Test
    @DisplayName("na fase 2: posicao da nave, aliens vivos e tiros")
    void level2ShowsShipAliensAndShots() {
        Level2State level2 = new Level2State();
        level2.init();
        level2.aliens.remove(0);
        level2.ship.simpleShot();
        show(StateManager.LEVEL2, level2);

        assertEquals(new StateSnapshot("LEVEL2", null, level2.ship.getX(), 7, 1), StateSnapshot.capture());
    }

    @Test
    @DisplayName("fase 2 ainda nao inicializada: so o nome da tela")
    void level2BeforeInit() {
        show(StateManager.LEVEL2, new Level2State());

        assertEquals(new StateSnapshot("LEVEL2", null, null, null, null), StateSnapshot.capture());
    }

    @ParameterizedTest(name = "indice {0} -> {1}")
    @CsvSource({ "1, HELP", "2, GAMEOVER", "3, YOUWIN", "4, LEVEL1" })
    @DisplayName("demais telas: so o nome")
    void otherScreensShowOnlyName(int index, String name) {
        show(index, mock(State.class));

        assertEquals(new StateSnapshot(name, null, null, null, null), StateSnapshot.capture());
    }

    @ParameterizedTest(name = "indice {0}")
    @CsvSource({ "-1", "6" })
    @DisplayName("indice fora da tabela vira UNKNOWN sem lancar excecao")
    void unknownIndex(int index) {
        // getState() throws ArrayIndexOutOfBoundsException for these indexes, so capture()
        // must check the index first. The first version did not, and this case caught it.
        StateManager.currentState = index;

        assertEquals(new StateSnapshot("UNKNOWN", null, null, null, null), StateSnapshot.capture());
    }

    @Test
    @DisplayName("a nave do snapshot e a mesma da fase (nao uma copia desatualizada)")
    void shipPositionFollowsTheShip() {
        Level2State level2 = new Level2State();
        level2.init();
        show(StateManager.LEVEL2, level2);
        Ship ship = level2.ship;

        ship.moveShip(1);

        assertEquals(ship.getX(), StateSnapshot.capture().shipX());
    }
}
