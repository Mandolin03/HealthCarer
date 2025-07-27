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
import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--allow-insecure-localhost");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

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
    }

    @Test
    @DisplayName("Login with existing user")
    public void loginWithExistingUser() {
        driver.get("https://localhost:" + this.port + "/login");
        wait.until(ExpectedConditions.titleIs("Inicio de sesión"));
        driver.findElement(By.id("email")).sendKeys("bob@example.com");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("login")).click();
        wait.until(ExpectedConditions.titleIs("HealthCareR"));

    }

    @Test
    @DisplayName("Register and login with the new user")
    public void registerAndLogin(){
        String email = "test" + System.currentTimeMillis() + "@email.com";
        driver.get("https://localhost:" + this.port + "/register");
        driver.findElement(By.id("name")).sendKeys("test");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys("testpass");
        driver.findElement(By.id("register")).click();
        wait.until(ExpectedConditions.titleIs("Inicio de sesión"));
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys("testpass");
        driver.findElement(By.id("login")).click();
        wait.until(ExpectedConditions.titleIs("HealthCareR"));
        assertThat(driver.getTitle()).isEqualTo("HealthCareR");
    }

    @Test
    @DisplayName("Register with existing email")
    public void registerWithExistingEmail(){

        driver.get("https://localhost:" + this.port + "/register");
        driver.findElement(By.id("name")).sendKeys("test2");
        driver.findElement(By.id("email")).sendKeys("alice@example.com");
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.id("register")).click();
        wait.until(ExpectedConditions.titleIs("Error"));
        assertThat(driver.findElement(By.id("error")).getText()).isEqualTo("El email ya está en uso");
    }

    @Test
    @DisplayName("Logout redirects to login page")
    public void logoutRedirectsToLoginPage() {
        login();
        driver.findElement(By.id("logout")).click();
        wait.until(ExpectedConditions.titleIs("Inicio de sesión"));
        assertThat(driver.getTitle()).isEqualTo("Inicio de sesión");
    }

    @Test
    @DisplayName("Profile page is accessible after login")
    public void profilePageAccessibleAfterLogin() {
       login();
        driver.findElement(By.id("profile")).click();
        wait.until(ExpectedConditions.titleIs("Perfil"));
        assertThat(driver.findElement(By.id("profile-name")).getText()).isEqualTo("Bob Johnson");
        assertThat(driver.findElement(By.id("profile-email")).getText()).isEqualTo("bob@example.com");
    }

    @Test
    @DisplayName("Edit profile updates user information")
    public void editProfileUpdatesUserInformation() {
        login();
        driver.findElement(By.id("profile")).click();
        wait.until(ExpectedConditions.titleIs("Perfil"));
        driver.findElement(By.id("editProfile")).click();
        wait.until(ExpectedConditions.titleIs("Editar Perfil"));
        driver.findElement(By.id("name")).clear();
        driver.findElement(By.id("name")).sendKeys("Updated Bob");
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys("updatedbob@example.com");
        driver.findElement(By.id("update")).click();
        wait.until(ExpectedConditions.titleIs("Perfil"));
        assertThat(driver.findElement(By.id("profile-name")).getText()).isEqualTo("Updated Bob");
        assertThat(driver.findElement(By.id("profile-email")).getText()).isEqualTo("updatedbob@example.com");
    }

}
