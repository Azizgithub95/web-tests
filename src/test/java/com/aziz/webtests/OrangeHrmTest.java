package com.aziz.webtests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

public class OrangeHrmTest {

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        // Lancer Chrome
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    }

    @Test
    void loginAndChangePasswordNavigation() {
        // 1) Ouvrir le site OrangeHRM
        driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");

        // 2) Entrer le username
        WebElement username = driver.findElement(By.name("username"));
        username.sendKeys("Admin");

        // 3) Entrer le mot de passe (ici déchiffré manuellement de ton script Katalon : admin123)
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("admin123");

        // 4) Cliquer sur le bouton Login
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        loginBtn.click();

        // 5) Ouvrir le menu utilisateur (avatar en haut à droite)
        WebElement userMenu = driver.findElement(By.cssSelector("img.oxd-userdropdown-img"));
        userMenu.click();

        // 6) Cliquer sur "Change Password"
        WebElement changePwd = driver.findElement(By.linkText("Change Password"));
        changePwd.click();

        // Vérification simple
        String currentUrl = driver.getCurrentUrl();
        System.out.println("URL actuelle après clic: " + currentUrl);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
