package com.example.security.cahierdeCharge;

import com.example.security.email.GlobalNotificationService;
import com.example.security.reception.*;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChargeSheetService {

    private final ChargeSheetRepository repository;
    private final ChargeSheetItemRepository itemRepository;
    private final GlobalNotificationService notificationService;
    private final UserRepository userRepository;
    // Ajoutez cette dépendance
    private final ReceptionHistoryRepository receptionHistoryRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ChargeSheet createChargeSheet(ChargeSheetDto.CreateDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        String userSite = currentUser.getSiteName();
        // ✅ Vérifier que l'utilisateur ne crée pas un cahier pour un autre site
        if (dto.getPlant() != null && !dto.getPlant().equals(userSite)) {
            throw new RuntimeException("Vous ne pouvez créer un cahier que pour votre site: " + userSite);
        }

        ChargeSheet chargeSheet = ChargeSheet.builder()
                .plant(userSite)
                .project(dto.getProject())
                .harnessRef(dto.getHarnessRef())
                .issuedBy(dto.getIssuedBy())
                .emailAddress(dto.getEmailAddress())
                .phoneNumber(dto.getPhoneNumber())
                .orderNumber(dto.getOrderNumber())
                .costCenterNumber(dto.getCostCenterNumber())
                .date(dto.getDate())
                .preferredDeliveryDate(dto.getPreferredDeliveryDate())
                .status(ChargeSheetStatus.DRAFT)
                .createdBy(currentUser.getEmail())
                .createdAt(LocalDate.now())
                .build();

        ChargeSheet savedSheet = repository.save(chargeSheet);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ChargeSheetDto.ItemDto itemDto : dto.getItems()) {
                ChargeSheetItem item = mapToItem(itemDto, savedSheet, currentUser);
                savedSheet.addItem(item);
            }
        } else {
            ChargeSheetItem defaultItem = createDefaultItem(savedSheet, currentUser);
            savedSheet.addItem(defaultItem);
        }

        ChargeSheet finalSheet = repository.save(savedSheet);
        String subject = "Nouveau Cahier des Charges";
        String htmlMessage = buildChargeSheetCreatedHtml(finalSheet);
        notificationService.sendHtmlNotificationToProjectAndSiteUsers(subject, htmlMessage, finalSheet.getProject(), finalSheet.getPlant());

        return finalSheet;
    }
    private String buildChargeSheetCreatedHtml(ChargeSheet chargeSheet) {
        int numberOfItems = chargeSheet.getItems().size();

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>LEONI - Nouveau Cahier des Charges</title>\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
                "        body {\n" +
                "            font-family: 'Segoe UI', Arial, Helvetica, sans-serif;\n" +
                "            background-color: #F5F7FA;\n" +
                "            margin: 0;\n" +
                "            padding: 20px;\n" +
                "            line-height: 1.5;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 650px;\n" +
                "            margin: 0 auto;\n" +
                "            background: #FFFFFF;\n" +
                "            border-radius: 12px;\n" +
                "            overflow: hidden;\n" +
                "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "        .header {\n" +
                "            background: linear-gradient(135deg, #003366 0%, #0052A5 100%);\n" +
                "            padding: 25px 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .logo {\n" +
                "            font-size: 28px;\n" +
                "            font-weight: bold;\n" +
                "            color: #FFFFFF;\n" +
                "            margin-bottom: 10px;\n" +
                "        }\n" +
                "        .logo span {\n" +
                "            color: #00D4FF;\n" +
                "        }\n" +
                "        .header h1 {\n" +
                "            color: #FFFFFF;\n" +
                "            margin: 0;\n" +
                "            font-size: 22px;\n" +
                "            font-weight: normal;\n" +
                "        }\n" +
                "        .content {\n" +
                "            padding: 30px;\n" +
                "        }\n" +
                "        .greeting {\n" +
                "            font-size: 16px;\n" +
                "            color: #333333;\n" +
                "            margin-bottom: 25px;\n" +
                "            padding-bottom: 15px;\n" +
                "            border-bottom: 2px solid #E0E0E0;\n" +
                "        }\n" +
                "        .info-section {\n" +
                "            background: #F8F9FC;\n" +
                "            border-radius: 8px;\n" +
                "            padding: 20px;\n" +
                "            margin-bottom: 25px;\n" +
                "            border-left: 4px solid #0052A5;\n" +
                "        }\n" +
                "        .section-title {\n" +
                "            font-size: 16px;\n" +
                "            font-weight: bold;\n" +
                "            color: #003366;\n" +
                "            margin-bottom: 15px;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            gap: 8px;\n" +
                "        }\n" +
                "        .info-row {\n" +
                "            display: flex;\n" +
                "            margin-bottom: 10px;\n" +
                "            flex-wrap: wrap;\n" +
                "        }\n" +
                "        .info-label {\n" +
                "            width: 140px;\n" +
                "            color: #666666;\n" +
                "            font-weight: 600;\n" +
                "            font-size: 13px;\n" +
                "        }\n" +
                "        .info-value {\n" +
                "            flex: 1;\n" +
                "            color: #222222;\n" +
                "            font-weight: 500;\n" +
                "            font-size: 13px;\n" +
                "        }\n" +
                "        .badge {\n" +
                "            display: inline-block;\n" +
                "            background: #E8F4FD;\n" +
                "            color: #0052A5;\n" +
                "            padding: 4px 12px;\n" +
                "            border-radius: 20px;\n" +
                "            font-size: 12px;\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "        .action-button {\n" +
                "            display: inline-block;\n" +
                "            background: linear-gradient(135deg, #003366 0%, #0052A5 100%);\n" +
                "            color: #FFFFFF;\n" +
                "            text-decoration: none;\n" +
                "            padding: 12px 28px;\n" +
                "            border-radius: 8px;\n" +
                "            font-weight: bold;\n" +
                "            margin: 15px 0;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            background: #F5F7FA;\n" +
                "            padding: 20px;\n" +
                "            text-align: center;\n" +
                "            font-size: 11px;\n" +
                "            color: #888888;\n" +
                "            border-top: 1px solid #E0E0E0;\n" +
                "        }\n" +
                "        .footer p {\n" +
                "            margin: 5px 0;\n" +
                "        }\n" +
                "        .highlight {\n" +
                "            color: #0052A5;\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "        hr {\n" +
                "            border: none;\n" +
                "            border-top: 1px solid #E0E0E0;\n" +
                "            margin: 20px 0;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <div class=\"logo\">LEONI<span>|Quality</span></div>\n" +
                "            <h1>📋 Nouveau Cahier des Charges</h1>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <div class=\"greeting\">\n" +
                "                <strong>Bonjour,</strong><br>\n" +
                "                Un nouveau cahier des charges vient d'être créé dans le système LEONI Quality Management.\n" +
                "                Veuillez trouver ci-dessous les informations principales.\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"info-section\">\n" +
                "                <div class=\"section-title\">\n" +
                "                    <span>📄</span> INFORMATIONS GÉNÉRALES\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">N° Cahier :</div>\n" +
                "                    <div class=\"info-value\"><span class=\"badge\">#" + chargeSheet.getId() + "</span></div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">N° Commande :</div>\n" +
                "                    <div class=\"info-value\">" + escapeHtml(chargeSheet.getOrderNumber()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Site de production :</div>\n" +
                "                    <div class=\"info-value\">" + escapeHtml(chargeSheet.getPlant()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Projet :</div>\n" +
                "                    <div class=\"info-value\"><span class=\"highlight\">" + escapeHtml(chargeSheet.getProject()) + "</span></div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Référence Harnais :</div>\n" +
                "                    <div class=\"info-value\">" + escapeHtml(chargeSheet.getHarnessRef()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Émis par :</div>\n" +
                "                    <div class=\"info-value\">" + escapeHtml(chargeSheet.getIssuedBy()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Email contact :</div>\n" +
                "                    <div class=\"info-value\">" + escapeHtml(chargeSheet.getEmailAddress()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Téléphone :</div>\n" +
                "                    <div class=\"info-value\">" + escapeHtml(chargeSheet.getPhoneNumber()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Centre de coût :</div>\n" +
                "                    <div class=\"info-value\">" + escapeHtml(chargeSheet.getCostCenterNumber()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Date de création :</div>\n" +
                "                    <div class=\"info-value\">" + formatDate(chargeSheet.getDate()) + "</div>\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Date livraison souhaitée :</div>\n" +
                "                    <div class=\"info-value\">" + formatDate(chargeSheet.getPreferredDeliveryDate()) + "</div>\n" +
                "                </div>\n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"info-section\">\n" +
                "                <div class=\"section-title\">\n" +
                "                    <span>🔧</span> DÉTAIL DES ITEMS\n" +
                "                </div>\n" +
                "                <div class=\"info-row\">\n" +
                "                    <div class=\"info-label\">Nombre total d'items :</div>\n" +
                "                    <div class=\"info-value\"><span class=\"badge\">" + numberOfItems + " item(s)</span></div>\n" +
                "                </div>\n" +
                "                <hr>\n" +
                "                <div style=\"font-size: 12px; color: #666; margin-top: 10px;\">\n" +
                "                    ⚠️ Les fiches techniques des modules de test doivent être complétées avant validation.\n" +
                "                </div>\n" +
                "            </div>\n" +
                "\n" +
                "            <div style=\"text-align: center;\">\n" +
                "                <a href=\"https://leoni-quality.com/charge-sheets/" + chargeSheet.getId() + "\" class=\"action-button\">\n" +
                "                    🔗 ACCÉDER AU CAHIER DES CHARGES\n" +
                "                </a>\n" +
                "            </div>\n" +
                "\n" +
                "            <hr>\n" +
                "\n" +
                "            <div style=\"font-size: 12px; color: #666; background: #FFF8E1; padding: 12px; border-radius: 8px; margin-top: 20px;\">\n" +
                "                <strong>📌 À noter :</strong><br>\n" +
                "                • Ce cahier des charges est actuellement en statut <strong>BROUILLON</strong><br>\n" +
                "                • La validation par le service ING est requise avant transmission au fournisseur<br>\n" +
                "                • Les délais de livraison doivent être respectés selon la date souhaitée\n" +
                "            </div>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"footer\">\n" +
                "            <p><strong>LEONI Wiring Systems</strong> - Quality Management System</p>\n" +
                "            <p>Cet email est généré automatiquement, merci de ne pas y répondre.</p>\n" +
                "            <p>© 2026 LEONI Group - Tous droits réservés</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    /**
     * Échappe les caractères HTML pour éviter les injections et les erreurs d'affichage
     */
    private String escapeHtml(String text) {
        if (text == null) return "-";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Formate une date LocalDate en string lisible
     */
    private String formatDate(LocalDate date) {
        if (date == null) return "Non spécifiée";
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    @Transactional
    public ChargeSheetItem updateTechnicalFields(Long sheetId, Long itemId, ChargeSheetDto.UpdateTechDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheetItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getChargeSheet().getId().equals(sheetId)) {
            throw new RuntimeException("Item does not belong to this charge sheet");
        }

        updateItemFromDto(item, dto);

        item.setUpdatedBy(currentUser.getEmail());
        item.setUpdatedAt(LocalDate.now());
        item.setItemStatus("TECH_FILLED");

        ChargeSheetItem updated = itemRepository.save(item);

        // ✅ METTRE À JOUR LE STATUT GLOBAL ICI
        updateGlobalStatusAfterItemUpdate(item.getChargeSheet());

        ChargeSheet sheet = item.getChargeSheet();
        notificationService.notifyChargeSheetUpdatedToProjectAndSite(
                sheetId,
                "Item " + item.getItemNumber() + " modifié",
                currentUser.getEmail(),
                "PT",
                sheet.getProject(),
                sheet.getPlant()
        );

        return updated;
    }
    private void updateGlobalStatusAfterItemUpdate(ChargeSheet sheet) {
        // Vérifier si tous les items sont TECH_FILLED
        boolean allItemsFilled = sheet.getItems().stream()
                .allMatch(item -> "TECH_FILLED".equals(item.getItemStatus()));

        // Si tous les items sont remplis
        if (allItemsFilled) {
            ChargeSheetStatus currentStatus = sheet.getStatus();

            if (currentStatus == ChargeSheetStatus.DRAFT) {
                // DRAFT -> TECH_FILLED (les items sont remplis mais ING n'a pas encore validé)
                sheet.setStatus(ChargeSheetStatus.TECH_FILLED);
                repository.save(sheet);
            } else if (currentStatus == ChargeSheetStatus.VALIDATED_ING) {
                // ✅ Si ING a déjà validé et tous les items sont remplis
                // On passe à TECH_FILLED (en attendant validation PT)
                sheet.setStatus(ChargeSheetStatus.TECH_FILLED);
                repository.save(sheet);
            }
        }
    }

    @Transactional
    public ChargeSheet addItem(Long sheetId, ChargeSheetDto.ItemDto itemDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        ChargeSheetItem item = mapToItem(itemDto, chargeSheet, currentUser);
        chargeSheet.addItem(item);

        ChargeSheet updated = repository.save(chargeSheet);

        notificationService.notifyChargeSheetUpdatedToProjectAndSite(
                sheetId,
                "Nouvel item ajouté: " + item.getItemNumber(),
                currentUser.getEmail(),
                "ING",
                chargeSheet.getProject(),
                chargeSheet.getPlant()
        );

        return updated;
    }

    @Transactional
    public void removeItem(Long sheetId, Long itemId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        ChargeSheetItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getChargeSheet().getId().equals(sheetId)) {
            throw new RuntimeException("Item does not belong to this charge sheet");
        }

        chargeSheet.removeItem(item);
        itemRepository.delete(item);
        repository.save(chargeSheet);

        // CORRECTION: Le troisième paramètre doit être Long, pas String
        notificationService.notifyDocumentDeletedToProjectAndSite(
                "Item de Cahier des Charges",
                itemId,
                sheetId,
                currentUser.getEmail(),
                chargeSheet.getProject(),
                chargeSheet.getPlant()
        );
    }

    public ChargeSheet getChargeSheetById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));
    }

    public ChargeSheetDto.CompleteDto getChargeSheetComplete(Long id) {
        ChargeSheet chargeSheet = getChargeSheetById(id);
        return mapToCompleteDto(chargeSheet);
    }

    // ChargeSheetService.java - Modifier la méthode getAllChargeSheets()

    public List<ChargeSheetDto.CompleteDto> getAllChargeSheets() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userRole = currentUser.getRole().name();
        String userProjectsString = currentUser.getProjetsNames();

        // ✅ Convertir en liste pour faciliter le filtrage
        List<String> userProjects = userProjectsString != null ?
                Arrays.asList(userProjectsString.split(", ")) :
                List.of(); // Retourne List<String>
        // ✅ Récupérer le site de l'utilisateur connecté
        String userSite = currentUser.getSiteName();  // Ex: "Manzel Hayet", "LTN1", etc.

        List<ChargeSheet> sheets;

        // ✅ ADMIN voit tout (sans filtre site car il peut gérer tous les sites)
        if (userRole.equals("ADMIN")) {
            sheets = repository.findAll();
        }
        // ✅ Pour les autres rôles, filtrer par site (plant = site)
        else {
            // Filtrer par site ET par projet (selon les règles existantes)
            switch (userRole) {
                case "ING":
                    // ING voit les cahiers de son projet ET de son site
                    sheets = repository.findByProjectInAndPlant(userProjects, userSite);
                    break;

                case "PT":
                    // PT voit les cahiers validés par ING de son projet ET de son site
                    sheets = repository.findByProjectInAndPlantAndStatusIn(
                            userProjects,
                            userSite,
                            List.of(
                                    ChargeSheetStatus.VALIDATED_ING,
                                    ChargeSheetStatus.TECH_FILLED,
                                    ChargeSheetStatus.VALIDATED_PT,
                                    ChargeSheetStatus.SENT_TO_SUPPLIER,
                                    ChargeSheetStatus.RECEIVED_FROM_SUPPLIER,
                                    ChargeSheetStatus.COMPLETED
                            )
                    );
                    break;

                case "PP":
                    // PP voit les cahiers validés par PT de son projet ET de son site
                    sheets = repository.findByProjectInAndPlantAndStatusIn(
                            userProjects,
                            userSite,
                            List.of(
                                    ChargeSheetStatus.VALIDATED_PT,
                                    ChargeSheetStatus.SENT_TO_SUPPLIER,
                                    ChargeSheetStatus.RECEIVED_FROM_SUPPLIER,
                                    ChargeSheetStatus.COMPLETED
                            )
                    );
                    break;

                case "MC":
                case "MP":
                    // MC et MP voient seulement les cahiers complétés de leur projet ET site
                    sheets = repository.findByProjectInAndPlantAndStatus(
                            userProjects,
                            userSite,
                            ChargeSheetStatus.COMPLETED
                    );
                    break;

                default:
                    sheets = List.of();
                    break;
            }
        }

        return sheets.stream()
                .map(this::mapToCompleteDto)
                .collect(Collectors.toList());
    }


    public ChargeSheet save(ChargeSheet chargeSheet) {
        return repository.save(chargeSheet);
    }

    public ChargeSheetDto.ItemDto mapToItemDtoPublic(ChargeSheetItem item) {
        return mapToItemDto(item);
    }

    // === Méthodes privées ===

    private ChargeSheetItem mapToItem(ChargeSheetDto.ItemDto dto, ChargeSheet sheet, User user) {
        return ChargeSheetItem.builder()
                .chargeSheet(sheet)
                .itemNumber(dto.getItemNumber())
                .samplesExist(dto.getSamplesExist())
                .ways(dto.getWays())
                .housingColour(dto.getHousingColour())
                .testModuleExistInDatabase(dto.getTestModuleExistInDatabase())
                .housingReferenceLeoni(dto.getHousingReferenceLeoni())
                .housingReferenceSupplierCustomer(dto.getHousingReferenceSupplierCustomer())
                .referenceSealsClipsCableTiesCap(dto.getReferenceSealsClipsCableTiesCap())
                .realConnectorPicture(dto.getRealConnectorPicture())
                .quantityOfTestModules(dto.getQuantityOfTestModules())
                .itemStatus("DRAFT")
                .createdBy(user.getEmail())
                .createdAt(LocalDate.now())
                .build();
    }

    private ChargeSheetItem createDefaultItem(ChargeSheet sheet, User user) {
        return ChargeSheetItem.builder()
                .chargeSheet(sheet)
                .itemNumber("1")
                .samplesExist("No")
                .quantityOfTestModules(1)
                .itemStatus("DRAFT")
                .createdBy(user.getEmail())
                .createdAt(LocalDate.now())
                .build();
    }

    private void updateItemFromDto(ChargeSheetItem item, ChargeSheetDto.UpdateTechDto dto) {
        if (dto.getHousingReferenceLeoni() != null) {
            item.setHousingReferenceLeoni(dto.getHousingReferenceLeoni());
        }
        if (dto.getQuantityOfTestModules() != null) {
            item.setQuantityOfTestModules(dto.getQuantityOfTestModules());
        }
        item.setOutsideHousingExist(dto.getOutsideHousingExist());
        item.setInsideHousingExist(dto.getInsideHousingExist());
        item.setMechanicalCoding(dto.getMechanicalCoding());
        item.setElectricalCoding(dto.getElectricalCoding());
        item.setCpaExistOpen(dto.getCpaExistOpen());
        item.setCpaExistClosed(dto.getCpaExistClosed());
        item.setCoverHoodExist(dto.getCoverHoodExist());
        item.setCoverHoodClosed(dto.getCoverHoodClosed());
        item.setCapExist(dto.getCapExist());
        item.setBayonetCapExist(dto.getBayonetCapExist());
        item.setBracketExist(dto.getBracketExist());
        item.setBracketOpen(dto.getBracketOpen());
        item.setBracketClosed(dto.getBracketClosed());
        item.setLatchWingExist(dto.getLatchWingExist());
        item.setSliderExist(dto.getSliderExist());
        item.setSliderOpen(dto.getSliderOpen());
        item.setSliderClosed(dto.getSliderClosed());
        item.setSecondaryLockExist(dto.getSecondaryLockExist());
        item.setSecondaryLockOpen(dto.getSecondaryLockOpen());
        item.setSecondaryLockClosed(dto.getSecondaryLockClosed());
        item.setOffsetTest(dto.getOffsetTest());
        item.setPushBackTest(dto.getPushBackTest());
        item.setTerminalOrientation(dto.getTerminalOrientation());
        item.setTerminalDifferentiation(dto.getTerminalDifferentiation());
        item.setAirbagTestViaServiceWindow(dto.getAirbagTestViaServiceWindow());
        item.setLeakTestPressure(dto.getLeakTestPressure());
        item.setLeakTestVacuum(dto.getLeakTestVacuum());
        item.setSealExist(dto.getSealExist());
        item.setCableTieExist(dto.getCableTieExist());
        item.setCableTieLeft(dto.getCableTieLeft());
        item.setCableTieRight(dto.getCableTieRight());
        item.setCableTieMiddle(dto.getCableTieMiddle());
        item.setCableTieLeftRight(dto.getCableTieLeftRight());
        item.setClipExist(dto.getClipExist());
        item.setScrewExist(dto.getScrewExist());
        item.setNutExist(dto.getNutExist());
        item.setConvolutedConduitExist(dto.getConvolutedConduitExist());
        item.setConvolutedConduitClosed(dto.getConvolutedConduitClosed());
        item.setAntennaOnlyPresenceTest(dto.getAntennaOnlyPresenceTest());
        item.setAntennaOnlyContactingOfShield(dto.getAntennaOnlyContactingOfShield());
        item.setAntennaContactingOfShieldAndCoreWire(dto.getAntennaContactingOfShieldAndCoreWire());
        item.setRingTerminal(dto.getRingTerminal());
        item.setDiameterInside(dto.getDiameterInside());
        item.setDiameterOutside(dto.getDiameterOutside());
        item.setSingleContact(dto.getSingleContact());
        item.setHeatShrinkExist(dto.getHeatShrinkExist());
        item.setOpenShuntsAirbag(dto.getOpenShuntsAirbag());
        item.setFlowTest(dto.getFlowTest());
        item.setSolidMetalContour(dto.getSolidMetalContour());
        item.setMetalContourAdjustable(dto.getMetalContourAdjustable());
        item.setGrommetExist(dto.getGrommetExist());
        item.setGrommetOrientation(dto.getGrommetOrientation());
        item.setCableChannelExist(dto.getCableChannelExist());
        item.setCableChannelClosed(dto.getCableChannelClosed());
        item.setColourDetectionPrepared(dto.getColourDetectionPrepared());
        item.setExtraLED(dto.getExtraLED());
        item.setSpring(dto.getSpring());
        item.setOtherDetection(dto.getOtherDetection());
        item.setSpacerClosingUnit(dto.getSpacerClosingUnit());
        item.setLeakTestComplex(dto.getLeakTestComplex());
        item.setPinStraightnessCheck(dto.getPinStraightnessCheck());
        item.setPresenceTestOfOneSideConnectedShield(dto.getPresenceTestOfOneSideConnectedShield());
        item.setContrastDetectionGreyValueSensor(dto.getContrastDetectionGreyValueSensor());
        item.setColourDetection(dto.getColourDetection());
        item.setAttenuationWithModeScrambler(dto.getAttenuationWithModeScrambler());
        item.setAttenuationWithoutModeScrambler(dto.getAttenuationWithoutModeScrambler());
        item.setInsulationResistance(dto.getInsulationResistance());
        item.setHighVoltageModule(dto.getHighVoltageModule());
        item.setKelvinMeasurementHV(dto.getKelvinMeasurementHV());
        item.setActuatorTestHV(dto.getActuatorTestHV());
        item.setChargingSystemElectrical(dto.getChargingSystemElectrical());
        item.setPtuPipeTestUnit(dto.getPtuPipeTestUnit());
        item.setGtuGrommetTestUnit(dto.getGtuGrommetTestUnit());
        item.setLedLEDTestModule(dto.getLedLEDTestModule());
        item.setTigTerminalInsertionGuidance(dto.getTigTerminalInsertionGuidance());
        item.setLinBusFunctionalityTest(dto.getLinBusFunctionalityTest());
        item.setCanBusFunctionalityTest(dto.getCanBusFunctionalityTest());
        item.setEsdConformModule(dto.getEsdConformModule());
        item.setFixedBlock(dto.getFixedBlock());
        item.setMovingBlock(dto.getMovingBlock());
        item.setTiltModule(dto.getTiltModule());
        item.setSlideModule(dto.getSlideModule());
        item.setHandAdapter(dto.getHandAdapter());
        item.setLsmLeoniSmartModule(dto.getLsmLeoniSmartModule());
        item.setLeoniStandardTestTable(dto.getLeoniStandardTestTable());
        item.setMetalRailsFasteningSystem(dto.getMetalRailsFasteningSystem());
        item.setMetalPlatesFasteningSystem(dto.getMetalPlatesFasteningSystem());
        item.setQuickConnectionByCanonConnector(dto.getQuickConnectionByCanonConnector());
        item.setTestBoard(dto.getTestBoard());
        item.setWeetech(dto.getWeetech());
        item.setBak(dto.getBak());
        item.setOgc(dto.getOgc());
        item.setAdaptronicHighVoltage(dto.getAdaptronicHighVoltage());
        item.setEmdepHVBananaPlug(dto.getEmdepHVBananaPlug());
        item.setLeoniEMOStandardHV(dto.getLeoniEMOStandardHV());
        item.setClipOrientation(dto.getClipOrientation());
        item.setUnitPrice(dto.getUnitPrice());
        item.setTotalPrice(dto.getTotalPrice());
    }


    private ChargeSheetDto.CompleteDto mapToCompleteDto(ChargeSheet sheet) {
        List<ChargeSheetDto.ItemDto> itemDtos = sheet.getItems().stream()
                .map(this::mapToItemDto)
                .collect(Collectors.toList());

        return ChargeSheetDto.CompleteDto.builder()
                .id(sheet.getId())
                .plant(sheet.getPlant())
                .project(sheet.getProject())
                .harnessRef(sheet.getHarnessRef())
                .issuedBy(sheet.getIssuedBy())
                .emailAddress(sheet.getEmailAddress())
                .phoneNumber(sheet.getPhoneNumber())
                .orderNumber(sheet.getOrderNumber())
                .costCenterNumber(sheet.getCostCenterNumber())
                .date(sheet.getDate())
                .preferredDeliveryDate(sheet.getPreferredDeliveryDate())
                .items(itemDtos)
                .status(sheet.getStatus())
                .createdBy(sheet.getCreatedBy())
                .createdAt(sheet.getCreatedAt())
                .updatedBy(sheet.getUpdatedBy())
                .updatedAt(sheet.getUpdatedAt())
                .build();
    }

    private ChargeSheetDto.ItemDto mapToItemDto(ChargeSheetItem item) {
        return ChargeSheetDto.ItemDto.builder()
                .id(item.getId())
                .itemNumber(item.getItemNumber())
                .samplesExist(item.getSamplesExist())
                .ways(item.getWays())
                .housingColour(item.getHousingColour())
                .testModuleExistInDatabase(item.getTestModuleExistInDatabase())
                .housingReferenceLeoni(item.getHousingReferenceLeoni())
                .housingReferenceSupplierCustomer(item.getHousingReferenceSupplierCustomer())
                .referenceSealsClipsCableTiesCap(item.getReferenceSealsClipsCableTiesCap())
                .realConnectorPicture(item.getRealConnectorPicture())
                .quantityOfTestModules(item.getQuantityOfTestModules())
                .outsideHousingExist(item.getOutsideHousingExist())
                .insideHousingExist(item.getInsideHousingExist())
                .mechanicalCoding(item.getMechanicalCoding())
                .electricalCoding(item.getElectricalCoding())
                .cpaExistOpen(item.getCpaExistOpen())
                .cpaExistClosed(item.getCpaExistClosed())
                .coverHoodExist(item.getCoverHoodExist())
                .coverHoodClosed(item.getCoverHoodClosed())
                .capExist(item.getCapExist())
                .bayonetCapExist(item.getBayonetCapExist())
                .bracketExist(item.getBracketExist())
                .bracketOpen(item.getBracketOpen())
                .bracketClosed(item.getBracketClosed())
                .latchWingExist(item.getLatchWingExist())
                .sliderExist(item.getSliderExist())
                .sliderOpen(item.getSliderOpen())
                .sliderClosed(item.getSliderClosed())
                .secondaryLockExist(item.getSecondaryLockExist())
                .secondaryLockOpen(item.getSecondaryLockOpen())
                .secondaryLockClosed(item.getSecondaryLockClosed())
                .offsetTest(item.getOffsetTest())
                .pushBackTest(item.getPushBackTest())
                .terminalOrientation(item.getTerminalOrientation())
                .terminalDifferentiation(item.getTerminalDifferentiation())
                .airbagTestViaServiceWindow(item.getAirbagTestViaServiceWindow())
                .leakTestPressure(item.getLeakTestPressure())
                .leakTestVacuum(item.getLeakTestVacuum())
                .sealExist(item.getSealExist())
                .cableTieExist(item.getCableTieExist())
                .cableTieLeft(item.getCableTieLeft())
                .cableTieRight(item.getCableTieRight())
                .cableTieMiddle(item.getCableTieMiddle())
                .cableTieLeftRight(item.getCableTieLeftRight())
                .clipExist(item.getClipExist())
                .screwExist(item.getScrewExist())
                .nutExist(item.getNutExist())
                .convolutedConduitExist(item.getConvolutedConduitExist())
                .convolutedConduitClosed(item.getConvolutedConduitClosed())
                .antennaOnlyPresenceTest(item.getAntennaOnlyPresenceTest())
                .antennaOnlyContactingOfShield(item.getAntennaOnlyContactingOfShield())
                .antennaContactingOfShieldAndCoreWire(item.getAntennaContactingOfShieldAndCoreWire())
                .ringTerminal(item.getRingTerminal())
                .diameterInside(item.getDiameterInside())
                .diameterOutside(item.getDiameterOutside())
                .singleContact(item.getSingleContact())
                .heatShrinkExist(item.getHeatShrinkExist())
                .openShuntsAirbag(item.getOpenShuntsAirbag())
                .flowTest(item.getFlowTest())
                .solidMetalContour(item.getSolidMetalContour())
                .metalContourAdjustable(item.getMetalContourAdjustable())
                .grommetExist(item.getGrommetExist())
                .grommetOrientation(item.getGrommetOrientation())
                .cableChannelExist(item.getCableChannelExist())
                .cableChannelClosed(item.getCableChannelClosed())
                .colourDetectionPrepared(item.getColourDetectionPrepared())
                .extraLED(item.getExtraLED())
                .spring(item.getSpring())
                .otherDetection(item.getOtherDetection())
                .spacerClosingUnit(item.getSpacerClosingUnit())
                .leakTestComplex(item.getLeakTestComplex())
                .pinStraightnessCheck(item.getPinStraightnessCheck())
                .presenceTestOfOneSideConnectedShield(item.getPresenceTestOfOneSideConnectedShield())
                .contrastDetectionGreyValueSensor(item.getContrastDetectionGreyValueSensor())
                .colourDetection(item.getColourDetection())
                .attenuationWithModeScrambler(item.getAttenuationWithModeScrambler())
                .attenuationWithoutModeScrambler(item.getAttenuationWithoutModeScrambler())
                .insulationResistance(item.getInsulationResistance())
                .highVoltageModule(item.getHighVoltageModule())
                .kelvinMeasurementHV(item.getKelvinMeasurementHV())
                .actuatorTestHV(item.getActuatorTestHV())
                .chargingSystemElectrical(item.getChargingSystemElectrical())
                .ptuPipeTestUnit(item.getPtuPipeTestUnit())
                .gtuGrommetTestUnit(item.getGtuGrommetTestUnit())
                .ledLEDTestModule(item.getLedLEDTestModule())
                .tigTerminalInsertionGuidance(item.getTigTerminalInsertionGuidance())
                .linBusFunctionalityTest(item.getLinBusFunctionalityTest())
                .canBusFunctionalityTest(item.getCanBusFunctionalityTest())
                .esdConformModule(item.getEsdConformModule())
                .fixedBlock(item.getFixedBlock())
                .movingBlock(item.getMovingBlock())
                .tiltModule(item.getTiltModule())
                .slideModule(item.getSlideModule())
                .handAdapter(item.getHandAdapter())
                .lsmLeoniSmartModule(item.getLsmLeoniSmartModule())
                .leoniStandardTestTable(item.getLeoniStandardTestTable())
                .metalRailsFasteningSystem(item.getMetalRailsFasteningSystem())
                .metalPlatesFasteningSystem(item.getMetalPlatesFasteningSystem())
                .quickConnectionByCanonConnector(item.getQuickConnectionByCanonConnector())
                .testBoard(item.getTestBoard())
                .weetech(item.getWeetech())
                .bak(item.getBak())
                .ogc(item.getOgc())
                .adaptronicHighVoltage(item.getAdaptronicHighVoltage())
                .emdepHVBananaPlug(item.getEmdepHVBananaPlug())
                .leoniEMOStandardHV(item.getLeoniEMOStandardHV())
                .clipOrientation(item.getClipOrientation())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .itemStatus(item.getItemStatus())
                .createdBy(item.getCreatedBy())
                .createdAt(item.getCreatedAt())
                .updatedBy(item.getUpdatedBy())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    @Transactional
    public void deleteChargeSheet(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found with id: " + id));

        // Supprimer le cahier (les items seront supprimés automatiquement grâce à cascade)
        repository.delete(chargeSheet);

        // Notification
        notificationService.notifyDocumentDeletedToProjectAndSite(
                "Cahier des Charges",
                id,
                null,
                currentUser.getEmail(),
                chargeSheet.getProject(),
                chargeSheet.getPlant()
        );
    }
    @Transactional
    public ChargeSheet validateByIng(Long sheetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier que l'utilisateur est ING
        if (!currentUser.getRole().name().equals("ING") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul ING peut valider cette étape");
        }

        // Vérifier que le statut actuel est DRAFT
        if (chargeSheet.getStatus() != ChargeSheetStatus.DRAFT) {
            throw new RuntimeException("Le cahier doit être en mode DRAFT pour être validé par ING");
        }

        chargeSheet.setStatus(ChargeSheetStatus.VALIDATED_ING);
        chargeSheet.setUpdatedBy(currentUser.getEmail());
        chargeSheet.setUpdatedAt(LocalDate.now());

        ChargeSheet validated = repository.save(chargeSheet);

        // Notification
        notificationService.notifyChargeSheetUpdatedToProjectAndSite(
                sheetId,
                "Validé par ING",
                currentUser.getEmail(),
                "ING",
                chargeSheet.getProject(),
                chargeSheet.getPlant()
        );
        return validated;
    }
    @Transactional
    public ChargeSheet validateByPt(Long sheetId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier que l'utilisateur est PT
        if (!currentUser.getRole().name().equals("PT") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT peut valider cette étape");
        }

        // Vérifier que tous les items sont TECH_FILLED
        boolean allItemsFilled = chargeSheet.getItems().stream()
                .allMatch(item -> "TECH_FILLED".equals(item.getItemStatus()));

        if (!allItemsFilled) {
            throw new RuntimeException("Tous les items doivent être remplis avant validation PT");
        }

        ChargeSheetStatus newStatus;
        String message;

        // WORKFLOW COMPLET:
        // DRAFT → VALIDATED_ING → TECH_FILLED → VALIDATED_PT → SENT_TO_SUPPLIER → RECEIVED_FROM_SUPPLIER → COMPLETED

        switch (chargeSheet.getStatus()) {
            case VALIDATED_ING:
                // ING a validé, PT valide maintenant → VALIDATED_PT
                newStatus = ChargeSheetStatus.VALIDATED_PT;
                message = "Validé par PT - En attente d'envoi au fournisseur";
                break;

            case TECH_FILLED:
                // Les items sont remplis, PT valide maintenant → VALIDATED_PT
                newStatus = ChargeSheetStatus.VALIDATED_PT;
                message = "Validé par PT - En attente d'envoi au fournisseur";
                break;

            case DRAFT:
                // Cas exceptionnel: PT valide directement un brouillon → VALIDATED_PT
                newStatus = ChargeSheetStatus.VALIDATED_PT;
                message = "Validé par PT directement - En attente d'envoi au fournisseur";
                break;

            default:
                throw new RuntimeException("Statut invalide pour validation PT: " + chargeSheet.getStatus());
        }

        chargeSheet.setStatus(newStatus);
        chargeSheet.setUpdatedBy(currentUser.getEmail());
        chargeSheet.setUpdatedAt(LocalDate.now());

        ChargeSheet validated = repository.save(chargeSheet);

        // Notification
        notificationService.notifyChargeSheetUpdatedToProjectAndSite(
                sheetId,
                message,
                currentUser.getEmail(),
                "PT",
                chargeSheet.getProject(),
                chargeSheet.getPlant()
        );

        return validated;
    }
    // ChargeSheetService.java - Modifier getDashboardStats()

    public Map<String, Object> getDashboardStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userRole = currentUser.getRole().name();
        String userProjectsString = currentUser.getProjetsNames();
        String userSite = currentUser.getSiteName();  // ✅ AJOUTER

        Map<String, Object> stats = new HashMap<>();

        stats.put("userRole", userRole);
        stats.put("userProject", userProjectsString);
        stats.put("userSite", userSite);  // ✅ AJOUTER
        stats.put("projectName", userProjectsString);

        if (userRole.equals("ADMIN")) {
            // ADMIN voit tout
            long totalSheets = repository.count();
            long pendingIng = repository.countByStatus(ChargeSheetStatus.DRAFT);
            long pendingPt = repository.countByStatus(ChargeSheetStatus.VALIDATED_ING);
            long pendingFinal = repository.countByStatus(ChargeSheetStatus.VALIDATED_PT);
            long completed = repository.countByStatus(ChargeSheetStatus.COMPLETED);
            long techFilled = repository.countByStatus(ChargeSheetStatus.TECH_FILLED);

            stats.put("totalSheets", totalSheets);
            stats.put("pendingIng", pendingIng);
            stats.put("pendingPt", pendingPt);
            stats.put("pendingFinal", pendingFinal);
            stats.put("completed", completed);
            stats.put("techFilled", techFilled);

            double completionRate = totalSheets > 0 ? (completed * 100.0 / totalSheets) : 0;
            stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        }
        else {
            // ✅ Pour les autres rôles, filtrer par projet ET site
            List<String> userProjects = userProjectsString != null ?
                    Arrays.asList(userProjectsString.split(", ")) :
                    List.of();

            long totalSheets = 0;
            long pendingIng = 0;
            long pendingPt = 0;
            long pendingFinal = 0;
            long completed = 0;
            long techFilled = 0;

            // ✅ Additionner les statistiques pour chaque projet
            for (String project : userProjects) {
                totalSheets += repository.countByProjectAndPlant(project, userSite);
                pendingIng += repository.countByProjectAndPlantAndStatus(project, userSite, ChargeSheetStatus.DRAFT);
                pendingPt += repository.countByProjectAndPlantAndStatus(project, userSite, ChargeSheetStatus.VALIDATED_ING);
                pendingFinal += repository.countByProjectAndPlantAndStatus(project, userSite, ChargeSheetStatus.VALIDATED_PT);
                completed += repository.countByProjectAndPlantAndStatus(project, userSite, ChargeSheetStatus.COMPLETED);
                techFilled += repository.countByProjectAndPlantAndStatus(project, userSite, ChargeSheetStatus.TECH_FILLED);
            }

            stats.put("totalSheets", totalSheets);
            stats.put("pendingIng", pendingIng);
            stats.put("pendingPt", pendingPt);
            stats.put("pendingFinal", pendingFinal);
            stats.put("completed", completed);
            stats.put("techFilled", techFilled);

            double completionRate = totalSheets > 0 ? (completed * 100.0 / totalSheets) : 0;
            stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        }

        return stats;
    }
    @Transactional
    public ChargeSheet sendToSupplier(Long sheetId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // ✅ Vérification rôle
        if (!currentUser.getRole().name().equals("PT") &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT ou ADMIN peut envoyer au fournisseur");
        }

        if (sheet.getStatus() != ChargeSheetStatus.VALIDATED_PT) {
            throw new RuntimeException("Le cahier doit être VALIDATED_PT");
        }

        sheet.setStatus(ChargeSheetStatus.SENT_TO_SUPPLIER);
        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    @Transactional
    public ChargeSheet confirmReception(Long sheetId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // ✅ Vérification rôle
        if (!currentUser.getRole().name().equals("PT") &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT ou ADMIN peut confirmer la réception");
        }

        if (sheet.getStatus() != ChargeSheetStatus.SENT_TO_SUPPLIER) {
            throw new RuntimeException("Le cahier doit être envoyé au fournisseur");
        }

        sheet.setStatus(ChargeSheetStatus.RECEIVED_FROM_SUPPLIER);
        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    @Transactional
    public ChargeSheet completeChargeSheet(Long sheetId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // ✅ Vérification rôle
        if (!currentUser.getRole().name().equals("PT") &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT ou ADMIN peut compléter le cahier");
        }

        if (sheet.getStatus() != ChargeSheetStatus.RECEIVED_FROM_SUPPLIER) {
            throw new RuntimeException("La réception doit être confirmée avant completion");
        }

        sheet.setStatus(ChargeSheetStatus.COMPLETED);
        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    public ChargeSheet updateChargeSheet(Long id, ChargeSheetDto.UpdateGeneralDto dto) {
        ChargeSheet sheet = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier que le statut est DRAFT
        if (sheet.getStatus() != ChargeSheetStatus.DRAFT) {
            throw new RuntimeException("Cannot update charge sheet when status is not DRAFT");
        }

        // Vérifier les permissions (l'utilisateur doit être ING du même projet)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userSite = currentUser.getSiteName();
        String userProjectsString = currentUser.getProjetsNames();
        List<String> userProjects = userProjectsString != null ?
                Arrays.asList(userProjectsString.split(", ")) :
                List.of();
        if (!sheet.getPlant().equals(userSite)) {
            throw new RuntimeException("Vous ne pouvez modifier que les cahiers de votre site: " + userSite);
        }
        if (!currentUser.getRole().name().equals("ING") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Only ING can update charge sheets");
        }

        if (!currentUser.getRole().name().equals("ADMIN") &&
                !userProjects.contains(sheet.getProject())) {
            throw new RuntimeException("You can only update charge sheets from your projects");
        }

        // Mettre à jour les champs

        sheet.setProject(dto.getProject());
        sheet.setHarnessRef(dto.getHarnessRef());
        sheet.setPhoneNumber(dto.getPhoneNumber());
        sheet.setOrderNumber(dto.getOrderNumber());
        sheet.setCostCenterNumber(dto.getCostCenterNumber());
        sheet.setDate(dto.getDate() != null ? LocalDate.parse(dto.getDate()) : null);
        sheet.setPreferredDeliveryDate(dto.getPreferredDeliveryDate() != null ?
                LocalDate.parse(dto.getPreferredDeliveryDate()) : null);

        sheet.setUpdatedBy(currentUser.getEmail());
        sheet.setUpdatedAt(LocalDate.now());

        return repository.save(sheet);
    }
    // ============ MÉTHODES DE RÉCEPTION ============

    /**
     * Prépare les données pour la réception (quantités commandées, déjà reçues, restantes)
     */
    public ReceptionDto.ReceptionResponseDto prepareReceptionData(Long sheetId) {
        ChargeSheet sheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier le statut
        if (sheet.getStatus() != ChargeSheetStatus.SENT_TO_SUPPLIER) {
            throw new RuntimeException("Le cahier doit être en statut SENT_TO_SUPPLIER");
        }

        // Récupérer l'historique des réceptions pour calculer les quantités déjà reçues
        Map<Long, Integer> totalReceivedMap = new HashMap<>();
        List<ReceptionHistory> historyList = receptionHistoryRepository.findByChargeSheetId(sheetId);

        for (ReceptionHistory history : historyList) {
            totalReceivedMap.put(history.getItem().getId(),
                    totalReceivedMap.getOrDefault(history.getItem().getId(), 0) + history.getQuantityReceived());
        }

        // Préparer la liste des items avec les quantités
        List<ReceptionDto.ReceptionItemDto> itemDtos = new ArrayList<>();

        for (ChargeSheetItem item : sheet.getItems()) {
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;
            int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
            int quantityRemaining = quantityOrdered - totalReceived;

            ReceptionDto.ReceptionItemDto itemDto = ReceptionDto.ReceptionItemDto.builder()
                    .itemId(item.getId())
                    .itemNumber(item.getItemNumber())
                    .quantityOrdered(quantityOrdered)
                    .quantityReceived(0)
                    .quantityRemaining(quantityRemaining)
                    .build();

            itemDtos.add(itemDto);
        }

        boolean isComplete = itemDtos.stream().allMatch(item -> item.getQuantityRemaining() == 0);

        return ReceptionDto.ReceptionResponseDto.builder()
                .chargeSheetId(sheetId)
                .items(itemDtos)
                .message("Prêt pour la réception")
                .complete(isComplete)
                .build();
    }

    /**
     * Confirme une réception partielle avec les quantités saisies
     */
    /**
     * Confirme une réception partielle avec les quantités saisies
     */
    /**
     * Confirme une réception partielle avec les quantités saisies
     */
    /**
     * Confirme une réception partielle avec les quantités saisies
     */
    @Transactional
    public ReceptionDto.ReceptionResponseDto confirmPartialReception(ReceptionDto.ReceptionRequestDto request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        ChargeSheet sheet = repository.findById(request.getChargeSheetId())
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        if (sheet.getStatus() != ChargeSheetStatus.SENT_TO_SUPPLIER) {
            throw new RuntimeException("Le cahier doit être en statut SENT_TO_SUPPLIER");
        }

        // Récupérer les totaux déjà reçus
        Map<Long, Integer> totalReceivedMap = new HashMap<>();
        List<ReceptionHistory> existingHistory = receptionHistoryRepository.findByChargeSheetId(sheet.getId());
        for (ReceptionHistory h : existingHistory) {
            totalReceivedMap.put(h.getItem().getId(),
                    totalReceivedMap.getOrDefault(h.getItem().getId(), 0) + h.getQuantityReceived());
        }

        // Enregistrer les anciens totaux avant mise à jour
        Map<Long, Integer> oldTotals = new HashMap<>(totalReceivedMap);

        // Traiter chaque item reçu
        List<ReceptionHistory> newHistories = new ArrayList<>();
        for (ReceptionDto.ReceptionItemDto itemDto : request.getItems()) {
            if (itemDto.getQuantityReceived() <= 0) continue;

            ChargeSheetItem item = itemRepository.findById(itemDto.getItemId())
                    .orElseThrow(() -> new RuntimeException("Item not found: " + itemDto.getItemId()));

            int previousTotal = totalReceivedMap.getOrDefault(item.getId(), 0);
            int newTotal = previousTotal + itemDto.getQuantityReceived();
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;

            // Vérifier qu'on ne dépasse pas la quantité commandée
            if (newTotal > quantityOrdered) {
                throw new RuntimeException("La quantité reçue pour l'item " + item.getItemNumber() +
                        " dépasse la quantité commandée (" + quantityOrdered + ")");
            }

            // Créer l'historique
            ReceptionHistory history = ReceptionHistory.builder()
                    .chargeSheet(sheet)
                    .item(item)
                    .quantityReceived(itemDto.getQuantityReceived())
                    .previousTotalReceived(previousTotal)
                    .newTotalReceived(newTotal)
                    .quantityOrdered(quantityOrdered)
                    .deliveryNoteNumber(request.getDeliveryNoteNumber())
                    .receptionDate(request.getReceptionDate() != null ?
                            LocalDate.parse(request.getReceptionDate()) : LocalDate.now())
                    .receivedBy(currentUser.getEmail())
                    .comments(request.getComments())
                    .createdAt(LocalDate.now())
                    .build();

            receptionHistoryRepository.save(history);
            newHistories.add(history);

            // Mettre à jour la map des totaux reçus
            totalReceivedMap.put(item.getId(), newTotal);
        }

        // 🔥 VÉRIFIER SI TOUS LES ITEMS SONT COMPLÈTEMENT REÇUS
        boolean allItemsCompletelyReceived = true;
        StringBuilder remainingItemsMessage = new StringBuilder();

        for (ChargeSheetItem item : sheet.getItems()) {
            int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;

            if (totalReceived < quantityOrdered) {
                allItemsCompletelyReceived = false;
                remainingItemsMessage.append("Item ").append(item.getItemNumber())
                        .append(": ").append(totalReceived).append("/").append(quantityOrdered).append(" reçus, ")
                        .append(quantityOrdered - totalReceived).append(" restant\n");
            }
        }

        // NE PAS CHANGER LE STATUT SI TOUS LES ITEMS NE SONT PAS ENCORE REÇUS
        if (allItemsCompletelyReceived) {
            sheet.setStatus(ChargeSheetStatus.RECEIVED_FROM_SUPPLIER);
            sheet.setUpdatedBy(currentUser.getEmail());
            sheet.setUpdatedAt(LocalDate.now());
            repository.save(sheet);
        }

        // ✅ ENVOYER L'EMAIL DE NOTIFICATION DE RÉCEPTION
        sendReceptionNotification(sheet, request, newHistories, totalReceivedMap, oldTotals, currentUser);

        // Recalculer les quantités restantes pour la réponse
        List<ReceptionDto.ReceptionItemDto> responseItems = new ArrayList<>();
        for (ChargeSheetItem item : sheet.getItems()) {
            int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;

            responseItems.add(ReceptionDto.ReceptionItemDto.builder()
                    .itemId(item.getId())
                    .itemNumber(item.getItemNumber())
                    .quantityOrdered(quantityOrdered)
                    .quantityReceived(0)
                    .quantityRemaining(quantityOrdered - totalReceived)
                    .build());
        }

        String message;
        if (allItemsCompletelyReceived) {
            message = "✅ Tous les items ont été reçus complètement. Le cahier passe en statut RECEIVED_FROM_SUPPLIER";
        } else {
            message = "📦 Réception partielle enregistrée avec succès.\n" +
                    "Items restants à recevoir :\n" + remainingItemsMessage.toString();
        }

        return ReceptionDto.ReceptionResponseDto.builder()
                .chargeSheetId(sheet.getId())
                .items(responseItems)
                .message(message)
                .complete(allItemsCompletelyReceived)
                .build();
    }
    /**
     * Envoie une notification email pour la réception
     */
    /**
     * Envoie une notification email pour la réception
     */
    private void sendReceptionNotification(ChargeSheet sheet, ReceptionDto.ReceptionRequestDto request,
                                           List<ReceptionHistory> newHistories,
                                           Map<Long, Integer> totalReceivedMap,
                                           Map<Long, Integer> oldTotals,
                                           User currentUser) {

        String subject = "📦 LEONI - Réception enregistrée - Cahier N°" + sheet.getOrderNumber();

        // Construction du message HTML professionnel
        StringBuilder htmlMessage = new StringBuilder();
        htmlMessage.append("<!DOCTYPE html>\n");
        htmlMessage.append("<html>\n");
        htmlMessage.append("<head>\n");
        htmlMessage.append("    <meta charset=\"UTF-8\">\n");
        htmlMessage.append("    <title>LEONI - Notification de réception</title>\n");
        htmlMessage.append("    <style>\n");
        htmlMessage.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        htmlMessage.append("        body {\n");
        htmlMessage.append("            font-family: 'Segoe UI', Arial, Helvetica, sans-serif;\n");
        htmlMessage.append("            background-color: #F0F2F5;\n");
        htmlMessage.append("            margin: 0;\n");
        htmlMessage.append("            padding: 20px;\n");
        htmlMessage.append("            line-height: 1.5;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .container {\n");
        htmlMessage.append("            max-width: 800px;\n");
        htmlMessage.append("            margin: 0 auto;\n");
        htmlMessage.append("            background: #FFFFFF;\n");
        htmlMessage.append("            border-radius: 12px;\n");
        htmlMessage.append("            overflow: hidden;\n");
        htmlMessage.append("            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .header {\n");
        htmlMessage.append("            background: linear-gradient(135deg, #003366 0%, #0052A5 100%);\n");
        htmlMessage.append("            padding: 25px 20px;\n");
        htmlMessage.append("            text-align: center;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .logo {\n");
        htmlMessage.append("            font-size: 28px;\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            color: #FFFFFF;\n");
        htmlMessage.append("            margin-bottom: 10px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .logo span {\n");
        htmlMessage.append("            color: #00D4FF;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .header h1 {\n");
        htmlMessage.append("            color: #FFFFFF;\n");
        htmlMessage.append("            margin: 0;\n");
        htmlMessage.append("            font-size: 22px;\n");
        htmlMessage.append("            font-weight: normal;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .content {\n");
        htmlMessage.append("            padding: 30px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .greeting {\n");
        htmlMessage.append("            background: #E8F4FD;\n");
        htmlMessage.append("            padding: 15px 20px;\n");
        htmlMessage.append("            border-radius: 8px;\n");
        htmlMessage.append("            margin-bottom: 25px;\n");
        htmlMessage.append("            border-left: 4px solid #0052A5;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .greeting p {\n");
        htmlMessage.append("            margin: 5px 0;\n");
        htmlMessage.append("            color: #003366;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .info-section {\n");
        htmlMessage.append("            background: #F8F9FC;\n");
        htmlMessage.append("            border-radius: 8px;\n");
        htmlMessage.append("            padding: 20px;\n");
        htmlMessage.append("            margin-bottom: 25px;\n");
        htmlMessage.append("            border: 1px solid #E0E0E0;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .section-title {\n");
        htmlMessage.append("            font-size: 16px;\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            color: #003366;\n");
        htmlMessage.append("            margin-bottom: 15px;\n");
        htmlMessage.append("            padding-bottom: 8px;\n");
        htmlMessage.append("            border-bottom: 2px solid #0052A5;\n");
        htmlMessage.append("            display: flex;\n");
        htmlMessage.append("            align-items: center;\n");
        htmlMessage.append("            gap: 8px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .info-row {\n");
        htmlMessage.append("            display: flex;\n");
        htmlMessage.append("            margin-bottom: 10px;\n");
        htmlMessage.append("            flex-wrap: wrap;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .info-label {\n");
        htmlMessage.append("            width: 160px;\n");
        htmlMessage.append("            color: #555555;\n");
        htmlMessage.append("            font-weight: 600;\n");
        htmlMessage.append("            font-size: 13px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .info-value {\n");
        htmlMessage.append("            flex: 1;\n");
        htmlMessage.append("            color: #222222;\n");
        htmlMessage.append("            font-weight: 500;\n");
        htmlMessage.append("            font-size: 13px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        table {\n");
        htmlMessage.append("            width: 100%;\n");
        htmlMessage.append("            border-collapse: collapse;\n");
        htmlMessage.append("            margin-top: 10px;\n");
        htmlMessage.append("            font-size: 12px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        th {\n");
        htmlMessage.append("            background: #003366;\n");
        htmlMessage.append("            color: #FFFFFF;\n");
        htmlMessage.append("            padding: 10px;\n");
        htmlMessage.append("            text-align: center;\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        td {\n");
        htmlMessage.append("            padding: 8px 10px;\n");
        htmlMessage.append("            text-align: center;\n");
        htmlMessage.append("            border-bottom: 1px solid #E0E0E0;\n");
        htmlMessage.append("            background-color: #FFFFFF;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .badge-success {\n");
        htmlMessage.append("            background: #D4EDDA;\n");
        htmlMessage.append("            color: #155724;\n");
        htmlMessage.append("            padding: 4px 12px;\n");
        htmlMessage.append("            border-radius: 20px;\n");
        htmlMessage.append("            font-size: 11px;\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            display: inline-block;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .badge-warning {\n");
        htmlMessage.append("            background: #FFF3CD;\n");
        htmlMessage.append("            color: #856404;\n");
        htmlMessage.append("            padding: 4px 12px;\n");
        htmlMessage.append("            border-radius: 20px;\n");
        htmlMessage.append("            font-size: 11px;\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            display: inline-block;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .badge-info {\n");
        htmlMessage.append("            background: #D1ECF1;\n");
        htmlMessage.append("            color: #0C5460;\n");
        htmlMessage.append("            padding: 4px 12px;\n");
        htmlMessage.append("            border-radius: 20px;\n");
        htmlMessage.append("            font-size: 11px;\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            display: inline-block;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .alert-success {\n");
        htmlMessage.append("            background: #D4EDDA;\n");
        htmlMessage.append("            border-left: 4px solid #28A745;\n");
        htmlMessage.append("            padding: 15px;\n");
        htmlMessage.append("            border-radius: 8px;\n");
        htmlMessage.append("            margin-top: 20px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .alert-warning {\n");
        htmlMessage.append("            background: #FFF3CD;\n");
        htmlMessage.append("            border-left: 4px solid #FFC107;\n");
        htmlMessage.append("            padding: 15px;\n");
        htmlMessage.append("            border-radius: 8px;\n");
        htmlMessage.append("            margin-top: 20px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .alert-title {\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            font-size: 14px;\n");
        htmlMessage.append("            margin-bottom: 5px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .footer {\n");
        htmlMessage.append("            background: #F5F7FA;\n");
        htmlMessage.append("            padding: 20px;\n");
        htmlMessage.append("            text-align: center;\n");
        htmlMessage.append("            font-size: 11px;\n");
        htmlMessage.append("            color: #888888;\n");
        htmlMessage.append("            border-top: 1px solid #E0E0E0;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .footer p {\n");
        htmlMessage.append("            margin: 5px 0;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .action-button {\n");
        htmlMessage.append("            display: inline-block;\n");
        htmlMessage.append("            background: linear-gradient(135deg, #003366 0%, #0052A5 100%);\n");
        htmlMessage.append("            color: #FFFFFF;\n");
        htmlMessage.append("            text-decoration: none;\n");
        htmlMessage.append("            padding: 12px 28px;\n");
        htmlMessage.append("            border-radius: 8px;\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            margin: 15px 0;\n");
        htmlMessage.append("            text-align: center;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        .quantity-highlight {\n");
        htmlMessage.append("            font-weight: bold;\n");
        htmlMessage.append("            font-size: 14px;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("        hr {\n");
        htmlMessage.append("            border: none;\n");
        htmlMessage.append("            border-top: 1px solid #E0E0E0;\n");
        htmlMessage.append("            margin: 20px 0;\n");
        htmlMessage.append("        }\n");
        htmlMessage.append("    </style>\n");
        htmlMessage.append("</head>\n");
        htmlMessage.append("<body>\n");
        htmlMessage.append("    <div class=\"container\">\n");
        htmlMessage.append("        <div class=\"header\">\n");
        htmlMessage.append("            <div class=\"logo\">LEONI<span>|Quality</span></div>\n");
        htmlMessage.append("            <h1>📦 RÉCEPTION DE MARCHANDISES</h1>\n");
        htmlMessage.append("        </div>\n");
        htmlMessage.append("        <div class=\"content\">\n");

        // Message d'accueil
        htmlMessage.append("            <div class=\"greeting\">\n");
        htmlMessage.append("                <p><strong>Bonjour,</strong></p>\n");
        htmlMessage.append("                <p>Une réception de marchandises vient d'être enregistrée dans le système LEONI Quality Management.</p>\n");
        htmlMessage.append("                <p>Veuillez trouver ci-dessous le détail des produits reçus.</p>\n");
        htmlMessage.append("            </div>\n");

        // Informations générales
        htmlMessage.append("            <div class=\"info-section\">\n");
        htmlMessage.append("                <div class=\"section-title\">\n");
        htmlMessage.append("                    <span>📄</span> INFORMATIONS GÉNÉRALES\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <div class=\"info-row\">\n");
        htmlMessage.append("                    <div class=\"info-label\">N° Cahier des charges :</div>\n");
        htmlMessage.append("                    <div class=\"info-value\"><strong>").append(escapeHtml(sheet.getOrderNumber())).append("</strong></div>\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <div class=\"info-row\">\n");
        htmlMessage.append("                    <div class=\"info-label\">Projet :</div>\n");
        htmlMessage.append("                    <div class=\"info-value\">").append(escapeHtml(sheet.getProject())).append("</div>\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <div class=\"info-row\">\n");
        htmlMessage.append("                    <div class=\"info-label\">Site de production :</div>\n");
        htmlMessage.append("                    <div class=\"info-value\">").append(escapeHtml(sheet.getPlant())).append("</div>\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <div class=\"info-row\">\n");
        htmlMessage.append("                    <div class=\"info-label\">N° Bon de livraison :</div>\n");
        htmlMessage.append("                    <div class=\"info-value\"><strong>").append(escapeHtml(request.getDeliveryNoteNumber())).append("</strong></div>\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <div class=\"info-row\">\n");
        htmlMessage.append("                    <div class=\"info-label\">Date de réception :</div>\n");
        htmlMessage.append("                    <div class=\"info-value\">").append(formatDate(request.getReceptionDate())).append("</div>\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <div class=\"info-row\">\n");
        htmlMessage.append("                    <div class=\"info-label\">Réceptionné par :</div>\n");
        htmlMessage.append("                    <div class=\"info-value\">").append(escapeHtml(currentUser.getEmail())).append("</div>\n");
        htmlMessage.append("                </div>\n");
        if (request.getComments() != null && !request.getComments().isEmpty()) {
            htmlMessage.append("                <div class=\"info-row\">\n");
            htmlMessage.append("                    <div class=\"info-label\">Commentaires :</div>\n");
            htmlMessage.append("                    <div class=\"info-value\"><em>").append(escapeHtml(request.getComments())).append("</em></div>\n");
            htmlMessage.append("                </div>\n");
        }
        htmlMessage.append("            </div>\n");

        // Détails des items reçus
        htmlMessage.append("            <div class=\"info-section\">\n");
        htmlMessage.append("                <div class=\"section-title\">\n");
        htmlMessage.append("                    <span>📦</span> DÉTAIL DES ARTICLES REÇUS\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <table>\n");
        htmlMessage.append("                    <thead>\n");
        htmlMessage.append("                        <tr>\n");
        htmlMessage.append("                            <th>Article N°</th>\n");
        htmlMessage.append("                            <th>Quantité reçue</th>\n");
        htmlMessage.append("                            <th>Total avant</th>\n");
        htmlMessage.append("                            <th>Total après</th>\n");
        htmlMessage.append("                            <th>Quantité commandée</th>\n");
        htmlMessage.append("                            <th>Statut</th>\n");
        htmlMessage.append("                        </tr>\n");
        htmlMessage.append("                    </thead>\n");
        htmlMessage.append("                    <tbody>\n");

        for (ReceptionHistory history : newHistories) {
            String status = history.getNewTotalReceived() >= history.getQuantityOrdered() ?
                    "<span class='badge-success'>✓ COMPLET</span>" :
                    "<span class='badge-warning'>⏳ PARTIEL (" + (history.getQuantityOrdered() - history.getNewTotalReceived()) + " restant)</span>";

            htmlMessage.append("                        <tr>\n");
            htmlMessage.append("                            <td><strong>").append(escapeHtml(history.getItem().getItemNumber())).append("</strong></td>\n");
            htmlMessage.append("                            <td><span class='quantity-highlight' style='color: #28A745;'>+ ").append(history.getQuantityReceived()).append("</span></td>\n");
            htmlMessage.append("                            <td>").append(history.getPreviousTotalReceived()).append("</td>\n");
            htmlMessage.append("                            <td><strong>").append(history.getNewTotalReceived()).append("</strong></td>\n");
            htmlMessage.append("                            <td>").append(history.getQuantityOrdered()).append("</td>\n");
            htmlMessage.append("                            <td>").append(status).append("</td>\n");
            htmlMessage.append("                        </tr>\n");
        }
        htmlMessage.append("                    </tbody>\n");
        htmlMessage.append("                </table>\n");
        htmlMessage.append("            </div>\n");

        // Récapitulatif complet
        htmlMessage.append("            <div class=\"info-section\">\n");
        htmlMessage.append("                <div class=\"section-title\">\n");
        htmlMessage.append("                    <span>📊</span> RÉCAPITULATIF COMPLET PAR ARTICLE\n");
        htmlMessage.append("                </div>\n");
        htmlMessage.append("                <table>\n");
        htmlMessage.append("                    <thead>\n");
        htmlMessage.append("                        <tr>\n");
        htmlMessage.append("                            <th>Article N°</th>\n");
        htmlMessage.append("                            <th>Quantité commandée</th>\n");
        htmlMessage.append("                            <th>Total reçu</th>\n");
        htmlMessage.append("                            <th>Restant à recevoir</th>\n");
        htmlMessage.append("                            <th>Statut</th>\n");
        htmlMessage.append("                        </tr>\n");
        htmlMessage.append("                    </thead>\n");
        htmlMessage.append("                    <tbody>\n");

        for (ChargeSheetItem item : sheet.getItems()) {
            int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
            int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;
            int remaining = quantityOrdered - totalReceived;

            String status;
            if (remaining == 0) {
                status = "<span class='badge-success'>✓ COMPLET</span>";
            } else if (totalReceived > 0) {
                status = "<span class='badge-warning'>⏳ PARTIEL</span>";
            } else {
                status = "<span class='badge-info'>⏳ EN ATTENTE</span>";
            }

            htmlMessage.append("                        <tr>\n");
            htmlMessage.append("                            <td><strong>").append(escapeHtml(item.getItemNumber())).append("</strong></td>\n");
            htmlMessage.append("                            <td>").append(quantityOrdered).append("</td>\n");
            htmlMessage.append("                            <td><strong>").append(totalReceived).append("</strong></td>\n");
            htmlMessage.append("                            <td>").append(remaining).append("</td>\n");
            htmlMessage.append("                            <td>").append(status).append("</td>\n");
            htmlMessage.append("                        </tr>\n");
        }
        htmlMessage.append("                    </tbody>\n");
        htmlMessage.append("                </table>\n");
        htmlMessage.append("            </div>\n");

        // Message de statut global
        boolean allComplete = totalReceivedMap.entrySet().stream().allMatch(entry -> {
            ChargeSheetItem item = itemRepository.findById(entry.getKey()).orElse(null);
            return item != null && entry.getValue() >= (item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0);
        });

        if (allComplete) {
            htmlMessage.append("            <div class=\"alert-success\">\n");
            htmlMessage.append("                <div class=\"alert-title\">✅ RÉCEPTION COMPLÈTE</div>\n");
            htmlMessage.append("                <p>Tous les articles commandés ont été reçus.</p>\n");
            htmlMessage.append("                <p>Le cahier des charges passe automatiquement en statut <strong>RECEIVED_FROM_SUPPLIER</strong>.</p>\n");
            htmlMessage.append("            </div>\n");
        } else {
            int remainingItems = 0;
            int totalRemaining = 0;
            for (ChargeSheetItem item : sheet.getItems()) {
                int totalReceived = totalReceivedMap.getOrDefault(item.getId(), 0);
                int quantityOrdered = item.getQuantityOfTestModules() != null ? item.getQuantityOfTestModules() : 0;
                if (totalReceived < quantityOrdered) {
                    remainingItems++;
                    totalRemaining += (quantityOrdered - totalReceived);
                }
            }
            htmlMessage.append("            <div class=\"alert-warning\">\n");
            htmlMessage.append("                <div class=\"alert-title\">⚠️ RÉCEPTION PARTIELLE</div>\n");
            htmlMessage.append("                <p>Il reste <strong>").append(totalRemaining).append(" article(s)</strong> à recevoir répartis sur <strong>").append(remainingItems).append(" référence(s)</strong>.</p>\n");
            htmlMessage.append("                <p>Une nouvelle notification sera envoyée lors de la prochaine réception.</p>\n");
            htmlMessage.append("            </div>\n");
        }

        // Bouton d'action
        htmlMessage.append("            <div style=\"text-align: center;\">\n");
        htmlMessage.append("                <a href=\"https://leoni-quality.com/charge-sheets/").append(sheet.getId()).append("/receptions\" class=\"action-button\">\n");
        htmlMessage.append("                    🔗 CONSULTER L'HISTORIQUE DES RÉCEPTIONS\n");
        htmlMessage.append("                </a>\n");
        htmlMessage.append("            </div>\n");

        htmlMessage.append("            <hr>\n");
        htmlMessage.append("            <div style=\"font-size: 12px; color: #666; background: #F8F9FC; padding: 12px; border-radius: 8px;\">\n");
        htmlMessage.append("                <strong>📌 À noter :</strong><br>\n");
        htmlMessage.append("                • Les fiches de conformité doivent être créées pour chaque article reçu<br>\n");
        htmlMessage.append("                • Les articles partiellement reçus feront l'objet d'un suivi automatique<br>\n");
        htmlMessage.append("                • En cas de non-conformité, veuillez créer une réclamation\n");
        htmlMessage.append("            </div>\n");

        htmlMessage.append("        </div>\n");
        htmlMessage.append("        <div class=\"footer\">\n");
        htmlMessage.append("            <p><strong>LEONI Wiring Systems</strong> - Quality Management System</p>\n");
        htmlMessage.append("            <p>Cet email est généré automatiquement, merci de ne pas y répondre.</p>\n");
        htmlMessage.append("            <p>© 2026 LEONI Group - Tous droits réservés</p>\n");
        htmlMessage.append("        </div>\n");
        htmlMessage.append("    </div>\n");
        htmlMessage.append("</body>\n");
        htmlMessage.append("</html>\n");

        // Envoyer la notification
        notificationService.sendHtmlNotificationToProjectAndSiteUsers(subject, htmlMessage.toString(), sheet.getProject(), sheet.getPlant());
    }
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "Non spécifiée";
        return dateStr;
    }
    /**
     * Récupère l'historique des réceptions
     */
    public List<ReceptionHistoryDto> getReceptionHistoryDto(Long sheetId) {
        List<ReceptionHistory> histories = receptionHistoryRepository.findByChargeSheetIdOrderByCreatedAtDesc(sheetId);

        return histories.stream().map(history -> {
            ReceptionHistoryDto.ItemInfoDto itemInfo = ReceptionHistoryDto.ItemInfoDto.builder()
                    .id(history.getItem().getId())
                    .itemNumber(history.getItem().getItemNumber())
                    .build();

            return ReceptionHistoryDto.builder()
                    .id(history.getId())
                    .item(itemInfo)
                    .quantityReceived(history.getQuantityReceived())
                    .previousTotalReceived(history.getPreviousTotalReceived())
                    .newTotalReceived(history.getNewTotalReceived())
                    .quantityOrdered(history.getQuantityOrdered())
                    .deliveryNoteNumber(history.getDeliveryNoteNumber())
                    .receptionDate(history.getReceptionDate())
                    .receivedBy(history.getReceivedBy())
                    .comments(history.getComments())
                    .createdAt(history.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
// Dans ChargeSheetService.java - Modifiez les méthodes pour l'admin

    /**
     * Calcule la variation entre deux mois spécifiques
     */
    public Map<String, Object> getVariationBetweenMonths(String project, String month1, String month2) {
        List<Object[]> results;

        // Si l'utilisateur est ADMIN ou project = null, on prend tous les projets
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty())) {
            // Admin voit tous les projets
            results = repository.countByMonthForAllProjects();
        } else {
            String userProjectsString = currentUser.getProjetsNames();
            List<String> userProjects = userProjectsString != null ?
                    Arrays.asList(userProjectsString.split(", ")) :
                    List.of();
            // ✅ Si projet spécifié, vérifier qu'il est dans les projets de l'utilisateur
            String targetProject = project;
            if (targetProject == null || targetProject.isEmpty()) {
                // Si pas de projet spécifié, prendre le premier projet de l'utilisateur
                targetProject = userProjects.isEmpty() ? null : userProjects.get(0);
            }
            // Utilisateur normal ou admin avec projet spécifique
            if (targetProject == null) {
                return new HashMap<>();
            }

            results = repository.countByMonthForProject(targetProject);
        }

        Map<String, Long> monthlyCounts = new HashMap<>();
        for (Object[] result : results) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            monthlyCounts.put(month, count);
        }

        Long countMonth1 = monthlyCounts.getOrDefault(month1, 0L);
        Long countMonth2 = monthlyCounts.getOrDefault(month2, 0L);

        double variation = 0.0;
        String trend = "stable";
        String formula = "";

        if (countMonth1 > 0) {
            variation = ((countMonth2 - countMonth1) * 100.0) / countMonth1;
            variation = Math.round(variation * 10.0) / 10.0;

            if (variation > 0) {
                trend = "hausse";
            } else if (variation < 0) {
                trend = "baisse";
            }

            formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                    countMonth2, countMonth1, countMonth1, variation);
        } else if (countMonth1 == 0 && countMonth2 > 0) {
            variation = 100.0;
            trend = "hausse";
            formula = String.format("((%d - %d) / %d) × 100 = %.1f%% (création depuis zéro)",
                    countMonth2, countMonth1, 1, variation);
        } else if (countMonth1 == 0 && countMonth2 == 0) {
            formula = "Aucune création sur les deux mois";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("project", isAdmin && (project == null || project.isEmpty()) ? "TOUS_PROJETS" : project);
        result.put("month1", month1);
        result.put("month1Count", countMonth1);
        result.put("month2", month2);
        result.put("month2Count", countMonth2);
        result.put("variation", variation);
        result.put("trend", trend);
        result.put("formula", formula);

        return result;
    }

    /**
     * Récupère les statistiques de création mensuelles
     */
    public MonthlyStatsDto getMonthlyCreationStats(String project, int numberOfMonths) {
        List<Object[]> results;

        // Si l'utilisateur est ADMIN ou project = null, on prend tous les projets
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty())) {
            // Admin voit tous les projets
            results = repository.countByMonthForAllProjects();
        } else {
            // Utilisateur normal ou admin avec projet spécifique
            String userProjectsString = currentUser.getProjetsNames();
            List<String> userProjects = userProjectsString != null ?
                    Arrays.asList(userProjectsString.split(", ")) :
                    List.of();

            String targetProject = project;
            if (targetProject == null || targetProject.isEmpty()) {
                targetProject = userProjects.isEmpty() ? null : userProjects.get(0);
            }
            if (targetProject == null) {
                return MonthlyStatsDto.builder()
                        .monthlyCounts(new LinkedHashMap<>())
                        .monthlyVariations(new LinkedHashMap<>())
                        .build();
            }

            results = repository.countByMonthForProject(targetProject);
        }

        Map<String, Long> monthlyCounts = new LinkedHashMap<>();
        Map<String, Double> monthlyVariations = new LinkedHashMap<>();

        // Transformer les résultats en Map
        for (Object[] result : results) {
            String month = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            monthlyCounts.put(month, count);
        }

        // Calculer les variations
        List<String> months = new ArrayList<>(monthlyCounts.keySet());
        for (int i = 0; i < months.size() - 1; i++) {
            String currentMonth = months.get(i);
            String previousMonth = months.get(i + 1);

            Long currentCount = monthlyCounts.get(currentMonth);
            Long previousCount = monthlyCounts.get(previousMonth);

            if (previousCount != null && previousCount > 0) {
                double variation = ((currentCount - previousCount) * 100.0) / previousCount;
                monthlyVariations.put(currentMonth, Math.round(variation * 10.0) / 10.0);
            } else if (previousCount != null && previousCount == 0) {
                monthlyVariations.put(currentMonth, currentCount > 0 ? 100.0 : 0.0);
            } else {
                monthlyVariations.put(currentMonth, 0.0);
            }
        }

        // Calculer la variation pour les deux derniers mois
        String currentMonth = null;
        String previousMonth = null;
        double variationPercentage = 0.0;
        String trend = "stable";

        if (months.size() >= 2) {
            currentMonth = months.get(0);
            previousMonth = months.get(1);
            variationPercentage = monthlyVariations.getOrDefault(currentMonth, 0.0);

            if (variationPercentage > 0) {
                trend = "hausse";
            } else if (variationPercentage < 0) {
                trend = "baisse";
            }
        }

        // Limiter au nombre de mois demandé
        Map<String, Long> limitedMonthlyCounts = new LinkedHashMap<>();
        Map<String, Double> limitedMonthlyVariations = new LinkedHashMap<>();

        int count = 0;
        for (Map.Entry<String, Long> entry : monthlyCounts.entrySet()) {
            if (count >= numberOfMonths) break;
            limitedMonthlyCounts.put(entry.getKey(), entry.getValue());
            if (monthlyVariations.containsKey(entry.getKey())) {
                limitedMonthlyVariations.put(entry.getKey(), monthlyVariations.get(entry.getKey()));
            }
            count++;
        }

        String formula = "";
        if (currentMonth != null && previousMonth != null) {
            Long currentCount = monthlyCounts.get(currentMonth);
            Long prevCount = monthlyCounts.get(previousMonth);
            if (prevCount != null && prevCount > 0) {
                formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                        currentCount, prevCount, prevCount, variationPercentage);
            }
        }

        return MonthlyStatsDto.builder()
                .monthlyCounts(limitedMonthlyCounts)
                .monthlyVariations(limitedMonthlyVariations)
                .currentMonth(currentMonth)
                .previousMonth(previousMonth)
                .variationPercentage(variationPercentage)
                .trend(trend)
                .formula(formula)
                .build();
    }

    /**
     * Version simplifiée pour le dashboard avec les deux derniers mois
     */
    public Map<String, Object> getLastTwoMonthsVariation(String project) {
        List<Object[]> results;

        // Si l'utilisateur est ADMIN ou project = null, on prend tous les projets
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin && (project == null || project.isEmpty())) {
            // Admin voit tous les projets
            results = repository.countByMonthForAllProjects();
        } else {
            String userProjectsString = currentUser.getProjetsNames();
            List<String> userProjects = userProjectsString != null ?
                    Arrays.asList(userProjectsString.split(", ")) :
                    List.of();

            String targetProject = project;
            if (targetProject == null || targetProject.isEmpty()) {
                targetProject = userProjects.isEmpty() ? null : userProjects.get(0);
            }

            if (targetProject == null) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("message", "Aucun projet disponible");
                return emptyResult;
            }

            results = repository.countByMonthForProject(targetProject);
        }

        if (results == null || results.size() < 2) {
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("message", "Pas assez de données pour calculer la variation");
            emptyResult.put("availableMonths", results != null ? results.size() : 0);
            return emptyResult;
        }

        // Les résultats sont déjà triés par date DESC (du plus récent au plus ancien)
        Object[] latestMonth = results.get(0);     // Mois le plus récent (ex: Mars)
        Object[] previousMonth = results.get(1);   // Mois précédent (ex: Février)

        String monthLatest = (String) latestMonth[0];
        Long countLatest = ((Number) latestMonth[1]).longValue();
        String monthPrevious = (String) previousMonth[0];
        Long countPrevious = ((Number) previousMonth[1]).longValue();

        double variation = 0.0;
        String trend = "stable";
        String formula = "";

        if (countPrevious > 0) {
            variation = ((countLatest - countPrevious) * 100.0) / countPrevious;
            variation = Math.round(variation * 10.0) / 10.0;

            if (variation > 0) {
                trend = "hausse";
            } else if (variation < 0) {
                trend = "baisse";
            }

            formula = String.format("((%d - %d) / %d) × 100 = %.1f%%",
                    countLatest, countPrevious, countPrevious, variation);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentMonth", monthLatest);
        result.put("currentMonthCount", countLatest);
        result.put("previousMonth", monthPrevious);
        result.put("previousMonthCount", countPrevious);
        result.put("variation", variation);
        result.put("trend", trend);
        result.put("formula", formula);
        result.put("interpretation", getInterpretation(trend, variation, countLatest, countPrevious));
        result.put("isGlobalView", isAdmin && (project == null || project.isEmpty()));

        return result;
    }

    private String getInterpretation(String trend, double variation, long currentCount, long previousCount) {
        if (trend.equals("hausse")) {
            if (variation > 50) {
                return String.format("📈 Forte augmentation de %.1f%% (%d vs %d)", variation, currentCount, previousCount);
            } else {
                return String.format("📈 Augmentation de %.1f%% (%d vs %d)", variation, currentCount, previousCount);
            }
        } else if (trend.equals("baisse")) {
            if (variation < -50) {
                return String.format("📉 Forte baisse de %.1f%% (%d vs %d)", Math.abs(variation), currentCount, previousCount);
            } else {
                return String.format("📉 Baisse de %.1f%% (%d vs %d)", Math.abs(variation), currentCount, previousCount);
            }
        } else {
            return String.format("➡️ Stable (%d créations pour les deux mois)", currentCount);
        }
    }
    // ChargeSheetService.java - Modifier la méthode

    // ChargeSheetService.java - Version sans champ en base

    // ChargeSheetService.java - Corriger la méthode revertToIng

    @Transactional
    public ChargeSheet revertToIng(Long sheetId, String reason) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("Veuillez indiquer la raison du retour à ING");
        }

        ChargeSheet chargeSheet = repository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Charge sheet not found"));

        // Vérifier que l'utilisateur est PT ou ADMIN
        if (!currentUser.getRole().name().equals("PT") && !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Seul PT ou ADMIN peut retourner un cahier à ING");
        }

        // Vérifier que le statut actuel est VALIDATED_ING ou TECH_FILLED
        if (chargeSheet.getStatus() != ChargeSheetStatus.VALIDATED_ING &&
                chargeSheet.getStatus() != ChargeSheetStatus.TECH_FILLED) {
            throw new RuntimeException("Seul un cahier au statut VALIDATED_ING ou TECH_FILLED peut être retourné à ING");
        }

        // ✅ CORRECTION : Retourner au statut DRAFT (pas VALIDATED_ING)
        // Pour que ING puisse tout modifier (général + items)
        chargeSheet.setStatus(ChargeSheetStatus.DRAFT);
        chargeSheet.setUpdatedBy(currentUser.getEmail());
        chargeSheet.setUpdatedAt(LocalDate.now());

        // Remettre tous les items en DRAFT
        for (ChargeSheetItem item : chargeSheet.getItems()) {
            item.setItemStatus("DRAFT");
            itemRepository.save(item);
        }

        ChargeSheet reverted = repository.save(chargeSheet);

        // Notification avec la raison
        notificationService.notifyChargeSheetUpdatedToProjectAndSite(
                sheetId,
                "Retourné à ING par PT pour corrections. Raison: " + reason + " - Le cahier est maintenant en mode BROUILLON",
                currentUser.getEmail(),
                "PT",
                chargeSheet.getProject(),
                chargeSheet.getPlant()
        );

        return reverted;
    }
    /**
     * Récupère les statistiques de réception
     */
    public ReceptionStatisticsDto getReceptionStatistics(String project, String site, int numberOfMonths) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String userRole = currentUser.getRole().name();
        String userSite = currentUser.getSiteName();
        String userProjectsString = currentUser.getProjetsNames();
        List<String> userProjects = userProjectsString != null ?
                Arrays.asList(userProjectsString.split(", ")) :
                List.of();

        // ✅ Définir la période (derniers X mois)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(numberOfMonths).withDayOfMonth(1);

        // ✅ Filtrer par projet et site selon les droits
        String effectiveProject = project;
        String effectiveSite = site;

        if (!userRole.equals("ADMIN")) {
            // Non-admin: filtrer par ses projets et son site
            if (effectiveProject == null || effectiveProject.isEmpty()) {
                effectiveProject = userProjects.isEmpty() ? null : userProjects.get(0);
            }
            effectiveSite = userSite;
        }

        // ✅ Récupérer les stats mensuelles
        List<Object[]> monthlyResults;
        if (userRole.equals("ADMIN") && (project == null || project.isEmpty())) {
            monthlyResults = receptionHistoryRepository.getMonthlyReceptionStats(startDate, endDate);
        } else {
            monthlyResults = receptionHistoryRepository.getMonthlyReceptionStatsFiltered(
                    startDate, endDate, effectiveProject, effectiveSite);
        }

        // ✅ Construire les stats mensuelles
        Map<String, ReceptionStatisticsDto.MonthlyReceptionStats> monthlyStatsMap = new LinkedHashMap<>();
        List<String> months = new ArrayList<>();
        List<Long> receivedData = new ArrayList<>();
        List<Long> orderedData = new ArrayList<>();

        for (Object[] result : monthlyResults) {
            int year = (int) result[0];
            int month = (int) result[1];
            Long quantityReceived = ((Number) result[2]).longValue();

            String monthKey = String.format("%d-%02d", year, month);
            String monthLabel = getMonthLabel(month, year);

            // Récupérer la quantité commandée pour ce mois
            Long quantityOrdered = getOrderedQuantityForMonth(year, month, effectiveProject, effectiveSite);

            ReceptionStatisticsDto.MonthlyReceptionStats stats = ReceptionStatisticsDto.MonthlyReceptionStats.builder()
                    .month(monthLabel)
                    .year(year)
                    .quantityReceived(quantityReceived)
                    .quantityOrdered(quantityOrdered)
                    .completionRate(quantityOrdered > 0 ? (quantityReceived * 100.0 / quantityOrdered) : 0)
                    .numberOfReceptions(((Number) result[3]).intValue())
                    .build();

            monthlyStatsMap.put(monthKey, stats);
            months.add(monthLabel);
            receivedData.add(quantityReceived);
            orderedData.add(quantityOrdered);
        }

        // ✅ Récupérer les stats par item
        List<ReceptionStatisticsDto.ItemReceptionStats> itemStats = getItemReceptionStats(effectiveProject, effectiveSite);

        // ✅ Calculer les totaux globaux
        Long totalReceived = receptionHistoryRepository.getTotalQuantityReceived();
        Long totalOrdered = getTotalOrderedQuantity(effectiveProject, effectiveSite);

        // ✅ Construire les données du graphique
        List<Double> percentages = new ArrayList<>();
        for (int i = 0; i < receivedData.size(); i++) {
            double percent = orderedData.get(i) > 0 ? (receivedData.get(i) * 100.0 / orderedData.get(i)) : 0;
            percentages.add(Math.round(percent * 10.0) / 10.0);
        }

        ReceptionStatisticsDto.ChartData chartData = ReceptionStatisticsDto.ChartData.builder()
                .labels(months)
                .receivedData(receivedData)
                .orderedData(orderedData)
                .percentages(percentages)
                .build();

        return ReceptionStatisticsDto.builder()
                .totalQuantityReceived(totalReceived)
                .totalQuantityOrdered(totalOrdered)
                .completionRate(totalOrdered > 0 ? (totalReceived * 100.0 / totalOrdered) : 0)
                .numberOfItems(itemStats.size())
                .numberOfSheets(getNumberOfSheetsWithReceptions(effectiveProject, effectiveSite))
                .monthlyStats(monthlyStatsMap)
                .itemStats(itemStats)
                .chartData(chartData)
                .build();
    }

    /**
     * Récupère la quantité commandée pour un mois spécifique
     */
    private Long getOrderedQuantityForMonth(int year, int month, String project, String site) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        String jpql = """
        SELECT COALESCE(SUM(i.quantityOfTestModules), 0)
        FROM ChargeSheetItem i
        WHERE i.createdAt BETWEEN :startDate AND :endDate
        AND i.chargeSheet.status = 'SENT_TO_SUPPLIER'
    """;

        if (project != null && !project.isEmpty()) {
            jpql += " AND i.chargeSheet.project = :project";
        }
        if (site != null && !site.isEmpty()) {
            jpql += " AND i.chargeSheet.plant = :site";
        }

        jakarta.persistence.Query query = entityManager.createQuery(jpql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        if (project != null && !project.isEmpty()) {
            query.setParameter("project", project);
        }
        if (site != null && !site.isEmpty()) {
            query.setParameter("site", site);
        }

        return (Long) query.getSingleResult();
    }

    /**
     * Récupère les statistiques par item
     */
    private List<ReceptionStatisticsDto.ItemReceptionStats> getItemReceptionStats(String project, String site) {
        String jpql = """
        SELECT 
            i.id,
            i.itemNumber,
            i.chargeSheet.project,
            i.chargeSheet.plant,
            i.quantityOfTestModules,
            COALESCE(SUM(r.quantityReceived), 0)
        FROM ChargeSheetItem i
        LEFT JOIN ReceptionHistory r ON r.item.id = i.id
        WHERE i.chargeSheet.status = 'SENT_TO_SUPPLIER'
    """;

        if (project != null && !project.isEmpty()) {
            jpql += " AND i.chargeSheet.project = :project";
        }
        if (site != null && !site.isEmpty()) {
            jpql += " AND i.chargeSheet.plant = :site";
        }

        jpql += " GROUP BY i.id, i.itemNumber, i.chargeSheet.project, i.chargeSheet.plant, i.quantityOfTestModules";
        jpql += " ORDER BY COALESCE(SUM(r.quantityReceived), 0) DESC";

        jakarta.persistence.Query query = entityManager.createQuery(jpql);

        if (project != null && !project.isEmpty()) {
            query.setParameter("project", project);
        }
        if (site != null && !site.isEmpty()) {
            query.setParameter("site", site);
        }

        List<Object[]> results = query.getResultList();
        List<ReceptionStatisticsDto.ItemReceptionStats> stats = new ArrayList<>();

        for (Object[] result : results) {
            Long itemId = (Long) result[0];
            String itemNumber = (String) result[1];
            String itemProject = (String) result[2];
            String itemSite = (String) result[3];
            Integer quantityOrdered = ((Number) result[4]).intValue();
            Integer quantityReceived = ((Number) result[5]).intValue();
            int pending = quantityOrdered - quantityReceived;

            String status;
            if (pending == 0) {
                status = "COMPLET";
            } else if (quantityReceived > 0) {
                status = "PARTIAL";
            } else {
                status = "PENDING";
            }

            stats.add(ReceptionStatisticsDto.ItemReceptionStats.builder()
                    .itemId(itemId)
                    .itemNumber(itemNumber)
                    .project(itemProject)
                    .plant(itemSite)
                    .quantityOrdered(quantityOrdered)
                    .quantityReceived(quantityReceived)
                    .completionRate(quantityOrdered > 0 ? (quantityReceived * 100.0 / quantityOrdered) : 0)
                    .pendingQuantity(pending)
                    .status(status)
                    .build());
        }

        return stats;
    }

    /**
     * Calcule la quantité totale commandée
     */
    private Long getTotalOrderedQuantity(String project, String site) {
        String jpql = """
        SELECT COALESCE(SUM(i.quantityOfTestModules), 0)
        FROM ChargeSheetItem i
        WHERE i.chargeSheet.status = 'SENT_TO_SUPPLIER'
    """;

        if (project != null && !project.isEmpty()) {
            jpql += " AND i.chargeSheet.project = :project";
        }
        if (site != null && !site.isEmpty()) {
            jpql += " AND i.chargeSheet.plant = :site";
        }

        jakarta.persistence.Query query = entityManager.createQuery(jpql);

        if (project != null && !project.isEmpty()) {
            query.setParameter("project", project);
        }
        if (site != null && !site.isEmpty()) {
            query.setParameter("site", site);
        }

        return (Long) query.getSingleResult();
    }

    /**
     * Compte le nombre de cahiers avec des réceptions
     */
    private Integer getNumberOfSheetsWithReceptions(String project, String site) {
        String jpql = """
        SELECT COUNT(DISTINCT r.chargeSheet.id)
        FROM ReceptionHistory r
        WHERE 1=1
    """;

        if (project != null && !project.isEmpty()) {
            jpql += " AND r.chargeSheet.project = :project";
        }
        if (site != null && !site.isEmpty()) {
            jpql += " AND r.chargeSheet.plant = :site";
        }

        jakarta.persistence.Query query = entityManager.createQuery(jpql);

        if (project != null && !project.isEmpty()) {
            query.setParameter("project", project);
        }
        if (site != null && !site.isEmpty()) {
            query.setParameter("site", site);
        }

        return ((Number) query.getSingleResult()).intValue();
    }

    /**
     * Formate le label du mois
     */
    private String getMonthLabel(int month, int year) {
        String[] monthNames = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
                "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        return monthNames[month - 1] + " " + year;
    }
}