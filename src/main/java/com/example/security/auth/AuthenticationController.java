package com.example.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.security.common.ApiResponse;

/**
 * Contrôleur REST pour la gestion de l'authentification et des tokens JWT.
 * <p>
 * Ce contrôleur expose les endpoints permettant l'inscription, la connexion,
 * le rafraîchissement des tokens JWT et la récupération du token courant.
 * Tous les endpoints sont préfixés par {@code /api/v1/auth}.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li><strong>Inscription (/register)</strong> : Création d'un nouveau compte utilisateur</li>
 *     <li><strong>Connexion (/authenticate)</strong> : Authentification et génération d'un token JWT</li>
 *     <li><strong>Rafraîchissement (/refresh-token)</strong> : Génération d'un nouveau token à partir d'un refresh token valide</li>
 *     <li><strong>Token courant (/current-token)</strong> : Récupération du token JWT de l'utilisateur connecté</li>
 * </ul>
 *
 * <p><strong>Flux d'authentification typique :</strong></p>
 * <ol>
 *     <li>L'utilisateur s'inscrit via {@code POST /register} avec ses informations</li>
 *     <li>L'utilisateur se connecte via {@code POST /authenticate} avec email/mot de passe/site</li>
 *     <li>Le serveur retourne un access token JWT et un refresh token</li>
 *     <li>L'utilisateur inclut l'access token dans le header {@code Authorization: Bearer &lt;token&gt;}</li>
 *     <li>Lorsque l'access token expire, l'utilisateur appelle {@code POST /refresh-token}</li>
 *     <li>Un nouvel access token est généré à partir du refresh token</li>
 * </ol>
 *
 * <p><strong>Sécurité :</strong>
 * L'endpoint {@code /current-token} est protégé et nécessite une authentification
 * préalable (annotation {@code @PreAuthorize("isAuthenticated()")}).</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see AuthenticationService
 * @see AuthenticationRequest
 * @see AuthenticationResponse
 * @see RegisterRequest
 * @since Sprint 2
 */
