package br.system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.function.Predicate;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the game page: the tests say what the player does ("press S",
 * "hold space") and what the page shows ("state", "menu option"), and only this
 * class knows the element ids and how Selenium sends keys.
 *
 * Every wait polls the DOM with WebDriverWait instead of sleeping a fixed time,
 * so the tests do not depend on how fast the machine is.
 */
final class GamePage {

    static final String W = "w", A = "a", S = "s", D = "d";
    // Keys.RETURN is the main Enter key (code "Enter"). Keys.ENTER is the numeric keypad
    // Enter (code "NumpadEnter") in the WebDriver spec. Using Keys.ENTER first made every
    // ENTER step fail, which showed the web page ignored the keypad Enter.
    static final CharSequence ENTER = Keys.RETURN, NUMPAD_ENTER = Keys.ENTER, SPACE = Keys.SPACE;

    private static final Path SCREENSHOTS = Path.of("target", "evidencias-sistema");

    private final WebDriver driver;

    GamePage(WebDriver driver) {
        this.driver = driver;
    }

    GamePage open(String url) {
        driver.get(url);
        driver.findElement(By.tagName("body")).click(); // keyboard focus on the page
        waitUntil(Duration.ofSeconds(10), page -> "conectado".equals(page.connection()));
        return this;
    }

    // ---- what the page shows ----

    String connection() { return text("connection"); }
    String state() { return text("state"); }
    String menuOption() { return text("menu-option"); }
    Integer shipX() { return number("ship-x"); }
    Integer aliens() { return number("aliens"); }
    Integer fps() { return number("fps"); }

    double slowestFrameMs() {
        String value = text("slowest-frame-ms");
        return "-".equals(value) ? Double.NaN : Double.parseDouble(value);
    }

    private String text(String id) {
        return driver.findElement(By.id(id)).getText().trim();
    }

    private Integer number(String id) {
        String value = text(id);
        return "-".equals(value) || value.isEmpty() ? null : Integer.valueOf(value);
    }

    // ---- what the player does ----

    void tap(CharSequence key) {
        new Actions(driver).keyDown(key).pause(Duration.ofMillis(50)).keyUp(key).perform();
    }

    void press(CharSequence key) {
        new Actions(driver).keyDown(key).perform();
    }

    void release(CharSequence key) {
        new Actions(driver).keyUp(key).perform();
    }

    // ---- waiting ----

    GamePage waitForState(String expected, Duration timeout) {
        return waitUntil(timeout, page -> expected.equals(page.state()));
    }

    GamePage waitForMenuOption(String expected) {
        return waitUntil(Duration.ofSeconds(3), page -> expected.equals(page.menuOption()));
    }

    GamePage waitUntil(Duration timeout, Predicate<GamePage> condition) {
        new WebDriverWait(driver, timeout, Duration.ofMillis(20)).until(d -> condition.test(this));
        return this;
    }

    /** Saves a browser screenshot as evidence, under target/evidencias-sistema. */
    void screenshot(String name) throws IOException {
        Files.createDirectories(SCREENSHOTS);
        Path file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
        Files.copy(file, SCREENSHOTS.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);
    }
}
