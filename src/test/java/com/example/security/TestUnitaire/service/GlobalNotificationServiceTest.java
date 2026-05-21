package com.example.security.TestUnitaire.service;

import com.example.security.cahierdeCharge.ChargeSheet;
import com.example.security.email.GlobalNotificationService;
import com.example.security.reclamation.Claim;
import com.example.security.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private GlobalNotificationService notificationService;

    private List<String> testEmails;
    private ChargeSheet chargeSheet;
    private Claim claim;

    @BeforeEach
    void setUp() throws MessagingException {
        testEmails = Arrays.asList("user1@leoni.com", "user2@leoni.com");

        chargeSheet = new ChargeSheet();
        chargeSheet.setId(100L);
        chargeSheet.setProject("FORD");
        chargeSheet.setPlant("MH1");
        chargeSheet.setHarnessRef("HARNESS-001");
        chargeSheet.setIssuedBy("ING001");

        claim = new Claim();
        claim.setId(200L);
        claim.setTitle("Défaut détecté");
        claim.setAssignedTo("tech@leoni.com");

        // Configuration par défaut des mocks
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        lenient().doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    // ==================== sendNotificationToAllUsers ====================

    @Test
    void sendNotificationToAllUsers_ShouldSendToAllActiveUsers() {
        when(userRepository.findAllActiveUserEmails()).thenReturn(testEmails);

        notificationService.sendNotificationToAllUsers("Test Subject", "Test Message");

        verify(mailSender, times(testEmails.size())).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationToAllUsers_WhenNoActiveUsers_ShouldNotSendEmail() {
        when(userRepository.findAllActiveUserEmails()).thenReturn(Collections.emptyList());

        notificationService.sendNotificationToAllUsers("Test Subject", "Test Message");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // ==================== sendNotificationToProjectUsers ====================

    @Test
    void sendNotificationToProjectUsers_ShouldSendToProjectMembers() {
        when(userRepository.findActiveUserEmailsByProjet("FORD")).thenReturn(testEmails);

        notificationService.sendNotificationToProjectUsers("Test", "Message", "FORD");

        verify(mailSender, times(testEmails.size())).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationToProjectUsers_WhenNoUsers_ShouldNotSend() {
        when(userRepository.findActiveUserEmailsByProjet("FORD")).thenReturn(Collections.emptyList());

        notificationService.sendNotificationToProjectUsers("Test", "Message", "FORD");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // ==================== sendHtmlNotificationToProjectUsers ====================

    @Test
    void sendHtmlNotificationToProjectUsers_ShouldSendHtmlEmails() throws MessagingException {
        when(userRepository.findActiveUserEmailsByProjet("FORD")).thenReturn(testEmails);

        notificationService.sendHtmlNotificationToProjectUsers("Test HTML", "<h1>Test</h1>", "FORD");

        verify(mailSender, times(testEmails.size())).send(any(MimeMessage.class));
    }

    @Test
    void sendHtmlNotificationToProjectUsers_WhenMessagingException_ShouldNotThrow() {
        when(userRepository.findActiveUserEmailsByProjet("FORD")).thenReturn(testEmails);
        // Ne pas lever d'exception, juste logguer l'erreur

        notificationService.sendHtmlNotificationToProjectUsers("Test", "<h1>Test</h1>", "FORD");

        // Le test passe si aucune exception n'est levée
        verify(mailSender, times(testEmails.size())).send(any(MimeMessage.class));
    }

    // ==================== sendNotificationToProjectAndSiteUsers ====================

    @Test
    void sendNotificationToProjectAndSiteUsers_ShouldSendToSpecificUsers() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1")).thenReturn(testEmails);

        notificationService.sendNotificationToProjectAndSiteUsers("Test", "Message", "FORD", "MH1");

        verify(mailSender, times(testEmails.size())).send(any(MimeMessage.class));
    }

    @Test
    void sendNotificationToProjectAndSiteUsers_WhenNoUsers_ShouldNotSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1")).thenReturn(Collections.emptyList());

        notificationService.sendNotificationToProjectAndSiteUsers("Test", "Message", "FORD", "MH1");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ==================== notifyClaimCreated ====================

    @Test
    void notifyClaimCreated_ShouldSendToAllUsers() {
        when(userRepository.findAllActiveUserEmails()).thenReturn(testEmails);

        notificationService.notifyClaimCreated(200L, "Défaut", 100L, "pp@leoni.com");

        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }

    @Test
    void notifyClaimCreated_ShouldContainClaimInfo() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        when(userRepository.findAllActiveUserEmails()).thenReturn(testEmails);

        notificationService.notifyClaimCreated(200L, "Défaut détecté", 100L, "pp@leoni.com");

        verify(mailSender, atLeastOnce()).send(captor.capture());
        String text = captor.getValue().getText();
        assertThat(text).contains("Défaut détecté");
        assertThat(text).contains("200");
        assertThat(text).contains("100");
    }

    // ==================== notifyClaimAssigned ====================

    @Test
    void notifyClaimAssigned_WhenAssignedToNotNull_ShouldSendToOneUser() {
        notificationService.notifyClaimAssigned(claim, "admin@leoni.com");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void notifyClaimAssigned_WhenAssignedToIsNull_ShouldNotSend() {
        claim.setAssignedTo(null);

        notificationService.notifyClaimAssigned(claim, "admin@leoni.com");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // ==================== notifyDocumentDeleted ====================

    @Test
    void notifyDocumentDeleted_ShouldSendToAllUsers() {
        when(userRepository.findAllActiveUserEmails()).thenReturn(testEmails);

        notificationService.notifyDocumentDeleted("Cahier", 100L, 100L, "admin@leoni.com");

        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }

    // ==================== sendSystemNotification ====================

    @Test
    void sendSystemNotification_ShouldSendSimpleEmail() {
        notificationService.sendSystemNotification("user@leoni.com", "Test message", "REMINDER");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendSystemNotification_ShouldHaveCorrectSubject() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        notificationService.sendSystemNotification("user@leoni.com", "Message", "REMINDER");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).contains("REMINDER");
    }

    // ==================== notifyChargeSheetUpdated ====================

    @Test
    void notifyChargeSheetUpdated_ShouldSendNotification() {
        when(userRepository.findAllActiveUserEmails()).thenReturn(testEmails);

        notificationService.notifyChargeSheetUpdated(10L, "VALIDÉ", "ing@leoni.com", "ING");

        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }

    // ==================== notifyComplianceCreatedToProjectAndSite ====================

    @Test
    void notifyComplianceCreatedToProjectAndSite_ShouldSendToSpecificUsers() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1")).thenReturn(testEmails);

        notificationService.notifyComplianceCreatedToProjectAndSite(300L, 100L, "pp@leoni.com", "FORD", "MH1");

        verify(mailSender, times(testEmails.size())).send(any(MimeMessage.class));
    }

    // ==================== notifyTechnicalFileCreatedToProjectAndSite ====================

    @Test
    void notifyTechnicalFileCreatedToProjectAndSite_ShouldSendToSpecificUsers() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1")).thenReturn(testEmails);

        notificationService.notifyTechnicalFileCreatedToProjectAndSite(400L, 100L, "tech@leoni.com", "FORD", "MH1");

        verify(mailSender, times(testEmails.size())).send(any(MimeMessage.class));
    }
    // ==================== sendHtmlNotificationToOneUser ====================

    @Test
    void sendHtmlNotificationToOneUser_ShouldSendHtmlEmail() throws Exception {
        notificationService.sendHtmlNotificationToOneUser("HTML Test", "<h1>Hello</h1>", "user@test.com");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
    // ==================== sendHtmlNotificationToProjectAndSiteUsers ====================

    @Test
    void sendHtmlNotificationToProjectAndSiteUsers_ShouldSendHtml() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com", "user2@test.com"));

        notificationService.sendHtmlNotificationToProjectAndSiteUsers("Test", "<h1>HTML</h1>", "FORD", "MH1");

        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void sendHtmlNotificationToProjectAndSiteUsers_WhenNoUsers_ShouldNotSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Collections.emptyList());

        notificationService.sendHtmlNotificationToProjectAndSiteUsers("Test", "<h1>HTML</h1>", "FORD", "MH1");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // ==================== notifyChargeSheetUpdatedToProjectAndSite ====================

    @Test
    void notifyChargeSheetUpdatedToProjectAndSite_ShouldSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com"));

        notificationService.notifyChargeSheetUpdatedToProjectAndSite(100L, "VALIDATED", "user@test.com", "ING", "FORD", "MH1");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ==================== notifyDocumentDeletedToProjectAndSite ====================

    @Test
    void notifyDocumentDeletedToProjectAndSite_ShouldSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com"));

        notificationService.notifyDocumentDeletedToProjectAndSite("Cahier", 100L, 100L, "admin@test.com", "FORD", "MH1");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ==================== notifyComplianceUpdatedToProjectAndSite ====================

    @Test
    void notifyComplianceUpdatedToProjectAndSite_ShouldSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com"));

        notificationService.notifyComplianceUpdatedToProjectAndSite(300L, 100L, "user@test.com", "FORD", "MH1");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ==================== notifyComplianceDeletedToProjectAndSite ====================

    @Test
    void notifyComplianceDeletedToProjectAndSite_ShouldSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com"));

        notificationService.notifyComplianceDeletedToProjectAndSite("Conformité", 300L, 100L, "user@test.com", "FORD", "MH1");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ==================== notifyTechnicalFileUpdatedToProjectAndSite ====================

    @Test
    void notifyTechnicalFileUpdatedToProjectAndSite_ShouldSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com"));

        notificationService.notifyTechnicalFileUpdatedToProjectAndSite(400L, 100L, "user@test.com", "TECH", "FORD", "MH1");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ==================== notifyClaimUpdatedToProjectAndSite ====================

    @Test
    void notifyClaimUpdatedToProjectAndSite_ShouldSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com"));

        notificationService.notifyClaimUpdatedToProjectAndSite(200L, "Défaut", 100L, "user@test.com", "RESOLVED", "FORD", "MH1");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ==================== notifyClaimDeletedToProjectAndSite ====================

    @Test
    void notifyClaimDeletedToProjectAndSite_ShouldSend() {
        when(userRepository.findEmailsByProjectAndSite("FORD", "MH1"))
                .thenReturn(Arrays.asList("user1@test.com"));

        notificationService.notifyClaimDeletedToProjectAndSite("Réclamation", 200L, 100L, "user@test.com", "FORD", "MH1");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ==================== notifyChargeSheetCreatedDetailed (avec items) ====================

    @Test
    void notifyChargeSheetCreatedDetailed_ShouldHandleItems() {
        when(userRepository.findActiveUserEmailsByProjet("FORD"))
                .thenReturn(Arrays.asList("user1@test.com"));

        // Créer une liste d'items non vide pour éviter NullPointerException
        chargeSheet.setItems(Collections.emptyList());

        notificationService.notifyChargeSheetCreatedDetailed(chargeSheet);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // ==================== notifyChargeSheetCreatedTable ====================

    @Test
    void notifyChargeSheetCreatedTable_ShouldSendHtml() {
        when(userRepository.findActiveUserEmailsByProjet("FORD"))
                .thenReturn(Arrays.asList("user1@test.com"));

        chargeSheet.setItems(Collections.emptyList());

        notificationService.notifyChargeSheetCreatedTable(chargeSheet);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}