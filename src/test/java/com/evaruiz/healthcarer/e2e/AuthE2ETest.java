package com.evaruiz.healthcarer.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AuthE2ETest {
    @LocalServerPort
    int port;

    protected WebDriver driver;
    protected WebDriverWait wait;



    @BeforeEach
    public void setUpTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.manage().deleteAllCookies();
            driver.quit();
        }
    }

    @Test
    @DisplayName("Registration and login")
    public void registerAndLogin(){
        String email = "test" + System.currentTimeMillis() + "@email.com";
        driver.get("https://localhost:" + this.port + "/register");
        driver.findElement(By.id("name")).sendKeys("test");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys("testpass");
        driver.findElement(By.id("register")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys("testpass");
        driver.findElement(By.id("login")).click();
        wait.until(ExpectedConditions.urlContains("/"));
        assertThat(driver.getTitle()).isEqualTo("HealthCareR");
    }

    @Test
    @DisplayName("Registration with existing email")
    public void registerWithExistingEmail(){

        String email = "test2" + System.currentTimeMillis() + "@email.com";
        driver.get("https://localhost:" + this.port + "/register");
        driver.findElement(By.id("name")).sendKeys("test");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys("testpass");
        driver.findElement(By.id("register")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        driver.get("https://localhost:" + this.port + "/register");
        driver.findElement(By.id("name")).sendKeys("test2");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys("testpass2");
        driver.findElement(By.id("register")).click();
        wait.until(ExpectedConditions.urlContains("/errorPage"));
        assertThat(driver.getTitle()).isEqualTo("Error");
        assertThat(driver.findElement(By.id("error")).getText()).isEqualTo("El email ya está en uso");
    }
}
