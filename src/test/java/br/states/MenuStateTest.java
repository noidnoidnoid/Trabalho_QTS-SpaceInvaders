package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;

import br.Game;
import br.audio.AudioPlayer;
import br.characters.Alien;
import br.display.Background;

/**
 * Unit tests for br.states.MenuState.
 *
 * Owner: Roger Egito (220083118).
 *
 * Dependencies and how each one is isolated:
 * - AudioPlayer (theme music): mocked and injected through the package-private
 *   constructor, so no sound is played and play() calls can be counted.
 * - System.exit (EXIT option): replaced by a mocked Runnable through the same
 *   constructor, so the EXIT branch can run without killing the test JVM.
 * - StateManager (static registry of states): its public static array is filled
 *   with mocked State objects before each test and restored afterwards, so START
 *   and HELP can be checked without building the real levels.
 * - Background and Alien (load PNG files from disk in their constructors):
 *   replaced with mockConstruction only in render() tests.
 * - Graphics: a mock that records which color was active for each drawString.
 *
 * Test design: equivalence partitioning on keys and menu choices, boundary values
 * on the four walls of the bouncing alien animation, aiming at 100% of the edges.
 */
@DisplayName("MenuState - menu principal")
class MenuStateTest {

    private static final int START = 0, HELP = 1, EXIT = 2;

    /** Alien sprite size used by limits(): 50 wide, 80 tall (as written in the SUT). */
    private static final int RIGHT_LIMIT_X = Game.WIDTH - 50;    // 750: largest x without bouncing
    private static final int BOTTOM_LIMIT_Y = Game.HEIGHT - 80;  // 1000: largest y without bouncing

    private AudioPlayer themeSound;
    private Runnable exitAction;
    private MenuState menu;

    private State[] originalStates;
    private int originalCurrentState;

    @BeforeEach
    void setUp() {
        themeSound = mock(AudioPlayer.class);
        exitAction = mock(Runnable.class);
        menu = new MenuState(themeSound, exitAction);

        originalStates = StateManager.states.clone();
        originalCurrentState = StateManager.currentState;
        for (int i = 0; i < StateManager.states.length; i++) {
            StateManager.states[i] = mock(State.class);
        }
        StateManager.currentState = StateManager.MENU;
    }

    @AfterEach
    void restoreStateManager() {
        System.arraycopy(originalStates, 0, StateManager.states, 0, originalStates.length);
        StateManager.currentState = originalCurrentState;
    }

    @Nested
    @DisplayName("construcao e init()")
    class Lifecycle {

        @Test
        @DisplayName("construtor nao toca a musica")
        void constructorDoesNotPlay() {
            verifyNoInteractions(themeSound);
        }

        @Test
        @DisplayName("estado inicial: START selecionado, alien em (0,0) indo para baixo e direita")
        void initialState() {
            assertEquals(START, menu.choice);
            assertEquals(0, menu.x);
            assertEquals(0, menu.y);
            assertEquals(3, menu.movex);
            assertEquals(3, menu.movey);
        }

        @Test
        @DisplayName("init() toca o tema uma vez a cada chamada (voltar ao menu reinicia a musica)")
        void initPlaysThemeEachTime() {
            menu.init();
            verify(themeSound, times(1)).play();

            menu.init();
            verify(themeSound, times(2)).play();
        }
    }

    @Nested
    @DisplayName("update(): animacao do alien e limites da tela")
    class Animation {

        @Test
        @DisplayName("um tick soma o deslocamento nos dois eixos")
        void tickMovesByVelocity() {
            menu.update();

            assertEquals(3, menu.x);
            assertEquals(3, menu.y);
        }

        @ParameterizedTest(name = "x final {0} -> movex {1}")
        @CsvSource({ "750, 3", "751, -3" })
        @DisplayName("parede direita: so inverte quando x + 50 passa de WIDTH")
        void rightWall(int finalX, int expectedMoveX) {
            menu.x = finalX - 3;
            menu.movex = 3;

            menu.update();

            assertEquals(finalX, menu.x);
            assertEquals(expectedMoveX, menu.movex);
        }

        @ParameterizedTest(name = "y final {0} -> movey {1}")
        @CsvSource({ "1000, 3", "1001, -3" })
        @DisplayName("parede de baixo: so inverte quando y + 80 passa de HEIGHT")
        void bottomWall(int finalY, int expectedMoveY) {
            menu.y = finalY - 3;
            menu.movey = 3;

            menu.update();

            assertEquals(finalY, menu.y);
            assertEquals(expectedMoveY, menu.movey);
        }

