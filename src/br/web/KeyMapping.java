package br.web;

import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Translates browser key names (KeyboardEvent.code) into the AWT key codes the game
 * listens to. Only the keys the game uses are accepted, so the HTTP endpoint cannot
 * be used to inject arbitrary key codes.
 */
public final class KeyMapping {

	private static final Map<String, Integer> CODES = Map.of(
			"KeyW", KeyEvent.VK_W,
			"KeyA", KeyEvent.VK_A,
			"KeyS", KeyEvent.VK_S,
			"KeyD", KeyEvent.VK_D,
			"Space", KeyEvent.VK_SPACE,
			"Enter", KeyEvent.VK_ENTER,
			// The numeric keypad Enter. AWT reports both Enter keys as VK_ENTER, so the
			// desktop game accepts both and the web version must too.
			"NumpadEnter", KeyEvent.VK_ENTER);

	private KeyMapping() {}

	public static OptionalInt toKeyCode(String browserCode) {
		Integer code = browserCode == null ? null : CODES.get(browserCode);
		return code == null ? OptionalInt.empty() : OptionalInt.of(code);
	}
}
