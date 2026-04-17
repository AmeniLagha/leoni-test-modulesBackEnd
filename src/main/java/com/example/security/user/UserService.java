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
    // --- Supprimer un utilisateur ---
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
    public boolean checkEmailExists(String email) {
        return repository.findByEmail(email).isPresent();
    }

    // --- Mettre à jour un utilisateur ---
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
    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    /**
     * Envoie un email de réinitialisation de mot de passe
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
     * Réinitialise le mot de passe avec un token valide
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
     * Vérifie si un token de réinitialisation est valide
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
    // Dans UserService.java

    /**
     * Changer le mot de passe d'un utilisateur (par ADMIN)
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

    /**
     * Changer son propre mot de passe
     */
    @Transactional
    public boolean changeMyPassword(Integer userId, String oldPassword, String newPassword) {
        try {
            User user = repository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }

            // Vérifier l'ancien mot de passe
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return false;
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            repository.save(user);

            // Révoquer tous les tokens JWT de l'utilisateur (déconnexion forcée)
            tokenRepository.deleteByUserId(userId);

            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur changement mot de passe: " + e.getMessage());
            return false;
        }
    }

}
