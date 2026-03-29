package com.example.security.user;
import com.example.security.token.TokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    @Value("${app.base-url}")
    private String baseUrl;
    public List<UserDto> getUserListWithPermissions() {
        List<User> userList = repository.findAll();
        List<UserDto> usersWithPermissions = new ArrayList<>();

        for (User user : userList) {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setFirstname(user.getFirstname());
            userDto.setLastname(user.getLastname());
            userDto.setMatricule(user.getMatricule());
            userDto.setProjet(user.getProjet());
            userDto.setEmail(user.getEmail());
            userDto.setRole(user.getRole().toString());

            List<String> perms = user.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .toList();
            userDto.setPermissions(perms);

            usersWithPermissions.add(userDto);
        }

        return usersWithPermissions;
    }
    // --- Supprimer un utilisateur ---
    public boolean deleteUser(Integer id) {
        if (!repository.existsById(id)) {
            return false;
        }
        tokenRepository.deleteByUserId(id);
        repository.deleteById(id);

        return true;
    }
    public boolean checkEmailExists(String email) {
        return repository.findByEmail(email).isPresent();
    }

    // --- Mettre à jour un utilisateur ---
    public UserDto updateUser(Integer id, UserDto userDto) {
        return repository.findById(id)
                .map(user -> {
                    user.setFirstname(userDto.getFirstname());
                    user.setLastname(userDto.getLastname());
                    user.setEmail(userDto.getEmail());
                    user.setProjet(userDto.getProjet());
                    user.setMatricule(userDto.getMatricule());
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
                    updatedDto.setRole(user.getRole().name());

                    return updatedDto;
                })
                .orElse(null);
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
    }

    /**
     * Vérifie si un token de réinitialisation est valide
     */
    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElse(null);

        if (resetToken == null || resetToken.isUsed()) {
            return false;
        }

        return !resetToken.getExpiryDate().isBefore(LocalDateTime.now());
    }

}
