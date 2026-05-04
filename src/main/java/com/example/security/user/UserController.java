package com.example.security.user;

import com.example.security.common.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> usersWithPermissions = service.getUserListWithPermissions();

        ApiResponse<List<UserDto>> response = ApiResponse.success(
                "Liste des utilisateurs récupérée avec succès",
                usersWithPermissions
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Infos utilisateur courant", description = "Récupère les informations de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            ApiResponse<Map<String, Object>> errorResponse = ApiResponse.error(
                    "Utilisateur non authentifié",
                    HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        String email = authentication.getName();
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
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("updatedAt", user.getUpdatedAt());
        userInfo.put("permissions", user.getRole().getPermissions().stream()
                .map(Permission::getPermission)
                .collect(Collectors.toList()));

        ApiResponse<Map<String, Object>> response = ApiResponse.success(
                "Informations utilisateur récupérées avec succès",
                userInfo
        );
        return ResponseEntity.ok(response);
    }

    // --- Supprimer un utilisateur ---
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur par ID (ADMIN uniquement)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Integer id) {
        boolean deleted = service.deleteUser(id);
        if (!deleted) {
            ApiResponse<Void> response = ApiResponse.error(
                    "Utilisateur non trouvé",
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiResponse<Void> response = ApiResponse.success("Utilisateur supprimé avec succès");
        return ResponseEntity.ok(response);
    }

    // --- Mettre à jour un utilisateur ---
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier un utilisateur", description = "Met à jour les informations d'un utilisateur par ID (ADMIN uniquement)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Integer id, @RequestBody UserDto userDto) {
        UserDto updatedUser = service.updateUser(id, userDto);
        if (updatedUser == null) {
            ApiResponse<UserDto> response = ApiResponse.error(
                    "Utilisateur non trouvé",
                    HttpStatus.NOT_FOUND.value()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiResponse<UserDto> response = ApiResponse.success(
                "Utilisateur mis à jour avec succès",
                updatedUser
        );
        return ResponseEntity.ok(response);
    }
    // UserController.java - Modifier getProjectEmails()

    @GetMapping("/project-emails")
    @Operation(summary = "Emails du projet", description = "Récupère les emails des utilisateurs selon le projet et le rôle de l'utilisateur courant")
    public ResponseEntity<ApiResponse<List<String>>> getProjectEmails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> emails;

        if (currentUser.getRole() == Role.ADMIN) {
            emails = repository.findAllUserEmails();
        } else {
            String userProject = currentUser.getProjetsNames();

            if (userProject == null || userProject.isEmpty()) {
                emails = repository.findActiveUserEmailsByProjet("ADMIN");
            } else {
                emails = repository.findActiveUserEmailsByProjetExcludingCurrent(
                        userProject,
                        currentUser.getEmail()
                );
            }
        }

        ApiResponse<List<String>> response = ApiResponse.success(
                "Emails du projet récupérés avec succès",
                emails
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project-site-emails")
    public ResponseEntity<ApiResponse<List<String>>> getEmailsByProjectAndSite() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();

        List<String> emails;

        if (currentUser.getRole().name().equals("ADMIN")) {
            emails = repository.findAllUserEmails();
        } else {
            String userSite = currentUser.getSiteName();
            String userProjectsString = currentUser.getProjetsNames();

            List<String> userProjects = userProjectsString != null ?
                    java.util.Arrays.asList(userProjectsString.split(", ")) :
                    java.util.List.of();

            if (userProjects.isEmpty()) {
                emails = java.util.List.of();
            } else {
                emails = repository.findEmailsByProjectsAndSite(userProjects, userSite);
            }
        }

        ApiResponse<List<String>> response = ApiResponse.success(
                "Emails par projet et site récupérés avec succès",
                emails
        );
        return ResponseEntity.ok(response);
    }

    // --- Réinitialisation mot de passe ---
    @PostMapping("/forgot-password")
    @Operation(summary = "Demander réinitialisation mot de passe", description = "Envoie un email pour réinitialiser le mot de passe")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@RequestBody PasswordResetRequestDto request) {
        try {
            boolean sent = service.sendPasswordResetEmail(request.getEmail());

            Map<String, String> data = new HashMap<>();
            data.put("message", "Un email de réinitialisation a été envoyé si l'adresse existe dans notre système.");

            ApiResponse<Map<String, String>> response = ApiResponse.success(
                    "Demande de réinitialisation traitée",
                    data
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error(
                    "Une erreur technique est survenue. Veuillez réessayer plus tard.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser mot de passe", description = "Réinitialise le mot de passe via un token")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(@RequestBody PasswordResetDto request) {
        try {
            boolean reset = service.resetPassword(request.getResetToken(), request.getNewPassword());

            Map<String, String> data = new HashMap<>();
            if (reset) {
                data.put("message", "Votre mot de passe a été réinitialisé avec succès.");
                ApiResponse<Map<String, String>> response = ApiResponse.success(
                        "Mot de passe réinitialisé",
                        data
                );
                return ResponseEntity.ok(response);
            } else {
                data.put("message", "Token invalide ou expiré.");
                ApiResponse<Map<String, String>> response = ApiResponse.error(
                        "Token invalide ou expiré",
                        HttpStatus.BAD_REQUEST.value()
                );
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<Map<String, String>> response = ApiResponse.error(
                    "Une erreur technique est survenue. Veuillez réessayer plus tard.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /**
     * Vérifier si un email existe
     */
    @GetMapping("/check-email")
    @Operation(summary = "Vérifier si email existe", description = "Retourne true si l'email existe dans le système")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmailExists(@RequestParam String email) {
        boolean exists = service.checkEmailExists(email);

        Map<String, Boolean> data = new HashMap<>();
        data.put("exists", exists);

        ApiResponse<Map<String, Boolean>> response = ApiResponse.success(
                "Vérification d'email effectuée",
                data
        );
        return ResponseEntity.ok(response);
    }
// UserController.java - Ajouter cette méthode

    @GetMapping("/check-matricule")
    @Operation(summary = "Vérifier si matricule existe", description = "Retourne true si le matricule existe déjà")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkMatriculeExists(@RequestParam Integer matricule) {
        boolean exists = service.checkMatriculeExists(matricule);

        Map<String, Boolean> data = new HashMap<>();
        data.put("exists", exists);

        ApiResponse<Map<String, Boolean>> response = ApiResponse.success(
                "Vérification de matricule effectuée",
                data
        );
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
    public ResponseEntity<ApiResponse<Map<String, String>>> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        boolean sent = verificationCodeService.sendVerificationCode(email);

        Map<String, String> data = new HashMap<>();
        if (sent) {
            data.put("message", "Un code de vérification a été envoyé à votre email.");
            ApiResponse<Map<String, String>> response = ApiResponse.success(
                    "Code envoyé",
                    data
            );
            return ResponseEntity.ok(response);
        } else {
            data.put("message", "Email non trouvé dans notre système.");
            ApiResponse<Map<String, String>> response = ApiResponse.error(
                    "Email non trouvé",
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Vérifier le code saisi par l'utilisateur
     */
    @PostMapping("/verify-code")
    @Operation(summary = "Vérifier code", description = "Vérifie le code saisi par l'utilisateur")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean isValid = verificationCodeService.verifyCode(email, code);

        Map<String, Boolean> data = new HashMap<>();
        data.put("valid", isValid);

        ApiResponse<Map<String, Boolean>> response = ApiResponse.success(
                "Code vérifié",
                data
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Envoyer le lien de réinitialisation (après vérification du code)
     */
    @PostMapping("/send-reset-link")
    @Operation(summary = "Envoyer lien réinitialisation", description = "Envoie le lien de réinitialisation après vérification du code")

    public ResponseEntity<ApiResponse<Map<String, String>>> sendResetLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> data = new HashMap<>();

        boolean sent = service.sendPasswordResetEmail(email);

        if (sent) {
            data.put("message", "Un lien de réinitialisation a été envoyé à votre email.");
            ApiResponse<Map<String, String>> response = ApiResponse.success(
                    "Lien envoyé",
                    data
            );
            return ResponseEntity.ok(response);
        } else {
            data.put("message", "Email non trouvé dans notre système.");
            ApiResponse<Map<String, String>> response = ApiResponse.error(
                    "Email non trouvé",
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = service.validateResetToken(token);
            Map<String, Boolean> data = new HashMap<>();
            data.put("valid", isValid);

            ApiResponse<Map<String, Boolean>> response = ApiResponse.success(
                    "Token validé",
                    data
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Boolean> data = new HashMap<>();
            data.put("valid", false);

            ApiResponse<Map<String, Boolean>> response = ApiResponse.success(
                    "Token invalidé",
                    data
            );
            return ResponseEntity.ok(response);
        }
    }


    @PutMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changeUserPassword(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) auth.getPrincipal();
        String newPassword = request.get("newPassword");

        if (!currentUser.getRole().name().equals("ADMIN")) {
            ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    "Seul l'administrateur peut modifier les mots de passe",
                    HttpStatus.FORBIDDEN.value()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    "Le nouveau mot de passe est requis",
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.badRequest().body(response);
        }

        if (newPassword.length() < 6) {
            ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    "Le mot de passe doit contenir au moins 6 caractères",
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = service.changePassword(id, newPassword);

        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("message", "Mot de passe modifié avec succès");

            ApiResponse<Map<String, Object>> response = ApiResponse.success(
                    "Mot de passe modifié",
                    data
            );
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Map<String, Object>> response = ApiResponse.error(
                    "Utilisateur non trouvé",
                    HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}

