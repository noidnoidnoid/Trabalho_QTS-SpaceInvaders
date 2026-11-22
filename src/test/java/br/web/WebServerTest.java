package br.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.states.StateSnapshot;

/**
 * Unit tests for the web adapter's HTTP layer (WebServer and KeyMapping).
 *
 * Owner: Roger Egito (220083118).
 *
 * The adapter was generated with AI help, so it is tested like any other code.
 * WebServer runs for real on a free port, but the game behind it is a fake
 * GameSession: these tests check the HTTP contract, not the game.
 */
@DisplayName("Adaptador web - camada HTTP")
class WebServerTest {

    /** Records the calls the server makes, and returns fixed values. */
    static class FakeSession implements GameSession {
        final List<int[]> keys = new ArrayList<>();
        StateSnapshot snapshot = new StateSnapshot("MENU", "HELP", null, null, null);
        final byte[] frame = { (byte) 0xFF, (byte) 0xD8, 1, 2, 3 };

        @Override public byte[] frameJpeg() { return frame; }
        @Override public StateSnapshot snapshot() { return snapshot; }
        @Override public int framesPerSecond() { return 60; }
        @Override public long slowestFrameMicros() { return 12_345; }
        @Override public synchronized void key(int keyCode, boolean pressed) {
            keys.add(new int[] { keyCode, pressed ? 1 : 0 });
        }
    }

    @Nested
    @DisplayName("rotas HTTP (servidor real, jogo falso)")
    class Routes {

        private FakeSession session;
        private WebServer server;
        private final HttpClient client = HttpClient.newHttpClient();

        @BeforeEach
        void start() throws IOException {
            session = new FakeSession();
            server = new WebServer(session, 0);
            server.start();
        }

        @AfterEach
        void stop() {
            server.stop();
        }

