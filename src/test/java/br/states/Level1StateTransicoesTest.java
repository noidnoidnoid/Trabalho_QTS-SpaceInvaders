package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.characters.Alien;
import br.characters.Ship;
import br.characters.Shot;
import br.display.Background;
import br.input.KeyManager;

/**
 * Behavior of Level1State.render(): screen transitions and what each frame draws
 * and moves. Complements Level1StateTest and Level1StateCoberturaMutacaoTest,
 * which cover input, screen limits and collision detection.
 *
 * Isolation:
 * - StateManager.states is filled with mocked State objects (restored afterwards),
 *   so a transition can be checked without building level 2 or the Game Over screen.
 * - The ship, the alien and the shots are Mockito spies placed in the private fields
 *   through reflection (the class belongs to another member, so its fields were not
 *   widened). Their real behavior runs, and the test only checks which calls happen.
 * - Background is replaced with mockConstruction, so no PNG is read.
 */
@DisplayName("Level1State - transicoes de tela e o que render() desenha")
class Level1StateTransicoesTest {

    private final Graphics g = mock(Graphics.class);
    private Level1State state;

    private State[] originalStates;
    private int originalCurrentState;

    @BeforeEach
    void setUp() {
        KeyManager.a = false;
        KeyManager.d = false;
        KeyManager.space = false;

        originalStates = StateManager.states.clone();
        originalCurrentState = StateManager.currentState;
        for (int i = 0; i < StateManager.states.length; i++) {
            StateManager.states[i] = mock(State.class);
        }
        StateManager.currentState = StateManager.LEVEL1;

        state = new Level1State();
        state.init();
    }

    @AfterEach
    void restoreStateManager() {
        System.arraycopy(originalStates, 0, StateManager.states, 0, originalStates.length);
        StateManager.currentState = originalCurrentState;
    }

    // ---------- helpers ----------

    private Object get(String name) throws Exception {
        Field f = Level1State.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(state);
    }

    private void set(String name, Object value) throws Exception {
        Field f = Level1State.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(state, value);
    }

    @SuppressWarnings("unchecked")
    private List<Shot> shots() throws Exception {
        return (List<Shot>) get("shots");
    }

    /** Replaces the alien with a spy whose rectangle is fixed by the test. */
    private Alien alienAt(Rectangle bounds) throws Exception {
        Alien alien = spy((Alien) get("alien"));
        doReturn(bounds).when(alien).getBounds();
        set("alien", alien);
        return alien;
    }

    /** Alien far from the ship and from any shot: nothing collides. */
    private Alien alienOutOfTheWay() throws Exception {
        return alienAt(new Rectangle(0, 0, 10, 10));
    }

    private void renderWithMockedBackground() {
        try (MockedConstruction<Background> backgrounds = mockConstruction(Background.class)) {
            state.render(g);
        }
    }

    // ---------- transitions ----------

    @Nested
    @DisplayName("troca de tela")
    class Transitions {

        @Test
        @DisplayName("tiro no alien leva para a fase 2 e a inicializa")
        void victoryGoesToLevel2() throws Exception {
            shots().add(new Shot(400, 300)); // far from the ship, see vitoriaNaoMarcaDerrota
            alienAt(new Rectangle(390, 250, 50, 100));

            renderWithMockedBackground();

            assertEquals(StateManager.LEVEL2, StateManager.currentState);
            verify(StateManager.states[StateManager.LEVEL2]).init();
        }

        @Test
        @DisplayName("alien na nave leva ao Game Over e o inicializa")
        void gameOverGoesToGameOverScreen() throws Exception {
            Ship ship = (Ship) get("ship");
            alienAt(ship.getBounds());

            renderWithMockedBackground();

            assertEquals(StateManager.GAMEOVER, StateManager.currentState);
            verify(StateManager.states[StateManager.GAMEOVER]).init();
        }

        @Test
        @DisplayName("sem colisao, a fase 1 continua e nenhuma tela e inicializada")
        void noCollisionStaysOnLevel1() throws Exception {
            alienOutOfTheWay();

            renderWithMockedBackground();

            assertEquals(StateManager.LEVEL1, StateManager.currentState);
            verifyNoInteractions(StateManager.states[StateManager.LEVEL2]);
            verifyNoInteractions(StateManager.states[StateManager.GAMEOVER]);
        }
    }

    // ---------- what a frame draws and moves ----------

    @Nested
    @DisplayName("cada frame de render()")
    class Frame {

        @Test
        @DisplayName("desenha o fundo, a nave e o alien")
        void drawsBackgroundShipAndAlien() throws Exception {
            Ship ship = spy((Ship) get("ship"));
            set("ship", ship);
            Alien alien = alienOutOfTheWay();

            try (MockedConstruction<Background> backgrounds = mockConstruction(Background.class)) {
                state.render(g);

                verify(backgrounds.constructed().get(0)).print(g);
            }
            verify(ship).paint(g);
            verify(alien).paint(g);
        }

        @Test
        @DisplayName("move o alien")
        void movesTheAlien() throws Exception {
            Alien alien = alienOutOfTheWay();

            renderWithMockedBackground();

            verify(alien).update();
        }

        @Test
        @DisplayName("desenha e move cada tiro visivel")
        void drawsAndMovesVisibleShots() throws Exception {
            alienOutOfTheWay();
            Shot shot = spy(new Shot(100, 600));
            shots().add(shot);

            renderWithMockedBackground();

            verify(shot).paint(g);
            verify(shot).update();
            assertTrue(shots().contains(shot));
        }

        @Test
        @DisplayName("tiro invisivel sai da lista sem ser desenhado nem movido")
        void removesInvisibleShotWithoutDrawingIt() throws Exception {
            alienOutOfTheWay();
            Shot shot = spy(new Shot(100, 600));
            shot.setVisible(false);
            shots().add(shot);

            renderWithMockedBackground();

            verify(shot, never()).paint(any(Graphics.class));
            verify(shot, never()).update();
            assertFalse(shots().contains(shot));
        }

        @Test
        @DisplayName("issue #10: dois tiros invisiveis em sequencia saem da lista no mesmo frame")
        void removesConsecutiveInvisibleShots() throws Exception {
            alienOutOfTheWay();
            Shot first = new Shot(100, 600);
            Shot second = new Shot(200, 600);
            first.setVisible(false);
            second.setVisible(false);
            shots().add(first);
            shots().add(second);

            renderWithMockedBackground();

            assertTrue(shots().isEmpty(), "shots left after one frame: " + shots().size());
        }
    }
}
