package com.example.security.steps.login;

import com.example.security.pages.LoginPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

public class LoginSteps {

    WebDriver driver;
    LoginPage loginPage;

    // ✅ URL depuis variable d'environnement (GitHub Secrets)
    // En local      → http://localhost:4200
    // En CI/CD      → https://leoni-test-modulesfrontend.onrender.com
    private static final String BASE_URL = System.getProperty("app.url",
            System.getenv("APP_URL") != null
                    ? System.getenv("APP_URL")
                    : "http://localhost:4200");

    // ✅ Email/password admin depuis GitHub Secrets
    private static final String ADMIN_EMAIL = System.getProperty("test.admin.email",
            System.getenv("TEST_ADMIN_EMAIL") != null
                    ? System.getenv("TEST_ADMIN_EMAIL") : "admin@test.com");

    private static final String ADMIN_PASSWORD = System.getProperty("test.admin.password",
            System.getenv("TEST_ADMIN_PASSWORD") != null
                    ? System.getenv("TEST_ADMIN_PASSWORD") : "admin123");

    @Before
    public void setUp() {
        System.out.println("========================================");
        System.out.println("🚀 Démarrage test LOGIN...");
        System.out.println("🌐 URL : " + BASE_URL);
        System.out.println("========================================");

        ChromeOptions options = new ChromeOptions();

        // ✅ CORRECTION PRINCIPALE :
        // Détecte si on est en CI (Linux GitHub Actions) ou en local (Windows)
        boolean isCI = System.getenv("CI") != null;

        if (isCI) {
            // ── Mode CI/CD GitHub Actions (Linux, pas d'écran) ──
            System.out.println("🤖 Mode CI/CD détecté → Chrome headless");
            WebDriverManager.chromedriver().setup(); // Télécharge automatiquement
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        } else {
            // ── Mode local Windows (avec écran) ──
            System.out.println("🖥️  Mode local détecté → Chrome visible");
            System.setProperty("webdriver.chrome.driver", "C:\\cd\\chromedriver.exe");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
        }

        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-extensions");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(30));
        driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(90));
        System.out.println("✅ Chrome prêt !");
    }

    @Given("je suis sur la page de connexion")
    public void je_suis_sur_la_page_de_connexion() {
        loginPage = new LoginPage(driver);
        System.out.println("🔗 Navigation vers : " + BASE_URL);
        loginPage.goToLoginPage(BASE_URL);
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("onrender.com") ||
                        driver.getCurrentUrl().contains("localhost"),
                "URL inattendue: " + driver.getCurrentUrl()
        );
    }

    @When("je saisis l'email {string}")
    public void je_saisis_l_email(String email) {
        // ✅ Si email vide dans le feature → utilise admin email réel des secrets
        String actualEmail = email.isEmpty() ? ADMIN_EMAIL : email;
        loginPage.enterEmail(actualEmail);
    }

    @When("je saisis le mot de passe {string}")
    public void je_saisis_le_mot_de_passe(String password) {
        String actualPassword = password.isEmpty() ? ADMIN_PASSWORD : password;
        loginPage.enterPassword(actualPassword);
    }

    @When("je sélectionne le site {string}")
    public void je_selectionne_le_site(String siteName) {
        loginPage.selectSite(siteName);
    }

    @When("je clique sur le bouton {string}")
    public void je_clique_sur_le_bouton(String buttonText) {
        loginPage.clickLogin();
    }

    @Then("je suis redirigé vers le tableau de bord")
    public void je_suis_redirige_vers_le_tableau_de_bord() {
        Assertions.assertTrue(
                loginPage.isDashboardDisplayed(),
                "Dashboard non affiché. URL: " + driver.getCurrentUrl()
        );
    }

    @Then("je vois le titre contenant {string}")
    public void je_vois_le_titre_contenant(String expectedText) {
        String title = loginPage.getDashboardTitleText();
        Assertions.assertTrue(title.contains(expectedText),
                "Titre attendu: '" + expectedText + "' trouvé: '" + title + "'");
    }

    @Then("un message d'erreur s'affiche {string}")
    public void un_message_d_erreur_s_affiche(String expectedError) {
        String actualError = loginPage.getErrorMessage();
        Assertions.assertTrue(actualError.contains(expectedError),
                "Attendu: '" + expectedError + "', reçu: '" + actualError + "'");
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && driver != null) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "screenshot_failure");
        }
        if (driver != null) {
            driver.quit();
        }
    }
}