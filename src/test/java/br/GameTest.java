package br;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.awt.Canvas;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Answers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

/**
 * Casos de teste unitarios da classe br.Game (Entrega 2).
 *
 * Disciplina: Qualidade e Teste de Software
 * Responsavel: Leon Stevans (123083047) - Projetista/Testador da classe Game
 */
@DisplayName("Game - ciclo de vida, Game Loop e renderizacao")
class GameTest {

    private MockedConstruction<?> displayMockConstruction;
    private MockedStatic<?> stateManagerStaticMock;
    private Canvas canvasMock;
    private BufferStrategy bsMock;
    private Graphics graphicsMock;
    private AtomicBoolean returnNullBufferStrategy;

    private Game game;
    private Object displayMock;
    private Object realSm;
    private Object realKm;
    private Object smSpy;
    private Object kmSpy;
    private Class<?> stateManagerClass;
    private Object menuState;
    private Object initialStateSnapshot;

    @BeforeEach
    void prepararEstado() throws Exception {
        Class<?> displayClass = Class.forName("br.display.Display");
        stateManagerClass = Class.forName("br.states.StateManager");
        returnNullBufferStrategy = new AtomicBoolean(false);

        garantirArrayEstadosInicializado(stateManagerClass);

        FontMetrics fontMetricsMock = mock(FontMetrics.class);
        when(fontMetricsMock.stringWidth(anyString())).thenReturn(10);
        when(fontMetricsMock.getHeight()).thenReturn(14);

        graphicsMock = mock(Graphics.class);
        when(graphicsMock.getFontMetrics()).thenReturn(fontMetricsMock);
        when(graphicsMock.getFontMetrics(any())).thenReturn(fontMetricsMock);

        bsMock = mock(BufferStrategy.class);
        when(bsMock.getDrawGraphics()).thenReturn(graphicsMock);

        canvasMock = mock(Canvas.class);
        when(canvasMock.getBufferStrategy()).thenAnswer(
            inv -> returnNullBufferStrategy.get() ? null : bsMock
        );

        MockedConstruction<?> construction = mockConstruction(
            displayClass,
            withSettings().defaultAnswer(invocation -> {
                String methodName = invocation.getMethod().getName();
                if ("getCanvas".equals(methodName)) {
                    return canvasMock;
                }
                if ("getBufferStrategy".equals(methodName)) {
                    return returnNullBufferStrategy.get() ? null : bsMock;
                }
                if ("createBufferStrategy".equals(methodName)) {
                    returnNullBufferStrategy.set(false);
                    return null;
                }
                return Answers.RETURNS_DEFAULTS.answer(invocation);
            })
        );
        displayMockConstruction = construction;

        try {
            game = new Game();
            displayMock = displayMockConstruction.constructed().get(0);

            realSm = getPrivateField("sm");
            realKm = getPrivateField("km");
            menuState = stateManagerClass.getField("MENU").get(null);
            initialStateSnapshot = getCurrentState();

            smSpy = spy(realSm);
            kmSpy = spy(realKm);
            setPrivateField("sm", smSpy);
            setPrivateField("km", kmSpy);
        } catch (Exception e) {
            fecharMocks();
            throw e;
        }
    }

    @AfterEach
    void limparEstado() {
        try {
            if (game != null) {
                setPrivateField("running", false);
                Object threadObj = getPrivateField("thread");
                if (threadObj instanceof Thread t && t.isAlive()) {
                    t.interrupt();
                    t.join(500);
                }
            }
            if (stateManagerClass != null && initialStateSnapshot != null) {
                setCurrentState(initialStateSnapshot);
            }
        } catch (Exception ignored) {
            // Evita mascarar falhas no teardown
        } finally {
            fecharMocks();
        }
    }