        @ParameterizedTest(name = "x final {0} -> movex {1}")
        @CsvSource({ "0, -3", "-1, 3" })
        @DisplayName("parede esquerda: so inverte quando x fica negativo")
        void leftWall(int finalX, int expectedMoveX) {
            menu.x = finalX + 3;
            menu.movex = -3;
            menu.y = 500; // far from the top and bottom walls

            menu.update();

            assertEquals(finalX, menu.x);
            assertEquals(expectedMoveX, menu.movex);
        }

        @ParameterizedTest(name = "y final {0} -> movey {1}")
        @CsvSource({ "0, -3", "-1, 3" })
        @DisplayName("topo: so inverte quando y fica negativo")
        void topWall(int finalY, int expectedMoveY) {
            menu.y = finalY + 3;
            menu.movey = -3;
            menu.x = 400; // far from the side walls

            menu.update();

            assertEquals(finalY, menu.y);
            assertEquals(expectedMoveY, menu.movey);
        }

        @Test
        @DisplayName("canto inferior direito inverte os dois eixos no mesmo tick")
        void bottomRightCornerFlipsBoth() {
            menu.x = RIGHT_LIMIT_X;
            menu.y = BOTTOM_LIMIT_Y;

            menu.update();

            assertEquals(-3, menu.movex);
            assertEquals(-3, menu.movey);
        }
    }

    @Nested
    @DisplayName("KeyReleased(): navegacao com W/S")
    class Navigation {

        @ParameterizedTest(name = "W em {0} -> {1}")
        @CsvSource({ "0, 2", "1, 0", "2, 1" })
        @DisplayName("W sobe uma opcao e do topo vai para a ultima")
        void wMovesUpWithWraparound(int from, int expected) {
            menu.choice = from;

            menu.KeyReleased(KeyEvent.VK_W);

            assertEquals(expected, menu.choice);
        }

        @ParameterizedTest(name = "S em {0} -> {1}")
        @CsvSource({ "0, 1", "1, 2", "2, 0" })
        @DisplayName("S desce uma opcao e da ultima volta para o topo")
        void sMovesDownWithWraparound(int from, int expected) {
            menu.choice = from;

            menu.KeyReleased(KeyEvent.VK_S);

            assertEquals(expected, menu.choice);
        }

        @Test
        @DisplayName("tecla sem funcao no menu nao muda nada (classe invalida da particao)")
        void unrelatedKeyIsIgnored() {
            menu.choice = HELP;

            menu.KeyReleased(KeyEvent.VK_A);

            assertEquals(HELP, menu.choice);
            assertEquals(StateManager.MENU, StateManager.currentState);
            verifyNoInteractions(exitAction);
        }
    }

    @Nested
    @DisplayName("KeyReleased(): ENTER seleciona a opcao")
    class Selection {

        @Test
        @DisplayName("START vai para LEVEL1 e inicializa a fase")
        void startGoesToLevel1() {
            menu.choice = START;

            menu.KeyReleased(KeyEvent.VK_ENTER);

            assertEquals(StateManager.LEVEL1, StateManager.currentState);
            verify(StateManager.states[StateManager.LEVEL1]).init();
            verifyNoInteractions(exitAction);
        }

        @Test
        @DisplayName("HELP vai para a tela de ajuda e a inicializa")
        void helpGoesToHelp() {
            menu.choice = HELP;

            menu.KeyReleased(KeyEvent.VK_ENTER);

            assertEquals(StateManager.HELP, StateManager.currentState);
            verify(StateManager.states[StateManager.HELP]).init();
            verifyNoInteractions(exitAction);
        }

        @Test
        @DisplayName("EXIT encerra o jogo sem trocar de estado")
        void exitRunsExitAction() {
            menu.choice = EXIT;

            menu.KeyReleased(KeyEvent.VK_ENTER);

            verify(exitAction).run();
            assertEquals(StateManager.MENU, StateManager.currentState);
        }

        @Test
        @DisplayName("opcao fora do intervalo (ramo default) nao faz nada")
        void outOfRangeChoiceDoesNothing() {
            // Unreachable through the keyboard, because W/S always wrap into 0..2.
            // Set directly to cover the switch's default edge.
            menu.choice = 3;

            menu.KeyReleased(KeyEvent.VK_ENTER);

            assertEquals(StateManager.MENU, StateManager.currentState);
            verifyNoInteractions(exitAction);
        }
    }

