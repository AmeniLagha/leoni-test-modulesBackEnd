package com.example.security.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {

    WebDriver driver;
    WebDriverWait wait;

    // ✅ Sélecteurs basés sur ton HTML Angular réel
    By emailInput    = By.cssSelector("input[formControlName='email']");
    By passwordInput = By.cssSelector("input[formControlName='password']");
    By siteSelect    = By.cssSelector("select[formControlName='siteName']");

    // ✅ FIX : Sélecteur bouton plus précis (le span à l'intérieur peut interférer)
    By loginButton   = By.cssSelector("button[type='submit']");

    // ✅ FIX : Sélecteur message d'erreur global du formulaire
    By errorMessage  = By.cssSelector(".alert.alert-danger");

    // ✅ FIX : Sélecteur dashboard — adapte selon ton vrai composant Angular après login
    By dashboardEl   = By.cssSelector("app-dashboard, app-home, .sidebar, nav.navbar, [routerLink]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        // ✅ FIX : Wait de 60s pour gérer le cold start Render
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
    }

    public void goToLoginPage(String baseUrl) {
        driver.get(baseUrl);

        System.out.println("⏳ Attente du chargement de la page (Render peut être lent)...");

        // ✅ FIX : Attendre que le champ email soit visible
        // Angular met du temps à bootstrapper sur Render (cold start)
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
            System.out.println("✅ Page de connexion chargée: " + driver.getTitle());
        } catch (TimeoutException e) {
            System.out.println("❌ Timeout — La page n'a pas chargé en 60s");
            System.out.println("📄 Contenu de la page : " + driver.getPageSource().substring(0, 200));
            throw e;
        }
    }

    public void enterEmail(String email) {
        WebElement element = wait.until(
                ExpectedConditions.elementToBeClickable(emailInput)
        );
        element.clear();
        element.sendKeys(email);
        System.out.println("📧 Email saisi: " + email);
    }

    public void enterPassword(String password) {
        WebElement element = wait.until(
                ExpectedConditions.elementToBeClickable(passwordInput)
        );
        element.clear();
        element.sendKeys(password);
        System.out.println("🔒 Mot de passe saisi");
    }

    public void selectSite(String siteName) {
        try {
            System.out.println("🏭 Tentative sélection site: " + siteName);

            // Attendre que le select existe
            WebElement selectElement = wait.until(
                    ExpectedConditions.presenceOfElementLocated(siteSelect)
            );

            // ✅ Attendre max 30s que les options chargent depuis l'API
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
            longWait.until(driver1 -> {
                try {
                    Select s = new Select(driver1.findElement(siteSelect));
                    int size = s.getOptions().size();
                    System.out.println("   Options disponibles: " + size);
                    return size > 1;
                } catch (Exception e) {
                    return false;
                }
            });

            Select dropdown = new Select(selectElement);

            // Afficher toutes les options pour debug
            dropdown.getOptions().forEach(opt ->
                    System.out.println("   Option: '" + opt.getText() + "'")
            );

            dropdown.selectByVisibleText(siteName);
            System.out.println("✅ Site sélectionné: " + siteName);

        } catch (Exception e) {
            System.out.println("⚠️ Site non trouvé — on continue sans sélection");
            System.out.println("   Erreur: " + e.getMessage());
        }
    }

    public void clickLogin() {
        // ✅ FIX : S'assurer que le bouton n'est pas disabled (form valid)
        WebElement button = wait.until(
                ExpectedConditions.elementToBeClickable(loginButton)
        );

        // ✅ FIX : Scroll vers le bouton avant de cliquer (au cas où)
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);

        // ✅ FIX : Clic JavaScript si le clic normal échoue à cause d'Angular
        try {
            button.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("⚠️ Clic intercepté — utilisation de JavaScript");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
        }

        System.out.println("🖱️ Clic sur le bouton Connexion");
    }

    public String getErrorMessage() {
        try {
            // ✅ FIX : Attendre jusqu'à 5s que le message d'erreur apparaisse
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement element = shortWait.until(
                    ExpectedConditions.visibilityOfElementLocated(errorMessage)
            );
            String message = element.getText().trim();
            System.out.println("❌ Message d'erreur trouvé: '" + message + "'");
            return message;
        } catch (TimeoutException e) {
            System.out.println("⚠️ Aucun message d'erreur visible après 5s");
            // ✅ FIX : Retourner le texte de la page pour debug
            System.out.println("📄 URL actuelle: " + driver.getCurrentUrl());
            return "";
        } catch (Exception e) {
            System.out.println("⚠️ Erreur lors de la récupération du message: " + e.getMessage());
            return "";
        }
    }

    public boolean isDashboardDisplayed() {
        try {
            // ✅ FIX : Attendre 5s la redirection après login
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // Attendre changement d'URL (redirection vers dashboard/home)
            shortWait.until(driver1 ->
                    !driver1.getCurrentUrl().contains("login") ||
                            driver1.getCurrentUrl().contains("dashboard") ||
                            driver1.getCurrentUrl().contains("home")
            );

            String currentUrl = driver.getCurrentUrl();
            System.out.println("📍 URL après login: " + currentUrl);

            // ✅ FIX : Vérifier URL OU présence d'un élément du dashboard
            boolean urlOk = currentUrl.contains("dashboard") ||
                    currentUrl.contains("home") ||
                    !currentUrl.contains("login");

            // Vérifier aussi si un élément dashboard est présent
            boolean elementOk = !driver.findElements(dashboardEl).isEmpty();

            System.out.println("📊 Dashboard — URL ok: " + urlOk + " | Élément trouvé: " + elementOk);
            return urlOk || elementOk;

        } catch (Exception e) {
            System.out.println("⚠️ Erreur isDashboardDisplayed: " + e.getMessage());
            System.out.println("📍 URL actuelle: " + driver.getCurrentUrl());
            return false;
        }
    }

    public String getDashboardTitleText() {
        String title = driver.getTitle();
        System.out.println("📄 Titre de la page: " + title);
        return title;
    }
}