package com.example.security.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users", description = "Gestion des utilisateurs, récupération info, réinitialisation mot de passe et codes de vérification")
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserRepository repository;
    private final VerificationCodeService verificationCodeService;
    @GetMapping
    @Operation(summary = "Lister tous les utilisateurs", description = "Récupère tous les utilisateurs avec leurs permissions")
    @PreAuthorize("hasAuthority('admin:readuser')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> usersWithPermissions = service.getUserListWithPermissions();
        return ResponseEntity.ok(usersWithPermissions);
    }

    @GetMapping("/me")
    @Operation(summary = "Infos utilisateur courant", description = "Récupère les informations de l'utilisateur connecté")
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
        userInfo.put("site", user.getSiteName());
        userInfo.put("role", user.getRole().name());
        userInfo.put("permissions", user.getRole().getPermissions().stream()
                .map(Permission::getPermission)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(userInfo);
    }

    // --- Supprimer un utilisateur ---
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur par ID (ADMIN uniquement)")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {

        boolean deleted = service.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // --- Mettre à jour un utilisateur ---
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un utilisateur", description = "Met à jour les informations d'un utilisateur par ID (ADMIN uniquement)")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        UserDto updatedUser = service.updateUser(id, userDto);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }
    // UserController.java - Modifier getProjectEmails()

    @GetMapping("/project-emails")
    @Operation(summary = "Emails du projet", description = "Récupère les emails des utilisateurs selon le projet et le rôle de l'utilisateur courant")
    public List<String> getProjectEmails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        System.out.println("📧 Récupération des emails pour l'utilisateur: " + email);

        User currentUser = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("👤 Utilisateur trouvé: " + currentUser.getEmail());
        System.out.println("🏢 Projets: " + currentUser.getProjetsNames());
        System.out.println("👑 Rôle: " + currentUser.getRole());

        List<String> emails;

        // ✅ Si ADMIN → voir TOUS les emails
        if (currentUser.getRole() == Role.ADMIN) {
            emails = repository.findAllUserEmails();
            System.out.println("🔓 ADMIN - Tous les emails: " + emails);
        }
        // ✅ Sinon → emails du premier projet de l'utilisateur + ADMIN
        else {
            // Récupérer le premier projet de l'utilisateur (ou tous)
            String userProject = currentUser.getProjetsNames();

            if (userProject == null || userProject.isEmpty()) {
                // Si l'utilisateur n'a pas de projet, retourner seulement les admins
                emails = repository.findActiveUserEmailsByProjet("ADMIN");
            } else {
                emails = repository.findActiveUserEmailsByProjetExcludingCurrent(
                        userProject,
                        currentUser.getEmail()
                );
            }
            System.out.println("🔒 Non-ADMIN - Emails du projet: " + emails);
        }

        System.out.println("📨 Emails trouvés: " + emails.size());

        return emails;
    }
    @GetMapping("/project-site-emails")
    public ResponseEntity<List<String>> getEmailsByProjectAndSite() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        List<String> emails;

        // Si ADMIN, voir tous les emails
        if (currentUser.getRole().name().equals("ADMIN")) {
            emails = repository.findAllUserEmails();
        } else {
            // Pour les autres rôles, filtrer par site ET projet
            String userSite = currentUser.getSiteName();
            String userProjectsString = currentUser.getProjetsNames();

            // Récupérer la liste des projets de l'utilisateur
            List<String> userProjects = userProjectsString != null ?
                    java.util.Arrays.asList(userProjectsString.split(", ")) :
                    java.util.List.of();

            if (userProjects.isEmpty()) {
                emails = java.util.List.of();
            } else {
                // Récupérer les emails des utilisateurs qui ont au moins un projet en commun ET le même site
                emails = repository.findEmailsByProjectsAndSite(userProjects, userSite);
            }
        }

        return ResponseEntity.ok(emails);
    }

    // --- Réinitialisation mot de passe ---
    @PostMapping("/forgot-password")
    @Operation(summary = "Demander réinitialisation mot de passe", description = "Envoie un email pour réinitialiser le mot de passe")
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
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser mot de passe", description = "Réinitialise le mot de passe via un token")
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
     * Vérifier si un email existe
     */
    @GetMapping("/check-email")
    @Operation(summary = "Vérifier si email existe", description = "Retourne true si l'email existe dans le système")
    public ResponseEntity<Map<String, Boolean>> checkEmailExists(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        boolean exists = service.checkEmailExists(email);
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    // --- Gestion code de vérification ---
    /**
     * Envoyer un code de vérification par email
     */
    /**
     * Vérifier l'email et envoyer un code de vérification
     */
    @PostMapping("/send-verification-code")
    @Operation(summary = "Envoyer code de vérification", description = "Envoie un code à l'email pour validation")
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
    @Operation(summary = "Vérifier code", description = "Vérifie le code saisi par l'utilisateur")
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
    @Operation(summary = "Envoyer lien réinitialisation", description = "Envoie le lien de réinitialisation après vérification du code")

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

    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changeUserPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String newPassword = request.get("newPassword");

        // Vérifier que l'utilisateur est ADMIN
        if (!currentUser.getRole().name().equals("ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul l'administrateur peut modifier les mots de passe"));
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le nouveau mot de passe est requis"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe doit contenir au moins 6 caractères"));
        }

        boolean success = service.changePassword(id, newPassword);

        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mot de passe modifié avec succès");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Utilisateur non trouvé"));
        }
    }






}

