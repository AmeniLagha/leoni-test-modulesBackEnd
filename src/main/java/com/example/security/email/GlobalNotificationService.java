package com.example.security.email;

import com.example.security.cahierdeCharge.ChargeSheet;
import com.example.security.reclamation.Claim;
import com.example.security.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalNotificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Async
    @Transactional(readOnly = true)
    public void sendNotificationToAllUsers(String subject, String message) {
        try {
            List<String> allUserEmails = userRepository.findAllActiveUserEmails();

            if (allUserEmails.isEmpty()) {
                log.warn("Aucun utilisateur actif trouvé pour envoyer la notification");
                return;
            }

            log.info("Envoi de notification à {} utilisateurs: {}", allUserEmails.size(), subject);

            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom("noreply@leoni-system.com");
            email.setSubject("[Système Leoni] " + subject);
            email.setText(message);

            // Envoyer à chaque utilisateur individuellement
            for (String recipient : allUserEmails) {
                try {
                    email.setTo(recipient);
                    mailSender.send(email);
                    log.debug("Notification envoyée à: {}", recipient);
                    Thread.sleep(50); // Petit délai pour éviter le spam
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Interruption lors de l'envoi à {}", recipient, e);
                } catch (Exception e) {
                    log.error("Erreur lors de l'envoi à {}: {}", recipient, e.getMessage());
                }
            }

            log.info("Notification envoyée avec succès à tous les utilisateurs");
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification globale: {}", e.getMessage(), e);
        }
    }

    public void notifyChargeSheetCreated(Long chargeSheetId, String performedBy) {
        String subject = "Nouveau Cahier des Charges";
        String message = String.format(
                "Bonjour,\n\n" +
                        "📋 **NOUVEAU CAHIER DES CHARGES CRÉÉ**\n\n" +
                        "Un nouveau cahier des charges a été créé dans le système.\n\n" +
                        "🔢 **ID du Cahier:** %d\n" +
                        "👤 **Créé par:** %s\n" +
                        "🕐 **Date:** %s\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous au système pour consulter le nouveau cahier des charges.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                chargeSheetId, performedBy, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyChargeSheetUpdated(Long chargeSheetId, String actionType, String performedBy, String role) {
        String subject = "Cahier des Charges " + actionType;
        String message = String.format(
                "Bonjour,\n\n" +
                        "📋 **CAHIER DES CHARGES %s**\n\n" +
                        "Un cahier des charges a été %s dans le système.\n\n" +
                        "🔢 **ID du Cahier:** %d\n" +
                        "🔧 **Action:** %s\n" +
                        "👤 **Effectué par:** %s (%s)\n" +
                        "🕐 **Date:** %s\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous pour voir les modifications apportées.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                actionType, actionType.toLowerCase(), chargeSheetId, actionType, performedBy, role, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyComplianceCreated(Long complianceId, Long chargeSheetId, String performedBy) {
        String subject = "Nouvelle Fiche de Conformité";
        String message = String.format(
                "Bonjour,\n\n" +
                        "✅ **NOUVELLE FICHE DE CONFORMITÉ CRÉÉE**\n\n" +
                        "Une nouvelle fiche de conformité a été créée dans le système.\n\n" +
                        "🔢 **ID de la Conformité:** %d\n" +
                        "📋 **ID du Cahier associé:** %d\n" +
                        "👤 **Créé par:** %s (PP)\n" +
                        "🕐 **Date:** %s\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous pour consulter la fiche de conformité.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                complianceId, chargeSheetId, performedBy, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyComplianceUpdated(Long complianceId, Long chargeSheetId, String performedBy) {
        String subject = "Fiche de Conformité Modifiée";
        String message = String.format(
                "Bonjour,\n\n" +
                        "✅ **FICHE DE CONFORMITÉ MODIFIÉE**\n\n" +
                        "Une fiche de conformité a été modifiée dans le système.\n\n" +
                        "🔢 **ID de la Conformité:** %d\n" +
                        "📋 **ID du Cahier associé:** %d\n" +
                        "👤 **Modifié par:** %s (PP)\n" +
                        "🕐 **Date:** %s\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous pour voir les modifications.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                complianceId, chargeSheetId, performedBy, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyTechnicalFileCreated(Long technicalFileId, Long chargeSheetId, String performedBy) {
        String subject = "Nouveau Dossier Technique";
        String message = String.format(
                "Bonjour,\n\n" +
                        "🔧 **NOUVEAU DOSSIER TECHNIQUE CRÉÉ**\n\n" +
                        "Un nouveau dossier technique a été créé dans le système.\n\n" +
                        "🔢 **ID du Dossier:** %d\n" +
                        "📋 **ID du Cahier associé:** %d\n" +
                        "👤 **Créé par:** %s (PP)\n" +
                        "🕐 **Date:** %s\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous pour consulter le dossier technique.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                technicalFileId, chargeSheetId, performedBy, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyTechnicalFileUpdated(Long technicalFileId, Long chargeSheetId, String performedBy, String role) {
        String subject = "Dossier Technique Modifié";
        String message = String.format(
                "Bonjour,\n\n" +
                        "🔧 **DOSSIER TECHNIQUE MODIFIÉ**\n\n" +
                        "Un dossier technique a été modifié dans le système.\n\n" +
                        "🔢 **ID du Dossier:** %d\n" +
                        "📋 **ID du Cahier associé:** %d\n" +
                        "👤 **Modifié par:** %s (%s)\n" +
                        "🕐 **Date:** %s\n\n" +
                        "⚠️ **Note importante:**\n" +
                        "Cette modification peut affecter les opérations de maintenance.\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous pour voir les modifications.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                technicalFileId, chargeSheetId, performedBy, role, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyClaimCreated(Long claimId, String claimTitle, Long chargeSheetId, String performedBy) {
        String subject = "Nouvelle Réclamation: " + claimTitle;
        String message = String.format(
                "Bonjour,\n\n" +
                        "🚨 **NOUVELLE RÉCLAMATION CRÉÉE**\n\n" +
                        "Une nouvelle réclamation a été créée dans le système.\n\n" +
                        "📌 **Titre:** %s\n" +
                        "🔢 **ID de la Réclamation:** %d\n" +
                        "📋 **ID du Cahier associé:** %d\n" +
                        "👤 **Signalé par:** %s\n" +
                        "🕐 **Date:** %s\n\n" +
                        "⚠️ **Action requise:**\n" +
                        "Veuillez examiner cette réclamation dans les plus brefs délais.\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous pour traiter la réclamation.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                claimTitle, claimId, chargeSheetId, performedBy, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyClaimUpdated(Long claimId, String claimTitle, Long chargeSheetId, String performedBy, String action) {
        String subject = "Réclamation " + action + ": " + claimTitle;
        String message = String.format(
                "Bonjour,\n\n" +
                        "🚨 **RÉCLAMATION %s**\n\n" +
                        "Une réclamation a été %s dans le système.\n\n" +
                        "📌 **Titre:** %s\n" +
                        "🔢 **ID de la Réclamation:** %d\n" +
                        "📋 **ID du Cahier associé:** %d\n" +
                        "👤 **Traité par:** %s\n" +
                        "🕐 **Date:** %s\n\n" +
                        "🔗 **Accès rapide:**\n" +
                        "Connectez-vous pour voir les détails.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                action, action.toLowerCase(), claimTitle, claimId, chargeSheetId, performedBy, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }

    public void notifyDocumentDeleted(String documentType, Long documentId, Long chargeSheetId, String performedBy) {
        String subject = documentType + " Supprimé(e)";
        String message = String.format(
                "Bonjour,\n\n" +
                        "🗑️ **%s SUPPRIMÉ(E)**\n\n" +
                        "Un %s a été supprimé du système.\n\n" +
                        "📄 **Type de document:** %s\n" +
                        "🔢 **ID du document:** %d\n" +
                        "📋 **ID du Cahier associé:** %d\n" +
                        "👤 **Supprimé par:** %s\n" +
                        "🕐 **Date:** %s\n\n" +
                        "⚠️ **Note:**\n" +
                        "Cette action est définitive et ne peut pas être annulée.\n\n" +
                        "Cordialement,\n" +
                        "Système de Gestion Leoni",
                documentType, documentType.toLowerCase(), documentType, documentId, chargeSheetId, performedBy, java.time.LocalDateTime.now()
        );

        sendNotificationToAllUsers(subject, message);
    }
    @Async
    @Transactional(readOnly = true)
    public void sendNotificationToProjectUsers(String subject, String message, String projet) {
        try {

            List<String> emails = userRepository.findActiveUserEmailsByProjet(projet);

            if (emails.isEmpty()) {
                log.warn("Aucun utilisateur trouvé pour le projet {}", projet);
                return;
            }

            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom("noreply@leoni-system.com");
            email.setSubject("[Système Leoni] " + subject);
            email.setText(message);

            for (String recipient : emails) {
                email.setTo(recipient);
                mailSender.send(email);
            }

            log.info("Notification envoyée au projet {}", projet);

        } catch (Exception e) {
            log.error("Erreur notification projet {}", projet, e);
        }
    }
    public void notifyChargeSheetCreatedDetailed(ChargeSheet chargeSheet) {
        String subject = "Nouveau Cahier des Charges";

        // On récupère le nombre d'items
        int numberOfItems = chargeSheet.getItems().size();

        // On prépare le message avec les 5 premiers champs principaux
        String message = String.format(
                "Bonjour,\n\n" +
                        "📋 **NOUVEAU CAHIER DES CHARGES CRÉÉ**\n\n" +
                        "ID : %d\n" +
                        "Plant : %s\n" +
                        "Projet : %s\n" +
                        "Harness Ref : %s\n" +
                        "Issued By : %s\n" +
                        "Nombre d'items : %d\n\n" +
                        "Connectez-vous au système pour plus de détails.",
                chargeSheet.getId(),
                chargeSheet.getPlant(),
                chargeSheet.getProject(),
                chargeSheet.getHarnessRef(),
                chargeSheet.getIssuedBy(),
                numberOfItems
        );

        // Envoi aux utilisateurs du projet
        sendNotificationToProjectUsers(subject, message, chargeSheet.getProject());
    }
    @Async
    @Transactional(readOnly = true)
    public void sendHtmlNotificationToProjectUsers(String subject, String htmlMessage, String projet) {
        try {
            List<String> emails = userRepository.findActiveUserEmailsByProjet(projet);

            if (emails.isEmpty()) {
                log.warn("Aucun utilisateur trouvé pour le projet {}", projet);
                return;
            }

            for (String recipient : emails) {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
                helper.setFrom("noreply@leoni-system.com");
                helper.setTo(recipient);
                helper.setSubject("[Système Leoni] " + subject);
                helper.setText(htmlMessage, true); // true = HTML

                mailSender.send(message);
            }

            log.info("Notification HTML envoyée au projet {}", projet);

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi HTML pour le projet {}", projet, e);
        }
    }
    public void notifyChargeSheetCreatedTable(ChargeSheet chargeSheet) {
        String subject = "Nouveau Cahier des Charges";

        int numberOfItems = chargeSheet.getItems().size();

        // Construction du tableau HTML
        String htmlMessage = "<html><body>"
                + "<h3>📋 NOUVEAU CAHIER DES CHARGES CRÉÉ</h3>"
                + "<table border='1' cellpadding='5' cellspacing='0'>"
                + "<tr><th>Champ</th><th>Valeur</th></tr>"
                + "<tr><td>ID</td><td>" + chargeSheet.getId() + "</td></tr>"
                + "<tr><td>Plant</td><td>" + chargeSheet.getPlant() + "</td></tr>"
                + "<tr><td>Projet</td><td>" + chargeSheet.getProject() + "</td></tr>"
                + "<tr><td>Harness Ref</td><td>" + chargeSheet.getHarnessRef() + "</td></tr>"
                + "<tr><td>Issued By</td><td>" + chargeSheet.getIssuedBy() + "</td></tr>"
                + "<tr><td>Nombre d'items</td><td>" + numberOfItems + "</td></tr>"
                + "</table>"
                + "<p>Connectez-vous au système pour plus de détails.</p>"
                + "</body></html>";

        sendHtmlNotificationToProjectUsers(subject, htmlMessage, chargeSheet.getProject());
    }
    @Async
    public void sendNotificationToOneUser(String subject, String message, String emailTo) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setFrom("noreply@leoni-system.com");
            email.setTo(emailTo);
            email.setSubject("[Système Leoni] " + subject);
            email.setText(message);

            mailSender.send(email);

            log.info("Notification envoyée à {}", emailTo);

        } catch (Exception e) {
            log.error("Erreur envoi email à {}: {}", emailTo, e.getMessage());
        }
    }
    public void notifyClaimAssigned(Claim claim, String performedBy) {
        if (claim.getAssignedTo() == null) return;

        String subject = "Réclamation assignée: " + claim.getTitle();

        String message = String.format(
                "Bonjour,\n\n" +
                        "Une réclamation vous a été assignée.\n\n" +
                        "Titre: %s\n" +
                        "ID: %d\n" +
                        "Assigné par: %s\n" +
                        "Date: %s\n\n" +
                        "Veuillez traiter cette réclamation.",
                claim.getTitle(),
                claim.getId(),
                performedBy,
                java.time.LocalDateTime.now()
        );

        sendNotificationToOneUser(subject, message, claim.getAssignedTo());
    }
    /**
     * Envoie une notification système (email) à un utilisateur spécifique
     * @param toEmail Email du destinataire
     * @param message Contenu du message
     * @param type Type de notification (ex: TECHNICAL_FILE_REMINDER)
     */
    @Async
    public void sendSystemNotification(String toEmail, String message, String type) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject("🔔 LEONI System Notification - " + type);
            mailMessage.setText(message);
            mailMessage.setFrom("noreply@leoni.com");

            mailSender.send(mailMessage);

            log.info("✅ System notification sent to {} (type: {})", toEmail, type);
        } catch (Exception e) {
            log.error("❌ Failed to send system notification to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Envoie une notification système avec template HTML
     */
    @Async
    public void sendSystemNotificationHtml(String toEmail, String message, String type) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🔔 LEONI System Notification - " + type);
            helper.setFrom("noreply@leoni.com");

            String htmlContent = buildSystemNotificationHtml(message, type);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            log.info("✅ HTML system notification sent to {} (type: {})", toEmail, type);
        } catch (Exception e) {
            log.error("❌ Failed to send HTML system notification: {}", e.getMessage());
        }
    }

    /**
     * Construit le template HTML pour les notifications système
     */
    private String buildSystemNotificationHtml(String message, String type) {
        String color = getColorForType(type);
        String icon = getIconForType(type);

        // Version sans text block pour éviter l'erreur
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; background-color: #0A0E1A; margin: 0; padding: 20px; }\n");
        html.append("        .container { max-width: 600px; margin: 0 auto; background: linear-gradient(135deg, #0F1525 0%, #0A0E1A 100%); border-radius: 20px; overflow: hidden; border: 1px solid rgba(0, 212, 255, 0.2); }\n");
        html.append("        .header { background: linear-gradient(135deg, #00D4FF, #0052CC); padding: 20px; text-align: center; }\n");
        html.append("        .header h1 { color: white; margin: 0; font-size: 24px; }\n");
        html.append("        .content { padding: 30px; }\n");
        html.append("        .message { background: rgba(255, 255, 255, 0.05); border-radius: 12px; padding: 20px; margin-bottom: 20px; white-space: pre-line; color: #E0E0E0; }\n");
        html.append("        .type-badge { display: inline-block; padding: 5px 15px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-bottom: 15px; background-color: ").append(color).append("; color: white; }\n");
        html.append("        .footer { background: rgba(0, 0, 0, 0.3); padding: 15px; text-align: center; font-size: 12px; color: #666; }\n");
        html.append("        .icon { font-size: 48px; text-align: center; margin-bottom: 10px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h1>🔔 LEONI System Notification</h1>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"content\">\n");
        html.append("            <div class=\"icon\">").append(icon).append("</div>\n");
        html.append("            <div class=\"type-badge\">").append(type).append("</div>\n");
        html.append("            <div class=\"message\">").append(message.replace("\n", "<br>")).append("</div>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"footer\">\n");
        html.append("            <p>© 2026 LEONI Group - Industrial Intelligence Platform</p>\n");
        html.append("            <p>Ce message est généré automatiquement, merci de ne pas y répondre.</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    private String getColorForType(String type) {
        if (type.contains("REMINDER")) return "#FFA500";
        if (type.contains("GLOBAL")) return "#00D4FF";
        return "#00FF88";
    }

    private String getIconForType(String type) {
        if (type.contains("REMINDER")) return "⚠️";
        if (type.contains("GLOBAL")) return "📊";
        return "🔔";
    }
    @Async
    public void sendHtmlNotificationToOneUser(String subject, String htmlMessage, String emailTo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@leoni-system.com");
            helper.setTo(emailTo);
            helper.setSubject("[Système Leoni] " + subject);
            helper.setText(htmlMessage, true);

            mailSender.send(message);

            log.info("📧 HTML notification envoyée à {}", emailTo);

        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi HTML à {}: {}", emailTo, e.getMessage());
        }
    }

}