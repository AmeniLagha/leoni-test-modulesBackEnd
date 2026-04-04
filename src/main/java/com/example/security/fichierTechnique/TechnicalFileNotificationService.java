package com.example.security.fichierTechnique;

import com.example.security.email.GlobalNotificationService;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicalFileNotificationService {

    private final TechnicalFileItemRepository itemRepository;
    private final GlobalNotificationService notificationService;
    private final UserRepository userRepository;

    // Seuil de notification (30 jours = 1 mois)
    private static final int NOTIFICATION_THRESHOLD_DAYS = 15;

    /**
     * Récupère la dernière date de modification (update ou création)
     */
    private LocalDate getLastActivityDate(TechnicalFileItem item) {
        if (item.getUpdatedAt() != null) {
            return item.getUpdatedAt();
        }
        return item.getCreatedAt();
    }

    /**
     * Récupère le code X ou une valeur par défaut
     */
    private String getItemCode(TechnicalFileItem item) {
        if (item.getXCode() != null && !item.getXCode().trim().isEmpty()) {
            return item.getXCode();
        }
        if (item.getLeoniReferenceNumber() != null && !item.getLeoniReferenceNumber().trim().isEmpty()) {
            return item.getLeoniReferenceNumber();
        }
        return "Sans code";
    }

    /**
     * Récupère le projet à partir d'un item technique
     */
    private String getProjectFromItem(TechnicalFileItem item) {
        try {
            if (item.getChargeSheetItem() != null &&
                    item.getChargeSheetItem().getChargeSheet() != null) {
                String project = item.getChargeSheetItem().getChargeSheet().getProject();
                return project != null ? project : "Non défini";
            }
        } catch (Exception e) {
            log.error("Erreur récupération projet: {}", e.getMessage());
        }
        return "Non défini";
    }

    /**
     * Vérifie les items qui n'ont pas été modifiés depuis plus de 30 jours
     * Planifié tous les jours à 8h00
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void checkPendingModifications() {
        LocalDate thresholdDate = LocalDate.now().minusDays(NOTIFICATION_THRESHOLD_DAYS);

        List<TechnicalFileItem> allItems = itemRepository.findAll();

        // Grouper les items par projet
        Map<String, List<TechnicalFileItem>> itemsByProject = new HashMap<>();

        for (TechnicalFileItem item : allItems) {
            LocalDate lastActivity = getLastActivityDate(item);

            if (lastActivity != null && lastActivity.isBefore(thresholdDate)) {
                String project = getProjectFromItem(item);
                itemsByProject.computeIfAbsent(project, k -> new ArrayList<>()).add(item);
            }
        }

        if (itemsByProject.isEmpty()) {
            log.info("Aucun item sans modification depuis plus de {} jours", NOTIFICATION_THRESHOLD_DAYS);
            return;
        }

        // Envoyer les notifications par projet
        for (Map.Entry<String, List<TechnicalFileItem>> entry : itemsByProject.entrySet()) {
            String project = entry.getKey();
            List<TechnicalFileItem> pendingItems = entry.getValue();

            // Notifier les utilisateurs MC et MP du projet
            notifyMcAndMpForProject(project, pendingItems);

            // Notifier l'admin (tous les projets)
            notifyAdmin(pendingItems);
        }

        log.info("Notifications envoyées pour {} projet(s)", itemsByProject.size());
    }

    /**
     * Notifier les utilisateurs MC et MP d'un projet spécifique
     */
    private void notifyMcAndMpForProject(String project, List<TechnicalFileItem> items) {
        List<User> mcAndMpUsers = userRepository.findMcAndMpByProject(project);

        if (mcAndMpUsers.isEmpty()) {
            log.info("Aucun utilisateur MC/MP trouvé pour le projet: {}", project);
            return;
        }

        String summary = buildNotificationSummary(items, project);

        for (User user : mcAndMpUsers) {
            notificationService.sendSystemNotification(
                    user.getEmail(),
                    summary,
                    "TECHNICAL_FILE_REMINDER_" + project
            );
        }

        log.info("Notifications envoyées à {} utilisateurs MC/MP pour le projet {}",
                mcAndMpUsers.size(), project);
    }

    /**
     * Notifier l'admin (tous les projets)
     */
    private void notifyAdmin(List<TechnicalFileItem> items) {
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("ADMIN"))
                .toList();

        if (adminUsers.isEmpty()) return;

        // Grouper les items par projet
        Map<String, Long> itemsByProject = new HashMap<>();
        for (TechnicalFileItem item : items) {
            String project = getProjectFromItem(item);
            itemsByProject.put(project, itemsByProject.getOrDefault(project, 0L) + 1);
        }

        StringBuilder adminSummary = new StringBuilder();
        adminSummary.append("📊 RAPPORT MENSUEL - Items techniques sans modification\n\n");
        adminSummary.append("📅 Date: ").append(LocalDate.now()).append("\n");
        adminSummary.append("⏰ Période: +").append(NOTIFICATION_THRESHOLD_DAYS).append(" jours sans activité\n\n");
        adminSummary.append("📝 Résumé par projet:\n");

        for (Map.Entry<String, Long> entry : itemsByProject.entrySet()) {
            adminSummary.append("  • ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append(" item(s)\n");
        }
        adminSummary.append("\n➡️ Veuillez rappeler aux équipes concernées de mettre à jour ces items.");

        for (User admin : adminUsers) {
            notificationService.sendSystemNotification(
                    admin.getEmail(),
                    adminSummary.toString(),
                    "TECHNICAL_FILE_MONTHLY_REMINDER"
            );
        }
    }

    /**
     * Construire le résumé des notifications pour MC/MP
     */
    private String buildNotificationSummary(List<TechnicalFileItem> items, String project) {
        StringBuilder summary = new StringBuilder();
        summary.append("⚠️ RAPPEL TECHNIQUE MENSUEL - Projet ").append(project).append("\n\n");
        summary.append("Les items suivants n'ont pas été modifiés depuis plus de ")
                .append(NOTIFICATION_THRESHOLD_DAYS).append(" jours:\n\n");

        for (TechnicalFileItem item : items) {
            LocalDate lastActivity = getLastActivityDate(item);
            long daysWithoutUpdate = ChronoUnit.DAYS.between(lastActivity, LocalDate.now());
            String itemCode = getItemCode(item);

            summary.append("🔹 Item #").append(item.getId());
            summary.append(" - Code: ").append(itemCode);
            summary.append("\n   📅 Dernière activité: ").append(lastActivity);
            summary.append(" (").append(daysWithoutUpdate).append(" jours)\n");
            summary.append("   🏷️ Statut validation: ").append(item.getValidationStatus() != null ?
                    item.getValidationStatus().getDisplayName() : "DRAFT");

            if (item.getRemarks() != null && !item.getRemarks().isEmpty()) {
                summary.append("\n   📝 Remarque: ").append(item.getRemarks());
            }
            summary.append("\n\n");
        }

        summary.append("➡️ Merci de mettre à jour ces items dès que possible.");

        return summary.toString();
    }

    /**
     * Récupérer les notifications pour le dashboard (frontend)
     */
    public List<Map<String, Object>> getPendingNotificationsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return new ArrayList<>();

        List<Map<String, Object>> notifications = new ArrayList<>();
        LocalDate thresholdDate = LocalDate.now().minusDays(NOTIFICATION_THRESHOLD_DAYS);

        List<TechnicalFileItem> allItems = itemRepository.findAll();

        for (TechnicalFileItem item : allItems) {
            LocalDate lastActivity = getLastActivityDate(item);

            if (lastActivity != null && lastActivity.isBefore(thresholdDate)) {
                // Filtrer par rôle
                String itemProject = getProjectFromItem(item);

                if (user.getRole().name().equals("ADMIN")) {
                    // Admin voit tout
                    addNotification(notifications, item, lastActivity, itemProject);
                } else if ((user.getRole().name().equals("MC") || user.getRole().name().equals("MP"))
                        && itemProject != null && itemProject.equals(user.getProjet())) {
                    // MC/MP voient uniquement leur projet
                    addNotification(notifications, item, lastActivity, itemProject);
                }
            }
        }

        // Trier par nombre de jours (du plus ancien au plus récent)
        notifications.sort((a, b) -> {
            long daysA = (long) a.get("daysWithoutUpdate");
            long daysB = (long) b.get("daysWithoutUpdate");
            return Long.compare(daysB, daysA); // Tri décroissant
        });

        return notifications;
    }

    private void addNotification(List<Map<String, Object>> notifications,
                                 TechnicalFileItem item,
                                 LocalDate lastActivity,
                                 String project) {
        long daysWithoutUpdate = ChronoUnit.DAYS.between(lastActivity, LocalDate.now());
        String itemCode = getItemCode(item);

        Map<String, Object> notif = new HashMap<>();
        notif.put("id", item.getId());
        notif.put("title", "⚠️ Item technique sans modification");
        notif.put("description", String.format(
                "L'item '%s' n'a pas été modifié depuis %d jours (dernière activité: %s)",
                itemCode,
                daysWithoutUpdate,
                lastActivity
        ));
        notif.put("daysWithoutUpdate", daysWithoutUpdate);
        notif.put("type", "TECHNICAL_FILE_PENDING");
        notif.put("project", project);
        notif.put("createdAt", LocalDateTime.now());

        // Déterminer la priorité (ajustée pour 30 jours)
        String priority;
        if (daysWithoutUpdate > 60) {
            priority = "CRITICAL";
        } else if (daysWithoutUpdate > NOTIFICATION_THRESHOLD_DAYS) {
            priority = "HIGH";
        } else {
            priority = "MEDIUM";
        }
        notif.put("priority", priority);

        // Ajouter des infos supplémentaires
        notif.put("lastActivityDate", lastActivity.toString());
        notif.put("validationStatus", item.getValidationStatus() != null ?
                item.getValidationStatus().name() : "DRAFT");
        notif.put("itemCode", itemCode);

        notifications.add(notif);
    }
}