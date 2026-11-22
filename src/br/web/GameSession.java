package br.web;

import java.io.IOException;

import br.states.StateSnapshot;

/**
 * What the HTTP layer needs from a running game. WebServer depends on this
 * interface instead of WebGame, so its routes can be unit tested with a fake.
 */
public interface GameSession {

	/** Latest rendered frame encoded as JPEG. */
	byte[] frameJpeg() throws IOException;

	/** State captured at the end of the latest frame. */
	StateSnapshot snapshot();

	/** Frames rendered during the last second. */
	int framesPerSecond();

	/** Longest time the game spent on one frame (keys, update, render) during the last second. */
	long slowestFrameMicros();

	/** Queues a key event, applied by the game loop at the start of the next frame. */
	void key(int keyCode, boolean pressed);
}
