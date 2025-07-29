package com.evaruiz.healthcarer.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
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
public class TreatmentE2ETest {

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
        //options.addArguments("--headless");
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
        driver.findElement(By.id("treatments")).click();
        wait.until(ExpectedConditions.titleIs("Lista de Tratamientos"));
    }

    @Test
    public void getTreatmentsE2E() {
        List<WebElement> treatments = driver.findElements(By.className("treatment-item"));
        assertThat(treatments.size()).isGreaterThan(0);
    }

    @Test
    public void getTreatmentE2E(){
        driver.findElement(By.className("details-button")).click();
        wait.until(ExpectedConditions.titleIs("Detalles del Tratamiento"));
        assertThat(driver.findElements(By.className("treatment-details-container")).size()).isEqualTo(1);
        assertThat(driver.findElement(By.id("name")).getText()).isNotEmpty();
        assertThat(driver.findElement(By.id("startDate")).getText()).isNotEmpty();
        assertThat(driver.findElement(By.id("endDate")).getText()).isNotEmpty();
        assertThat(driver.findElement(By.id("frequency")).getText()).isNotEmpty();
        assertThat(driver.findElements(By.className("medications")).size()).isGreaterThanOrEqualTo(1);
    }



    @Test
    public void updateTreatmentE2E(){
        wait.until(ExpectedConditions.elementToBeClickable(By.id("edit")));
        WebElement editButton = driver.findElement(By.id("edit"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);

        wait.until(ExpectedConditions.titleIs("Editar Tratamiento"));

        WebElement nameInput = driver.findElement(By.id("name"));
        nameInput.clear();
        nameInput.sendKeys("Tratamiento Actualizado");

        WebElement startDateInput = driver.findElement(By.id("startDate"));
        startDateInput.clear();
        startDateInput.sendKeys("11-11-2022" + Keys.TAB + "10:10" + Keys.TAB + "10:10");

        WebElement endDateInput = driver.findElement(By.id("endDate"));
        endDateInput.clear();
        endDateInput.sendKeys("12-12-2022" + Keys.TAB + "10:10" + Keys.TAB + "10:10");

        WebElement frequencyInput = driver.findElement(By.id("dispensingFrequency"));
        frequencyInput.clear();
        frequencyInput.sendKeys("48");

        List<WebElement> takenMedications = driver.findElements(By.className("checkbox-group"));
        assertThat(takenMedications.size()).isGreaterThanOrEqualTo(1);
        for (WebElement ignored : takenMedications) {
            driver.findElement(By.className("check-medication")).click();
        }

        driver.findElement(By.id("submit-treatment")).click();

        wait.until(ExpectedConditions.titleIs("Detalles del Tratamiento"));

        assertThat(driver.findElements(By.className("treatment-details-container")).size()).isEqualTo(1);
        assertThat(driver.findElement(By.id("name")).getText()).isEqualTo("Tratamiento Actualizado");
        assertThat(driver.findElement(By.id("startDate")).getText()).isEqualTo("11-11-2022");
        assertThat(driver.findElement(By.id("endDate")).getText()).isEqualTo("12-12-2022");
        assertThat(driver.findElement(By.id("frequency")).getText()).isEqualTo("48.0 horas");
    }

    @Test
    public void deleteTreatmentE2E(){
        int takeCount = driver.findElements(By.className("treatment-item")).size() - 1;
        WebElement deleteButton = driver.findElement(By.id("delete"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButton);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        wait.until(ExpectedConditions.titleIs("Lista de Tratamientos"));
        assertThat(driver.findElements(By.className("treatment-item")).size()).isEqualTo(takeCount);
    }

    @Test
    public void createTreatmentE2E() {
        driver.findElement(By.id("create-treatment")).click();
        wait.until(ExpectedConditions.titleIs("Añadir Nuevo Tratamiento"));

        driver.findElement(By.id("name")).sendKeys("Tratamiento de Prueba");
        driver.findElement(By.id("startDate")).sendKeys("10-01-2023" + Keys.TAB + "11:11");
        driver.findElement(By.id("endDate")).sendKeys("12-01-2023" + Keys.TAB + "11:11");
        driver.findElement(By.id("dispensingFrequency")).sendKeys("24");
        List<WebElement> takenMedications = driver.findElements(By.className("checkbox-group"));
        assertThat(takenMedications.size()).isGreaterThanOrEqualTo(1);
        for (WebElement ignored : takenMedications) {
            driver.findElement(By.className("check-medication")).click();
        }

        driver.findElement(By.id("submit-treatment")).click();

        wait.until(ExpectedConditions.titleIs("Detalles del Tratamiento"));
        assertThat(driver.findElements(By.className("treatment-details-container")).size()).isEqualTo(1);
        assertThat(driver.findElement(By.id("name")).getText()).isEqualTo("Tratamiento de Prueba");
        assertThat(driver.findElement(By.id("startDate")).getText()).isEqualTo("10-01-2023");
        assertThat(driver.findElement(By.id("endDate")).getText()).isEqualTo("12-01-2023");
        assertThat(driver.findElement(By.id("frequency")).getText()).isEqualTo("24.0 horas");
        assertThat(driver.findElements(By.className("medications")).size()).isGreaterThanOrEqualTo(1);
    }
}
