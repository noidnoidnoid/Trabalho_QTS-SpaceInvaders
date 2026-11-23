package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.characters.Alien;
import br.characters.Ship;
import br.characters.Shot;
import br.input.KeyManager;

/**
 * Casos adicionais da br.states.Level1State.
 *
 * Objetivo 1 (estrutural, todas-arestas): exercitar os desvios que os testes
 * originais nao cobrem (tecla A, nenhuma tecla, limites da tela, ausencia de
 * colisao, tiro que erra).
 *
 * Objetivo 2 (mutacao, PIT): matar mutantes de limite condicional, de
 * negacao e de atribuicao nos flags victory e gameOver.
 *
 * Suposicoes (conferir no codigo): campos privados ship, alien, shots,
 * victory e gameOver; passo da nave = 6; limites horizontais da nave em [0, 700].
 */
@DisplayName("Level1State - cobertura de arestas e mutacao")
class Level1StateCoberturaMutacaoTest {

    private Level1State state;
    private final Graphics g = mock(Graphics.class);

    @BeforeEach
    void preparar() {
        KeyManager.a = false;
        KeyManager.d = false;
        KeyManager.space = false;
        new StateManager(); // evita NullPointerException na troca de estado
        state = new Level1State();
        state.init();
    }

    // ---------- auxiliares ----------

    private Object get(String nome) throws Exception {
        Field f = Level1State.class.getDeclaredField(nome);
        f.setAccessible(true);
        return f.get(state);
    }

    private void set(String nome, Object valor) throws Exception {
        Field f = Level1State.class.getDeclaredField(nome);
        f.setAccessible(true);
        f.set(state, valor);
    }

    private Ship ship() throws Exception {
        return (Ship) get("ship");
    }

    private boolean victory() throws Exception {
        return (boolean) get("victory");
    }

    private boolean gameOver() throws Exception {
        return (boolean) get("gameOver");
    }

    /** Troca o alien por um spy cujo retangulo e controlado pelo teste. */
    private void alienComRetangulo(Rectangle r) throws Exception {
        Alien spyAlien = spy((Alien) get("alien"));
        doReturn(r).when(spyAlien).getBounds();
        set("alien", spyAlien);
    }

    // ---------- estrutural: update() ----------

    @Nested
    @DisplayName("update() - desvios de teclado")
    class Teclado {

        @Test
        @DisplayName("tecla A move a nave 6 pixels para a esquerda")
        void moveParaEsquerda() throws Exception {
            int xInicial = ship().getX();
            KeyManager.a = true;

            state.update();

            assertEquals(xInicial - 6, ship().getX());
        }

        @Test
        @DisplayName("sem nenhuma tecla a nave fica parada e nao atira")
        @SuppressWarnings("unchecked")
        void semTeclas() throws Exception {
            int xInicial = ship().getX();

            state.update();

            assertEquals(xInicial, ship().getX());
            assertEquals(0, ((List<Shot>) get("shots")).size());
        }

        @Test
        @DisplayName("tecla D pressionada por muito tempo nao passa do limite direito")
        void limiteDireito() throws Exception {
            KeyManager.d = true;

            for (int i = 0; i < 300; i++) {
                state.update();
            }

            assertTrue(ship().getX() <= 700);
        }

        @Test
        @DisplayName("tecla A pressionada por muito tempo nao passa do limite esquerdo")
        void limiteEsquerdo() throws Exception {
            KeyManager.a = true;

            for (int i = 0; i < 300; i++) {
                state.update();
            }

            assertTrue(ship().getX() >= 0);
        }
    }

    @Nested
    @DisplayName("init() - reinicializacao")
    class Reinicializacao {

        @Test
        @DisplayName("init() devolve a nave a posicao inicial e esvazia os tiros")
        @SuppressWarnings("unchecked")
        void init_reiniciaEstado() throws Exception {
            int xInicial = ship().getX();
            KeyManager.d = true;
            KeyManager.space = true;
            state.update();
            KeyManager.d = false;
            KeyManager.space = false;

            state.init();

            assertEquals(xInicial, ship().getX());
            assertEquals(0, ((List<Shot>) get("shots")).size());
            assertFalse(victory());
            assertFalse(gameOver());
        }
    }

    // ---------- estrutural + mutacao: checarColisoes() via render ----------

    @Nested
    @DisplayName("checarColisoes() via render(Graphics)")
    class Colisoes {

        @Test
        @DisplayName("sem colisao e sem tiro: o jogo continua")
        void semColisao() throws Exception {
            alienComRetangulo(new Rectangle(0, 0, 1, 1));

            state.render(g);

            assertFalse(victory());
            assertFalse(gameOver());
        }

        @Test
        @DisplayName("tiro que nao atinge o alien nao gera vitoria")
        void tiroQueErra() throws Exception {
            ship().simpleShot();
            alienComRetangulo(new Rectangle(0, 0, 1, 1));

            state.render(g);

            assertFalse(victory());
        }

        @Test
        @DisplayName("alien encostando na borda da nave (sem sobrepor) nao colide")
        void encostarNaoColide() throws Exception {
            Rectangle n = ship().getBounds();
            alienComRetangulo(new Rectangle(n.x + n.width, n.y, 10, 10));

            state.render(g);

            assertFalse(gameOver());
        }

        @Test
        @DisplayName("alien sobrepondo 1 pixel da nave colide")
        void sobreporUmPixelColide() throws Exception {
            Rectangle n = ship().getBounds();
            alienComRetangulo(new Rectangle(n.x + n.width - 1, n.y, 10, 10));

            state.render(g);

            assertTrue(gameOver());
        }

        @Test
        @DisplayName("colisao nave-alien marca derrota e nao marca vitoria")
        void derrotaNaoMarcaVitoria() throws Exception {
            alienComRetangulo(ship().getBounds());

            state.render(g);

            assertTrue(gameOver());
            assertFalse(victory());
        }

        @Test
        @DisplayName("tiro no alien marca vitoria e nao marca derrota")
        @SuppressWarnings("unchecked")
        void vitoriaNaoMarcaDerrota() throws Exception {
            // The shot cannot be the one just fired: it spawns inside the ship's
            // rectangle, so an alien placed on it would also touch the ship and the
            // game would correctly mark Game Over. A shot far above the ship isolates
            // the shot-alien collision. The alien's rectangle covers the 7 px the
            // shot moves during render() before the collision check.
            Shot tiro = new Shot(400, 300);
            ((List<Shot>) get("shots")).add(tiro);
            alienComRetangulo(new Rectangle(390, 250, 50, 100));

            state.render(g);

            assertTrue(victory());
            assertFalse(gameOver());
        }
    }
}
