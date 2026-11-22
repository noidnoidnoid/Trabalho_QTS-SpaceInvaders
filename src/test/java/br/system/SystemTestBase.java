package br.system;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Lifecycle shared by the system tests: one headless Chrome per test class and one
 * fresh game process per test, so every test starts from the main menu.
 */
abstract class SystemTestBase {

    private static WebDriver driver;

    protected GameServerProcess server;
    protected GamePage page;

    @BeforeAll
    static void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,1200", "--mute-audio");
        driver = new ChromeDriver(options);
    }

    @AfterAll
    static void stopBrowser() {
        if (driver != null) driver.quit();
    }

    @BeforeEach
    void startGame() throws Exception {
        server = GameServerProcess.start();
        page = new GamePage(driver).open(server.url());
    }

    @AfterEach
    void stopGame() {
        if (server != null) server.close();
    }

    /** Main menu -> level 1. */
    protected void startLevel1() {
        page.tap(GamePage.ENTER);
        page.waitForState("LEVEL1", java.time.Duration.ofSeconds(3));
    }

    /**
     * Level 1 -> level 2 by playing: hold space with the ship still at the center until
     * a shot hits the alien. The alien first runs to the right wall, comes back one row
     * lower and crosses the ship's column after about 3 s.
     */
    protected void reachLevel2() {
        startLevel1();
        page.press(GamePage.SPACE);
        page.waitForState("LEVEL2", java.time.Duration.ofSeconds(20));
        page.release(GamePage.SPACE);
    }
}
