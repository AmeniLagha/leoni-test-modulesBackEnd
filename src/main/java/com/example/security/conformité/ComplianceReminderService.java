package com.example.security.conformité;

import com.example.security.cahierdeCharge.*;
import com.example.security.email.GlobalNotificationService;
import com.example.security.reception.ReceptionHistory;
import com.example.security.reception.ReceptionHistoryRepository;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplianceReminderService {

    private final ChargeSheetRepository chargeSheetRepository;
    private final ChargeSheetItemRepository itemRepository;
    private final ReceptionHistoryRepository receptionHistoryRepository;
    private final ComplianceRepository complianceRepository;
    private final UserRepository userRepository;
    private final GlobalNotificationService notificationService;

    // Stockage pour suivre les rappels envoyés (pour éviter les doublons par jour)
    private final Map<String, LocalDate> reminderSentMap = new HashMap<>();

    /**
     * Vérifie si la quantité reçue a déjà des fiches de conformité
     */
    private boolean isAllReceivedQuantityHasCompliance(Long itemId) {
        int totalReceived = getTotalReceivedForItem(itemId);

        if (totalReceived == 0) {
            return true;
        }

        List<Compliance> compliances = complianceRepository.findByItemId(itemId);
        int totalCompliance = compliances.size();
        boolean isComplete = totalCompliance >= totalReceived;

        log.debug("Item {} - Reçu: {}, Conformités: {}, Complet: {}",
                itemId, totalReceived, totalCompliance, isComplete);

        return isComplete;
    }

    /**
     * Calcule la quantité qui n'a pas encore de fiche de conformité
     */
    private int getPendingComplianceQuantity(Long itemId) {
        int totalReceived = getTotalReceivedForItem(itemId);
        List<Compliance> compliances = complianceRepository.findByItemId(itemId);
        int totalCompliance = compliances.size();
        return Math.max(0, totalReceived - totalCompliance);
    }

    /**
     * Tâche programmée qui s'exécute tous les jours à 9h00
     */
    @Scheduled(cron = "0 49 11 * * *")
    @Transactional(readOnly = true)
    public void sendPendingComplianceReminders() {
        log.info("🔔 Démarrage de la vérification des rappels de conformité");

        // Récupérer tous les items qui ont des réceptions
        List<ReceptionHistory> allHistories = receptionHistoryRepository.findAll();
        Set<Long> itemIdsWithReceptions = allHistories.stream()
                .map(h -> h.getItem().getId())
                .collect(Collectors.toSet());

        int remindersSent = 0;

        for (Long itemId : itemIdsWithReceptions) {
            Optional<ChargeSheetItem> itemOpt = itemRepository.findById(itemId);
            if (itemOpt.isEmpty()) continue;

            ChargeSheetItem item = itemOpt.get();
            ChargeSheet sheet = item.getChargeSheet();

            // Vérifier si toute la quantité reçue a des fiches
            if (!isAllReceivedQuantityHasCompliance(item.getId())) {
                int pendingQuantity = getPendingComplianceQuantity(item.getId());
                int totalReceived = getTotalReceivedForItem(item.getId());
                int existingCompliances = totalReceived - pendingQuantity;

                boolean reminderSent = sendReminderForItem(sheet, item, pendingQuantity, existingCompliances);
                if (reminderSent) {
                    remindersSent++;
                }
            } else {
                resetRemindersForItem(item.getId());
                log.debug("Item #{} - Toutes les quantités reçues ont des fiches, pas de rappel", item.getItemNumber());
            }
        }

        log.info("✅ Vérification terminée. {} rappel(s) envoyé(s)", remindersSent);
    }

    /**
     * Envoie un rappel pour un item spécifique - UNIQUEMENT aux PP du même projet et site
     */
    private boolean sendReminderForItem(ChargeSheet sheet, ChargeSheetItem item,
                                        int pendingQuantity, int existingCompliances) {

        // Vérification supplémentaire avant d'envoyer
        if (isAllReceivedQuantityHasCompliance(item.getId())) {
            log.info("Item #{} - Plus de rappel nécessaire (toutes les quantités ont des fiches)", item.getItemNumber());
            resetRemindersForItem(item.getId());
            return false;
        }

        // Vérifier si nous devons envoyer un rappel aujourd'hui
        String reminderKey = item.getId() + "_reminder";
        LocalDate lastReminderSent = reminderSentMap.get(reminderKey);
        LocalDate today = LocalDate.now();

        if (lastReminderSent != null && lastReminderSent.equals(today)) {
            return false;
        }

        // Récupérer la date de la dernière réception
        LocalDate lastReceptionDate = getLastReceptionDateForItem(item.getId());
        if (lastReceptionDate == null) {
            return false;
        }

        // Calculer le nombre de jours depuis la dernière réception
        long daysSinceLastReception = java.time.temporal.ChronoUnit.DAYS.between(lastReceptionDate, today);

        // Ne pas envoyer de rappel avant 1 jour
        if (daysSinceLastReception < 1) {
            return false;
        }

        // Limiter à 14 jours de rappels
        if (daysSinceLastReception > 14) {
            log.info("Item #{} - Plus de 14 jours sans conformité, arrêt des rappels", item.getItemNumber());
            return false;
        }

        // ✅ Récupérer UNIQUEMENT les PP du projet ET du site (SANS FALLBACK)
        String projectName = sheet.getProject();
        String siteName = sheet.getPlant(); // Le plant = site

        log.info("🔍 Recherche des PP pour le projet '{}' et le site '{}'", projectName, siteName);

        List<User> ppUsers = userRepository.findPpUsersByProjectAndSite(projectName, siteName);

        // ✅ SI AUCUN PP TROUVÉ → PAS D'ENVOI (pas de fallback)
        if (ppUsers.isEmpty()) {
            log.warn("❌ Aucun PP trouvé pour le projet '{}' et site '{}'. Aucun email envoyé.", projectName, siteName);
            return false;
        }

        log.info("📧 Envoi du rappel à {} utilisateur(s) PP (uniquement ceux du projet {} et site {})",
                ppUsers.size(), projectName, siteName);

        // Construire et envoyer l'email
        String subject = buildReminderSubject(item, daysSinceLastReception);
        String htmlMessage = buildReminderHtml(sheet, item, pendingQuantity, existingCompliances, daysSinceLastReception);

        for (User ppUser : ppUsers) {
            notificationService.sendHtmlNotificationToOneUser(subject, htmlMessage, ppUser.getEmail());
            log.info("📧 Rappel envoyé à PP {} pour l'item #{} (Jour {}/14) - {} fiche(s) restante(s)",
                    ppUser.getEmail(), item.getItemNumber(), daysSinceLastReception, pendingQuantity);
        }

        reminderSentMap.put(reminderKey, today);
        return true;
    }

    /**
     * Calcule la quantité totale reçue pour un item
     */
    private int getTotalReceivedForItem(Long itemId) {
        List<ReceptionHistory> histories = receptionHistoryRepository.findByItemId(itemId);
        return histories.stream().mapToInt(ReceptionHistory::getQuantityReceived).sum();
    }

    /**
     * Récupère la date de la dernière réception
     */
    private LocalDate getLastReceptionDateForItem(Long itemId) {
        return receptionHistoryRepository.findByItemIdOrderByReceptionDateDesc(itemId)
                .stream()
                .findFirst()
                .map(ReceptionHistory::getReceptionDate)
                .orElse(null);
    }

    /**
     * Construit le sujet de l'email
     */
    private String buildReminderSubject(ChargeSheetItem item, long daysSinceLastReception) {
        String urgency = "";
        if (daysSinceLastReception >= 7) {
            urgency = "⚠️ URGENT - ";
        } else if (daysSinceLastReception >= 3) {
            urgency = "🔔 IMPORTANT - ";
        }
        return urgency + "Rappel: Créer les fiches de conformité - Item #" + item.getItemNumber();
    }

    /**
     * Construit le HTML de l'email
     */
    private String buildReminderHtml(ChargeSheet sheet, ChargeSheetItem item,
                                     int pendingQuantity, int existingCompliances,
                                     long daysSinceLastReception) {

        int totalReceived = pendingQuantity + existingCompliances;
        int daysRemaining = 14 - (int) daysSinceLastReception;

        String urgencyColor;
        String urgencyBadge;
        if (daysSinceLastReception >= 7) {
            urgencyColor = "#FF4444";
            urgencyBadge = "<span style='background: #FF4444; color: white; padding: 5px 15px; border-radius: 20px;'>⚠️ URGENT - " + pendingQuantity + " fiche(s) à créer immédiatement</span>";
        } else if (daysSinceLastReception >= 3) {
            urgencyColor = "#FFA500";
            urgencyBadge = "<span style='background: #FFA500; color: white; padding: 5px 15px; border-radius: 20px;'>🔔 IMPORTANT - " + pendingQuantity + " fiche(s) à créer</span>";
        } else {
            urgencyColor = "#00D4FF";
            urgencyBadge = "<span style='background: #00D4FF; color: white; padding: 5px 15px; border-radius: 20px;'>📋 Rappel - " + pendingQuantity + " fiche(s) à créer</span>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; background-color: #0A0E1A; margin: 0; padding: 20px; }\n");
        html.append(".container { max-width: 650px; margin: 0 auto; background: #0F1525; border-radius: 20px; overflow: hidden; border: 1px solid rgba(0, 212, 255, 0.2); }\n");
        html.append(".header { background: linear-gradient(135deg, #00D4FF, #0052CC); padding: 20px; text-align: center; }\n");
        html.append(".header h1 { color: white; margin: 0; font-size: 24px; }\n");
        html.append(".content { padding: 30px; }\n");
        html.append(".info-section { background: rgba(255, 255, 255, 0.05); border-radius: 12px; padding: 20px; margin-bottom: 20px; }\n");
        html.append(".info-section h3 { color: #00D4FF; margin-top: 0; margin-bottom: 15px; }\n");
        html.append(".info-row { display: flex; margin-bottom: 10px; }\n");
        html.append(".info-label { width: 180px; color: #888; font-weight: bold; }\n");
        html.append(".info-value { color: #E0E0E0; }\n");
        html.append(".urgent-box { background: ").append(urgencyColor).append("22; border-left: 4px solid ").append(urgencyColor).append("; padding: 15px; margin: 20px 0; border-radius: 8px; }\n");
        html.append(".progress-bar-bg { background: #1A2335; border-radius: 10px; height: 30px; overflow: hidden; margin: 15px 0; }\n");
        html.append(".progress-bar-fill { background: linear-gradient(90deg, #00D4FF, #0052CC); height: 100%; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; }\n");
        html.append(".action-button { background: linear-gradient(135deg, #00D4FF, #0052CC); color: white; padding: 12px 25px; text-decoration: none; border-radius: 8px; display: inline-block; margin-top: 15px; font-weight: bold; }\n");
        html.append(".footer { background: rgba(0, 0, 0, 0.3); padding: 15px; text-align: center; font-size: 12px; color: #666; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class=\"container\">\n");
        html.append("<div class=\"header\">\n");
        html.append("<h1>📋 CRÉATION DE FICHES DE CONFORMITÉ</h1>\n");
        html.append("</div>\n");
        html.append("<div class=\"content\">\n");

        html.append("<div style=\"text-align: center; margin-bottom: 20px;\">\n");
        html.append(urgencyBadge).append("\n");
        html.append("</div>\n");

        html.append("<div class=\"urgent-box\">\n");
        html.append("<strong>⚠️ Action requise (PP) :</strong> ").append(pendingQuantity).append(" module(s) ont été reçus mais n'ont pas encore de fiches de conformité.<br>\n");
        html.append("📅 <strong>Jour ").append(daysSinceLastReception).append("/14</strong> - ").append(daysRemaining).append(" jour(s) restant(s) avant fin des rappels\n");
        html.append("</div>\n");

        html.append("<div class=\"info-section\">\n");
        html.append("<h3>📋 Informations du cahier</h3>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Cahier N°:</span><span class=\"info-value\">").append(sheet.getOrderNumber()).append("</span></div>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Projet:</span><span class=\"info-value\">").append(sheet.getProject()).append("</span></div>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Plant/Site:</span><span class=\"info-value\">").append(sheet.getPlant()).append("</span></div>\n");
        html.append("</div>\n");

        html.append("<div class=\"info-section\">\n");
        html.append("<h3>🔧 Informations de l'item</h3>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Item N°:</span><span class=\"info-value\">").append(item.getItemNumber()).append("</span></div>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Réf. Leoni:</span><span class=\"info-value\">").append(item.getHousingReferenceLeoni() != null ? item.getHousingReferenceLeoni() : "-").append("</span></div>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Réf. Client:</span><span class=\"info-value\">").append(item.getHousingReferenceSupplierCustomer() != null ? item.getHousingReferenceSupplierCustomer() : "-").append("</span></div>\n");
        html.append("</div>\n");

        html.append("<div class=\"info-section\">\n");
        html.append("<h3>📊 Statut des fiches de conformité</h3>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Quantité totale reçue:</span><span class=\"info-value\"><strong>").append(totalReceived).append("</strong></span></div>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Fiches de conformité créées:</span><span class=\"info-value\"><strong style='color: #00FF88;'>").append(existingCompliances).append("</strong></span></div>\n");
        html.append("<div class=\"info-row\"><span class=\"info-label\">Fiches restant à créer:</span><span class=\"info-value\"><strong style='color: #FFA500;'>").append(pendingQuantity).append("</strong></span></div>\n");

        int percentageComplete = totalReceived > 0 ? (existingCompliances * 100 / totalReceived) : 0;
        html.append("<div class=\"progress-bar-bg\">\n");
        html.append("<div class=\"progress-bar-fill\" style=\"width: ").append(percentageComplete).append("%;\">").append(percentageComplete).append("%</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        html.append("<div style=\"text-align: center;\">\n");
        html.append("<a href='https://votre-societe.com/compliance/create?itemId=").append(item.getId()).append("' class='action-button'>\n");
        html.append("➕ CRÉER LES ").append(pendingQuantity).append(" FICHE(S) DE CONFORMITÉ\n");
        html.append("</a>\n");
        html.append("</div>\n");

        html.append("</div>\n");
        html.append("<div class=\"footer\">\n");
        html.append("<p>© 2026 LEONI Group - Système de Gestion des Cahiers des Charges</p>\n");
        html.append("<p>Ce rappel est automatique. Les rappels s'arrêteront automatiquement quand toutes les fiches seront créées.</p>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Réinitialise les rappels pour un item
     */
    public void resetRemindersForItem(Long itemId) {
        String reminderKey = itemId + "_reminder";
        reminderSentMap.remove(reminderKey);
        log.info("🔄 Rappels réinitialisés pour l'item #{}", itemId);
    }

    /**
     * Vérifie le statut d'un item (peut être appelé par API)
     */
    public Map<String, Object> getItemComplianceStatus(Long itemId) {
        int totalReceived = getTotalReceivedForItem(itemId);
        List<Compliance> compliances = complianceRepository.findByItemId(itemId);
        int totalCompliance = compliances.size();
        int pendingQuantity = Math.max(0, totalReceived - totalCompliance);
        boolean isComplete = pendingQuantity == 0;

        Map<String, Object> status = new HashMap<>();
        status.put("itemId", itemId);
        status.put("totalReceived", totalReceived);
        status.put("totalCompliance", totalCompliance);
        status.put("pendingQuantity", pendingQuantity);
        status.put("isComplete", isComplete);
        status.put("reminderActive", !isComplete && totalReceived > 0);

        return status;
    }
}