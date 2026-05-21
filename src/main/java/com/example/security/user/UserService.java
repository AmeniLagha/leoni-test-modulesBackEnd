package com.example.security.user;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.token.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des utilisateurs.
 * <p>
 * Cette classe contient la logique métier pour toutes les opérations liées
 * aux utilisateurs : création, modification, suppression, réinitialisation
 * de mot de passe, gestion des tokens, et envoi d'emails de notification.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Récupération de la liste des utilisateurs avec leurs permissions</li>
 *     <li>Suppression d'un utilisateur (avec nettoyage des tokens associés)</li>
 *     <li>Mise à jour des informations utilisateur (email, matricule, projets, site, rôle)</li>
 *     <li>Vérification d'existence par email ou matricule</li>
 *     <li>Réinitialisation de mot de passe avec token</li>
 *     <li>Changement de mot de passe (par ADMIN ou par l'utilisateur lui-même)</li>
 * </ul>
 *
 * <p><strong>Sécurité :</strong>
 * Les mots de passe sont encodés avec BCrypt avant stockage. Les tokens
 * de réinitialisation ont une durée de vie limitée (24h) et sont à usage unique.
 * </p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see User
 * @see UserRepository
 * @see TokenRepository
 * @see PasswordResetTokenRepository
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final SiteRepository siteRepository;
    private final ProjetRepository projetRepository;
    @Value("${app.base-url}")
    private String baseUrl;

    // ============================================================
    // MÉTHODES DE RÉCUPÉRATION D'UTILISATEURS
    // ============================================================

    /**
     * Récupère la liste de tous les utilisateurs avec leurs permissions.
     * <p>
     * Pour chaque utilisateur, cette méthode construit un {@link UserDto}
     * contenant les informations suivantes : identité, matricule, email,
     * site, projets associés, rôle et liste des permissions granulaires.
     * </p>
     *
     * @return Liste des {@link UserDto} contenant les informations de tous
     *         les utilisateurs. Retourne une liste vide en cas d'erreur.
     */
    public List<UserDto> getUserListWithPermissions() {
        try {
            List<User> userList = repository.findAll();
            List<UserDto> usersWithPermissions = new ArrayList<>();

            for (User user : userList) {
                UserDto userDto = new UserDto();
                userDto.setId(user.getId());
                userDto.setFirstname(user.getFirstname());
                userDto.setLastname(user.getLastname());
                userDto.setMatricule(user.getMatricule());
                userDto.setSite(user.getSiteName());
                userDto.setPassword(passwordEncoder.encode(user.getPassword()));
                // ✅ Récupérer la liste des noms de projets
                List<String> projetNames = user.getProjets().stream()
                        .map(Projet::getName)
                        .collect(Collectors.toList());
                userDto.setProjets(projetNames);

                userDto.setEmail(user.getEmail());
                userDto.setRole(user.getRole().toString());

                List<String> perms = user.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toList();
                userDto.setPermissions(perms);

                usersWithPermissions.add(userDto);
            }

            return usersWithPermissions;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getUserListWithPermissions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    // ============================================================
    // MÉTHODES DE SUPPRESSION D'UTILISATEUR
    // ============================================================

    /**
     * Supprime un utilisateur par son identifiant.
     * <p>
     * Cette méthode supprime d'abord tous les tokens d'authentification
     * (JWT et reset tokens) associés à l'utilisateur, puis supprime
     * l'utilisateur lui-même.
     * </p>
     *
     * @param id L'identifiant de l'utilisateur à supprimer
     * @return {@code true} si la suppression a réussi,
     *         {@code false} si l'utilisateur n'existe pas ou en cas d'erreur
     */
    public boolean deleteUser(Integer id) {
        try {
            if (!repository.existsById(id)) {
                return false;
            }
            tokenRepository.deleteByUserId(id);
            repository.deleteById(id);

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans deleteUser: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // ============================================================
    // MÉTHODES DE VÉRIFICATION D'EXISTENCE
    // ============================================================

    /**
     * Vérifie si une adresse email existe déjà dans le système.
     *
     * @param email L'adresse email à vérifier
     * @return {@code true} si l'email existe, {@code false} sinon
     */
    public boolean checkEmailExists(String email) {
        return repository.findByEmail(email).isPresent();
    }
    /**
     * Vérifie si un matricule existe déjà dans le système.
     *
     * @param matricule Le matricule à vérifier
     * @return {@code true} si le matricule existe, {@code false} sinon
     */

    public boolean checkMatriculeExists(Integer matricule) {
        return repository.existsByMatricule(matricule);
    }
    // ============================================================
    // MÉTHODES DE MISE À JOUR D'UTILISATEUR
    // ============================================================

    /**
     * Met à jour les informations d'un utilisateur existant.
     * <p>
     * Cette méthode permet de modifier : le prénom, le nom, l'email,
     * les projets associés, le matricule, le site et le rôle.
     * </p>
     *
     * @param id L'identifiant de l'utilisateur à modifier
     * @param userDto Les nouvelles informations de l'utilisateur
     * @return Le {@link UserDto} mis à jour, ou {@code null} si l'utilisateur
     *         n'existe pas ou en cas d'erreur
     */
    public UserDto updateUser(Integer id, UserDto userDto) {
        try {
            return repository.findById(id)
                    .map(user -> {
                        user.setFirstname(userDto.getFirstname());
                        user.setLastname(userDto.getLastname());
                        user.setEmail(userDto.getEmail());
                        if (userDto.getProjets() != null && !userDto.getProjets().isEmpty()) {
                            Set<Projet> projets = new HashSet<>();
                            for (String projetName : userDto.getProjets()) {
                                Projet projet = projetRepository.findByName(projetName)
                                        .orElseThrow(() -> new RuntimeException("Projet non trouvé: " + projetName));
                                projets.add(projet);
                            }
                            user.setProjets(projets);
                        }
                        user.setMatricule(userDto.getMatricule());
                        if (userDto.getSite() != null && !userDto.getSite().isEmpty()) {
                            Site site = siteRepository.findByName(userDto.getSite())
                                    .orElseThrow(() -> new RuntimeException("Site non trouvé: " + userDto.getSite()));
                            user.setSite(site);
                        }
                        if (userDto.getRole() != null) {
                            user.setRole(Role.valueOf(userDto.getRole()));
                        }
                        repository.save(user);

                        // Retourner DTO mis à jour
                        UserDto updatedDto = new UserDto();
                        updatedDto.setId(user.getId());
                        updatedDto.setFirstname(user.getFirstname());
                        updatedDto.setLastname(user.getLastname());
                        updatedDto.setEmail(user.getEmail());
                        updatedDto.setMatricule(user.getMatricule());
                        updatedDto.setPassword(user.getPassword());
                        List<String> projetNames = user.getProjets().stream()
                                .map(Projet::getName)
                                .collect(Collectors.toList());
                        updatedDto.setProjets(projetNames);
                        updatedDto.setSite(user.getSiteName());
                        updatedDto.setRole(user.getRole().name());

                        return updatedDto;
                    })
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("❌ Erreur dans updateUser: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // ============================================================
    // MÉTHODES DE RÉINITIALISATION DE MOT DE PASSE
    // ============================================================

    /**
     * Envoie un email de réinitialisation de mot de passe à l'utilisateur.
     * <p>
     * Cette méthode génère un token unique, le persiste en base avec une
     * durée de validité de 24 heures, puis envoie un email contenant
     * le lien de réinitialisation à l'utilisateur.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Vérification de l'existence de l'email</li>
     *     <li>Suppression des anciens tokens de l'utilisateur</li>
     *     <li>Génération d'un nouveau token UUID</li>
     *     <li>Création et persistance du {@link PasswordResetToken}</li>
     *     <li>Construction de l'URL de réinitialisation</li>
     *     <li>Envoi de l'email avec le lien</li>
     * </ol>
     *
     * @param email L'adresse email de l'utilisateur
     * @return {@code true} si l'email a été envoyé (ou l'utilisateur existe),
     *         {@code false} si l'email n'existe pas ou en cas d'erreur
     */
    @Transactional
    public boolean sendPasswordResetEmail(String email) {
        try {
            User user = repository.findByEmail(email).orElse(null);
            if (user == null) {
                System.out.println("❌ Email non trouvé: " + email);
                return false;
            }

            System.out.println("✅ Email trouvé: " + user.getEmail());

            // Supprimer les anciens tokens
            passwordResetTokenRepository.deleteByUserId(user.getId());

            // Créer un nouveau token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // Construire l'URL de réinitialisation
            String resetUrl = baseUrl + "/reset-password?token=" + token;

            System.out.println("=========================================");
            System.out.println("🔐 LIEN DE RÉINITIALISATION");
            System.out.println("Email: " + user.getEmail());
            System.out.println("Token: " + token);
            System.out.println("Lien: " + resetUrl);
            System.out.println("=========================================");

            // Tenter d'envoyer l'email (optionnel)
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(user.getEmail());
                mailMessage.setSubject("Réinitialisation de votre mot de passe - Leoni Test Module System");
                mailMessage.setText(String.format("""
                    Bonjour %s %s,
                    
                    Vous avez demandé la réinitialisation de votre mot de passe.
                    
                    Cliquez sur le lien ci-dessous pour réinitialiser votre mot de passe :
                    %s
                    
                    Ce lien est valable 24 heures.
                    
                    Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                    
                    Cordialement,
                    L'équipe Leoni Test Module System
                    """, user.getFirstname(), user.getLastname(), resetUrl));
                mailMessage.setFrom("noreply@leoni.com");
                mailSender.send(mailMessage);
                System.out.println("✅ Email envoyé avec succès à: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("❌ Erreur d'envoi d'email: " + e.getMessage());
                // On continue car le lien est affiché dans la console
            }

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans sendPasswordResetEmail: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur avec un token valide.
     * <p>
     * Cette méthode vérifie la validité du token (existence, non utilisé,
     * non expiré), puis met à jour le mot de passe de l'utilisateur et
     * invalide tous ses tokens JWT actifs.
     * </p>
     *
     * @param token Le token de réinitialisation (reçu par email)
     * @param newPassword Le nouveau mot de passe (sera encodé avec BCrypt)
     * @return {@code true} si la réinitialisation a réussi,
     *         {@code false} si le token est invalide, expiré ou déjà utilisé
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        try {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

            if (resetToken == null || resetToken.isUsed()) {
                return false;
            }

            if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return false;
            }

            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            repository.save(user);

            // Marquer le token comme utilisé
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);

            // Révoquer tous les anciens tokens JWT de l'utilisateur
            tokenRepository.deleteByUserId(user.getId());

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur dans resetPassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifie si un token de réinitialisation est valide.
     * <p>
     * Cette méthode vérifie que le token existe, n'a pas été utilisé,
     * et n'est pas expiré.
     * </p>
     *
     * @param token Le token de réinitialisation à valider
     * @return {@code true} si le token est valide, {@code false} sinon
     */
    public boolean validateResetToken(String token) {
        try {
            PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

            if (resetToken == null || resetToken.isUsed()) {
                return false;
            }

            return !resetToken.getExpiryDate().isBefore(LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("❌ Erreur dans validateResetToken: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    // ============================================================
    // MÉTHODES DE CHANGEMENT DE MOT DE PASSE
    // ============================================================

    /**
     * Change le mot de passe d'un utilisateur (réservé à l'ADMIN).
     * <p>
     * Cette méthode permet à un administrateur de modifier le mot de passe
     * d'un utilisateur sans connaître l'ancien mot de passe. Utile pour
     * le support ou le déblocage de comptes bloqués.
     * </p>
     *
     * @param userId L'identifiant de l'utilisateur concerné
     * @param newPassword Le nouveau mot de passe (sera encodé avec BCrypt)
     * @return {@code true} si le changement a réussi,
     *         {@code false} si l'utilisateur n'existe pas ou en cas d'erreur
     */
    @Transactional
    public boolean changePassword(Integer userId, String newPassword) {
        try {
            User user = repository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            repository.save(user);

            // Révoquer tous les tokens JWT de l'utilisateur
            tokenRepository.deleteByUserId(userId);

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur changement mot de passe: " + e.getMessage());
            return false;
        }
    }
}
