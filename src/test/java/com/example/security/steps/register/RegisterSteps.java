package com.example.security.steps.register;

import com.example.security.pages.LoginPage;
import com.example.security.pages.RegisterPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class RegisterSteps {

    WebDriver driver;
    WebDriverWait wait;
    LoginPage loginPage;
    RegisterPage registerPage;

    private static final String BASE_URL = System.getProperty("app.url",
            System.getenv("APP_URL") != null ? System.getenv("APP_URL") : "http://localhost:4200");

    private static final String ADMIN_EMAIL = System.getProperty("test.admin.email",
            System.getenv("TEST_ADMIN_EMAIL") != null ? System.getenv("TEST_ADMIN_EMAIL") : "admin@test.com");

    private static final String ADMIN_PASSWORD = System.getProperty("test.admin.password",
            System.getenv("TEST_ADMIN_PASSWORD") != null ? System.getenv("TEST_ADMIN_PASSWORD") : "admin123");

    @Before
    public void setUp() {
        System.out.println("🚀 Démarrage test REGISTER... URL: " + BASE_URL);

        ChromeOptions options = new ChromeOptions();
        boolean isCI = System.getenv("CI") != null;

        if (isCI) {
            WebDriverManager.chromedriver().setup();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        } else {
            System.setProperty("webdriver.chrome.driver", "C:\\cd\\chromedriver.exe");
            options.addArguments("--start-maximized");
        }

        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Given("je suis connecté en tant qu'administrateur")
    public void je_suis_connecte_en_tant_qu_administrateur() {
        loginPage = new LoginPage(driver);
        loginPage.goToLoginPage(BASE_URL);
        loginPage.enterEmail(ADMIN_EMAIL);
        loginPage.enterPassword(ADMIN_PASSWORD);
        loginPage.selectSite("");
        loginPage.clickLogin();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        System.out.println("✅ Admin connecté");
    }

    @Given("je navigue vers la page d'inscription")
    public void je_navigue_vers_la_page_d_inscription() {
        try {
            By usersLink = By.xpath("//li/a[contains(@class,'nav-item-leoni')]//span[contains(text(),'Utilisateurs')]");
            WebElement usersMenu = wait.until(ExpectedConditions.elementToBeClickable(usersLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", usersMenu);
            Thread.sleep(500);
            usersMenu.click();
            By nouveauLink = By.xpath("//a[@routerlink='/register']");
            wait.until(ExpectedConditions.elementToBeClickable(nouveauLink)).click();
        } catch (Exception e) {
            driver.get(BASE_URL + "/register");
        }
        try { Thread.sleep(2000); } catch(Exception e) {}
        registerPage = new RegisterPage(driver);
        Assertions.assertTrue(registerPage.isOnRegisterPage(), "Page d'inscription non chargée");
    }

    @When("je saisis le prénom {string}")
    public void je_saisis_le_prenom(String firstname) { registerPage.enterFirstname(firstname); }

    @When("je saisis le nom {string}")
    public void je_saisis_le_nom(String lastname) { registerPage.enterLastname(lastname); }

    @When("je saisis l'email professionnel {string}")
    public void je_saisis_l_email_professionnel(String email) { registerPage.enterEmail(email); }

    @When("je saisis le matricule {string}")
    public void je_saisis_le_matricule(String matricule) { registerPage.enterMatricule(matricule); }

    @When("je passe à l'étape sécurité")
    public void je_passe_a_l_etape_securite() { registerPage.nextStep(); }

    @When("je saisis le mot de passe {string}")
    public void je_saisis_le_mot_de_passe(String password) { registerPage.enterPassword(password); }

    @When("je passe à l'étape affectation")
    public void je_passe_a_l_etape_affectation() { registerPage.nextStep(); }

    @When("je sélectionne le rôle {string}")
    public void je_selectionne_le_role(String role) { registerPage.selectRole(role); }

    @When("je sélectionne le projet {string}")
    public void je_selectionne_le_projet(String project) { registerPage.selectProject(project); }

    @When("je sélectionne le site {string}")
    public void je_selectionne_le_site(String site) { registerPage.selectSite(site); }

    @When("je soumets le formulaire")
    public void je_soumet_le_formulaire() { registerPage.submitForm(); }

    @When("je remplis le formulaire avec email existant {string}")
    public void je_remplis_formulaire_email_existant(String email) {
        registerPage.enterFirstname("Test"); registerPage.enterLastname("User");
        registerPage.enterEmail(email); registerPage.enterMatricule("99999");
        registerPage.nextStep(); registerPage.enterPassword("password123");
        registerPage.nextStep(); registerPage.selectRole("Ingénieur");
        registerPage.selectProject("FORD"); registerPage.selectSite("MH1");
    }

    @When("je remplis le formulaire avec matricule existant {string}")
    public void je_remplis_formulaire_matricule_existant(String matricule) {
        registerPage.enterFirstname("Test"); registerPage.enterLastname("User");
        registerPage.enterEmail("nouveau@test.com"); registerPage.enterMatricule(matricule);
        registerPage.nextStep(); registerPage.enterPassword("password123");
        registerPage.nextStep(); registerPage.selectRole("Ingénieur");
        registerPage.selectProject("FORD"); registerPage.selectSite("MH1");
    }

    @Then("l'utilisateur est créé avec succès")
    public void l_utilisateur_est_cree_avec_succes() {
        Assertions.assertTrue(registerPage.isRegisterSuccess(), "Utilisateur non créé");
    }

    @Then("un message de succès s'affiche")
    public void un_message_de_succes_s_affiche() {
        String message = registerPage.getSuccessMessage();
        Assertions.assertFalse(message.isEmpty(), "Aucun message de succès");
    }

    @Then("un message d'erreur {string} s'affiche")
    public void un_message_d_erreur_s_affiche(String expectedError) {
        String actualError = registerPage.getErrorMessage();
        Assertions.assertTrue(actualError.contains(expectedError),
                "Attendu: '" + expectedError + "', reçu: '" + actualError + "'");
    }

    @Then("le formulaire ne se soumet pas")
    public void le_formulaire_ne_se_soumet_pas() {
        try {
            Thread.sleep(2000);
            WebElement submitBtn = driver.findElement(By.cssSelector("button.btn-submit"));
            Assertions.assertFalse(submitBtn.isEnabled(), "Le bouton devrait être désactivé");
        } catch (Exception e) {
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertFalse(currentUrl.contains("/listeuser"), "Formulaire soumis alors qu'invalide");
        }
    }

    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed() && driver != null) {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "screenshot_failure");
        }
        if (driver != null) driver.quit();
    }
}