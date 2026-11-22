package br.states;

/**
 * Read-only picture of what the player sees, taken by the game loop after each frame.
 * The web adapter (br.web) sends it to the browser, where the system tests read it.
 *
 * It lives in br.states so it can read the package-private fields MenuState and
 * Level2State already expose for their tests, without changing those classes or
 * adding public getters. Fields that are not visible from here are left null:
 * Level1State keeps its ship and alien private.
 *
 * @param state      name of the current screen: MENU, HELP, GAMEOVER, YOUWIN, LEVEL1, LEVEL2
 * @param menuOption selected option on the main menu, or null on other screens
 * @param shipX      x of the ship on level 2, or null
 * @param aliens     aliens still alive on level 2, or null
 * @param shots      shots in the ship's list on level 2, or null
 */
public record StateSnapshot(String state, String menuOption, Integer shipX, Integer aliens, Integer shots) {

	static final String[] STATE_NAMES = { "MENU", "HELP", "GAMEOVER", "YOUWIN", "LEVEL1", "LEVEL2" };
	private static final String[] MENU_OPTIONS = { "START", "HELP", "EXIT" };

	public static StateSnapshot capture() {
		int index = StateManager.currentState;
		if (index < 0 || index >= STATE_NAMES.length) {
			return new StateSnapshot("UNKNOWN", null, null, null, null);
		}
		String name = STATE_NAMES[index];
		State current = StateManager.getState();

		if (current instanceof MenuState menu) {
			String option = menu.choice >= 0 && menu.choice < MENU_OPTIONS.length ? MENU_OPTIONS[menu.choice] : null;
			return new StateSnapshot(name, option, null, null, null);
		}
		if (current instanceof Level2State level2 && level2.ship != null) {
			return new StateSnapshot(name, null, level2.ship.getX(), level2.aliens.size(), level2.shots.size());
		}
		return new StateSnapshot(name, null, null, null, null);
	}
}
