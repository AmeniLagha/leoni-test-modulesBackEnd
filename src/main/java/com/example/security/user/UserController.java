package com.example.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserRepository repository;
    private final VerificationCodeService verificationCodeService;
    @GetMapping
    @PreAuthorize("hasAuthority('admin:readuser')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> usersWithPermissions = service.getUserListWithPermissions();
        return ResponseEntity.ok(usersWithPermissions);
    }

    @GetMapping("/getUsers")
    @PreAuthorize("hasAuthority('admin:readuser')")
    public ResponseEntity<List<UserDto>> getUserListWithPermissions() {
        List<UserDto> usersWithPermissions = service.getUserListWithPermissions();

        return ResponseEntity.ok(usersWithPermissions);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName(); // ✅ récupère le username/email

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("firstname", user.getFirstname());
        userInfo.put("lastname", user.getLastname());
        userInfo.put("email", user.getEmail());
        userInfo.put("matricule", user.getMatricule());
        userInfo.put("role", user.getRole().name());
        userInfo.put("permissions", user.getRole().getPermissions().stream()
                .map(Permission::getPermission)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(userInfo);
    }

    // --- Supprimer un utilisateur ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {

        boolean deleted = service.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // --- Mettre à jour un utilisateur ---
    @PreAuthorize("hasRole('ADMIN')")

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        UserDto updatedUser = service.updateUser(id, userDto);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }
    @GetMapping("/project-emails")
    public List<String> getProjectEmails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        System.out.println("📧 Récupération des emails pour l'utilisateur: " + email);

        User currentUser = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("👤 Utilisateur trouvé: " + currentUser.getEmail());
        System.out.println("🏢 Projet: " + currentUser.getProjet());
        System.out.println("👑 Rôle: " + currentUser.getRole());

        List<String> emails;

        // ✅ Si ADMIN → voir TOUS les emails
        if (currentUser.getRole() == Role.ADMIN) {
            emails = repository.findAllUserEmails(); // Tous les emails
            System.out.println("🔓 ADMIN - Tous les emails: " + emails);
        }
        // ✅ Sinon → emails du projet + ADMIN
        else {
            emails = repository.findActiveUserEmailsByProjetExcludingCurrent(
                    currentUser.getProjet(),
                    currentUser.getEmail()
            );
            System.out.println("🔒 Non-ADMIN - Emails du projet: " + emails);
        }

        System.out.println("📨 Emails trouvés: " + emails.size());

        return emails;
    }
    /**
     * Endpoint pour demander la réinitialisation du mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody PasswordResetRequestDto request) {
        Map<String, String> response = new HashMap<>();

        try {
            boolean sent = service.sendPasswordResetEmail(request.getEmail());

            if (sent) {
                response.put("message", "Un email de réinitialisation a été envoyé si l'adresse existe dans notre système.");
                return ResponseEntity.ok(response);
            } else {
                // Pour des raisons de sécurité, on renvoie le même message
                response.put("message", "Un email de réinitialisation a été envoyé si l'adresse existe dans notre système.");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            System.err.println("Erreur dans forgot-password: " + e.getMessage());
            e.printStackTrace();
            response.put("message", "Une erreur technique est survenue. Veuillez réessayer plus tard.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = service.validateResetToken(token);
            Map<String, Boolean> response = new HashMap<>();
            response.put("valid", isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Erreur dans validate-reset-token: " + e.getMessage());
            Map<String, Boolean> response = new HashMap<>();
            response.put("valid", false);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetDto request) {
        Map<String, String> response = new HashMap<>();

        try {
            boolean reset = service.resetPassword(request.getResetToken(), request.getNewPassword());

            if (reset) {
                response.put("message", "Votre mot de passe a été réinitialisé avec succès.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Token invalide ou expiré.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            System.err.println("Erreur dans reset-password: " + e.getMessage());
            e.printStackTrace();
            response.put("message", "Une erreur technique est survenue. Veuillez réessayer plus tard.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * Envoyer un code de vérification par email
     */
    /**
     * Vérifier l'email et envoyer un code de vérification
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<Map<String, String>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();

        boolean sent = verificationCodeService.sendVerificationCode(email);

        if (sent) {
            response.put("message", "Un code de vérification a été envoyé à votre email.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Email non trouvé dans notre système.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vérifier le code saisi par l'utilisateur
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Boolean>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean isValid = verificationCodeService.verifyCode(email, code);

        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);

        return ResponseEntity.ok(response);
    }

    /**
     * Envoyer le lien de réinitialisation (après vérification du code)
     */
    @PostMapping("/send-reset-link")
    public ResponseEntity<Map<String, String>> sendResetLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();

        // Vérifier d'abord que l'utilisateur a bien validé son code
        // On pourrait vérifier s'il existe un code validé récemment
        boolean sent = service.sendPasswordResetEmail(email);

        if (sent) {
            response.put("message", "Un lien de réinitialisation a été envoyé à votre email.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Email non trouvé dans notre système.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vérifier si un email existe
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        boolean exists = service.checkEmailExists(email);
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}

