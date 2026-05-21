package com.example.security.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class GestionConfigPage {

    WebDriver driver;
    WebDriverWait wait;
    WebDriverWait shortWait;

    // Onglets
    By projetsTab = By.xpath("//button[contains(., 'Projets')]");
    By sitesTab = By.xpath("//button[contains(., 'Sites')]");

    // Boutons
    By nouveauProjetBtn = By.xpath("//button[contains(text(), 'Nouveau projet')]");
    By nouveauSiteBtn = By.xpath("//button[contains(text(), 'Nouveau site')]");

    // Modals
    By modalProjet = By.cssSelector(".leoni-modal");
    By modalSite = By.cssSelector(".leoni-modal");
    By modalAssociation = By.cssSelector(".leoni-modal.leoni-modal-lg");

    // Formulaires
    By projetNameInput = By.cssSelector("input[formControlName='name']");
    By projetDescriptionInput = By.cssSelector("textarea[formControlName='description']");
    By siteNameInput = By.cssSelector("input[formControlName='name']");
    By siteDescriptionInput = By.cssSelector("textarea[formControlName='description']");

    // Boutons de validation
    By submitProjetBtn = By.xpath("//button[contains(text(), 'Créer') or contains(text(), 'Modifier')]");
    By submitSiteBtn = By.xpath("//button[contains(text(), 'Créer') or contains(text(), 'Modifier')]");
    By saveAssociationBtn = By.xpath("//button[contains(text(), 'Enregistrer les associations')]");

    // Messages
    By successMessage = By.cssSelector(".alert-success, .toast-success");
    By errorMessage = By.cssSelector(".alert-danger");

    // Checkboxes
    String checkboxSelector = "//label[contains(., '%s')]//input[@type='checkbox']";

    public GestionConfigPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Navigation
    public void goToProjetsTab() {
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(projetsTab));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", tab);
        tab.click();
        try { Thread.sleep(500); } catch(Exception e) {}
    }

    public void goToSitesTab() {
        System.out.println("📍 Navigation vers l'onglet Sites...");
        try {
            // ✅ Scroller tout en haut de la page
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500);

            // Attendre que l'onglet soit cliquable
            WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(sitesTab));

            // ✅ Scroller vers l'onglet
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", tab);
            Thread.sleep(500);

            // ✅ Utiliser JavaScript pour cliquer (évite les problèmes d'interception)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", tab);

            System.out.println("✅ Onglet Sites sélectionné");
            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println("❌ Erreur goToSitesTab: " + e.getMessage());
            throw new RuntimeException("Impossible de sélectionner l'onglet Sites", e);
        }
    }

    // Projets
    public void clickNouveauProjet() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(nouveauProjetBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalProjet));
    }

    public void enterProjetName(String name) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(projetNameInput));
        input.clear();
        input.sendKeys(name);
    }

    public void enterProjetDescription(String description) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(projetDescriptionInput));
        input.clear();
        input.sendKeys(description);
    }

    public void submitProjet() {
        System.out.println("📤 Soumission du formulaire...");
        By submitSelector = By.xpath("//button[contains(text(), 'Créer') or contains(text(), 'Modifier')]");

        try {
            // Attendre que le bouton soit présent dans le DOM
            wait.until(ExpectedConditions.presenceOfElementLocated(submitSelector));

            // Attendre que le bouton soit cliquable (cela le re-localise automatiquement)
            WebElement submit = wait.until(ExpectedConditions.elementToBeClickable(submitSelector));

            if (!submit.isEnabled()) {
                System.out.println("⚠️ Bouton désactivé - soumission impossible");
                return;
            }

            // Scroller jusqu'au bouton
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", submit);
            Thread.sleep(500);

            // Clic JavaScript (plus fiable que le clic normal)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);

            System.out.println("✅ Formulaire soumis avec succès");
            Thread.sleep(3000);

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la soumission: " + e.getMessage());
            throw new RuntimeException("Impossible de soumettre le formulaire", e);
        }
    }

    public boolean isSubmitProjetEnabled() {
        WebElement btn = driver.findElement(submitProjetBtn);
        return btn.isEnabled();
    }

    public boolean projetExists(String projetName) {
        try {
            By projetRow = By.xpath("//table//tr[contains(., '" + projetName + "')]");
            return wait.until(ExpectedConditions.presenceOfElementLocated(projetRow)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickEditProjet(String projetName) {
        By editBtn = By.xpath("//tr[contains(., '" + projetName + "')]//button[@class='action-btn edit']");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(editBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalProjet));
    }

    public void clickDeleteProjet(String projetName) {
        By deleteBtn = By.xpath("//tr[contains(., '" + projetName + "')]//button[@class='action-btn delete']");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        btn.click();
    }

    public void confirmDelete() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (Exception e) {
            // Fallback: cliquer sur bouton de confirmation
            try {
                By confirmBtn = By.xpath("//button[contains(text(), 'Confirmer') or contains(text(), 'Oui')]");
                wait.until(ExpectedConditions.elementToBeClickable(confirmBtn)).click();
            } catch (Exception ex) {}
        }
        try { Thread.sleep(2000); } catch(Exception e) {}
    }

    // Sites
    public void clickNouveauSite() {
        System.out.println("🖱️ Clic sur Nouveau site...");
        try {
            // ✅ Attendre que le bouton soit présent et cliquable
            By nouveauSiteBtn = By.xpath("//button[contains(text(), 'Nouveau site')]");
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(nouveauSiteBtn));

            // ✅ Scroller jusqu'au bouton
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            Thread.sleep(500);

            // ✅ Clic normal d'abord
            try {
                btn.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            }
            System.out.println("✅ Clic sur Nouveau site effectué");

            // ✅ Attendre que le modal devienne visible (pas juste présent)
            Thread.sleep(2000);

            // ✅ Chercher le modal avec display:flex
            By modalSelector = By.cssSelector(".leoni-modal[style*='display: flex'], .leoni-modal.show, .modal.show");

            try {
                WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalSelector));
                System.out.println("✅ Modal visible");
            } catch (Exception e) {
                // Alternative: vérifier si l'input existe directement
                By nameInput = By.cssSelector("input[formControlName='name']");
                try {
                    WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
                    System.out.println("✅ Input trouvé directement (modal déjà ouvert)");
                } catch (Exception ex) {
                    System.out.println("⚠️ Modal non trouvé, mais on continue...");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur clickNouveauSite: " + e.getMessage());
            throw new RuntimeException("Impossible d'ouvrir le modal Nouveau site", e);
        }
    }

    public void enterSiteName(String name) {
        System.out.println("📝 Saisie nom du site: " + name);
        try {
            Thread.sleep(2000);

            WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("siteName")));
            wait.until(ExpectedConditions.visibilityOf(input));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", input);
            Thread.sleep(500);
            input.clear();
            input.sendKeys(name);

            System.out.println("✅ Nom du site saisi: " + input.getAttribute("value"));
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            throw new RuntimeException("Impossible de saisir le nom du site", e);
        }
    }

    public void enterSiteDescription(String description) {
        System.out.println("📝 Saisie description: " + description);
        try {
            // Attendre que l'élément soit présent dans le DOM
            Thread.sleep(2000);

            // Utiliser WebDriverWait pour attendre l'élément
            WebElement textarea = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("siteDescription")));

            // Attendre qu'il soit visible
            wait.until(ExpectedConditions.visibilityOf(textarea));

            // Scroller jusqu'à l'élément
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", textarea);
            Thread.sleep(500);

            // Effacer et saisir
            textarea.clear();
            textarea.sendKeys(description);

            // Vérifier que la valeur a bien été saisie
            String actualValue = textarea.getAttribute("value");
            System.out.println("✅ Description saisie: '" + actualValue + "'");

        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void submitSite() {
        System.out.println("📤 Soumission formulaire site...");
        try {
            Thread.sleep(2000);

            // JavaScript pour trouver le bouton dans le modal visible
            String script = "var modal = document.querySelector('.leoni-modal[style*=\"flex\"]');" +
                    "if(modal) {" +
                    "  var btn = modal.querySelector('.modal-footer-custom .btn-leoni-primary');" +
                    "  if(btn && !btn.disabled) {" +
                    "    btn.click();" +
                    "    return 'clicked';" +
                    "  } else if(btn && btn.disabled) {" +
                    "    return 'disabled';" +
                    "  }" +
                    "}" +
                    "return 'not found';";

            Object result = ((JavascriptExecutor) driver).executeScript(script);
            System.out.println("Résultat soumission: " + result);

            if ("disabled".equals(result)) {
                System.out.println("⚠️ Bouton désactivé - formulaire invalide");
            } else if ("clicked".equals(result)) {
                System.out.println("✅ Bouton cliqué avec succès");
            } else {
                System.out.println("⚠️ Bouton non trouvé");
            }

            Thread.sleep(3000);

        } catch (Exception e) {
            System.out.println("❌ Erreur soumission: " + e.getMessage());
        }
    }


    public boolean isSubmitSiteEnabled() {
        WebElement btn = driver.findElement(submitSiteBtn);
        return btn.isEnabled();
    }

    public boolean siteExists(String siteName) {
        try {
            By siteRow = By.xpath("//table//tr[contains(., '" + siteName + "')]");
            return wait.until(ExpectedConditions.presenceOfElementLocated(siteRow)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickEditSite(String siteName) {
        System.out.println("🖱️ Clic sur Modifier pour le site: " + siteName);
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
            Thread.sleep(500);

            By editBtn = By.xpath("//tr[contains(., '" + siteName + "')]//button[@class='action-btn edit']");
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(editBtn));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
            System.out.println("✅ Clic sur Modifier effectué");

            Thread.sleep(2000);

            // ✅ Chercher le modal de modification (peut contenir "Modifier site")
            By modalSelector = By.xpath("//div[contains(@class, 'leoni-modal') and (contains(., 'Modifier site') or contains(., 'Nouveau site')) and contains(@style, 'flex')]");
            WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(modalSelector));
            System.out.println("✅ Modal de modification visible");

        } catch (Exception e) {
            System.out.println("❌ Erreur clickEditSite: " + e.getMessage());
            throw new RuntimeException("Impossible de modifier le site", e);
        }
    }

    public void clickDeleteSite(String siteName) {
        By deleteBtn = By.xpath("//tr[contains(., '" + siteName + "')]//button[@class='action-btn delete']");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        btn.click();
    }

    // Association
    public void clickAssociationButton(String siteName) {
        By associationBtn = By.xpath("//tr[contains(., '" + siteName + "')]//button[@class='action-btn link']");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(associationBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
        btn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(modalAssociation));
    }

    public void selectProjet(String projetName) {
        By checkbox = By.xpath(String.format(checkboxSelector, projetName));
        WebElement cb = wait.until(ExpectedConditions.elementToBeClickable(checkbox));
        if (!cb.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
        }
    }

    public void deselectProjet(String projetName) {
        By checkbox = By.xpath(String.format(checkboxSelector, projetName));
        WebElement cb = wait.until(ExpectedConditions.elementToBeClickable(checkbox));
        if (cb.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cb);
        }
    }

    public void saveAssociation() {
        System.out.println("💾 Sauvegarde des associations...");
        try {
            Thread.sleep(2000);

            // Sélecteur plus robuste
            By[] selectors = {
                    By.xpath("//button[contains(text(), 'Enregistrer les associations')]"),
                    By.xpath("//div[@class='modal-footer-custom']//button[contains(@class, 'btn-leoni-primary')]"),
                    By.xpath("//button[contains(@class, 'btn-leoni-primary') and contains(text(), 'Enregistrer')]")
            };

            WebElement btn = null;
            for (By selector : selectors) {
                try {
                    btn = wait.until(ExpectedConditions.elementToBeClickable(selector));
                    if (btn != null && btn.isDisplayed()) {
                        System.out.println("✅ Bouton trouvé avec: " + selector);
                        break;
                    }
                } catch (Exception e) {
                    // Continuer
                }
            }

            if (btn == null) {
                System.out.println("⚠️ Bouton non trouvé");
                return;
            }

            if (!btn.isEnabled()) {
                System.out.println("⚠️ Bouton désactivé");
                return;
            }

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            Thread.sleep(500);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

            System.out.println("✅ Associations sauvegardées");
            Thread.sleep(3000);

        } catch (Exception e) {
            System.out.println("❌ Erreur saveAssociation: " + e.getMessage());
        }
    }

    public boolean isProjetAssociated(String siteName, String projetName) {
        try {
            By siteRow = By.xpath("//tr[contains(., '" + siteName + "')]");
            WebElement row = driver.findElement(siteRow);
            String rowText = row.getText();
            return rowText.contains(projetName);
        } catch (Exception e) {
            return false;
        }
    }

    public String getSuccessMessage() {
        try {
            WebElement element = shortWait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
            return element.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getErrorMessage() {
        try {
            WebElement element = shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return element.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public void closeModal() {
        try {
            By closeBtn = By.cssSelector(".modal-close");
            wait.until(ExpectedConditions.elementToBeClickable(closeBtn)).click();
        } catch (Exception e) {}
    }
}