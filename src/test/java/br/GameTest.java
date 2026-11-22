package br;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.awt.Canvas;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Answers;
import org.mockito.MockedConstruction;

@DisplayName("Game - ciclo de vida e Game Loop principal")
class GameTest {

    private MockedConstruction<?> displayMockConstruction;
    private Game game;

    @BeforeEach
    void prepararEstado() throws Exception {
        Class<?> displayClass = Class.forName("br.display.Display");

        FontMetrics fontMetricsMock = mock(FontMetrics.class);
        when(fontMetricsMock.stringWidth(anyString())).thenReturn(10);
        when(fontMetricsMock.getHeight()).thenReturn(14);

        Graphics graphicsMock = mock(Graphics.class);
        when(graphicsMock.getFontMetrics()).thenReturn(fontMetricsMock);
        when(graphicsMock.getFontMetrics(any())).thenReturn(fontMetricsMock);

        BufferStrategy bsMock = mock(BufferStrategy.class);
        when(bsMock.getDrawGraphics()).thenReturn(graphicsMock);

        Canvas canvasMock = mock(Canvas.class);
        when(canvasMock.getBufferStrategy()).thenReturn(bsMock);

        displayMockConstruction = mockConstruction(
            displayClass,
            withSettings().defaultAnswer(invocation -> {
                String methodName = invocation.getMethod().getName();
                if ("getCanvas".equals(methodName)) {
                    return canvasMock;
                }
                if ("getBufferStrategy".equals(methodName)) {
                    return bsMock;
                }
                return Answers.RETURNS_DEFAULTS.answer(invocation);
            })
        );

        game = new Game();
    }

    @AfterEach
    void limparEstado() throws Exception {
        if (game != null) {
            setPrivateField("running", false);
            Object threadObj = getPrivateField("thread");
            if (threadObj instanceof Thread t && t.isAlive()) {
                t.interrupt();
                t.join(500);
            }
        }
        if (displayMockConstruction != null) {
            displayMockConstruction.close();
        }
    }

    private Object getPrivateField(String fieldName) throws Exception {
        Field field = Game.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(game);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = Game.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(game, value);
    }

    @Nested
    @DisplayName("start()")
    class Inicializacao {

        @Test
        @DisplayName("inicia a thread e define running como true")
        void iniciaThreadERunning() throws Exception {
            assertNull(getPrivateField("thread"));
            assertFalse((boolean) getPrivateField("running"));

            game.start();

            assertNotNull(getPrivateField("thread"));
            assertTrue((boolean) getPrivateField("running"));
        }

        @Test
        @DisplayName("nao sobrescreve a thread se ela ja existir (return prematuro)")
        void naoSobrescreveThreadExistente() throws Exception {
            Thread threadMock = mock(Thread.class);
            setPrivateField("thread", threadMock);
            setPrivateField("running", false);

            game.start();

            assertTrue(getPrivateField("thread") == threadMock);
            assertFalse((boolean) getPrivateField("running"));
        }
    }

    @Nested
    @DisplayName("stop()")
    class Encerramento {

        @Test
        @DisplayName("ignora a tentativa de parar se a thread for nula")
        void ignoraParaThreadNula() throws Exception {
            assertNull(getPrivateField("thread"));
            game.stop();
        }

        @Test
        @Timeout(5)
        @DisplayName("acopla (join) a thread encerrando-a suavemente")
        void acoplaThreadNaParada() throws Exception {
            Thread t = new Thread(() -> {});
            t.start();
            t.join(500);

            setPrivateField("thread", t);
            setPrivateField("running", false);

            game.stop();

            assertFalse(t.isAlive());
        }
    }

    @Nested
    @DisplayName("run() - comportamento do Frame Rate")
    class GameLoop {

        @Test
        @Timeout(5)
        @DisplayName("o laco 'while(running)' processa os frames e encerra ao desativar running")
        void processaLacoSincronizado() throws Exception {
            game.start();
            Thread t = (Thread) getPrivateField("thread");

            Thread.sleep(50);
            setPrivateField("running", false);
            t.join(1000);
            if (t.isAlive()) {
                t.interrupt();
                t.join(500);
            }

            assertFalse((boolean) getPrivateField("running"));
            assertFalse(t.isAlive());
        }
    }
}