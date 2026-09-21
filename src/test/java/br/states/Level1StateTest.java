package br.states;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.awt.Graphics;
import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.Game;
import br.characters.Alien;
import br.characters.Ship;
import br.characters.Shot;
import br.input.KeyManager;

/**
 * Casos de teste unitarios da classe br.states.Level1State.
 *
 * Disciplina: Qualidade e Teste de Software
 * Testador: Leon Stevans - Testador da classe Level1State
 */
@DisplayName("Level1State - primeira fase do jogo")
class Level1StateTest {

    private Level1State state;

    @BeforeEach
    void prepararEstado() {
        KeyManager.a = false;
        KeyManager.d = false;
        KeyManager.space = false;
        state = new Level1State();
    }

    private Object getPrivateField(String fieldName) throws Exception {
        Field field = Level1State.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(state);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = Level1State.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(state, value);
    }

    @Nested
    @DisplayName("init()")
    class Inicializacao {

        @Test
        @DisplayName("instancia e centraliza 1 unico alien no topo da tela")
        void instanciaAlienNoCentro() throws Exception {
            state.init();

            Alien alien = (Alien) getPrivateField("alien");
            assertEquals(Game.WIDTH / 2, alien.getX());
            assertEquals(Game.HEIGHT / 4, alien.getY());
        }

        @Test
        @DisplayName("comeca sem vitoria nem derrota")
        void comecaSemFimDeJogo() throws Exception {
            state.init();

            assertFalse((boolean) getPrivateField("victory"));
            assertFalse((boolean) getPrivateField("gameOver"));
        }
    }

    @Nested
    @DisplayName("update() - movimentacao e tiros")
    class Update {

        @BeforeEach
        void prepararUpdate() {
            state.init();
        }

        @Test
        @DisplayName("tecla D move a nave para a direita")
        void moveParaDireita() throws Exception {
            Ship ship = (Ship) getPrivateField("ship");
            int xInicial = ship.getX();
            KeyManager.d = true;

            state.update();

            assertEquals(xInicial + 6, ship.getX());
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("tecla Space dispara um tiro")
        void disparaTiro() throws Exception {
            KeyManager.space = true;
            
            state.update();

            List<Shot> shots = (List<Shot>) getPrivateField("shots");
            assertEquals(1, shots.size());
        }
    }

    @Nested
    @DisplayName("checarColisoes() via render(Graphics)")
    class Colisoes {

        private final Graphics g = mock(Graphics.class);

        @BeforeEach
        void prepararColisoes() {
            state.init();
            new StateManager(); // Evita NullPointerException ao trocar de estado
        }

        @Test
        @DisplayName("colisao direta nave-alien causa gameOver")
        void naveGeraGameOver() throws Exception {
            Ship ship = (Ship) getPrivateField("ship");
            Alien alienSpy = spy((Alien) getPrivateField("alien"));
            
            // Forca o retangulo do Alien a ser exatamente o mesmo da Nave
            doReturn(ship.getBounds()).when(alienSpy).getBounds();
            setPrivateField("alien", alienSpy);

            state.render(g);

            assertTrue((boolean) getPrivateField("gameOver"));
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("tiro atingindo o alien causa vitoria")
        void tiroGeraVitoria() throws Exception {
            Ship ship = (Ship) getPrivateField("ship");
            ship.simpleShot();
            
            List<Shot> shots = (List<Shot>) getPrivateField("shots");
            Shot tiro = shots.get(0);

            Alien alienSpy = spy((Alien) getPrivateField("alien"));
            // O retangulo do Alien sera equivalente a posicao atual do tiro
            doAnswer(invocation -> tiro.getBounds()).when(alienSpy).getBounds();
            setPrivateField("alien", alienSpy);

            state.render(g);

            assertTrue((boolean) getPrivateField("victory"));
        }
    }
}