    @Nested
    @DisplayName("render()")
    class Render {

        private Graphics g;
        private final Map<String, Color> colorByText = new HashMap<>();
        private final Map<String, int[]> positionByText = new HashMap<>();
        private Color currentColor;

        @BeforeEach
        void setUpGraphics() {
            FontMetrics metrics = mock(FontMetrics.class);
            when(metrics.stringWidth(anyString())).thenReturn(40);
            when(metrics.getHeight()).thenReturn(20);
            g = mock(Graphics.class);
            when(g.getFontMetrics()).thenReturn(metrics);
            when(g.getFontMetrics(any(Font.class))).thenReturn(metrics);
            doAnswer(call -> {
                currentColor = call.getArgument(0);
                return null;
            }).when(g).setColor(any(Color.class));
            doAnswer(call -> {
                colorByText.put(call.getArgument(0), currentColor);
                positionByText.put(call.getArgument(0), new int[] { call.getArgument(1), call.getArgument(2) });
                return null;
            }).when(g).drawString(anyString(), anyInt(), anyInt());

            menu.init(); // creates the fonts used by render()
        }

        // Only behavior is checked here. Exact colors, fonts and positions are art
        // direction: changing them must not break a test (Roger's review).

        @ParameterizedTest(name = "selecionada: {0}")
        @CsvSource({ "0, START", "1, HELP", "2, EXIT" })
        @DisplayName("a opcao selecionada aparece com cor diferente das outras")
        void selectedOptionStandsOut(int choice, String selected) {
            menu.choice = choice;

            try (MockedConstruction<Background> backgrounds = mockConstruction(Background.class);
                    MockedConstruction<Alien> aliens = mockConstruction(Alien.class)) {
                menu.render(g);
            }

            Color selectedColor = colorByText.get(selected);
            for (String option : new String[] { "START", "HELP", "EXIT" }) {
                if (!option.equals(selected)) {
                    assertNotEquals(selectedColor, colorByText.get(option), option + " looks selected too");
                }
            }
            // Without this, an inverted highlight (every other option highlighted) would
            // pass: the selected one would still be the only different one. The title is
            // plain text, so the highlight must not look like it.
            assertNotEquals(colorByText.get("Space Invaders"), selectedColor, "selected option looks like plain text");
        }

        @Test
        @DisplayName("desenha o titulo, as tres opcoes, o fundo e o alien na posicao atual da animacao")
        void drawsEverythingTheMenuShows() {
            menu.x = 120;
            menu.y = 340;

            try (MockedConstruction<Background> backgrounds = mockConstruction(Background.class);
                    MockedConstruction<Alien> aliens = mockConstruction(Alien.class,
                            (alien, context) -> {
                                assertEquals(120, context.arguments().get(0));
                                assertEquals(340, context.arguments().get(1));
                            })) {
                menu.render(g);

                verify(backgrounds.constructed().get(0)).print(g);
                verify(aliens.constructed().get(0)).paint(g);
            }
            assertEquals(java.util.Set.of("Space Invaders", "START", "HELP", "EXIT"), colorByText.keySet());
        }

        @Test
        @DisplayName("todo texto fica dentro da tela, e as opcoes aparecem abaixo do titulo, de cima para baixo na ordem do menu")
        void textIsVisibleAndOptionsAreInOrder() {
            try (MockedConstruction<Background> backgrounds = mockConstruction(Background.class);
                    MockedConstruction<Alien> aliens = mockConstruction(Alien.class)) {
                menu.render(g);
            }

            positionByText.forEach((text, xy) -> {
                assertTrue(xy[0] >= 0 && xy[0] < Game.WIDTH, text + " starts outside the screen at x = " + xy[0]);
                assertTrue(xy[1] > 0 && xy[1] <= Game.HEIGHT, text + " is outside the screen at y = " + xy[1]);
            });
            int titleY = positionByText.get("Space Invaders")[1];
            int startY = positionByText.get("START")[1];
            int helpY = positionByText.get("HELP")[1];
            int exitY = positionByText.get("EXIT")[1];
            assertTrue(titleY < startY && startY < helpY && helpY < exitY,
                    "expected title, START, HELP, EXIT from top to bottom: " + titleY + ", " + startY + ", " + helpY + ", " + exitY);
        }
    }
}
