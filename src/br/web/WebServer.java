package br.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import br.states.StateSnapshot;

/**
 * HTTP layer of the web version, built on the JDK's com.sun.net.httpserver.
 *
 * GET  /           the page (resources/web/index.html)
 * GET  /frame.jpg  latest frame
 * GET  /state      JSON with the current screen, menu option, ship, aliens, shots and FPS
 * POST /key        form body code=KeyD&type=down|up
 *
 * It listens on localhost only: the game is meant to be played and tested on the
 * same machine.
 */
public class WebServer {

	private static final String PAGE_RESOURCE = "/web/index.html";

	private final GameSession game;
	private final HttpServer server;
	private final ExecutorService executor = Executors.newFixedThreadPool(4);

	public WebServer(GameSession game, int port) throws IOException {
		this.game = game;
		this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
		server.createContext("/", this::route);
		server.setExecutor(executor);
	}

	public void start() {
		server.start();
	}

	public void stop() {
		server.stop(0);
		executor.shutdownNow();
	}

	/** Actual port, useful when created with port 0 (any free port). */
	public int port() {
		return server.getAddress().getPort();
	}

	private void route(HttpExchange exchange) throws IOException {
		try {
			String path = exchange.getRequestURI().getPath();
			String method = exchange.getRequestMethod();
			switch (path) {
			case "/":
				requireMethod(method, "GET");
				sendPage(exchange);
				break;
			case "/frame.jpg":
				requireMethod(method, "GET");
				send(exchange, 200, "image/jpeg", game.frameJpeg());
				break;
			case "/state":
				requireMethod(method, "GET");
				send(exchange, 200, "application/json", toJson(game.snapshot(), game.framesPerSecond(), game.slowestFrameMicros()).getBytes(StandardCharsets.UTF_8));
				break;
			case "/key":
				requireMethod(method, "POST");
				handleKey(exchange);
				break;
			default:
				sendText(exchange, 404, "not found");
			}
		} catch (WrongMethodException e) {
			exchange.getResponseHeaders().set("Allow", e.allowed);
			sendText(exchange, 405, "method not allowed");
		} finally {
			exchange.close();
		}
	}

	private void sendPage(HttpExchange exchange) throws IOException {
		try (InputStream page = WebServer.class.getResourceAsStream(PAGE_RESOURCE)) {
			if (page == null) {
				sendText(exchange, 500, "page resource missing: " + PAGE_RESOURCE);
				return;
			}
			send(exchange, 200, "text/html; charset=utf-8", page.readAllBytes());
		}
	}

	private void handleKey(HttpExchange exchange) throws IOException {
		Map<String, String> form = parseForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
		OptionalInt keyCode = KeyMapping.toKeyCode(form.get("code"));
		String type = form.get("type");
		if (keyCode.isEmpty() || !("down".equals(type) || "up".equals(type))) {
			sendText(exchange, 400, "expected code=KeyW|KeyA|KeyS|KeyD|Space|Enter|NumpadEnter and type=down|up");
			return;
		}
		game.key(keyCode.getAsInt(), "down".equals(type));
		exchange.sendResponseHeaders(204, -1);
	}

	static Map<String, String> parseForm(String body) {
		Map<String, String> form = new HashMap<>();
		if (body == null || body.isEmpty()) {
			return form;
		}
		for (String pair : body.split("&")) {
			int eq = pair.indexOf('=');
			if (eq <= 0) {
				continue;
			}
			form.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
					URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
		}
		return form;
	}

	/** Hand-written to avoid a JSON dependency. Every value is a known name or a number. */
	static String toJson(StateSnapshot snapshot, int fps, long slowestFrameMicros) {
		return "{"
				+ "\"state\":" + quoted(snapshot.state()) + ","
				+ "\"menuOption\":" + quoted(snapshot.menuOption()) + ","
				+ "\"shipX\":" + snapshot.shipX() + ","
				+ "\"aliens\":" + snapshot.aliens() + ","
				+ "\"shots\":" + snapshot.shots() + ","
				+ "\"fps\":" + fps + ","
				+ "\"slowestFrameMs\":" + String.format(java.util.Locale.ROOT, "%.1f", slowestFrameMicros / 1000.0)
				+ "}";
	}

	private static String quoted(String value) {
		return value == null ? "null" : "\"" + value + "\"";
	}

	private static void requireMethod(String actual, String expected) {
		boolean headForGet = "GET".equals(expected) && "HEAD".equals(actual);
		if (!expected.equals(actual) && !headForGet) {
			throw new WrongMethodException(expected);
		}
	}

	private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
		send(exchange, status, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8));
	}

	private static void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
		exchange.getResponseHeaders().set("Content-Type", contentType);
		exchange.getResponseHeaders().set("Cache-Control", "no-store");
		if ("HEAD".equals(exchange.getRequestMethod())) {
			exchange.sendResponseHeaders(status, -1);
			return;
		}
		exchange.sendResponseHeaders(status, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

	private static final class WrongMethodException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		final String allowed;

		WrongMethodException(String allowed) {
			super(null, null, false, false);
			this.allowed = allowed;
		}
	}
}
