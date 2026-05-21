package com.example.security.steps.gestionconfig;

import com.example.security.pages.GestionConfigPage;
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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class GestionConfigSteps {

    WebDriver driver;
    WebDriverWait wait;
    WebDriverWait shortWait;
    LoginPage loginPage;
    GestionConfigPage gestionConfigPage;

    private static final String BASE_URL = System.getProperty("app.url",
            System.getenv("APP_URL") != null ? System.getenv("APP_URL") : "https://leoni-test-modulesfrontend.onrender.com");

    private static final String ADMIN_EMAIL = System.getProperty("test.admin.email",
            System.getenv("TEST_ADMIN_EMAIL") != null ? System.getenv("TEST_ADMIN_EMAIL") : "mehdi.chattii@leoni.com");

    private static final String ADMIN_PASSWORD = System.getProperty("test.admin.password",
            System.getenv("TEST_ADMIN_PASSWORD") != null ? System.getenv("TEST_ADMIN_PASSWORD") : "00000000");

    private String currentProjetName;
    private String currentSiteName;

    @Before
    public void setUp() {
        System.out.println("🚀 Démarrage test GESTION CONFIG... URL: " + BASE_URL);

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

        // ✅ 1. Créer le driver d'abord
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));

        // ✅ 2. Créer les waits (driver n'est plus null)
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Given("je suis connecté en tant qu'administrateur")
    public void je_suis_connecte_en_tant_qu_administrateur() {
        loginPage = new LoginPage(driver);
        loginPage.goToLoginPage(BASE_URL);
        loginPage.enterEmail(ADMIN_EMAIL);
        loginPage.enterPassword(ADMIN_PASSWORD);
        loginPage.selectSite("");
        loginPage.clickLogin();
        try { Thread.sleep(3000); } catch(Exception e) {}
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Connexion échouée");
    }

    @And("je navigue vers la page {string}")
    public void je_navigue_vers_la_page(String pageName) {
        try {
            if (pageName.equals("Gestion Configuration")) {
                By configLink = By.xpath("//a[contains(@routerLink, 'gestion-config') or contains(text(), 'Configuration')]");
                WebElement link = driver.findElement(configLink);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                link.click();
                try { Thread.sleep(2000); } catch(Exception e) {}
            }
        } catch (Exception e) {
            driver.get(BASE_URL + "/gestion-config");
        }
        gestionConfigPage = new GestionConfigPage(driver);
        try { Thread.sleep(2000); } catch(Exception e) {}
    }

    // ==================== PROJETS ====================

    @When("je clique sur {string}")
    public void je_clique_sur(String buttonText) {
        if (buttonText.equals("Nouveau projet")) {
            gestionConfigPage.clickNouveauProjet();
        } else if (buttonText.equals("Nouveau site")) {
            gestionConfigPage.clickNouveauSite();
        }
    }

    @When("je saisis le nom du projet {string}")
    public void je_saisis_le_nom_du_projet(String name) {
        if (name != null && !name.isEmpty()) {
            gestionConfigPage.enterProjetName(name);
            this.currentProjetName = name;
        }
    }

    @When("je laisse le nom du projet vide")
    public void je_laisse_le_nom_du_projet_vide() {
        gestionConfigPage.enterProjetName("");
    }

    @When("je saisis la description {string}")
    public void je_saisis_la_description(String description) {
        if (description != null && !description.isEmpty()) {
            gestionConfigPage.enterProjetDescription(description);
        }
    }
    @When("je saisis la description du site {string}")
    public void je_saisis_la_description_du_site(String description) {
        if (description != null && !description.isEmpty()) {
            gestionConfigPage.enterSiteDescription(description);
        }
    }
    private void closeAlertIfPresent() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            System.out.println("🔔 Alerte fermée: " + alertText);
            alert.accept();
            Thread.sleep(500);
        } catch (NoAlertPresentException e) {
            // Pas d'alerte
        } catch (Exception e) {
            System.out.println("⚠️ Erreur fermeture alerte: " + e.getMessage());
        }
    }

    @When("je valide le formulaire")
    public void je_valide_le_formulaire() {
        gestionConfigPage.submitSite();

    }

    @Given("un projet {string} existe")
    public void un_projet_existe(String projetName) {
        closeAlertIfPresent();
        gestionConfigPage.goToProjetsTab();
        if (!gestionConfigPage.projetExists(projetName)) {
            gestionConfigPage.clickNouveauProjet();
            gestionConfigPage.enterProjetName(projetName);
            gestionConfigPage.enterProjetDescription("Créé automatiquement pour test");
            gestionConfigPage.submitProjet();
            try { Thread.sleep(2000); } catch(Exception e) {}
        }
        this.currentProjetName = projetName;
    }

    @When("je clique sur le bouton {string} du projet")
    public void je_clique_sur_le_bouton_du_projet(String buttonType) {
        closeAlertIfPresent();
        if (buttonType.equals("Modifier")) {
            gestionConfigPage.clickEditProjet(currentProjetName);
        } else if (buttonType.equals("Supprimer")) {
            gestionConfigPage.clickDeleteProjet(currentProjetName);
        }
    }

    // ✅ UNE SEULE MÉTHODE POUR MODIFIER LE NOM DU PROJET
    @When("je modifie le nom du projet en {string}")
    public void je_modifie_le_nom_du_projet_en(String newName) {
        gestionConfigPage.enterProjetName(newName);
        this.currentProjetName = newName;
    }

    @When("je modifie la description du projet en {string}")
    public void je_modifie_la_description_du_projet_en(String newDesc) {
        gestionConfigPage.enterProjetDescription(newDesc);
    }

    @When("je confirme la suppression")
    public void je_confirme_la_suppression() {
        gestionConfigPage.confirmDelete();
    }

    // ==================== SITES ====================

    @When("je clique sur l'onglet {string}")
    public void je_clique_sur_l_onglet(String tabName) {
        closeAlertIfPresent();
        if (tabName.equals("Sites")) {
            gestionConfigPage.goToSitesTab();
            // ✅ Attendre que le contenu des sites soit chargé
            try {
                Thread.sleep(2000);
                // Vérifier que la table des sites est visible
                By sitesTable = By.cssSelector(".leoni-table");
                wait.until(ExpectedConditions.visibilityOfElementLocated(sitesTable));
                System.out.println("✅ Onglet Sites chargé");
            } catch (Exception e) {
                System.out.println("⚠️ Table des sites non trouvée");
            }
        } else if (tabName.equals("Projets")) {
            gestionConfigPage.goToProjetsTab();
        }
    }

    @When("je saisis le nom du site {string}")
    public void je_saisis_le_nom_du_site(String name) {
        if (name != null && !name.isEmpty()) {
            gestionConfigPage.enterSiteName(name);
            this.currentSiteName = name;
        }
    }

    @When("je laisse le nom du site vide")
    public void je_laisse_le_nom_du_site_vide() {
        gestionConfigPage.enterSiteName("");
    }

    @Given("un site {string} existe")
    public void un_site_existe(String siteName) {
        closeAlertIfPresent();

        gestionConfigPage.goToSitesTab();
        if (!gestionConfigPage.siteExists(siteName)) {
            gestionConfigPage.clickNouveauSite();
            gestionConfigPage.enterSiteName(siteName);
            gestionConfigPage.enterSiteDescription("Créé automatiquement pour test");
            gestionConfigPage.submitSite();
            closeAlertIfPresent();

            try { Thread.sleep(2000); } catch(Exception e) {}
        }
        this.currentSiteName = siteName;
    }

    @When("je clique sur le bouton {string} du site")
    public void je_clique_sur_le_bouton_du_site(String buttonType) {
        closeAlertIfPresent();

        System.out.println("🖱️ Action sur le site '" + currentSiteName + "' - Bouton: " + buttonType);

        if (buttonType.equals("Modifier")) {
            gestionConfigPage.clickEditSite(currentSiteName);
        } else if (buttonType.equals("Supprimer")) {
            gestionConfigPage.clickDeleteSite(currentSiteName);
        } else if (buttonType.equals("Associer")) {
            gestionConfigPage.clickAssociationButton(currentSiteName);
        }
    }

    // ✅ UNE SEULE MÉTHODE POUR MODIFIER LE NOM DU SITE
    @When("je modifie le nom du site en {string}")
    public void je_modifie_le_nom_du_site_en(String newName) {
        gestionConfigPage.enterSiteName(newName);
        this.currentSiteName = newName;
    }


    @When("je modifie la description du site en {string}")
    public void je_modifie_la_description_du_site_en(String newDesc) {
        gestionConfigPage.enterSiteDescription(newDesc);
    }

    // ==================== VALIDATIONS ====================

    @Then("le bouton de validation est désactivé")
    public void le_bouton_de_validation_est_desactive() {
        // ✅ Utiliser plusieurs sélecteurs possibles
        String[] buttonSelectors = {
                "//button[contains(text(), 'Créer')]",
                "//button[contains(text(), 'Modifier')]",
                "//button[@type='submit']",
                "//button[contains(@class, 'btn-leoni-primary')]"
        };

        WebElement button = null;
        for (String selector : buttonSelectors) {
            try {
                button = driver.findElement(By.xpath(selector));
                if (button != null && button.isDisplayed()) {
                    System.out.println("✅ Bouton trouvé avec: " + selector);
                    break;
                }
            } catch (Exception e) {
                // Continuer
            }
        }

        if (button == null) {
            Assertions.fail("Bouton de validation non trouvé");
        }

        // Vérifier l'attribut disabled
        String disabledAttr = button.getAttribute("disabled");
        boolean isDisabled = disabledAttr != null && !disabledAttr.equals("false");

        System.out.println("Bouton - disabled attribute: '" + disabledAttr + "'");
        System.out.println("Bouton - isEnabled(): " + button.isEnabled());

        Assertions.assertTrue(isDisabled || !button.isEnabled(),
                "Le bouton devrait être désactivé");
    }

    @Then("un message d'erreur {string} s'affiche")
    public void un_message_d_erreur_s_affiche(String expectedError) {
        // Chercher le message d'erreur Angular (formControl)
        By errorElement = By.cssSelector(".error-msg, .invalid-feedback, .ng-invalid ~ .error-msg");
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorElement));
            String actualError = error.getText();
            Assertions.assertTrue(actualError.contains(expectedError),
                    "Erreur attendue: " + expectedError + ", reçue: " + actualError);
        } catch (Exception e) {
            // Alternative: vérifier que le champ a la classe ng-invalid
            By nameInput = By.cssSelector("input[formControlName='name']");
            WebElement input = driver.findElement(nameInput);
            String classes = input.getAttribute("class");
            Assertions.assertTrue(classes.contains("ng-invalid"),
                    "Le champ devrait être invalide (ng-invalid)");
        }
    }

    @Then("un message de succès {string} s'affiche")
    public void un_message_de_succes_s_affiche(String expectedMsg) {
        try {
            // Attendre que l'alerte apparaisse (timeout 5 secondes)
            Alert alert = shortWait.until(ExpectedConditions.alertIsPresent());
            String actualMsg = alert.getText();
            System.out.println("📢 Message d'alerte: " + actualMsg);
            alert.accept();  // Fermer l'alerte
            Assertions.assertTrue(actualMsg.contains(expectedMsg),
                    "Message attendu: " + expectedMsg + ", reçu: " + actualMsg);
        } catch (TimeoutException e) {
            // Pas d'alerte, vérifier le message HTML
            String actualMsg = gestionConfigPage.getSuccessMessage();
            System.out.println("📢 Message HTML: " + actualMsg);
            if (actualMsg.isEmpty()) {
                Assertions.fail("Aucun message de succès trouvé (ni alerte, ni élément HTML)");
            }
            Assertions.assertTrue(actualMsg.contains(expectedMsg),
                    "Message attendu: " + expectedMsg + ", reçu: " + actualMsg);
        }
    }

    @Then("le projet {string} apparaît dans la liste")
    public void le_projet_apparait_dans_la_liste(String projetName) {
        gestionConfigPage.goToProjetsTab();
        Assertions.assertTrue(gestionConfigPage.projetExists(projetName),
                "Projet " + projetName + " non trouvé");
    }

    @Then("le projet {string} n'apparaît plus dans la liste")
    public void le_projet_n_apparait_plus_dans_la_liste(String projetName) {
        gestionConfigPage.goToProjetsTab();
        Assertions.assertFalse(gestionConfigPage.projetExists(projetName),
                "Projet " + projetName + " ne devrait pas exister");
    }

    @Then("le projet {string} remplace l'ancien")
    public void le_projet_remplace_l_ancien(String newName) {
        gestionConfigPage.goToProjetsTab();
        Assertions.assertTrue(gestionConfigPage.projetExists(newName),
                "Projet " + newName + " non trouvé");
    }

    @Then("le site {string} apparaît dans la liste")
    public void le_site_apparait_dans_la_liste(String siteName) {
        // ✅ Scroller vers le haut pour voir l'onglet
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        try { Thread.sleep(500); } catch(Exception e) {}

        gestionConfigPage.goToSitesTab();

        // ✅ Scroller à nouveau pour être sûr
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        try { Thread.sleep(500); } catch(Exception e) {}

        Assertions.assertTrue(gestionConfigPage.siteExists(siteName),
                "Site " + siteName + " non trouvé");
    }

    @Then("le site {string} n'apparaît plus dans la liste")
    public void le_site_n_apparait_plus_dans_la_liste(String siteName) {
        gestionConfigPage.goToSitesTab();
        Assertions.assertFalse(gestionConfigPage.siteExists(siteName),
                "Site " + siteName + " ne devrait pas exister");
    }

    @Then("le site {string} remplace l'ancien")
    public void le_site_remplace_l_ancien(String newName) {
        gestionConfigPage.goToSitesTab();
        Assertions.assertTrue(gestionConfigPage.siteExists(newName),
                "Site " + newName + " non trouvé");
    }

    // ==================== ASSOCIATION ====================

    @When("je clique sur le bouton {string} du site {string}")
    public void je_clique_sur_le_bouton_associer(String buttonType, String siteName) {
        this.currentSiteName = siteName;
        gestionConfigPage.clickAssociationButton(siteName);
    }

    @When("je sélectionne le projet {string}")
    public void je_selectionne_le_projet(String projetName) {
        gestionConfigPage.selectProjet(projetName);
    }

    @When("je désélectionne le projet {string}")
    public void je_deselectionne_le_projet(String projetName) {
        gestionConfigPage.deselectProjet(projetName);
    }

    @When("je sauvegarde les associations")
    public void je_sauvegarde_les_associations() {
        gestionConfigPage.saveAssociation();
    }

    @Given("un site {string} a les projets associés {string} et {string}")
    public void un_site_a_les_projets_associes(String siteName, String projet1, String projet2) {
        this.currentSiteName = siteName;
        gestionConfigPage.goToSitesTab();
        gestionConfigPage.clickAssociationButton(siteName);
        gestionConfigPage.selectProjet(projet1);
        gestionConfigPage.selectProjet(projet2);
        gestionConfigPage.saveAssociation();
        try { Thread.sleep(2000); } catch(Exception e) {}
    }

    @Then("les projets {string} et {string} sont associés au site")
    public void les_projets_sont_associes_au_site(String projet1, String projet2) {
        Assertions.assertTrue(gestionConfigPage.isProjetAssociated(currentSiteName, projet1),
                "Projet " + projet1 + " non associé");
        Assertions.assertTrue(gestionConfigPage.isProjetAssociated(currentSiteName, projet2),
                "Projet " + projet2 + " non associé");
    }

    @Then("le projet {string} n'est plus associé au site")
    public void le_projet_n_est_plus_associe_au_site(String projetName) {
        Assertions.assertFalse(gestionConfigPage.isProjetAssociated(currentSiteName, projetName),
                "Projet " + projetName + " ne devrait pas être associé");
    }

    @Then("le projet {string} reste associé au site")
    public void le_projet_reste_associe_au_site(String projetName) {
        Assertions.assertTrue(gestionConfigPage.isProjetAssociated(currentSiteName, projetName),
                "Projet " + projetName + " devrait être associé");
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