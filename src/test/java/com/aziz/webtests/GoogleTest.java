package com.aziz.webtests;

import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class GoogleTest extends BaseClassWeb {

    /** Ferme le pop-up natif Chrome ("No thanks" / "Continue") si présent. */
    private void dismissChromeFirstRunIfAny() {
        try {
            // Sauvegarde le contexte courant
            String current = driver.getContext();

            // Passe en natif
            driver.context("NATIVE_APP");

            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(3));
            By noThanks = AppiumBy.id("com.android.chrome:id/negative_button");
            By continueBtn = AppiumBy.id("com.android.chrome:id/positive_button");

            // Clique "No thanks" si visible, sinon "Continue"
            try {
                w.until(ExpectedConditions.elementToBeClickable(noThanks)).click();
            } catch (TimeoutException e) {
                try {
                    w.until(ExpectedConditions.elementToBeClickable(continueBtn)).click();
                } catch (TimeoutException ignored) {
                    // Rien à cliquer → pas de pop-up, on continue
                }
            }

            // Reviens au contexte web (Chrome = "CHROMIUM")
            // (parfois le nom peut être WEBVIEW_chrome, on choisit celui qui contient "CHROM")
            Set<String> contexts = driver.getContextHandles();
            for (String ctx : contexts) {
                if (ctx.toUpperCase().contains("CHROM")) {
                    driver.context(ctx);
                    break;
                }
            }

            // En cas d’échec, reviens au contexte initial
            if (!driver.getContext().toUpperCase().contains("CHROM")) {
                driver.context(current);
            }
        } catch (Exception ignored) {
            // On ignore si rien à faire
        }
    }

    @Test
    void openGoogleAndSearch() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Ouvre Google (version sans redirection locale)
        driver.get("https://www.google.com/ncr");

        // 0) Ferme le pop-up natif Chrome s'il apparaît
        dismissChromeFirstRunIfAny();

        // 1) Consentement (dans une iframe côté web)
        try {
            By consentFrame = By.cssSelector(
                "iframe[src*='consent'], iframe[aria-label*='consent'], iframe[role='dialog']"
            );
            wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(consentFrame));

            By accept = By.cssSelector(
                "#L2AGLb, button[aria-label*='Agree'], button[aria-label*='J\\'accepte'], div[role='button'][jsname][data-ved]"
            );
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(accept));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        } catch (TimeoutException ignored) {
            // pas de consentement
        } finally {
            try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}
        }

        // 2) Saisir la recherche
        By searchBox = By.cssSelector("textarea[name='q']");
        try {
            WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", box);
            box.click();
            try {
                box.clear();
                box.sendKeys("Appium Java Client", Keys.ENTER);
            } catch (ElementNotInteractableException e) {
                driver.switchTo().activeElement().sendKeys("Appium Java Client", Keys.ENTER);
            }
        } catch (TimeoutException e) {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='q']")));
            input.click();
            input.sendKeys("Appium Java Client", Keys.ENTER);
        }

        // 3) Vérification rapide
        wait.until(ExpectedConditions.titleContains("Appium"));
    }
}
