package com.example.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service pour la gestion des codes de vérification par email.
 * <p>
 * Cette classe contient la logique métier pour l'envoi et la validation
 * des codes de vérification à 6 chiffres utilisés dans le processus
 * de réinitialisation de mot de passe et autres opérations sensibles
 * nécessitant une confirmation par email.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Génération d'un code aléatoire à 6 chiffres</li>
 *     <li>Envoi du code par email à l'utilisateur</li>
 *     <li>Vérification de la validité du code saisi (existence, non expiré, non utilisé)</li>
 *     <li>Nettoyage des anciens codes avant création d'un nouveau</li>
 *     <li>Marquage des codes comme utilisés après validation réussie</li>
 * </ul>
 *
 * <p><strong>Processus complet de vérification :</strong></p>
 * <ol>
 *     <li>L'utilisateur saisit son email sur le formulaire "Mot de passe oublié"</li>
 *     <li>Le service vérifie que l'email existe dans le système</li>
 *     <li>Un code aléatoire à 6 chiffres est généré et persisté en base</li>
 *     <li>Le code est envoyé par email à l'utilisateur</li>
 *     <li>L'utilisateur saisit le code reçu dans l'interface</li>
 *     <li>Le service vérifie la validité du code (non expiré, non utilisé)</li>
 *     <li>Si valide, le code est marqué comme utilisé et la vérification réussit</li>
 * </ol>
 *
 * <p><strong>Durée de validité :</strong> 10 minutes (configurable dans le code)</p>
 * <p><strong>Code type :</strong> 6 chiffres (ex: 123456, 789012)</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see VerificationCode
 * @see VerificationCodeRepository
 * @see com.example.security.user.UserRepository
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    /**
     * Génère et envoie un code de vérification par email à l'utilisateur.
     * <p>
     * Cette méthode est appelée lorsqu'un utilisateur demande une réinitialisation
     * de mot de passe ou toute autre opération nécessitant une vérification
     * par email.
     * </p>
     *
     * <p><strong>Processus détaillé :</strong></p>
     * <ol>
     *     <li>Vérification de l'existence de l'email dans le système</li>
     *     <li>Suppression des anciens codes associés à cet email (nettoyage)</li>
     *     <li>Génération d'un nouveau code aléatoire à 6 chiffres</li>
     *     <li>Création et persistance d'un objet {@link VerificationCode} avec :
     *         <ul>
     *             <li>Email de l'utilisateur</li>
     *             <li>Code généré</li>
     *             <li>Date d'expiration (10 minutes)</li>
     *             <li>Flag {@code used} à {@code false}</li>
     *             <li>Date de création</li>
     *         </ul>
     *     </li>
     *     <li>Envoi d'un email contenant le code à l'utilisateur</li>
     * </ol>
     *
     * <p><strong>Note de sécurité :</strong>
     * Le code est affiché dans la console (pour les tests et le développement)
     * en plus de l'envoi par email, ce qui facilite le débogage mais doit être
     * supprimé en production.</p>
     *
     * @param email L'adresse email de l'utilisateur demandant la vérification
     * @return {@code true} si le code a été généré et envoyé avec succès,
     *         {@code false} si l'email n'existe pas ou en cas d'erreur technique
     *
     * @see VerificationCode
     * @see VerificationCodeRepository#deleteByEmail(String)
     * @see JavaMailSender#send(SimpleMailMessage)
     */
    @Transactional
    public boolean sendVerificationCode(String email) {
        try {
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
            System.err.println("❌ Erreur dans sendVerificationCode: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Vérifie si le code saisi par l'utilisateur est valide.
     * <p>
     * Cette méthode est appelée lorsque l'utilisateur soumet le code reçu
     * par email pour confirmer son identité.
     * </p>
     *
     * <p><strong>Processus de vérification :</strong></p>
     * <ol>
     *     <li>Recherche d'un code correspondant à l'email et la valeur fournis,
     *         qui n'a pas encore été utilisé ({@code used = false})</li>
     *     <li>Si aucun code n'est trouvé, la vérification échoue</li>
     *     <li>Vérification que la date d'expiration n'est pas dépassée</li>
     *     <li>Si le code est expiré, la vérification échoue</li>
     *     <li>Si toutes les vérifications sont réussies, le code est marqué comme utilisé</li>
     *     <li>La méthode retourne {@code true} pour indiquer le succès</li>
     * </ol>
     *
     * <p><strong>Cas d'échec :</strong></p>
     * <ul>
     *     <li>Email non trouvé dans la base</li>
     *     <li>Code incorrect</li>
     *     <li>Code déjà utilisé</li>
     *     <li>Code expiré (plus de 10 minutes)</li>
     * </ul>
     *
     * @param email L'adresse email de l'utilisateur
     * @param code Le code à 6 chiffres saisi par l'utilisateur
     * @return {@code true} si le code est valide et a été marqué comme utilisé,
     *         {@code false} sinon
     *
     * @see VerificationCode#getExpiryDate()
     * @see VerificationCode#isUsed()
     * @see VerificationCode#setUsed(boolean)
     * @see VerificationCodeRepository#findByEmailAndCodeAndUsedFalse(String, String)
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        try {
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
        } catch (Exception e) {
            System.err.println("❌ Erreur dans verifyCode: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}