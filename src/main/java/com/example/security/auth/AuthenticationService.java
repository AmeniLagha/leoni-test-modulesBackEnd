package com.example.security.auth;

import com.example.security.config.JwtService;
import com.example.security.projet.Projet;
import com.example.security.projet.ProjetRepository;
import com.example.security.site.Site;
import com.example.security.site.SiteRepository;
import com.example.security.token.Token;
import com.example.security.token.TokenRepository;
import com.example.security.token.TokenType;
import com.example.security.user.Role;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Service d'authentification pour la gestion des comptes utilisateurs et des tokens JWT.
 * <p>
 * Cette classe contient la logique métier pour l'inscription, l'authentification
 * et le rafraîchissement des jetons JWT. Elle gère également la validation des
 * règles métier spécifiques (unicité email/matricule, validation site/projets selon rôle).
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Inscription d'un nouvel utilisateur (avec validation site/projets pour non-ADMIN)</li>
 *     <li>Authentification et génération de tokens JWT</li>
 *     <li>Rafraîchissement des tokens JWT</li>
 *     <li>Gestion des tokens (persistance, révocation)</li>
 * </ul>
 *
 * <p><strong>Règles métier importantes :</strong></p>
 * <ul>
 *     <li>L'email et le matricule doivent être uniques</li>
 *     <li>L'ADMIN n'a pas besoin de site ni de projets</li>
 *     <li>Les autres rôles (ING, PT, PP, MC, MP) nécessitent un site et au moins un projet</li>
 *     <li>Lors de l'authentification, la vérification du site est obligatoire (sauf pour ADMIN)</li>
 *     <li>Les anciens tokens sont révoqués lors d'une nouvelle authentification</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see AuthenticationController
 * @see AuthenticationRequest
 * @see AuthenticationResponse
 * @see RegisterRequest
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SiteRepository siteRepository;
    private final ProjetRepository projetRepository ;
    // ============================================================
    // MÉTHODE D'INSCRIPTION
    // ============================================================

    /**
     * Inscrit un nouvel utilisateur dans le système.
     * <p>
     * Cette méthode crée un nouveau compte utilisateur après avoir validé
     * l'unicité de l'email et du matricule. Selon le rôle de l'utilisateur,
     * des règles spécifiques s'appliquent :
     * </p>
     *
     * <ul>
     *     <li><strong>ADMIN</strong> : Pas besoin de site ni de projets</li>
     *     <li><strong>Autres rôles (ING, PT, PP, MC, MP)</strong> :
     *         <ul>
     *             <li>Un site est obligatoire et doit exister en base</li>
     *             <li>Au moins un projet est obligatoire et doit exister en base</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Vérification de l'unicité de l'email (sinon erreur 409 CONFLICT)</li>
     *     <li>Vérification de l'unicité du matricule (sinon erreur 409 CONFLICT)</li>
     *     <li>Construction de l'utilisateur avec les informations fournies</li>
     *     <li>Pour les non-ADMIN : validation et association du site et des projets</li>
     *     <li>Encodage du mot de passe avec BCrypt</li>
     *     <li>Persistance de l'utilisateur en base de données</li>
     *     <li>Génération des tokens JWT (access + refresh)</li>
     *     <li>Stockage du refresh token en base</li>
     *     <li>Retour des tokens dans la réponse</li>
     * </ol>
     *
     * @param request Les informations d'inscription (nom, prénom, email, matricule,
     *                mot de passe, rôle, site, projets)
     * @return Un {@link AuthenticationResponse} contenant les tokens JWT
     * @throws ResponseStatusException avec code 409 CONFLICT si l'email ou le matricule existe déjà
     * @throws ResponseStatusException avec code 400 BAD_REQUEST si le site ou les projets sont manquants
     * @throws ResponseStatusException avec code 404 NOT_FOUND si le site ou un projet n'existe pas
     *
     * @see RegisterRequest
     * @see User
     */
    public AuthenticationResponse register(RegisterRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }
        if (repository.existsByMatricule(request.getMatricule())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Matricule déjà utilisé");
        }

        User.UserBuilder userBuilder = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .matricule(request.getMatricule())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .createdAt(LocalDateTime.now());

        // ✅ ADMIN : Pas besoin de site ni projets
        if (request.getRole() == Role.ADMIN) {
            userBuilder.site(null);
            userBuilder.projets(new HashSet<>());
        }
        // ✅ Autres rôles : Validation OBLIGATOIRE
        else {
            // Validation SITE
            if (request.getSiteName() == null || request.getSiteName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le site est obligatoire pour le rôle: " + request.getRole());
            }

            Site site = siteRepository.findByName(request.getSiteName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Site non trouvé: " + request.getSiteName()));
            userBuilder.site(site);

            // Validation PROJETS
            if (request.getProjets() == null || request.getProjets().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Au moins un projet est obligatoire pour le rôle: " + request.getRole());
            }

            Set<Projet> projets = new HashSet<>();
            for (String projetName : request.getProjets()) {
                Projet projet = projetRepository.findByName(projetName)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Projet non trouvé: " + projetName));
                projets.add(projet);
            }
            userBuilder.projets(projets);
        }

        var user = userBuilder.build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
// ============================================================
    // MÉTHODE D'AUTHENTIFICATION
    // ============================================================

    /**
     * Authentifie un utilisateur existant.
     * <p>
     * Cette méthode vérifie les identifiants de l'utilisateur et génère
     * de nouveaux tokens JWT. Les anciens tokens de l'utilisateur sont
     * révoqués pour des raisons de sécurité.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Délégation à {@link AuthenticationManager} pour la validation
     *         des identifiants (email + mot de passe)</li>
     *     <li>Récupération de l'utilisateur depuis la base de données</li>
     *     <li>Pour les non-ADMIN : vérification que le site correspond
     *         (sinon erreur 403 FORBIDDEN)</li>
     *     <li>Génération des nouveaux tokens JWT</li>
     *     <li>Révocation de tous les anciens tokens de l'utilisateur</li>
     *     <li>Persistance du nouveau token</li>
     *     <li>Retour des tokens dans la réponse</li>
     * </ol>
     *
     * <p><strong>Règles spécifiques :</strong></p>
     * <ul>
     *     <li>Le site est obligatoire pour tous les utilisateurs non-ADMIN</li>
     *     <li>L'utilisateur ne peut se connecter qu'à son site de rattachement</li>
     *     <li>L'ADMIN n'a pas de vérification de site (accès global)</li>
     * </ul>
     *
     * @param request Les informations d'authentification (email, mot de passe, siteName)
     * @return Un {@link AuthenticationResponse} contenant les nouveaux tokens JWT
     * @throws ResponseStatusException avec code 400 BAD_REQUEST si le site est manquant pour un non-ADMIN
     * @throws ResponseStatusException avec code 403 FORBIDDEN si l'utilisateur n'a pas accès au site
     *
     * @see AuthenticationRequest
     * @see AuthenticationManager#authenticate(org.springframework.security.core.Authentication)
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();

        // ✅ Vérification du site SEULEMENT si l'utilisateur n'est pas ADMIN
        if (user.getRole() != Role.ADMIN) {
            if (request.getSiteName() == null || request.getSiteName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veuillez sélectionner un site");
            }
            if (user.getSite() == null || !user.getSite().getName().equals(request.getSiteName())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'avez pas accès à ce site");
            }
        }
        // Si ADMIN → pas de vérification de site, il voit tout

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
    // ============================================================
    // MÉTHODES DE GESTION DES TOKENS
    // ============================================================

    /**
     * Persiste un token JWT en base de données.
     * <p>
     * Cette méthode sauvegarde le token généré avec les informations
     * de l'utilisateur associé, le type de token (BEARER) et les flags
     * d'expiration et de révocation initialisés à {@code false}.
     * </p>
     *
     * @param user L'utilisateur associé au token
     * @param jwtToken La valeur du token JWT à persister
     */
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
    /**
     * Révoque tous les tokens valides d'un utilisateur.
     * <p>
     * Cette méthode est appelée lors d'une nouvelle authentification pour
     * invalider tous les tokens précédents. Elle marque les tokens comme
     * expirés et révoqués, empêchant ainsi leur réutilisation.
     * </p>
     *
     * @param user L'utilisateur dont on souhaite révoquer les tokens
     */
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
    // ============================================================
    // MÉTHODE DE RAFRAÎCHISSEMENT DE TOKEN
    // ============================================================

    /**
     * Rafraîchit le token JWT à partir d'un refresh token valide.
     * <p>
     * Cette méthode permet de générer un nouvel access token lorsque l'ancien
     * a expiré, sans que l'utilisateur ait besoin de se reconnecter.
     * </p>
     *
     * <p><strong>Processus :</strong></p>
     * <ol>
     *     <li>Extraction du refresh token depuis le header Authorization</li>
     *     <li>Extraction de l'email depuis le refresh token</li>
     *     <li>Récupération de l'utilisateur correspondant</li>
     *     <li>Validation du refresh token (signature, expiration)</li>
     *     <li>Génération d'un nouvel access token</li>
     *     <li>Révocation des anciens tokens de l'utilisateur</li>
     *     <li>Persistance du nouvel access token</li>
     *     <li>Écriture de la réponse JSON avec les tokens</li>
     * </ol>
     *
     * @param request La requête HTTP contenant le refresh token dans le header Authorization
     * @param response La réponse HTTP dans laquelle écrire les nouveaux tokens
     * @throws IOException En cas d'erreur lors de l'écriture de la réponse JSON
     *
     * @see JwtService#extractUsername(String)
     */
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
