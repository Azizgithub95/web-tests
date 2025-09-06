package com.aziz.webtests;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseClassWeb {

    protected AndroidDriver driver;

    @SuppressWarnings("deprecation")
	@BeforeEach
    public void setup() throws MalformedURLException {
        UiAutomator2Options opts = new UiAutomator2Options()
                .setPlatformName("Android")
                .setAutomationName("UiAutomator2")
                .setDeviceName("emulator-5554")     // adapte si besoin
                .withBrowserName("Chrome");           // ← Appium 9 : utiliser setBrowserName

        // Appium sur le port 4725
        driver = new AndroidDriver(new URL("http://127.0.0.1:4725"), opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ===== Helpers utiles dans tes tests ===== */

    protected WebElement waitVisible(By locator, long seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void jsClick(WebElement el) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
    }

    protected void fillField(WebElement el, String value) {
        try {
            el.click();
            el.clear();
            el.sendKeys(value);
        } catch (ElementNotInteractableException e) {
            ((JavascriptExecutor) driver).executeScript(
                    "const el=arguments[0]; el.value=arguments[1]; el.dispatchEvent(new Event('input',{bubbles:true}));",
                    el, value
            );
        }
    }
}
