package com.evaruiz.healthcarer.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TakeE2ETest {
    @LocalServerPort
    int port;

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    public void setUpTest() {
        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        login();
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login() {
        driver.get("https://localhost:" + this.port + "/login");
        driver.findElement(By.id("email")).sendKeys("alice@example.com");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("login")).click();
        wait.until(ExpectedConditions.titleIs("HealthCareR"));
        driver.findElement(By.id("takes")).click();
        wait.until(ExpectedConditions.titleIs("Historial de tomas"));
    }


    @Test
    public void getTakesE2E() {
        List<WebElement> takes = driver.findElements(By.className("takes-item"));
        assertThat(takes.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void getTakeE2E() {
        driver.findElement(By.className("details-button")).click();
        wait.until(ExpectedConditions.titleIs("Detalles de la toma"));
        assertThat(driver.findElements(By.className("taken-medications-list")).size()).isEqualTo(1);
        assertThat(driver.findElement(By.id("date")).getText()).isNotEmpty();
        assertThat(driver.findElement(By.id("time")).getText()).isNotEmpty();
    }

    @Test
    public void createTakeE2E() {
        driver.findElement(By.id("create-take")).click();
        wait.until(ExpectedConditions.titleIs("Añadir Nueva Toma"));

        WebElement dateInput = driver.findElement(By.id("date"));
        dateInput.sendKeys("10-12-2020" + Keys.TAB + "12:15");

        List<WebElement> takenMedications = driver.findElements(By.className("checkbox-group"));
        assertThat(takenMedications.size()).isGreaterThanOrEqualTo(1);
        for (WebElement ignored : takenMedications) {
            driver.findElement(By.className("check-medication")).click();
        }

        driver.findElement(By.id("create")).click();
        wait.until(ExpectedConditions.titleIs("Detalles de la toma"));

        assertThat(driver.findElement(By.id("date")).getText()).isEqualTo("2020-12-10");
        assertThat(driver.findElement(By.id("time")).getText()).isEqualTo("12:15");
    }

    @Test
    public void updateTakeE2E() {
        driver.findElement(By.className("edit-button")).click();
        wait.until(ExpectedConditions.titleIs("Editar Toma"));

        WebElement dateInput = driver.findElement(By.id("date"));
        dateInput.clear();
        dateInput.sendKeys("11-11-1999" + Keys.TAB + "11:11");

        List<WebElement> takenMedications = driver.findElements(By.className("checkbox-group"));
        assertThat(takenMedications.size()).isGreaterThanOrEqualTo(1);
        for (WebElement ignored : takenMedications) {
            driver.findElement(By.className("check-medication")).click();
        }

        driver.findElement(By.id("edit")).click();
        wait.until(ExpectedConditions.titleIs("Detalles de la toma"));

        assertThat(driver.findElement(By.id("date")).getText()).isEqualTo("1999-11-11");
        assertThat(driver.findElement(By.id("time")).getText()).isEqualTo("11:11");
    }

    @Test
    public void deleteTakeE2E() {
        int takeCount = driver.findElements(By.className("takes-item")).size() - 1;
        driver.findElement(By.className("delete-button")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.titleIs("Historial de tomas"));
        assertThat(driver.findElements(By.className("takes-item")).size()).isEqualTo(takeCount);
    }

}
