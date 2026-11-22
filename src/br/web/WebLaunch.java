package br.web;

import java.io.IOException;

/**
 * Starts the web version of the game: the real game logic running without a window,
 * served at http://localhost:8080 (or the port given as the first argument).
 *
 * Run from the project root, because the sprites are loaded with relative paths:
 *   mvn compile exec:java -Dexec.mainClass=br.web.WebLaunch
 */
public final class WebLaunch {

	private WebLaunch() {}

	public static void main(String[] args) throws IOException {
		int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

		WebGame game = new WebGame();
		WebServer server = new WebServer(game, port);
		game.start();
		server.start();

		// System tests wait for this exact line before opening the browser.
		System.out.println("Space Invaders web: http://localhost:" + server.port());
	}
}
