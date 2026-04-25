package com.example.security.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class ChargeSheetPage {
    WebDriver driver;
    WebDriverWait wait;
    WebDriverWait shortWait;

    // Navigation
    By cahiersMenuLink = By.xpath("//aside//a[contains(@routerLink, 'charge-sheets')]//span[contains(text(), 'Cahiers des charges')]");
    By nouveauButton = By.xpath("//button[contains(text(), 'Nouveau cahier')]");

    // Formulaire - Informations générales
    By projectSelect = By.cssSelector("select[formControlName='project']");
    By harnessRefInput = By.cssSelector("input[formControlName='harnessRef']");
    By phoneInput = By.cssSelector("input[formControlName='phoneNumber']");
    By orderNumberInput = By.cssSelector("input[formControlName='orderNumber']");
    By costCenterInput = By.cssSelector("input[formControlName='costCenterNumber']");
    By dateInput = By.cssSelector("input[formControlName='date']");
    By deliveryDateInput = By.cssSelector("input[formControlName='preferredDeliveryDate']");

    // Items - première ligne (item existant)
    By firstRowSamplesSelect = By.cssSelector("table tbody tr:first-child select[formControlName='samplesExist']");
    By firstRowWaysInput = By.cssSelector("table tbody tr:first-child input[formControlName='ways']");
    By firstRowHousingColourInput = By.cssSelector("table tbody tr:first-child input[formControlName='housingColour']");
    By firstRowTestModuleSelect = By.cssSelector("table tbody tr:first-child select[formControlName='testModuleExistInDatabase']");
    By firstRowHousingRefLeoniInput = By.cssSelector("table tbody tr:first-child input[formControlName='housingReferenceLeoni']");
    By firstRowHousingRefSupplierInput = By.cssSelector("table tbody tr:first-child input[formControlName='housingReferenceSupplierCustomer']");
    By firstRowReferenceSealsInput = By.cssSelector("table tbody tr:first-child input[formControlName='referenceSealsClipsCableTiesCap']");
    By firstRowQuantityInput = By.cssSelector("table tbody tr:first-child input[formControlName='quantityOfTestModules']");

    // Submit
    By submitButton = By.cssSelector("button[type='submit']");
    By successMessage = By.cssSelector(".alert-success, .toast-success");
    By errorMessage = By.cssSelector(".alert-danger");

    public ChargeSheetPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Navigation via le menu latéral
    public void goToChargeSheetList() {
        System.out.println("📍 Navigation vers la liste des cahiers des charges via le menu...");
        try {
            WebElement cahiersLink = wait.until(ExpectedConditions.elementToBeClickable(cahiersMenuLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", cahiersLink);
            sleep(500);
            cahiersLink.click();
            System.out.println("✅ Lien 'Cahiers des charges' cliqué");
            sleep(3000);
        } catch (Exception e) {
            System.out.println("⚠️ Erreur navigation: " + e.getMessage());
            throw new RuntimeException("Impossible de naviguer vers Cahiers des charges");
        }
    }

    // Cliquer sur Nouveau cahier
    public void clickNewChargeSheet() {
        System.out.println("📍 Clic sur Nouveau cahier...");
        try {
            WebElement nouveau = wait.until(ExpectedConditions.elementToBeClickable(nouveauButton));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nouveau);
            sleep(500);
            nouveau.click();
            System.out.println("✅ Bouton Nouveau cahier cliqué");
            sleep(3000);
        } catch (Exception e) {
            System.out.println("⚠️ Erreur: " + e.getMessage());
            throw new RuntimeException("Bouton Nouveau cahier non trouvé");
        }
    }

    // Remplir les champs généraux
    public void selectProject(String projectName) {
        System.out.println("📁 Sélection du projet: " + projectName);
        try {
            WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(projectSelect));
            Select dropdown = new Select(selectElement);
            dropdown.selectByVisibleText(projectName);
            System.out.println("✅ Projet sélectionné: " + projectName);
        } catch (Exception e) {
            System.out.println("⚠️ Erreur sélection projet: " + e.getMessage());
        }
    }

    public void enterHarnessRef(String harnessRef) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(harnessRefInput));
        element.clear();
        element.sendKeys(harnessRef);
        System.out.println("🔧 Harness Ref: " + harnessRef);
    }

    public void enterPhoneNumber(String phone) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(phoneInput));
        element.clear();
        element.sendKeys(phone);
        System.out.println("📞 Téléphone: " + phone);
    }

    public void enterOrderNumber(String orderNumber) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(orderNumberInput));
        element.clear();
        element.sendKeys(orderNumber);
        System.out.println("📦 Order Number: " + orderNumber);
    }

    public void enterCostCenter(String costCenter) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(costCenterInput));
        element.clear();
        element.sendKeys(costCenter);
        System.out.println("💰 Cost Center: " + costCenter);
    }

    public void enterDate(String date) {
        System.out.println("📅 Date à saisir: " + date);
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(dateInput));

            // Effacer la valeur existante
            element.clear();

            // Utiliser JavaScript pour forcer la valeur (plus fiable)
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                    element, date
            );

            // Vérifier la valeur après saisie
            String actualValue = (String) ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].value;", element);
            System.out.println("📅 Date après saisie: " + actualValue);

        } catch (Exception e) {
            System.out.println("⚠️ Erreur saisie date: " + e.getMessage());
            throw new RuntimeException("Impossible de saisir la date: " + date);
        }
    }

    public void enterDeliveryDate(String date) {
        System.out.println("📅 Date livraison à saisir: " + date);
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(deliveryDateInput));

            // Effacer la valeur existante
            element.clear();

            // Utiliser JavaScript pour forcer la valeur
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                    element, date
            );

            // Vérifier la valeur après saisie
            String actualValue = (String) ((JavascriptExecutor) driver).executeScript(
                    "return arguments[0].value;", element);
            System.out.println("📅 Date livraison après saisie: " + actualValue);

        } catch (Exception e) {
            System.out.println("⚠️ Erreur saisie date livraison: " + e.getMessage());
            throw new RuntimeException("Impossible de saisir la date de livraison: " + date);
        }
    }

    // Remplir l'item existant (première ligne)
    public void fillExistingItem(String samplesExist, String ways, String housingColour,
                                 String testModuleExist, String housingRefLeoni,
                                 String housingRefSupplier, String referenceSeals,
                                 String quantity) {
        System.out.println("📝 Remplissage de l'item existant...");
        try {
            sleep(1000);

            // Samples Exist
            selectDropdownValue(firstRowSamplesSelect, samplesExist);

            // Ways
            fillInput(firstRowWaysInput, ways);

            // Housing Colour
            fillInput(firstRowHousingColourInput, housingColour);

            // Test Module Exist
            selectDropdownValue(firstRowTestModuleSelect, testModuleExist);

            // Housing Reference Leoni
            fillInput(firstRowHousingRefLeoniInput, housingRefLeoni);

            // Housing Reference Supplier
            fillInput(firstRowHousingRefSupplierInput, housingRefSupplier);

            // Reference Seals
            fillInput(firstRowReferenceSealsInput, referenceSeals);

            // Quantity
            fillInput(firstRowQuantityInput, quantity);

            System.out.println("✅ Item existant rempli");

        } catch (Exception e) {
            System.out.println("⚠️ Erreur remplissage item: " + e.getMessage());
            throw new RuntimeException("Impossible de remplir l'item");
        }
    }
    public void addNewItemAndFill(String samplesExist, String ways, String housingColour,
                                  String testModuleExist, String housingRefLeoni,
                                  String housingRefSupplier, String referenceSeals,
                                  String quantity) {
        System.out.println("➕ Ajout d'un nouvel item...");
        try {
            // Cliquer sur le bouton "Ajouter un item"
            By addItemButton = By.cssSelector("button.btn-primary.btn-sm");
            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(addItemButton));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addBtn);
            sleep(500);
            addBtn.click();
            System.out.println("   ✅ Bouton 'Ajouter un item' cliqué");
            sleep(1000);

            // Récupérer la dernière ligne ajoutée
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
            int lastRowIndex = rows.size() - 1;

            // Remplir le nouvel item
            fillItemRow(lastRowIndex, samplesExist, ways, housingColour, testModuleExist,
                    housingRefLeoni, housingRefSupplier, referenceSeals, quantity);

            System.out.println("✅ Nouvel item ajouté et rempli");

        } catch (Exception e) {
            System.out.println("⚠️ Erreur ajout item: " + e.getMessage());
            throw new RuntimeException("Impossible d'ajouter un nouvel item");
        }
    }

    private void fillItemRow(int rowIndex, String samplesExist, String ways, String housingColour,
                             String testModuleExist, String housingRefLeoni, String housingRefSupplier,
                             String referenceSeals, String quantity) {
        try {
            // Samples Exist
            By samplesSelect = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") select[formControlName='samplesExist']");
            selectDropdownValue(samplesSelect, samplesExist);

            // Ways
            By waysInput = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") input[formControlName='ways']");
            fillInput(waysInput, ways);

            // Housing Colour
            By housingColourInput = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") input[formControlName='housingColour']");
            fillInput(housingColourInput, housingColour);

            // Test Module Exist
            By testModuleSelect = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") select[formControlName='testModuleExistInDatabase']");
            selectDropdownValue(testModuleSelect, testModuleExist);

            // Housing Reference Leoni
            By housingRefLeoniInput = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") input[formControlName='housingReferenceLeoni']");
            fillInput(housingRefLeoniInput, housingRefLeoni);

            // Housing Reference Supplier
            By housingRefSupplierInput = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") input[formControlName='housingReferenceSupplierCustomer']");
            fillInput(housingRefSupplierInput, housingRefSupplier);

            // Reference Seals
            By referenceSealsInput = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") input[formControlName='referenceSealsClipsCableTiesCap']");
            fillInput(referenceSealsInput, referenceSeals);

            // Quantity
            By quantityInput = By.cssSelector("table tbody tr:nth-child(" + (rowIndex + 1) + ") input[formControlName='quantityOfTestModules']");
            fillInput(quantityInput, quantity);

            System.out.println("   ✅ Ligne " + (rowIndex + 1) + " remplie");

        } catch (Exception e) {
            System.out.println("   ⚠️ Erreur remplissage ligne " + (rowIndex + 1) + ": " + e.getMessage());
        }
    }
    private void selectDropdownValue(By selector, String value) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(selector));
            Select dropdown = new Select(element);
            if (value.equalsIgnoreCase("Yes") || value.equalsIgnoreCase("Oui")) {
                dropdown.selectByValue("Yes");
            } else {
                dropdown.selectByValue("No");
            }
        } catch (Exception e) {
            System.out.println("   ⚠️ Erreur sélection dropdown: " + e.getMessage());
        }
    }

    private void fillInput(By selector, String value) {
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(selector));
            element.clear();
            element.sendKeys(value);
        } catch (Exception e) {
            System.out.println("   ⚠️ Erreur remplissage champ: " + e.getMessage());
        }
    }

    // Soumettre le formulaire
    public void submitForm() {
        System.out.println("📤 Soumission du formulaire...");
        try {
            // Attendre que le bouton soit activé (plus désactivé)
            Thread.sleep(2000);

            // Chercher le bouton submit de différentes manières
            By[] submitSelectors = {
                    By.cssSelector("button[type='submit']"),
                    By.xpath("//button[contains(text(), 'Créer')]"),
                    By.xpath("//button[contains(text(), 'Créer le cahier')]"),
                    By.xpath("//button[@class='btn btn-primary px-4']")
            };

            WebElement submit = null;
            for (By selector : submitSelectors) {
                try {
                    submit = wait.until(ExpectedConditions.elementToBeClickable(selector));
                    if (submit != null && submit.isEnabled()) {
                        System.out.println("   ✅ Bouton trouvé et activé: " + selector);
                        break;
                    }
                } catch (Exception e) {}
            }

            if (submit == null) {
                // Vérifier pourquoi le bouton est désactivé
                WebElement disabledBtn = driver.findElement(By.cssSelector("button[type='submit']"));
                System.out.println("⚠️ Bouton désactivé - classes: " + disabledBtn.getAttribute("class"));
                throw new RuntimeException("Bouton submit désactivé");
            }

            // Scroller et cliquer
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submit);
            Thread.sleep(500);

            // Essayer le clic normal d'abord
            try {
                submit.click();
            } catch (Exception e) {
                // Fallback: clic JavaScript
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
            }

            System.out.println("✅ Formulaire soumis");

            // Attendre la redirection ou le message de succès
            Thread.sleep(5000);

            // Vérifier l'URL après soumission
            String currentUrl = driver.getCurrentUrl();
            System.out.println("📍 URL après soumission: " + currentUrl);

        } catch (Exception e) {
            System.out.println("⚠️ Erreur soumission: " + e.getMessage());
            throw new RuntimeException("Impossible de soumettre le formulaire: " + e.getMessage());
        }
    }

    public boolean isCreationSuccess() {
        try {
            Thread.sleep(3000);
            String currentUrl = driver.getCurrentUrl();
            System.out.println("🔍 Vérification création - URL: " + currentUrl);

            // Vérifier la redirection vers la liste
            if (currentUrl.contains("/charge-sheets/list")) {
                System.out.println("✅ Redirection vers la liste - création réussie");
                return true;
            }

            // Vérifier s'il y a un message de succès
            String successMsg = getSuccessMessage();
            if (!successMsg.isEmpty()) {
                System.out.println("✅ Message de succès: " + successMsg);
                return true;
            }

            // Vérifier s'il y a une erreur
            String errorMsg = getErrorMessage();
            if (!errorMsg.isEmpty()) {
                System.out.println("❌ Message d'erreur: " + errorMsg);
                return false;
            }

            // Si on est toujours sur la page de création
            if (currentUrl.contains("/charge-sheets/create")) {
                System.out.println("⚠️ Toujours sur la page de création - formulaire non soumis");
                return false;
            }

            return false;
        } catch (Exception e) {
            System.out.println("⚠️ Erreur vérification: " + e.getMessage());
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

}