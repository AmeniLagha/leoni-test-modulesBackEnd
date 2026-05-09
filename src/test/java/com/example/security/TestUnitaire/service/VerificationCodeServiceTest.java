package com.example.security.TestUnitaire.service;

import com.example.security.user.*;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerificationCodeServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationCodeService verificationCodeService;

    private User testUser;
    private VerificationCode validCode;
    private VerificationCode expiredCode;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .email("user@leoni.com")
                .firstname("Test")
                .lastname("User")
                .role(Role.ING)
                .build();

        validCode = VerificationCode.builder()
                .id(1L)
                .email("user@leoni.com")
                .code("123456")
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        expiredCode = VerificationCode.builder()
                .id(2L)
                .email("user@leoni.com")
                .code("654321")
                .expiryDate(LocalDateTime.now().minusMinutes(15))
                .used(false)
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .build();

        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
    }

    // ==================== sendVerificationCode ====================

    @Test
    void sendVerificationCode_WithExistingEmail_ShouldReturnTrue() {
        when(userRepository.findByEmail("user@leoni.com")).thenReturn(Optional.of(testUser));
        doNothing().when(verificationCodeRepository).deleteByEmail("user@leoni.com");
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);

        boolean result = verificationCodeService.sendVerificationCode("user@leoni.com");

        assertThat(result).isTrue();
    }

    @Test
    void sendVerificationCode_WithNonExistingEmail_ShouldReturnFalse() {
        when(userRepository.findByEmail("unknown@leoni.com")).thenReturn(Optional.empty());

        boolean result = verificationCodeService.sendVerificationCode("unknown@leoni.com");

        assertThat(result).isFalse();
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationCode_ShouldDeleteOldCodesFirst() {
        when(userRepository.findByEmail("user@leoni.com")).thenReturn(Optional.of(testUser));
        doNothing().when(verificationCodeRepository).deleteByEmail("user@leoni.com");
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);

        verificationCodeService.sendVerificationCode("user@leoni.com");

        verify(verificationCodeRepository).deleteByEmail("user@leoni.com");
    }

    @Test
    void sendVerificationCode_ShouldSaveNewCode() {
        when(userRepository.findByEmail("user@leoni.com")).thenReturn(Optional.of(testUser));
        doNothing().when(verificationCodeRepository).deleteByEmail(anyString());
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);

        verificationCodeService.sendVerificationCode("user@leoni.com");

        verify(verificationCodeRepository).save(any(VerificationCode.class));
    }

    @Test
    void sendVerificationCode_ShouldSendEmailWithCode() {
        when(userRepository.findByEmail("user@leoni.com")).thenReturn(Optional.of(testUser));
        doNothing().when(verificationCodeRepository).deleteByEmail(anyString());
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);

        verificationCodeService.sendVerificationCode("user@leoni.com");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendVerificationCode_ShouldSendToCorrectEmail() {
        when(userRepository.findByEmail("user@leoni.com")).thenReturn(Optional.of(testUser));
        doNothing().when(verificationCodeRepository).deleteByEmail(anyString());
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verificationCodeService.sendVerificationCode("user@leoni.com");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).contains("user@leoni.com");
    }

    @Test
    void sendVerificationCode_ShouldSendEmailWithLeoniSubject() {
        when(userRepository.findByEmail("user@leoni.com")).thenReturn(Optional.of(testUser));
        doNothing().when(verificationCodeRepository).deleteByEmail(anyString());
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verificationCodeService.sendVerificationCode("user@leoni.com");

        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).contains("Leoni");
    }

    @Test
    void sendVerificationCode_WhenMailFails_ShouldReturnFalse() {
        when(userRepository.findByEmail("user@leoni.com")).thenReturn(Optional.of(testUser));
        doNothing().when(verificationCodeRepository).deleteByEmail(anyString());
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = verificationCodeService.sendVerificationCode("user@leoni.com");

        assertThat(result).isFalse();
    }

    // ==================== verifyCode ====================

    @Test
    void verifyCode_WithValidCode_ShouldReturnTrue() {
        when(verificationCodeRepository.findByEmailAndCodeAndUsedFalse("user@leoni.com", "123456"))
                .thenReturn(Optional.of(validCode));
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);

        boolean result = verificationCodeService.verifyCode("user@leoni.com", "123456");

        assertThat(result).isTrue();
    }

    @Test
    void verifyCode_WithWrongCode_ShouldReturnFalse() {
        when(verificationCodeRepository.findByEmailAndCodeAndUsedFalse("user@leoni.com", "000000"))
                .thenReturn(Optional.empty());

        boolean result = verificationCodeService.verifyCode("user@leoni.com", "000000");

        assertThat(result).isFalse();
    }

    @Test
    void verifyCode_WithExpiredCode_ShouldReturnFalse() {
        when(verificationCodeRepository.findByEmailAndCodeAndUsedFalse("user@leoni.com", "654321"))
                .thenReturn(Optional.of(expiredCode));

        boolean result = verificationCodeService.verifyCode("user@leoni.com", "654321");

        assertThat(result).isFalse();
    }

    @Test
    void verifyCode_WithValidCode_ShouldMarkCodeAsUsed() {
        when(verificationCodeRepository.findByEmailAndCodeAndUsedFalse("user@leoni.com", "123456"))
                .thenReturn(Optional.of(validCode));
        when(verificationCodeRepository.save(any(VerificationCode.class))).thenReturn(validCode);

        verificationCodeService.verifyCode("user@leoni.com", "123456");

        assertThat(validCode.isUsed()).isTrue();
        verify(verificationCodeRepository).save(validCode);
    }

    @Test
    void verifyCode_WithNonExistentEmail_ShouldReturnFalse() {
        when(verificationCodeRepository.findByEmailAndCodeAndUsedFalse("nobody@leoni.com", "123456"))
                .thenReturn(Optional.empty());

        boolean result = verificationCodeService.verifyCode("nobody@leoni.com", "123456");

        assertThat(result).isFalse();
    }
}