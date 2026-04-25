package com.example.security.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class RegisterPage {
    WebDriver driver;
    WebDriverWait wait;
    WebDriverWait shortWait;

    // Sélecteurs basés sur votre HTML Register
    By firstnameInput = By.cssSelector("input[formControlName='firstname']");
    By lastnameInput = By.cssSelector("input[formControlName='lastname']");
    By emailInput = By.cssSelector("input[formControlName='email']");
    By matriculeInput = By.cssSelector("input[formControlName='matricule']");
    By passwordInput = By.cssSelector("input[formControlName='password']");

    // Navigation des étapes
    By nextButton = By.cssSelector("button.btn-nav.next");
    By prevButton = By.cssSelector("button.btn-nav.prev");
    By submitButton = By.cssSelector("button.btn-submit");

    // Sélecteurs pour les étapes
    By step1 = By.cssSelector(".step[data-step='1']");
    By step2 = By.cssSelector(".step[data-step='2']");
    By step3 = By.cssSelector(".step[data-step='3']");

    // Rôles
    By roleCards = By.cssSelector(".role-card");

    // Projets
    By projectChips = By.cssSelector(".project-chip");

    // Site
    By siteSelect = By.cssSelector("select[formControlName='siteName']");

    // Messages
    By successMessage = By.cssSelector(".alert-message.success");
    By errorMessage = By.cssSelector(".alert-message.error");

    // Dashboard navigation
    By usersMenu = By.xpath("//a[contains(text(), 'Utilisateurs')]");
    By nouveauButton = By.xpath("//a[contains(text(), 'Nouveau')]");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Navigation depuis le dashboard
    public void goToRegisterPage() {
        System.out.println("📍 Navigation vers la page d'inscription...");

        // Attendre que le dashboard soit chargé
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".sidebar, nav")));

        // Cliquer sur Utilisateurs
        WebElement users = wait.until(ExpectedConditions.elementToBeClickable(usersMenu));
        users.click();

        // Cliquer sur Nouveau
        WebElement nouveau = wait.until(ExpectedConditions.elementToBeClickable(nouveauButton));
        nouveau.click();

        // Attendre que le formulaire d'inscription se charge
        wait.until(ExpectedConditions.visibilityOfElementLocated(firstnameInput));
        System.out.println("✅ Page d'inscription chargée");
    }

    // Step 1 - Identité
    public void enterFirstname(String firstname) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(firstnameInput));
        element.clear();
        element.sendKeys(firstname);
        System.out.println("📝 Prénom: " + firstname);
    }

    public void enterLastname(String lastname) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(lastnameInput));
        element.clear();
        element.sendKeys(lastname);
        System.out.println("📝 Nom: " + lastname);
    }

    public void enterEmail(String email) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(emailInput));
        element.clear();
        element.sendKeys(email);
        System.out.println("📧 Email: " + email);
    }

    public void enterMatricule(String matricule) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(matriculeInput));
        element.clear();
        element.sendKeys(matricule);
        System.out.println("🆔 Matricule: " + matricule);
    }

    public void nextStep() {
        WebElement next = wait.until(ExpectedConditions.elementToBeClickable(nextButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", next);
        next.click();
        System.out.println("➡️ Étape suivante");
        // Attendre la transition
        try { Thread.sleep(500); } catch(Exception e) {}
    }

    public void prevStep() {
        WebElement prev = wait.until(ExpectedConditions.elementToBeClickable(prevButton));
        prev.click();
        System.out.println("⬅️ Étape précédente");
    }

    // Step 2 - Sécurité
    public void enterPassword(String password) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(passwordInput));
        element.clear();
        element.sendKeys(password);
        System.out.println("🔒 Mot de passe saisi");
    }

    // Step 3 - Affectation
    public void selectRole(String roleName) {
        System.out.println("🎭 Sélection du rôle: " + roleName);
        List<WebElement> roles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(roleCards));

        for (WebElement role : roles) {
            String roleText = role.getText().toLowerCase();
            if (roleText.contains(roleName.toLowerCase())) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", role);
                System.out.println("✅ Rôle sélectionné: " + roleName);
                return;
            }
        }
        System.out.println("⚠️ Rôle non trouvé: " + roleName);
    }

    public void selectProject(String projectName) {
        System.out.println("📁 Sélection du projet: " + projectName);
        List<WebElement> projects = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(projectChips));

        for (WebElement project : projects) {
            if (project.getText().equals(projectName)) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", project);
                System.out.println("✅ Projet sélectionné: " + projectName);
                return;
            }
        }
        System.out.println("⚠️ Projet non trouvé: " + projectName);
    }

    public void selectProjects(List<String> projectNames) {
        for (String projectName : projectNames) {
            selectProject(projectName);
        }
    }

    public void selectSite(String siteName) {
        System.out.println("🏭 Sélection du site: " + siteName);
        try {
            WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(siteSelect));
            Select dropdown = new Select(selectElement);
            dropdown.selectByVisibleText(siteName);
            System.out.println("✅ Site sélectionné: " + siteName);
        } catch (Exception e) {
            System.out.println("⚠️ Erreur sélection site: " + e.getMessage());
        }
    }

    public void submitForm() {
        WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit);

        try {
            submit.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
        }
        System.out.println("📤 Formulaire soumis");

        // Attendre la réponse de l'API (3 secondes)
        try { Thread.sleep(3000); } catch(Exception e) {}

        // Log l'URL actuelle
        System.out.println("📍 URL après soumission: " + driver.getCurrentUrl());

        // Vérifier s'il y a un message de succès ou d'erreur
        String successMsg = getSuccessMessage();
        String errorMsg = getErrorMessage();

        if (!successMsg.isEmpty()) {
            System.out.println("✅ Message de succès: " + successMsg);
        }
        if (!errorMsg.isEmpty()) {
            System.out.println("❌ Message d'erreur: " + errorMsg);
        }

        // Si aucun message, log le contenu de la page pour debug
        if (successMsg.isEmpty() && errorMsg.isEmpty()) {
            System.out.println("⚠️ AUCUN message de retour trouvé - vérification du DOM...");

            // Chercher tous les éléments de message possibles
            String[] selectors = {
                    ".alert-message", ".alert", ".toast", ".notification",
                    ".error", ".success", ".invalid-feedback", ".form-error"
            };

            for (String selector : selectors) {
                try {
                    var elements = driver.findElements(By.cssSelector(selector));
                    for (var el : elements) {
                        if (el.isDisplayed() && !el.getText().isEmpty()) {
                            System.out.println("   Message trouvé [" + selector + "]: " + el.getText());
                        }
                    }
                } catch (Exception ex) {}
            }
        }
    }



    public String getSuccessMessage() {
        try {
            // Chercher d'abord sur la page courante
            try {
                WebElement element = shortWait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
                String message = element.getText();
                System.out.println("✅ Message succès: " + message);
                return message;
            } catch (Exception e) {
                // Pas de message sur cette page
            }

            // Vérifier si on est sur /listeuser (succès implicite)
            if (driver.getCurrentUrl().contains("/listeuser")) {
                System.out.println("✅ Redirection vers listeuser - succès implicite");
                return "Utilisateur créé avec succès";
            }

            return "";
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isRegisterSuccess() {
        try {
            // Vérifier le message de succès
            String successMsg = getSuccessMessage();
            if (!successMsg.isEmpty()) {
                System.out.println("✅ Succès détecté: " + successMsg);
                return true;
            }

            // Vérifier si on a été redirigé vers la liste des utilisateurs
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl.contains("/listeuser")) {
                System.out.println("✅ Redirection vers /listeuser - CRÉATION RÉUSSIE!");
                return true;
            }

            // Vérifier s'il y a une erreur
            String errorMsg = getErrorMessage();
            if (!errorMsg.isEmpty()) {
                System.out.println("❌ Erreur détectée: " + errorMsg);
                return false;
            }

            System.out.println("⚠️ Ni succès ni erreur détectés - URL: " + currentUrl);
            return false;
        } catch (Exception e) {
            System.out.println("⚠️ Exception dans isRegisterSuccess: " + e.getMessage());
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            WebElement element = shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            String message = element.getText();
            System.out.println("❌ Message erreur: " + message);
            return message;
        } catch (Exception e) {
            return "";
        }
    }


    public boolean isOnRegisterPage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(firstnameInput)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}