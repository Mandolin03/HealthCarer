package com.evaruiz.healthcarer.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MedicationE2ETest {

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
        options.addArguments("--lang=en-US");
        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--headless");

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
        driver.get("https://localhost:"+this.port+"/login");
        driver.findElement(By.id("email")).sendKeys("bob@example.com");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("login")).click();
        wait.until(ExpectedConditions.titleIs("HealthCareR"));
        driver.findElement(By.id("medications")).click();
        wait.until(ExpectedConditions.titleIs("Medicamentos"));
    }

    @Test
    public void getMedicationsE2E() {
        List<WebElement> takes = driver.findElements(By.className("medications-item"));
        assertThat(takes.size()).isGreaterThanOrEqualTo(1);

    }

    @Test
    public void getMedicationDetailsE2E() {

        wait.until(ExpectedConditions.elementToBeClickable(By.className("details-button"))).click();
        wait.until(ExpectedConditions.titleIs("Detalles"));
        assertThat(driver.getTitle()).isEqualTo("Detalles");
        assertThat(driver.findElement(By.id("name")).getText()).isNotEmpty();
        assertThat(driver.findElement(By.id("photo")).isDisplayed()).isTrue();


    }
    
    @Test
    public void createMedicationE2E(){

        driver.findElement(By.id("createMedication")).click();
        wait.until(ExpectedConditions.titleIs("Nuevo medicamento"));
        driver.findElement(By.id("name")).sendKeys("Nuevo");
        driver.findElement(By.id("stock")).sendKeys("100");
        driver.findElement(By.id("instructions")).sendKeys("Tomar con agua");
        driver.findElement(By.id("dose")).sendKeys("2");
        driver.findElement(By.id("create")).click();
        wait.until(ExpectedConditions.titleIs("Detalles"));
        assertThat(driver.findElement(By.id("name")).getText()).contains("Nuevo");
    }

    @Test
    public void updateMedicationE2E() {

        wait.until(ExpectedConditions.elementToBeClickable(By.className("edit-button"))).click();
        wait.until(ExpectedConditions.titleIs("Editar Medicamento"));
        driver.findElement(By.id("name")).clear();
        driver.findElement(By.id("name")).sendKeys("Updated");
        driver.findElement(By.id("stock")).clear();
        driver.findElement(By.id("stock")).sendKeys("200");
        driver.findElement(By.id("instructions")).clear();
        driver.findElement(By.id("instructions")).sendKeys("Updated instructions");
        driver.findElement(By.id("dose")).clear();
        driver.findElement(By.id("dose")).sendKeys("3");
        driver.findElement(By.id("update")).click();

        wait.until(ExpectedConditions.titleIs("Detalles"));
        assertThat(driver.getTitle()).isEqualTo("Detalles");
        assertThat(driver.findElement(By.id("name")).getText()).contains("Updated");

    }

    @Test
    public void deleteMedicationE2E() {
        int medicationCount = driver.findElements(By.className("medications-item")).size() - 1;
        driver.findElement(By.className("delete-button")).click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.titleIs("Medicamentos"));
        assertThat(driver.findElements(By.className("medications-item")).size()).isEqualTo(medicationCount);


    }

}
