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
    @Scheduled(cron = "0 18 21 * * *" , zone = "Africa/Tunis")
    @Transactional(readOnly = true, timeout = 60)
    public void sendPendingComplianceReminders() {
        log.info("🔔 Démarrage de la vérification des rappels de conformité");
        long count = receptionHistoryRepository.count();
        log.info("📊 DB accessible - {} réceptions trouvées", count);
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
        String htmlMessage = buildReminderText(sheet, item, pendingQuantity, existingCompliances, daysSinceLastReception);

        for (User ppUser : ppUsers) {
            notificationService.sendNotificationToOneUser(subject, htmlMessage, ppUser.getEmail());
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
        return urgency + "Rappel: Créer les fiches de conformité - Item #" + item.getHousingReferenceSupplierCustomer();
    }

    /**
     * Construit l'email
     */
    private String buildReminderText(ChargeSheet sheet, ChargeSheetItem item,
                                     int pendingQuantity, int existingCompliances,
                                     long daysSinceLastReception) {

        int totalReceived = pendingQuantity + existingCompliances;

        StringBuilder text = new StringBuilder();

        // Objet
        text.append("Objet : Rappel - Création des fiches de conformité - Cahier ")
                .append(sheet.getOrderNumber()).append("\n\n");

        // Corps du message
        text.append("Bonjour,\n\n");
        text.append("Des modules réceptionnés pour l'item ci-dessous n'ont pas encore de fiches de conformité.\n\n");

        // Cahier des charges
        text.append("Cahier des charges :\n\n");
        text.append("N° commande : ").append(sheet.getOrderNumber()).append("\n");
        text.append("Projet : ").append(sheet.getProject()).append("\n");
        text.append("Site : ").append(sheet.getPlant()).append("\n\n");

        // Item concerné
        text.append("Item concerné :\n\n");
        text.append("N° item : ").append(item.getId()).append("\n");
        text.append("Réf. Leoni : ").append(item.getHousingReferenceLeoni() != null ? item.getHousingReferenceLeoni() : "-").append("\n");
        text.append("Réf. Client : ").append(item.getHousingReferenceSupplierCustomer() != null ? item.getHousingReferenceSupplierCustomer() : "-").append("\n\n");

        // État des fiches
        text.append("État des fiches :\n\n");
        text.append("Quantité totale reçue : ").append(totalReceived).append("\n");
        text.append("Fiches déjà créées : ").append(existingCompliances).append("\n");
        text.append("Fiches restantes à créer : ").append(pendingQuantity).append("\n\n");

        // Délai
        text.append("Délai : Jour ").append(daysSinceLastReception).append("/14\n\n");

        // Conclusion
        text.append("Merci de créer les fiches de conformité manquantes dès que possible.\n\n");
        text.append("Cordialement,\n\n");
        text.append("LEONI Tunisia\n");
        text.append("Gestion des Cahiers des Charges\n");

        return text.toString();
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