package com.example.security.TestUnitaire.service;

import com.example.security.email.GlobalNotificationService;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GlobalNotificationService globalNotificationService;

    private List<String> activeEmails;

    @BeforeEach
    void setUp() {
        activeEmails = Arrays.asList("user1@leoni.com", "user2@leoni.com", "user3@leoni.com");
        when(userRepository.findAllActiveUserEmails()).thenReturn(activeEmails);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
    }

    // ==================== sendNotificationToAllUsers ====================

    @Test
    void sendNotificationToAllUsers_ShouldSendToAllActiveUsers() {
        globalNotificationService.sendNotificationToAllUsers("Test Subject", "Test Message");

        // verify mailSender called at least once (async - best effort)
        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationToAllUsers_WhenNoActiveUsers_ShouldNotSendEmail() {
        when(userRepository.findAllActiveUserEmails()).thenReturn(Collections.emptyList());

        globalNotificationService.sendNotificationToAllUsers("Test Subject", "Test Message");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationToAllUsers_ShouldUseCorrectSubjectPrefix() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        globalNotificationService.sendNotificationToAllUsers("Mon Sujet", "Mon Message");

        verify(mailSender, atLeastOnce()).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getSubject()).contains("[Système Leoni]");
        assertThat(sent.getSubject()).contains("Mon Sujet");
    }

    @Test
    void sendNotificationToAllUsers_ShouldSetFromAddress() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        globalNotificationService.sendNotificationToAllUsers("Sujet", "Message");

        verify(mailSender, atLeastOnce()).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isEqualTo("noreply@leoni-system.com");
    }

    // ==================== sendNotificationToOneUser ====================

    @Test
    void sendNotificationToOneUser_ShouldSendToSpecificEmail() {
        globalNotificationService.sendNotificationToOneUser("user@leoni.com", "Sujet", "Contenu");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }
/*
    @Test
    void sendNotificationToOneUser_ShouldSetCorrectRecipient() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        globalNotificationService.sendNotificationToOneUser("target@leoni.com", "Sujet", "Contenu");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).contains("target@leoni.com");
    }
*/
    // ==================== notifyChargeSheetCreated ====================

    @Test
    void notifyChargeSheetCreated_ShouldCallSendToAllUsers() {
        globalNotificationService.notifyChargeSheetCreated(42L, "ing@leoni.com");

        verify(userRepository, atLeastOnce()).findAllActiveUserEmails();
        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }

    @Test
    void notifyChargeSheetCreated_ShouldContainChargeSheetId() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        globalNotificationService.notifyChargeSheetCreated(99L, "creator@leoni.com");

        verify(mailSender, atLeastOnce()).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("99");
    }

    // ==================== notifyChargeSheetUpdated ====================

    @Test
    void notifyChargeSheetUpdated_ShouldSendNotification() {
        globalNotificationService.notifyChargeSheetUpdated(10L, "VALIDÉ", "ing@leoni.com", "ING");

        verify(mailSender, atLeastOnce()).send(any(SimpleMailMessage.class));
    }

    @Test
    void notifyChargeSheetUpdated_ShouldContainActionAndPerformer() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        globalNotificationService.notifyChargeSheetUpdated(5L, "VALIDÉ", "ing@leoni.com", "ING");

        verify(mailSender, atLeastOnce()).send(captor.capture());
        String text = captor.getValue().getText();
        assertThat(text).contains("ing@leoni.com");
    }

    // ==================== notifyClaimUpdatedToProjectAndSite ====================
/*
    @Test
    void notifyClaimUpdatedToProjectAndSite_ShouldSendNotification() {
        globalNotificationService.notifyClaimUpdatedToProjectAndSite(
                1L, "FORD", 10L, "pp@test.com", "Test Claim", "MH1", "UPDATED");

        // Should attempt to send (may call findAllActiveUserEmails)
        verify(userRepository, atLeastOnce()).findAllActiveUserEmails();
    }
*/
    // ==================== notifyClaimDeletedToProjectAndSite ====================
/*
    @Test
    void notifyClaimDeletedToProjectAndSite_ShouldSendNotification() {
        globalNotificationService.notifyClaimDeletedToProjectAndSite(
                "FORD", 1L, 10L, "pp@test.com", "Test Claim", "MH1");

        verify(userRepository, atLeastOnce()).findAllActiveUserEmails();
    }
*/
    // ==================== notifyChargeSheetCreated — handles mail exception ====================

    @Test
    void sendNotificationToAllUsers_WhenMailFails_ShouldNotThrow() {
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        // Should not propagate exception
        globalNotificationService.sendNotificationToAllUsers("Sujet", "Message");

        // No exception thrown = test passes
    }
}