package com.example.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    /**
     * Génère et envoie un code de vérification par email
     */
    @Transactional
    public boolean sendVerificationCode(String email) {
        // Vérifier que l'email existe
        if (!userRepository.findByEmail(email).isPresent()) {
            return false;
        }

        // Supprimer les anciens codes
        verificationCodeRepository.deleteByEmail(email);

        // Générer un code à 6 chiffres
        String code = String.format("%06d", new Random().nextInt(999999));

        // Créer le code de vérification
        VerificationCode verificationCode = VerificationCode.builder()
                .email(email)
                .code(code)
                .expiryDate(LocalDateTime.now().plusMinutes(10)) // Expire dans 10 minutes
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        verificationCodeRepository.save(verificationCode);

        // Envoyer l'email avec le code
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject("Code de vérification - Leoni Test Module System");
            mailMessage.setText(String.format("""
                    Bonjour,
                    
                    Votre code de vérification est : %s
                    
                    Ce code est valable 10 minutes.
                    
                    Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.
                    
                    Cordialement,
                    L'équipe Leoni Test Module System
                    """, code));
            mailMessage.setFrom("noreply@leoni.com");
            mailSender.send(mailMessage);

            System.out.println("✅ Code de vérification envoyé à " + email + " : " + code);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si le code est valide
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElse(null);

        if (verificationCode == null) {
            return false;
        }

        // Vérifier si le code n'est pas expiré
        if (verificationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Marquer le code comme utilisé
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        return true;
    }
}