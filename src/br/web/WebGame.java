package br.web;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageIO;

import br.Game;
import br.input.KeyManager;
import br.states.StateManager;
import br.states.StateSnapshot;

/**
 * Runs the real game (StateManager, states, KeyManager) without a window, for the web
 * adapter. It replaces only what br.Game and br.display.Display do with the screen:
 * frames are drawn into an off-screen image instead of a Canvas.
 *
 * The loop is a copy of Game.run() on purpose. Reusing Game would need changes to
 * Game's constructor (it always opens a JFrame) and to GameTest, which belong to
 * another member. The duplication is recorded as technical debt in
 * docs/alteracoes-no-sut.md.
 *
 * Unlike the desktop game, key events are not applied from another thread: they are
 * queued and applied by the loop at the start of a frame, so only one thread ever
 * touches the game objects.
 */
public class WebGame implements Runnable, GameSession {

	private static final int FPS = 60;

	/** KeyEvent needs a source component. An anonymous Component works without a display. */
	private static final Component KEY_SOURCE = new Component() {
		private static final long serialVersionUID = 1L;
	};

	private final StateManager sm;
	private final KeyManager km;

	private final BufferedImage back = new BufferedImage(Game.WIDTH, Game.HEIGHT, BufferedImage.TYPE_INT_RGB);
	private final BufferedImage front = new BufferedImage(Game.WIDTH, Game.HEIGHT, BufferedImage.TYPE_INT_RGB);
	private final Object frameLock = new Object();

	private final Queue<int[]> pendingKeys = new ConcurrentLinkedQueue<>();
	private final Deque<long[]> frameTimes = new ArrayDeque<>();

	private volatile StateSnapshot snapshot;
	private volatile int framesPerSecond;
	private volatile long slowestFrameMicros;
	private volatile boolean running;
	private Thread thread;

	public WebGame() {
		sm = new StateManager();
		km = new KeyManager();
		StateManager.setState(StateManager.MENU); // same as Game()
		snapshot = StateSnapshot.capture();
	}

	public synchronized void start() {
		if (thread != null) {
			return;
		}
		running = true;
		thread = new Thread(this, "web-game-loop");
		thread.start();
	}

	public synchronized void stop() throws InterruptedException {
		running = false;
		if (thread != null) {
			thread.join();
			thread = null;
		}
	}

	@Override
	public void run() {
		// Same timing as Game.run(): one update and render every 1/60 s, busy waiting in between.
		double timePerTick = 1_000_000_000.0 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();

		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / timePerTick;
			lastTime = now;

			if (delta >= 1) {
				long frameStart = System.nanoTime();
				applyPendingKeys();
				update();
				render();
				long frameEnd = System.nanoTime();
				recordFrame(frameEnd, frameEnd - frameStart);
				delta--;
			}
		}
	}

	private void applyPendingKeys() {
		int[] key;
		while ((key = pendingKeys.poll()) != null) {
			boolean pressed = key[1] == 1;
			KeyEvent event = new KeyEvent(KEY_SOURCE, pressed ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED,
					System.currentTimeMillis(), 0, key[0], KeyEvent.CHAR_UNDEFINED);
			// Same listeners and order as Display.setKeyListener(sm) then setKeyListener(km).
			if (pressed) {
				sm.keyPressed(event);
				km.keyPressed(event);
			} else {
				sm.keyReleased(event);
				km.keyReleased(event);
			}
		}
	}

	private void update() {
		if (StateManager.getState() == null) {
			return;
		}
		sm.update();
		km.update();
	}

	private void render() {
		Graphics g = back.getGraphics();
		try {
			g.clearRect(0, 0, Game.WIDTH, Game.HEIGHT);
			if (StateManager.getState() != null) {
				sm.render(g);
			}
		} finally {
			g.dispose();
		}
		synchronized (frameLock) {
			Graphics copy = front.getGraphics();
			copy.drawImage(back, 0, 0, null);
			copy.dispose();
		}
		snapshot = StateSnapshot.capture();
	}

	/**
	 * Keeps one second of frames to report FPS and the slowest frame. The work time
	 * covers only the game (keys, update and render of the states), not the HTTP
	 * layer, so it measures the game's own cost against the 16.7 ms frame budget.
	 */
	private void recordFrame(long endNanos, long workNanos) {
		frameTimes.addLast(new long[] { endNanos, workNanos });
		while (!frameTimes.isEmpty() && endNanos - frameTimes.peekFirst()[0] >= 1_000_000_000L) {
			frameTimes.removeFirst();
		}
		long slowest = 0;
		for (long[] frame : frameTimes) {
			slowest = Math.max(slowest, frame[1]);
		}
		framesPerSecond = frameTimes.size();
		slowestFrameMicros = slowest / 1000;
	}

	@Override
	public byte[] frameJpeg() throws IOException {
		// Copy under the lock (about a millisecond) and encode outside it. Encoding while
		// holding the lock blocked the game loop and dropped it from ~60 to ~45 FPS
		// whenever the page was open.
		BufferedImage copy = new BufferedImage(Game.WIDTH, Game.HEIGHT, BufferedImage.TYPE_INT_RGB);
		synchronized (frameLock) {
			Graphics g = copy.getGraphics();
			g.drawImage(front, 0, 0, null);
			g.dispose();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(copy, "jpg", out);
		return out.toByteArray();
	}

	@Override
	public StateSnapshot snapshot() {
		return snapshot;
	}

	@Override
	public int framesPerSecond() {
		return framesPerSecond;
	}

	@Override
	public long slowestFrameMicros() {
		return slowestFrameMicros;
	}

	@Override
	public void key(int keyCode, boolean pressed) {
		pendingKeys.add(new int[] { keyCode, pressed ? 1 : 0 });
	}
}
