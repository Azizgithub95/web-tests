package com.aziz.webtests;

import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class OrangeHrmAppiumTest extends BaseClassWeb {

    /*------------------ Helpers consentement ------------------*/

    /** Ferme le 1er pop-up natif de Chrome (“No thanks” / “Continue”) s’il apparaît. */
    private void dismissChromeFirstRunIfAny() {
        try {
            String initial = driver.getContext();          // ex: CHROMIUM
            driver.context("NATIVE_APP");

            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(2));
            By noThanks   = AppiumBy.id("com.android.chrome:id/negative_button");
            By continueBt = AppiumBy.id("com.android.chrome:id/positive_button");

            try { w.until(ExpectedConditions.elementToBeClickable(noThanks)).click(); }
            catch (TimeoutException e1) {
                try { w.until(ExpectedConditions.elementToBeClickable(continueBt)).click(); }
                catch (TimeoutException ignore) { /* rien à fermer */ }
            }

            // Revenir au contexte web (CHROMIUM / WEBVIEW_chrome…)
            Set<String> ctxs = driver.getContextHandles();
            for (String ctx : ctxs) {
                if (ctx.toUpperCase().contains("CHROM")) { driver.context(ctx); break; }
            }
            if (!driver.getContext().toUpperCase().contains("CHROM")) driver.context(initial);
        } catch (Exception ignore) { }
    }

    /** Ferme une éventuelle bannière cookies côté web (libellés courants EN/FR). */
    private void dismissWebCookiesIfAny() {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(2));

        // Si la bannière est dans une iframe, on y bascule (sélecteur large)
        try {
            By anyConsentFrame = By.cssSelector("iframe[src*='consent'],iframe[title*='consent'],iframe[role='dialog']");
            w.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(anyConsentFrame));
        } catch (TimeoutException ignored) { /* peut être dans le DOM principal */ }

        try {
            By btn = By.xpath(
                    "//button[normalize-space()='Accept all' or normalize-space()='Accept All' or " +
                    "         normalize-space()='Reject all' or normalize-space()='Decline all' or " +
                    "         normalize-space()='Tout accepter' or normalize-space()='Tout refuser' or " +
                    "         contains(@class,'accept') or contains(@class,'reject') or contains(@class,'decline')]"
            );
            WebElement b = w.until(ExpectedConditions.presenceOfElementLocated(btn));
            jsClick(b);  // helper hérité de BaseClassWeb
        } catch (TimeoutException ignored) {
            // Rien trouvé, on laisse continuer
        } finally {
            try { driver.switchTo().defaultContent(); } catch (Exception ignored) {}
        }
    }

    /*------------------ Test OrangeHRM ------------------*/

    @Test
    void loginOrangeHrm() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

        // 1) Fermer les consentements
        dismissChromeFirstRunIfAny();
        dismissWebCookiesIfAny();

        // 2) Attendre l’écran de login
        wait.until(ExpectedConditions.urlContains("/auth/login"));

        // 3) Renseigner username / password
        WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement pass = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));

        fillField(user, "Admin");
        fillField(pass, "admin123");

        // 4) Cliquer sur Login
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        jsClick(loginBtn);

        // 5) Vérifier qu’on est bien loggé
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(.,'Dashboard') and self::* or self::h6]"))
        ));
     // ====== LOGOUT ======
        try {
            // 1) Ouvre le menu utilisateur (avatar / nom en haut à droite)
            By userMenu = By.cssSelector(
                    "p.oxd-userdropdown-name, " +                  // <p> avec le nom
                    "img.oxd-userdropdown-img, " +                 // avatar
                    "i.oxd-icon.bi-caret-down-fill"                // chevron
            );
            // XPaths de secours (ceux de Katalon)
            By userMenuFallback = By.xpath(
                    "//div[@id='app']/div/div/header/div/div[3]/ul/li/span/p|" +
                    "//div[@id='app']/div/div/header/div/div[3]/ul/li"
            );

            WebElement menuBtn;
            try {
                menuBtn = wait.until(ExpectedConditions.elementToBeClickable(userMenu));
            } catch (TimeoutException e) {
                menuBtn = wait.until(ExpectedConditions.elementToBeClickable(userMenuFallback));
            }
            jsClick(menuBtn);

            // 2) Clique sur "Logout" dans le menu déroulant
            By logout = By.xpath(
                    "//a[@href='/web/index.php/auth/logout' or normalize-space()='Logout']"
            );
            WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(logout));
            jsClick(logoutLink);

            // 3) Attends le retour à la page de login (champ username visible)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        } catch (Exception e) {
            System.out.println("Logout non effectué (menu introuvable ?) : " + e.getMessage());
        }

        // 6) Petite pause volontaire pour "voir" le dashboard
        Thread.sleep(5000); // <-- 5 secondes de pause avant fermeture par @AfterEach
    }

}