@RestController
@Tag(name = "Authentification", description = "Gestion de l'authentification et des tokens JWT")
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;
// ============================================================
    // ENDPOINT - Ajouter Utilisateurs
    // ============================================================

    /**
     * Inscrit un nouvel utilisateur dans le système.
     * <p>
     * Cet endpoint permet de créer un nouveau compte utilisateur. Les informations
     * fournies sont validées, l'utilisateur est créé en base de données, et un
     * token JWT (access + refresh) est généré et retourné immédiatement.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Validation des champs obligatoires (nom, prénom, email, matricule, site, rôle)</li>
     *     <li>Vérification de l'unicité de l'email et du matricule</li>
     *     <li>Encodage du mot de passe avec BCrypt</li>
     *     <li>Persistance de l'utilisateur en base de données</li>
     *     <li>Génération d'un access token JWT et d'un refresh token</li>
     *     <li>Retour des tokens dans la réponse</li>
     * </ol>
     *
     * @param request Les informations d'inscription (nom, prénom, email, matricule, site, rôle, mot de passe)
     * @return ResponseEntity contenant un {@link ApiResponse} avec les tokens JWT
     *         et un message de succès, statut HTTP 201 (CREATED)
     *
     * @see RegisterRequest
     * @see AuthenticationResponse
     * @see AuthenticationService#register(RegisterRequest)
     */
    @PostMapping("/register")
    @Operation(
            summary = "Inscription",
            description = "Permet de créer un nouveau compte utilisateur avec génération d’un token JWT"
    )
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse authResponse = service.register(request);

        ApiResponse<AuthenticationResponse> response = ApiResponse.success(
                "Utilisateur créé avec succès",
                authResponse,
                HttpStatus.CREATED.value()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
// ============================================================
    // ENDPOINT - CONNEXION
    // ============================================================

    /**
     * Authentifie un utilisateur existant.
     * <p>
     * Cet endpoint permet à un utilisateur de se connecter en fournissant
     * ses identifiants (email, mot de passe) et son site de rattachement.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Vérification de l'existence de l'utilisateur par email</li>
     *     <li>Validation du mot de passe (comparaison BCrypt)</li>
     *     <li>Vérification que l'utilisateur a accès au site sélectionné</li>
     *     <li>Génération d'un access token JWT et d'un refresh token</li>
     *     <li>Stockage du refresh token en base de données</li>
     *     <li>Retour des tokens dans la réponse</li>
     * </ol>
     *
     * <p><strong>Note de sécurité :</strong>
     * L'email et le site sont vérifiés pour garantir l'isolation multi-sites.</p>
     *
     * @param request Les informations d'authentification (email, mot de passe, site)
     * @return ResponseEntity contenant un {@link ApiResponse} avec les tokens JWT
     *         et un message de succès, statut HTTP 200 (OK)
     *
     * @see AuthenticationRequest
     * @see AuthenticationResponse
     * @see AuthenticationService#authenticate(AuthenticationRequest)
     */
    @PostMapping("/authenticate")
    @Operation(
            summary = "Connexion",
            description = "Authentifier un utilisateur et retourner un token JWT"
    )
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse authResponse = service.authenticate(request);

        ApiResponse<AuthenticationResponse> response = ApiResponse.success(
                "Authentification réussie",
                authResponse
        );

        return ResponseEntity.ok(response);
    }
    // ============================================================
    // ENDPOINT - RAFRAÎCHISSEMENT DE TOKEN
    // ============================================================

    /**
     * Rafraîchit le token JWT à partir d'un refresh token valide.
     * <p>
     * Cet endpoint permet de générer un nouvel access token lorsque l'ancien
     * a expiré, sans que l'utilisateur ait besoin de se reconnecter.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Extraction du refresh token depuis la requête (cookie ou body)</li>
     *     <li>Validation du refresh token (signature, expiration, existence en base)</li>
     *     <li>Récupération de l'utilisateur associé</li>
     *     <li>Génération d'un nouvel access token JWT</li>
     *     <li>Mise à jour du refresh token en base (optionnel)</li>
     *     <li>Retour du nouvel access token dans la réponse</li>
     * </ol>
     *
     * <p><strong>Note d'utilisation :</strong>
     * Cette méthode manipule directement les objets {@link HttpServletRequest}
     * et {@link HttpServletResponse} pour une gestion fine des cookies
     * ou des headers de réponse.</p>
     *
     * @param request La requête HTTP contenant le refresh token
     * @param response La réponse HTTP pour retourner le nouveau token
     * @throws IOException En cas d'erreur lors de l'écriture de la réponse
     *
     * @see AuthenticationService#refreshToken(HttpServletRequest, HttpServletResponse)
     */
    @Operation(
            summary = "Rafraîchir le token",
            description = "Générer un nouveau token JWT à partir d’un refresh token valide"
    )
    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        service.refreshToken(request, response);
    }
// ============================================================
    // ENDPOINT - TOKEN COURANT
    // ============================================================

    /**
     * Récupère le token JWT de l'utilisateur actuellement connecté.
     * <p>
     * Cet endpoint retourne le token JWT présent dans le header
     * {@code Authorization} de la requête. Il est utile pour les opérations
     * nécessitant de récupérer le token côté frontend.
     * </p>
     *
     * <p><strong>Conditions d'accès :</strong></p>
     * <ul>
     *     <li>L'utilisateur doit être authentifié (token valide)</li>
     *     <li>Le header {@code Authorization} doit être présent et au format {@code Bearer &lt;token&gt;}</li>
     * </ul>
     *
     * @param request La requête HTTP contenant le header Authorization
     * @return ResponseEntity contenant un {@link ApiResponse} avec le token JWT
     *         en cas de succès, ou un message d'erreur si le token est absent
     *
     * <p><strong>Réponses possibles :</strong></p>
     * <ul>
     *     <li><strong>200 OK</strong> : Token récupéré avec succès</li>
     *     <li><strong>400 Bad Request</strong> : Header Authorization manquant ou invalide</li>
     *     <li><strong>401 Unauthorized</strong> : Utilisateur non authentifié (géré par Spring Security)</li>
     * </ul>
     */
    @GetMapping("/current-token")
    @Operation(
            summary = "Récupérer le token actuel",
            description = "Retourne le token JWT de l’utilisateur connecté à partir du header Authorization"
    )
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCurrentToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("token", authHeader.substring(7));

            ApiResponse<Map<String, String>> response = ApiResponse.success(
                    "Token récupéré avec succès",
                    tokenData
            );
            return ResponseEntity.ok(response);
        }

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                "Aucun token trouvé",
                HttpStatus.BAD_REQUEST.value(),
                "Authorization",
                "Le header Authorization est manquant ou invalide"
        );

        return ResponseEntity.badRequest().body(response);
    }
}