        private HttpResponse<byte[]> send(String method, String path, String body) throws Exception {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path));
            HttpRequest.BodyPublisher publisher = body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(body);
            if (body != null) request.header("Content-Type", "application/x-www-form-urlencoded");
            return client.send(request.method(method, publisher).build(), HttpResponse.BodyHandlers.ofByteArray());
        }

        @Test
        @DisplayName("GET / devolve a pagina com os campos que os testes de sistema leem")
        void pageHasStateFields() throws Exception {
            HttpResponse<byte[]> response = send("GET", "/", null);

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
            String html = new String(response.body(), java.nio.charset.StandardCharsets.UTF_8);
            for (String id : new String[] { "frame", "state", "menu-option", "ship-x", "aliens", "shots", "fps", "connection" }) {
                assertTrue(html.contains("id=\"" + id + "\""), "missing #" + id);
            }
        }

        @Test
        @DisplayName("GET /state devolve o snapshot e as medidas de desempenho em JSON")
        void stateAsJson() throws Exception {
            session.snapshot = new StateSnapshot("LEVEL2", null, 406, 7, 3);

            HttpResponse<byte[]> response = send("GET", "/state", null);

            assertEquals(200, response.statusCode());
            assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(""));
            assertEquals("{\"state\":\"LEVEL2\",\"menuOption\":null,\"shipX\":406,\"aliens\":7,\"shots\":3,\"fps\":60,\"slowestFrameMs\":12.3}",
                    new String(response.body()));
        }

        @Test
        @DisplayName("GET /frame.jpg devolve os bytes do frame como image/jpeg, sem cache")
        void frameAsJpeg() throws Exception {
            HttpResponse<byte[]> response = send("GET", "/frame.jpg", null);

            assertEquals(200, response.statusCode());
            assertEquals("image/jpeg", response.headers().firstValue("Content-Type").orElse(""));
            assertEquals("no-store", response.headers().firstValue("Cache-Control").orElse(""));
            assertArrayEquals(session.frame, response.body());
        }

        @ParameterizedTest(name = "{0} {1}")
        @CsvSource({ "down, KeyD, 68, 1", "up, KeyD, 68, 0", "down, Enter, 10, 1", "down, Space, 32, 1" })
        @DisplayName("POST /key valido repassa o codigo AWT ao jogo e responde 204")
        void validKeyIsForwarded(String type, String code, int expectedKeyCode, int expectedPressed) throws Exception {
            HttpResponse<byte[]> response = send("POST", "/key", "code=" + code + "&type=" + type);

            assertEquals(204, response.statusCode());
            assertEquals(1, session.keys.size());
            assertEquals(expectedKeyCode, session.keys.get(0)[0]);
            assertEquals(expectedPressed, session.keys.get(0)[1]);
        }

        @ParameterizedTest(name = "corpo: \"{0}\"")
        @ValueSource(strings = { "code=KeyQ&type=down", "code=KeyD&type=hold", "code=KeyD", "type=down", "" })
        @DisplayName("POST /key invalido responde 400 e nao repassa nada")
        void invalidKeyIsRejected(String body) throws Exception {
            HttpResponse<byte[]> response = send("POST", "/key", body);

            assertEquals(400, response.statusCode());
            assertTrue(session.keys.isEmpty());
        }

        @ParameterizedTest(name = "{0} {1} -> 405, Allow: {2}")
        @CsvSource({ "GET, /key, POST", "POST, /state, GET", "DELETE, /, GET", "POST, /frame.jpg, GET" })
        @DisplayName("metodo errado responde 405 com o cabecalho Allow")
        void wrongMethod(String method, String path, String allowed) throws Exception {
            HttpResponse<byte[]> response = send(method, path, method.equals("POST") ? "" : null);

            assertEquals(405, response.statusCode());
            assertEquals(allowed, response.headers().firstValue("Allow").orElse(""));
        }

        @Test
        @DisplayName("HEAD / responde 200 sem corpo")
        void headWithoutBody() throws Exception {
            HttpResponse<byte[]> response = send("HEAD", "/", null);

            assertEquals(200, response.statusCode());
            assertEquals(0, response.body().length);
        }

        @Test
        @DisplayName("caminho desconhecido responde 404")
        void unknownPath() throws Exception {
            assertEquals(404, send("GET", "/admin", null).statusCode());
        }
    }

    @Nested
    @DisplayName("funcoes auxiliares")
    class Helpers {

        @ParameterizedTest(name = "{0} -> {1}")
        @CsvSource({ "KeyW, 87", "KeyA, 65", "KeyS, 83", "KeyD, 68", "Space, 32", "Enter, 10", "NumpadEnter, 10" })
        @DisplayName("teclas do jogo viram o KeyEvent.VK_* correspondente")
        void mapsGameKeys(String browserCode, int awtCode) {
            assertEquals(OptionalInt.of(awtCode), KeyMapping.toKeyCode(browserCode));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = { "KeyQ", "Escape", "keyw", "87" })
        @DisplayName("qualquer outra tecla e recusada")
        void rejectsOtherKeys(String browserCode) {
            assertEquals(OptionalInt.empty(), KeyMapping.toKeyCode(browserCode));
        }

        @Test
        @DisplayName("Enter do navegador vira VK_ENTER (10), nao o keyCode 13 do JavaScript")
        void enterUsesAwtCode() {
            assertEquals(KeyEvent.VK_ENTER, KeyMapping.toKeyCode("Enter").getAsInt());
        }

        @Test
        @DisplayName("parseForm decodifica pares e ignora pares sem nome")
        void parseForm() {
            assertEquals(Map.of("code", "Key D", "type", "down"), WebServer.parseForm("code=Key%20D&type=down&=x&solto"));
            assertEquals(Map.of(), WebServer.parseForm(null));
        }

        @Test
        @DisplayName("toJson escreve null para campos ausentes e arredonda o tempo de frame")
        void jsonWithNulls() {
            String json = WebServer.toJson(new StateSnapshot("HELP", null, null, null, null), 58, 16_650);

            assertEquals("{\"state\":\"HELP\",\"menuOption\":null,\"shipX\":null,\"aliens\":null,\"shots\":null,\"fps\":58,\"slowestFrameMs\":16.7}", json);
        }
    }
}