    private void garantirArrayEstadosInicializado(Class<?> smClass) throws Exception {
        for (Field f : smClass.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())
                    && !Modifier.isFinal(f.getModifiers())
                    && f.getType().isArray()) {
                f.setAccessible(true);
                Object atual = f.get(null);
                Class<?> componentType = f.getType().getComponentType();
                if (atual == null || Array.getLength(atual) < 10) {
                    Object novoArray = Array.newInstance(componentType, 10);
                    f.set(null, novoArray);
                }
            }
        }
    }

    private void fecharMocks() {
        fecharStaticStateManagerMock();
        if (displayMockConstruction != null) {
            try {
                displayMockConstruction.close();
            } catch (Exception ignored) {
                // Garante liberacao mesmo se ja fechado
            } finally {
                displayMockConstruction = null;
            }
        }
    }

    private void fecharStaticStateManagerMock() {
        if (stateManagerStaticMock != null) {
            try {
                stateManagerStaticMock.close();
            } catch (Exception ignored) {
                // Garante liberacao mesmo se ja fechado
            } finally {
                stateManagerStaticMock = null;
            }
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

    private void invokePrivateMethod(String methodName) throws Exception {
        Method method = Game.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(game);
    }

    private Object getCurrentState() throws Exception {
        return stateManagerClass.getMethod("getState").invoke(null);
    }

    private void setCurrentState(Object targetState) throws Exception {
        fecharStaticStateManagerMock();
        if (targetState == null) {
            stateManagerStaticMock = mockStatic(stateManagerClass, Answers.CALLS_REAL_METHODS);
            stateManagerStaticMock.when(() -> {
                try {
                    stateManagerClass.getMethod("getState").invoke(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).thenReturn(null);
            return;
        }
        if (targetState instanceof Number num) {
            for (Method m : stateManagerClass.getMethods()) {
                if ("setState".equals(m.getName())
                        && m.getParameterCount() == 1
                        && (m.getParameterTypes()[0] == int.class
                            || m.getParameterTypes()[0] == Integer.class)) {
                    m.invoke(null, num.intValue());
                    return;
                }
            }
        }
    }

    private long countInvocations(Object mockOrSpy, String methodName) {
        return mockingDetails(mockOrSpy).getInvocations().stream()
                .filter(inv -> methodName.equals(inv.getMethod().getName()))
                .count();
    }

    @Nested
    @DisplayName("Construtor Game() - inicializacao dos subsistemas")
    class Construtor {

        @Test
        @DisplayName("cria Display, registra listeners de teclado e define estado inicial MENU")
        void inicializaDisplayListenersEEstadoMenu() throws Exception {
            assertEquals(1, displayMockConstruction.constructed().size());
            assertNotNull(getPrivateField("sm"));
            assertNotNull(getPrivateField("km"));
            assertNotNull(getCurrentState(), "O estado inicial nao deve ser nulo");

            List<Object> listenerArgs = new ArrayList<>();
            mockingDetails(displayMock).getInvocations()
                .forEach(inv -> listenerArgs.addAll(Arrays.asList(inv.getArguments())));
            mockingDetails(canvasMock).getInvocations()
                .forEach(inv -> listenerArgs.addAll(Arrays.asList(inv.getArguments())));

            assertTrue(listenerArgs.contains(realSm), "Deve registrar StateManager como KeyListener");
            assertTrue(listenerArgs.contains(realKm), "Deve registrar KeyManager como KeyListener");
        }
    }

    @Nested
    @DisplayName("start() - inicializacao da thread principal")
    class Inicializacao {

        @Test
        @DisplayName("inicia a thread e define running como true quando thread e nula")
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

            assertSame(threadMock, getPrivateField("thread"));
            assertFalse((boolean) getPrivateField("running"));
        }
    }

    @Nested
    @DisplayName("stop() - encerramento e tratamento de interrupcao")
    class Encerramento {

        @Test
        @DisplayName("ignora a tentativa de parar se a thread for nula")
        void ignoraParaThreadNula() throws Exception {
            assertNull(getPrivateField("thread"));
            game.stop();
            assertNull(getPrivateField("thread"));
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

        @Test
        @Timeout(5)
        @DisplayName("captura InterruptedException se a thread chamadora for interrompida durante join()")
        void trataInterruptedExceptionDuranteJoin() throws Exception {
            Thread bloqueada = new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
            bloqueada.start();
            setPrivateField("thread", bloqueada);

            PrintStream originalErr = System.err;
            ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
            System.setErr(new PrintStream(errBuffer, true, StandardCharsets.UTF_8));
            try {
                Thread.currentThread().interrupt();
                game.stop();
            } finally {
                System.setErr(originalErr);
                Thread.interrupted();
                bloqueada.interrupt();
                bloqueada.join(500);
            }

            assertTrue(
                errBuffer.toString(StandardCharsets.UTF_8).contains("InterruptedException"),
                "Deve imprimir stack trace quando InterruptedException ocorrer"
            );
        }
    }

    @Nested
    @DisplayName("run() - controle de FPS e limites do Game Loop")
    class GameLoop {

        @Test
        @Timeout(5)
        @DisplayName("nao executa iteracoes se running ja iniciar como false (limite 0 ticks)")
        void naoExecutaLacoQuandoRunningFalso() throws Exception {
            Thread worker = new Thread(() -> {
                try {
                    Thread.sleep(120);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
            worker.start();
            setPrivateField("thread", worker);
            clearInvocations(smSpy, kmSpy, bsMock);
            setPrivateField("running", false);

            game.run();

            assertFalse(worker.isAlive(), "run() deve acionar stop() e aguardar join() da thread");
            assertEquals(0, countInvocations(smSpy, "update"));
            assertEquals(0, countInvocations(kmSpy, "update"));
            verify(bsMock, never()).show();
        }

        @Test
        @Timeout(5)
        @DisplayName("executa ticks sincronizados a 60 FPS enquanto running for true")
        void processaLacoSincronizado() throws Exception {
            clearInvocations(smSpy, kmSpy, bsMock);
            game.start();
            Thread t = (Thread) getPrivateField("thread");

            Thread.sleep(65);
            setPrivateField("running", false);
            t.join(1000);
            if (t.isAlive()) {
                t.interrupt();
                t.join(500);
            }

            long ticksExecutados = countInvocations(smSpy, "update");
            assertFalse((boolean) getPrivateField("running"));
            assertFalse(t.isAlive());
            assertTrue(ticksExecutados >= 1, "Deve executar pelo menos 1 tick quando delta >= 1");
            assertTrue(ticksExecutados <= 20, "Deve limitar taxa de atualizacao a ~60 FPS");
            assertTrue(countInvocations(bsMock, "show") >= 1, "Deve renderizar ao menos 1 frame no loop");
        }
    }

    @Nested
    @DisplayName("update() - cobertura estrutural das ramificacoes de estado")
    class AtualizacaoLogica {

        @Test
        @DisplayName("atualiza StateManager e KeyManager quando ha um estado ativo")
        void atualizaEstadoEEntradaQuandoEstadoAtivo() throws Exception {
            if (menuState instanceof Number num) {
                setCurrentState(num.intValue());
            }
            clearInvocations(smSpy, kmSpy);

            invokePrivateMethod("update");

            assertEquals(1, countInvocations(smSpy, "update"));
            assertEquals(1, countInvocations(kmSpy, "update"));
        }

        @Test
        @DisplayName("nao invoca sm.update() quando StateManager.getState() for nulo")
        void ignoraAtualizacaoDeEstadoQuandoEstadoNulo() throws Exception {
            setCurrentState(null);
            clearInvocations(smSpy, kmSpy);

            invokePrivateMethod("update");

            assertEquals(0, countInvocations(smSpy, "update"));
        }
    }

    @Nested
    @DisplayName("render() - cobertura estrutural de BufferStrategy e renderizacao")
    class Renderizacao {

        @Test
        @DisplayName("cria BufferStrategy quando bs e nulo e prossegue com a renderizacao")
        void criaBufferStrategyQuandoNuloERetorna() throws Exception {
            returnNullBufferStrategy.set(true);
            setCurrentState(menuState);
            clearInvocations(displayMock, canvasMock, bsMock, graphicsMock);

            invokePrivateMethod("render");

            long createBsCalls = mockingDetails(displayMock).getInvocations().stream()
                .filter(inv -> "createBufferStrategy".equals(inv.getMethod().getName()))
                .count()
                + mockingDetails(canvasMock).getInvocations().stream()
                .filter(inv -> "createBufferStrategy".equals(inv.getMethod().getName()))
                .count();

            assertTrue(createBsCalls >= 1, "Deve solicitar criacao do BufferStrategy quando bs e nulo");
            verify(graphicsMock).clearRect(0, 0, 800, 1080);
            verify(bsMock).show();
        }

        @Test
        @DisplayName("limpa tela, renderiza estado ativo, libera Graphics e exibe buffer")
        void renderizaCenaCompletaQuandoBufferEEstadoAtivos() throws Exception {
            returnNullBufferStrategy.set(false);
            if (menuState instanceof Number num) {
                setCurrentState(num.intValue());
            }
            clearInvocations(smSpy, graphicsMock, bsMock);

            invokePrivateMethod("render");

            verify(graphicsMock).clearRect(0, 0, 800, 1080);
            assertEquals(1, countInvocations(smSpy, "render"));
            verify(graphicsMock).dispose();
            verify(bsMock).show();
        }

        @Test
        @DisplayName("limpa tela e exibe buffer sem chamar sm.render() quando estado for nulo")
        void limpaTelaSemRenderizarEstadoQuandoEstadoNulo() throws Exception {
            returnNullBufferStrategy.set(false);
            setCurrentState(null);
            clearInvocations(smSpy, graphicsMock, bsMock);

            invokePrivateMethod("render");

            verify(graphicsMock).clearRect(0, 0, 800, 1080);
            assertEquals(0, countInvocations(smSpy, "render"));
            verify(graphicsMock).dispose();
            verify(bsMock).show();
        }
    }
}