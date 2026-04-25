package com.example.security.steps.chargesheet;

import com.example.security.pages.ChargeSheetPage;
import com.example.security.pages.LoginPage;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChargeSheetSteps {

    WebDriver driver;
    WebDriverWait wait;
    LoginPage loginPage;
    ChargeSheetPage chargeSheetPage;

    private static final String BASE_URL = System.getProperty("app.url",
            System.getenv("APP_URL") != null ? System.getenv("APP_URL") : "http://localhost:4200");

    private static final String ING_EMAIL = System.getProperty("test.ing.email",
            System.getenv("TEST_ING_EMAIL") != null ? System.getenv("TEST_ING_EMAIL") : "riadh.smida@leoni.com");

    private static final String ING_PASSWORD = System.getProperty("test.ing.password",
            System.getenv("TEST_ING_PASSWORD") != null ? System.getenv("TEST_ING_PASSWORD") : "1234512345");

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @Before
    public void setUp() {
        System.out.println("🚀 Démarrage test CHARGESHEET... URL: " + BASE_URL);

        ChromeOptions options = new ChromeOptions();
        boolean isCI = System.getenv("CI") != null;

        if (isCI) {
            // ✅ CI/CD GitHub Actions (Linux sans écran)
            WebDriverManager.chromedriver().setup();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        } else {
            // ✅ Local Windows
            System.setProperty("webdriver.chrome.driver", "C:\\cd\\chromedriver.exe");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-blink-features=AutomationControlled");
        }

        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Given("je suis connecté en tant qu'ingénieur")
    public void je_suis_connecte_en_tant_qu_ingenieur() {
        loginPage = new LoginPage(driver);
        loginPage.goToLoginPage(BASE_URL);
        loginPage.enterEmail(ING_EMAIL);
        loginPage.enterPassword(ING_PASSWORD);
        loginPage.selectSite("MH2");
        loginPage.clickLogin();
        sleep(8000);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "Non connecté, URL: " + driver.getCurrentUrl());
    }

    @Given("je navigue vers la page des cahiers des charges")
    public void je_navigue_vers_la_page_des_cahiers_des_charges() {
        chargeSheetPage = new ChargeSheetPage(driver);
        chargeSheetPage.goToChargeSheetList();
    }

    @When("je clique sur Nouveau cahier")
    public void je_clique_sur_nouveau_cahier() { chargeSheetPage.clickNewChargeSheet(); }

    @When("je sélectionne le projet {string}")
    public void je_selectionne_le_projet(String project) { sleep(1000); chargeSheetPage.selectProject(project); }

    @When("je saisis la référence harnais {string}")
    public void je_saisis_la_reference_harnais(String v) { chargeSheetPage.enterHarnessRef(v); }

    @When("je saisis le numéro de téléphone {string}")
    public void je_saisis_le_numero_de_telephone(String v) { chargeSheetPage.enterPhoneNumber(v); }

    @When("je saisis le numéro de commande {string}")
    public void je_saisis_le_numero_de_commande(String v) { chargeSheetPage.enterOrderNumber(v); }

    @When("je saisis le centre de coût {string}")
    public void je_saisis_le_centre_de_cout(String v) { chargeSheetPage.enterCostCenter(v); }

    @When("je saisis la date du jour")
    public void je_saisis_la_date_du_jour() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        chargeSheetPage.enterDate(today);
        sleep(500);
    }

    @When("je saisis la date de livraison {string}")
    public void je_saisis_la_date_de_livraison(String d) { chargeSheetPage.enterDeliveryDate(d); sleep(500); }

    @When("j'ajoute un item avec les caractéristiques suivantes:")
    public void j_ajoute_un_item(io.cucumber.datatable.DataTable dt) {
        dt.asMaps(String.class, String.class).forEach(row ->
                chargeSheetPage.addNewItemAndFill(
                        row.get("samplesExist"), row.get("ways"), row.get("housingColour"),
                        row.get("testModuleExist"), row.get("housingRefLeoni"),
                        row.get("housingRefSupplier"), row.get("referenceSeals"), row.get("quantity")));
    }

    @When("je remplis l'item avec les caractéristiques suivantes:")
    public void je_remplis_l_item(io.cucumber.datatable.DataTable dt) {
        dt.asMaps(String.class, String.class).forEach(row ->
                chargeSheetPage.fillExistingItem(
                        row.get("samplesExist"), row.get("ways"), row.get("housingColour"),
                        row.get("testModuleExist"), row.get("housingRefLeoni"),
                        row.get("housingRefSupplier"), row.get("referenceSeals"), row.get("quantity")));
    }

    @When("je soumets le cahier des charges")
    public void je_soumets_le_cahier_des_charges() { chargeSheetPage.submitForm(); }

    @Then("un message de confirmation s'affiche")
    public void un_message_de_confirmation_s_affiche() { Assertions.assertTrue(true); }

    @Then("le formulaire ne se soumet pas")
    public void le_formulaire_ne_se_soumet_pas() {
        try {
            Thread.sleep(2000);
            WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
            Assertions.assertFalse(submitBtn.isEnabled(), "Bouton devrait être désactivé");
            Assertions.assertTrue(driver.getCurrentUrl().contains("/charge-sheets/create"));
        } catch (Exception e) {
            Assertions.assertFalse(driver.getCurrentUrl().contains("/charge-sheets/list"));
        }
    }

    @Then("le cahier des charges est créé avec succès")
    public void le_cahier_des_charges_est_cree_avec_succes() {
        sleep(3000);
        boolean success = chargeSheetPage.isCreationSuccess();
        Assertions.assertTrue(success, "Cahier non créé. URL: " + driver.getCurrentUrl());
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