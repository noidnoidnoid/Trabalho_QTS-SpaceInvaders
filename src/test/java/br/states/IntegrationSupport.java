package br.states;

import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;

import br.Game;
import br.audio.AudioPlayer;
import br.characters.Shot;
import br.input.KeyManager;

/**
 * Shared wiring for the integration tests in br.states.
 *
 * It builds the real StateManager with every real state, exactly as Game() does,
 * and only swaps what touches hardware or the JVM: the menu gets a mocked theme
 * player (no music during the build) and a recorded exit action (no System.exit).
 * It also resets the static fields the game keeps between tests.
 */
final class IntegrationSupport {

    /** KeyEvent needs a source component. An anonymous Component works in headless mode. */
    private static final Component KEY_SOURCE = new Component() {
        private static final long serialVersionUID = 1L;
    };

    private final State[] originalStates;
    private final int originalCurrentState;

    final StateManager stateManager;
    final KeyManager keyManager;
    final AudioPlayer themeSound = mock(AudioPlayer.class);
    int exitRequests = 0;

    private final BufferedImage frame = new BufferedImage(Game.WIDTH, Game.HEIGHT, BufferedImage.TYPE_INT_ARGB);

    IntegrationSupport() {
        originalStates = StateManager.states.clone();
        originalCurrentState = StateManager.currentState;
        resetStaticGameState();

        stateManager = new StateManager();
        StateManager.states[StateManager.MENU] = new MenuState(themeSound, () -> exitRequests++);
        keyManager = new KeyManager();
    }

    /** Restores StateManager so the next test class starts from what it found. */
    void close() {
        System.arraycopy(originalStates, 0, StateManager.states, 0, originalStates.length);
        StateManager.currentState = originalCurrentState;
        resetStaticGameState();
    }

    private static void resetStaticGameState() {
        KeyManager.a = false;
        KeyManager.d = false;
        KeyManager.space = false;
        Shot.setVelocity(7);
    }

    /** Same order as Game.update(): the state reads the keys before KeyManager refreshes them. */
    void tick() {
        if (StateManager.getState() == null) return;
        stateManager.update();
        keyManager.update();
    }

    /** Same as Game.render(), drawing into an off-screen image instead of the window. */
    void render() {
        Graphics g = frame.getGraphics();
        try {
            stateManager.render(g);
        } finally {
            g.dispose();
        }
    }

    /** A full frame of the game loop. */
    void frame() {
        tick();
        render();
    }

    /** Sends a key to both listeners, as Display does with the real window. */
    void press(int keyCode) {
        KeyEvent event = keyEvent(KeyEvent.KEY_PRESSED, keyCode);
        stateManager.keyPressed(event);
        keyManager.keyPressed(event);
    }

    void release(int keyCode) {
        KeyEvent event = keyEvent(KeyEvent.KEY_RELEASED, keyCode);
        stateManager.keyReleased(event);
        keyManager.keyReleased(event);
    }

    void tap(int keyCode) {
        press(keyCode);
        release(keyCode);
    }

    private static KeyEvent keyEvent(int id, int keyCode) {
        return new KeyEvent(KEY_SOURCE, id, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    State current() {
        return StateManager.getState();
    }

    /**
     * Reads a private field. Used only as a test oracle for Level1State, whose fields
     * are private and belong to another member's class, so we do not widen them.
     */
    @SuppressWarnings("unchecked")
    static <T> T readField(Object target, String name) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return (T) field.get(target);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("No field " + name + " in " + target.getClass());
    }

    static void writeField(Object target, String name, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException("No field " + name + " in " + target.getClass());
    }
}
