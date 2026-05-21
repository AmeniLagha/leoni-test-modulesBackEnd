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

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * <p>
 * Ce contrôleur expose les endpoints permettant de gérer les comptes utilisateurs,
 * de récupérer les informations de l'utilisateur courant, de réinitialiser les mots
 * de passe et de gérer les codes de vérification.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Liste des utilisateurs (réservé ADMIN)</li>
 *     <li>Informations de l'utilisateur courant (/me)</li>
 *     <li>Création, modification, suppression d'utilisateurs (ADMIN)</li>
 *     <li>Réinitialisation de mot de passe (forgot-password, reset-password)</li>
 *     <li>Vérification par code de vérification (send-verification-code, verify-code)</li>
 *     <li>Validation de token de réinitialisation</li>
 *     <li>Changement de mot de passe par l'administrateur</li>
 * </ul>
 *
 * <p><strong>Sécurité :</strong>
 * La plupart des endpoints sont protégés par des annotations {@code @PreAuthorize}
 * pour restreindre l'accès selon les rôles et permissions.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see UserService
 * @see UserRepository
 * @see VerificationCodeService
 * @since Sprint 2
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@Tag(name = "Users", description = "Gestion des utilisateurs, récupération info, réinitialisation mot de passe et codes de vérification")
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserRepository repository;
    private final VerificationCodeService verificationCodeService;
    // ============================================================
    // ENDPOINTS - LISTE ET INFORMATIONS UTILISATEURS
    // ============================================================

    /**
     * Récupère la liste de tous les utilisateurs avec leurs permissions.
     * <p>
     * Endpoint réservé aux administrateurs. Retourne une liste d'objets
     * {@link UserDto} contenant les informations de chaque utilisateur
     * ainsi que leurs permissions associées.
     * </p>
     *
     * @return ResponseEntity contenant une {@link ApiResponse} avec la liste
     *         des utilisateurs et leurs permissions
     *
     * @see UserDto
     * @see Permission
     */
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
    /**
     * Récupère les informations de l'utilisateur actuellement connecté.
     * <p>
     * Cet endpoint retourne les informations détaillées de l'utilisateur
     * courant, y compris son identité, son site, son rôle et la liste
     * de ses permissions. Accessible à tout utilisateur authentifié.
     * </p>
     *
     * @return ResponseEntity contenant une {@link ApiResponse} avec les
     *         informations de l'utilisateur connecté (id, nom, email,
     *         matricule, site, rôle, permissions, dates de création/modification)
     *
     * @throws RuntimeException si l'utilisateur n'est pas trouvé en base
     */
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

    // ============================================================
    // ENDPOINTS - GESTION DES UTILISATEURS (CRUD)
    // ============================================================

    /**
     * Supprime un utilisateur par son identifiant.
     * <p>
     * Endpoint réservé aux administrateurs. Supprime définitivement
     * l'utilisateur de la base de données.
     * </p>
     *
     * @param id L'identifiant de l'utilisateur à supprimer
     * @return ResponseEntity contenant une {@link ApiResponse} indiquant
     *         le succès ou l'échec de l'opération
     */
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

    /**
     * Met à jour les informations d'un utilisateur.
     * <p>
     * Endpoint réservé aux administrateurs. Permet de modifier les
     * informations d'un utilisateur existant.
     * </p>
     *
     * @param id L'identifiant de l'utilisateur à modifier
     * @param userDto Les nouvelles informations de l'utilisateur
     * @return ResponseEntity contenant une {@link ApiResponse} avec
     *         les informations mises à jour de l'utilisateur
     */
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
    // ============================================================
    // ENDPOINTS - EMAILS PAR PROJET ET SITE
    // ============================================================

    /**
     * Récupère les adresses email des utilisateurs du même projet.
     * <p>
     * Cette méthode est utilisée pour les notifications par email.
     * Le comportement varie selon le rôle de l'utilisateur courant :
     * <ul>
     *     <li>ADMIN : récupère tous les emails de tous les utilisateurs</li>
     *     <li>Autres rôles : récupère les emails des utilisateurs du même projet</li>
     * </ul>
     * </p>
     *
     * @return ResponseEntity contenant une {@link ApiResponse} avec la liste
     *         des adresses email
     */
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
    /**
     * Récupère les adresses email des utilisateurs filtrés par projet et site.
     * <p>
     * Cette méthode combine deux filtres : le projet et le site de l'utilisateur
     * courant. Elle est utilisée pour les notifications ciblées.
     * </p>
     *
     * @return ResponseEntity contenant une {@link ApiResponse} avec la liste
     *         des adresses email correspondant aux critères
     */
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

    // ============================================================
    // ENDPOINTS - RÉINITIALISATION MOT DE PASSE
    // ============================================================

    /**
     * Demande une réinitialisation de mot de passe.
     * <p>
     * Envoie un email contenant un lien de réinitialisation à l'adresse
     * spécifiée si elle existe dans le système.
     * </p>
     *
     * @param request DTO contenant l'adresse email de l'utilisateur
     * @return ResponseEntity contenant une {@link ApiResponse} avec un message
     *         de confirmation (même si l'email n'existe pas, pour des raisons
     *         de sécurité)
     */
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
    /**
     * Réinitialise le mot de passe avec un token de validation.
     * <p>
     * Cette méthode est appelée après que l'utilisateur a cliqué sur le lien
     * reçu par email et a saisi son nouveau mot de passe.
     * </p>
     *
     * @param request DTO contenant l'email, le nouveau mot de passe et le token
     * @return ResponseEntity contenant une {@link ApiResponse} indiquant le succès
     *         ou l'échec de la réinitialisation
     */
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
    // ============================================================
    // ENDPOINTS - VALIDATION D'EMAIL ET MATRICULE
    // ============================================================

    /**
     * Vérifie si une adresse email existe déjà dans le système.
     * <p>
     * Endpoint utilisé par le frontend pour valider en temps réel
     * l'unicité de l'email lors de la création ou modification d'un utilisateur.
     * </p>
     *
     * @param email L'adresse email à vérifier (paramètre de requête)
     * @return ResponseEntity contenant une {@link ApiResponse} avec un booléen
     *         {@code true} si l'email existe, {@code false} sinon
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
    /**
     * Vérifie si un matricule existe déjà dans le système.
     * <p>
     * Endpoint utilisé par le frontend pour valider en temps réel
     * l'unicité du matricule lors de la création ou modification d'un utilisateur.
     * </p>
     *
     * @param matricule Le matricule à vérifier (paramètre de requête)
     * @return ResponseEntity contenant une {@link ApiResponse} avec un booléen
     *         {@code true} si le matricule existe, {@code false} sinon
     */
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
    // ============================================================
    // ENDPOINTS - CODE DE VÉRIFICATION
    // ============================================================

    /**
     * Envoie un code de vérification par email.
     * <p>
     * Cette méthode est utilisée pour la vérification en deux étapes
     * avant l'envoi du lien de réinitialisation.
     * </p>
     *
     * @param request Map contenant l'adresse email (clé "email")
     * @return ResponseEntity contenant une {@link ApiResponse} indiquant
     *         si le code a bien été envoyé
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
     * Vérifie le code saisi par l'utilisateur.
     * <p>
     * Cette méthode valide le code de vérification envoyé par email
     * avant de permettre la réinitialisation du mot de passe.
     * </p>
     *
     * @param request Map contenant l'adresse email (clé "email") et le code (clé "code")
     * @return ResponseEntity contenant une {@link ApiResponse} avec un booléen
     *         {@code true} si le code est valide, {@code false} sinon
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
     * Envoie le lien de réinitialisation après vérification du code.
     * <p>
     * Cette méthode est appelée après que l'utilisateur a validé son code
     * de vérification. Elle envoie alors le vrai lien de réinitialisation.
     * </p>
     *
     * @param request Map contenant l'adresse email (clé "email")
     * @return ResponseEntity contenant une {@link ApiResponse} indiquant
     *         si le lien a bien été envoyé
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

    // ============================================================
    // ENDPOINTS - VALIDATION DE TOKEN
    // ============================================================

    /**
     * Valide un token de réinitialisation de mot de passe.
     * <p>
     * Cette méthode vérifie si un token de réinitialisation est valide
     * (non expiré, non utilisé, existant). Elle est utilisée par le frontend
     * pour vérifier le lien avant d'afficher le formulaire de réinitialisation.
     * </p>
     *
     * @param token Le token de réinitialisation à valider (paramètre de requête)
     * @return ResponseEntity contenant une {@link ApiResponse} avec un booléen
     *         {@code true} si le token est valide, {@code false} sinon
     */
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

    // ============================================================
    // ENDPOINTS - CHANGEMENT DE MOT DE PASSE PAR ADMIN
    // ============================================================

    /**
     * Change le mot de passe d'un utilisateur (réservé ADMIN).
     * <p>
     * Permet à un administrateur de modifier le mot de passe d'un utilisateur
     * sans connaître l'ancien mot de passe. Utile pour le support ou le
     * déblocage de comptes.
     * </p>
     *
     * @param id L'identifiant de l'utilisateur concerné
     * @param request Map contenant le nouveau mot de passe (clé "newPassword")
     * @return ResponseEntity contenant une {@link ApiResponse} indiquant
     *         le succès ou l'échec de l'opération
     */
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

