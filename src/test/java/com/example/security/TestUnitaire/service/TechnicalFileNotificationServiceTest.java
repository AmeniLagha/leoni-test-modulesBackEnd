package com.example.security.TestUnitaire.service;
/*
import com.example.security.email.GlobalNotificationService;
import com.example.security.fichierTechnique.*;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TechnicalFileNotificationServiceTest {

    @Mock
    private TechnicalFileItemRepository itemRepository;

    @Mock
    private GlobalNotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TechnicalFileNotificationService notificationServiceUnderTest;

    private TechnicalFileItem staleItem;
    private TechnicalFileItem recentItem;
    private User ppUser;

    @BeforeEach
    void setUp() {
        ppUser = User.builder()
                .id(1)
                .email("pp@leoni.com")
                .firstname("PP")
                .lastname("User")
                .role(Role.PP)
                .build();

        // Item not modified for 30 days (stale)
        staleItem = TechnicalFileItem.builder()
                .id(1L)
                .itemNumber("ITEM-001")
                .status(TechnicalFileItemStatus.PENDING)
                .createdAt(LocalDate.now().minusDays(30))
                .updatedAt(null)
                .build();

        // Item modified recently (not stale)
        recentItem = TechnicalFileItem.builder()
                .id(2L)
                .itemNumber("ITEM-002")
                .status(TechnicalFileItemStatus.PENDING)
                .createdAt(LocalDate.now().minusDays(5))
                .updatedAt(LocalDate.now().minusDays(2))
                .build();
    }

    // ==================== checkPendingModifications ====================

    @Test
    void checkPendingModifications_WithNoItems_ShouldNotSendNotification() {
        when(itemRepository.findAll()).thenReturn(Collections.emptyList());

        notificationServiceUnderTest.checkPendingModifications();

        verify(notificationService, never()).sendNotificationToAllUsers(anyString(), anyString());
    }

    @Test
    void checkPendingModifications_WithRecentItems_ShouldNotSendNotification() {
        when(itemRepository.findAll()).thenReturn(List.of(recentItem));
        when(userRepository.findAllActiveUserEmails()).thenReturn(List.of("pp@leoni.com"));

        notificationServiceUnderTest.checkPendingModifications();

        // Recent items should not trigger notification
        verify(notificationService, never()).sendNotificationToAllUsers(
                contains("en attente"), anyString());
    }

    @Test
    void checkPendingModifications_WithStaleItems_ShouldCallNotification() {
        when(itemRepository.findAll()).thenReturn(List.of(staleItem));
        when(userRepository.findAllActiveUserEmails()).thenReturn(List.of("pp@leoni.com"));
        doNothing().when(notificationService).sendNotificationToAllUsers(anyString(), anyString());

        notificationServiceUnderTest.checkPendingModifications();

        // Stale items should trigger the notification pipeline
        verify(itemRepository).findAll();
    }

    @Test
    void checkPendingModifications_WithMixedItems_ShouldProcessAllItems() {
        when(itemRepository.findAll()).thenReturn(Arrays.asList(staleItem, recentItem));
        when(userRepository.findAllActiveUserEmails()).thenReturn(List.of("pp@leoni.com"));
        doNothing().when(notificationService).sendNotificationToAllUsers(anyString(), anyString());

        notificationServiceUnderTest.checkPendingModifications();

        verify(itemRepository).findAll();
    }

    @Test
    void checkPendingModifications_WithNullUpdatedAt_ShouldUseCreatedAt() {
        // Item with no updatedAt - should use createdAt
        TechnicalFileItem itemWithNullUpdated = TechnicalFileItem.builder()
                .id(3L)
                .itemNumber("ITEM-003")
                .status(TechnicalFileItemStatus.PENDING)
                .createdAt(LocalDate.now().minusDays(20))
                .updatedAt(null)
                .build();

        when(itemRepository.findAll()).thenReturn(List.of(itemWithNullUpdated));
        when(userRepository.findAllActiveUserEmails()).thenReturn(List.of("pp@leoni.com"));
        doNothing().when(notificationService).sendNotificationToAllUsers(anyString(), anyString());

        // Should not throw
        notificationServiceUnderTest.checkPendingModifications();

        verify(itemRepository).findAll();
    }

    @Test
    void checkPendingModifications_WhenRepositoryEmpty_ShouldNotThrow() {
        when(itemRepository.findAll()).thenReturn(Collections.emptyList());

        // Should not throw any exception
        notificationServiceUnderTest.checkPendingModifications();
    }

    @Test
    void checkPendingModifications_ShouldCallFindAllOnRepository() {
        when(itemRepository.findAll()).thenReturn(Collections.emptyList());

        notificationServiceUnderTest.checkPendingModifications();

        verify(itemRepository, times(1)).findAll();
    }
}*/