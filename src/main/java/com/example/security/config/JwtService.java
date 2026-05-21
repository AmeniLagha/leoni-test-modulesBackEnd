package com.example.security.config;

import com.example.security.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * Service pour la gestion des jetons JWT (JSON Web Tokens).
 * <p>
 * Cette classe fournit les opérations nécessaires à la création, la validation
 * et l'extraction d'informations des jetons JWT utilisés pour l'authentification
 * et l'autorisation dans l'application.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li><strong>Génération de tokens</strong> : Access token (courte durée) et Refresh token (longue durée)</li>
 *     <li><strong>Extraction d'informations</strong> : Nom d'utilisateur, claims, expiration, etc.</li>
 *     <li><strong>Validation de tokens</strong> : Vérification de la signature, de l'expiration et de la correspondance utilisateur</li>
 *     <li><strong>Enrichissement des claims</strong> : Inclusion des rôles, permissions, projets et informations utilisateur</li>
 * </ul>
 *
 * <p><strong>Structure des claims JWT :</strong></p>
 * <pre>
 * {
 *   "sub": "user@email.com",
 *   "iat": 1705315200,
 *   "exp": 1705317000,
 *   "authorities": ["ROLE_ING", "charge_sheet:basic:create", "charge_sheet:basic:read"],
 *   "roles": ["ROLE_ING"],
 *   "permissions": ["charge_sheet:basic:create", "charge_sheet:basic:read"],
 *   "role": "ING",
 *   "firstname": "Jean",
 *   "lastname": "Dupont",
 *   "projets": ["Mercedes", "BMW"],
 *   "projet": "Mercedes",
 *   "siteName": "Manzel Hayet",
 *   "fullName": "Jean Dupont"
 * }
 * </pre>
 *
 * <p><strong>Configuration (application.properties) :</strong></p>
 * <pre>
 * application.security.jwt.secret-key = votreCleSecreteBase64
 * application.security.jwt.expiration = 900000      # 15 minutes
 * application.security.jwt.refresh-token.expiration = 604800000  # 7 jours
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see JwtAuthenticationFilter
 * @see com.example.security.auth.AuthenticationService
 * @since Sprint 2
 */
@Service
public class JwtService {
    /**
     * Clé secrète pour signer les jetons JWT.
     * <p>
     * Doit être une chaîne Base64 d'au moins 256 bits (32 caractères).
     * Configurée via {@code application.security.jwt.secret-key}.
     * </p>
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    /**
     * Durée de validité de l'access token en millisecondes.
     * <p>
     * Valeur typique : 15 minutes (900000 ms).
     * Configurée via {@code application.security.jwt.expiration}.
     * </p>
     */
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    /**
     * Durée de validité du refresh token en millisecondes.
     * <p>
     * Valeur typique : 7 jours (604800000 ms).
     * Configurée via {@code application.security.jwt.refresh-token.expiration}.
     * </p>
     */
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;
    // ============================================================
    // EXTRACTION D'INFORMATIONS DU TOKEN
    // ============================================================

    /**
     * Extrait le nom d'utilisateur (email) du token JWT.
     * <p>
     * Le nom d'utilisateur est stocké dans la claim {@code sub} (subject).
     * </p>
     *
     * @param token Le token JWT
     * @return L'email de l'utilisateur extrait du token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    /**
     * Extrait une claim spécifique du token JWT.
     * <p>
     * Cette méthode générique permet d'extraire n'importe quelle claim
     * en fournissant une fonction de résolution appropriée.
     * </p>
     *
     * @param token Le token JWT
     * @param claimsResolver Fonction pour extraire la claim souhaitée
     * @param <T> Le type de la claim extraite
     * @return La valeur de la claim extraite
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    // ============================================================
    // GÉNÉRATION DE TOKENS
    // ============================================================

    /**
     * Génère un access token JWT pour un utilisateur.
     * <p>
     * Cette méthode enrichit le token avec les claims suivantes :
     * <ul>
     *     <li>authorities : Toutes les autorités (rôles + permissions)</li>
     *     <li>roles : Uniquement les rôles (préfixés par ROLE_)</li>
     *     <li>permissions : Uniquement les permissions granulaires</li>
     *     <li>role : Le rôle principal (ex: ING, PT, ADMIN)</li>
     *     <li>firstname : Prénom de l'utilisateur</li>
     *     <li>lastname : Nom de l'utilisateur</li>
     *     <li>projets : Liste des projets associés</li>
     *     <li>projet : Premier projet (pour compatibilité)</li>
     *     <li>siteName : Site de production</li>
     *     <li>fullName : Nom complet (prénom + nom)</li>
     * </ul>
     * </p>
     *
     * @param userDetails Les détails de l'utilisateur (implémente {@link UserDetails})
     * @return Le token JWT généré
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Extraire TOUTES les authorities (rôles + permissions)
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Ajouter les authorities au token
        claims.put("authorities", authorities);

        // Séparer les rôles des permissions
        List<String> roles = authorities.stream()
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        List<String> permissions = authorities.stream()
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        claims.put("roles", roles);
        claims.put("permissions", permissions);

        // Ajouter le rôle principal
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("role", user.getRole().name());
            claims.put("firstname", user.getFirstname());
            claims.put("lastname", user.getLastname());
            // ✅ CORRECTION : Utiliser getProjetName() qui retourne String
            List<String> projetNames = user.getProjetNamesAsList();
            claims.put("projets", projetNames);  // Liste des projets

            // Pour compatibilité avec l'ancien code, garder un projet principal
            if (projetNames != null && !projetNames.isEmpty()) {
                claims.put("projet", projetNames.get(0));  // Premier projet
            }

            if (user.getSite() != null) {
                claims.put("siteName", user.getSite().getName());
            }
            // Optionnel : fullName
            claims.put("fullName", user.getFirstname() + " " + user.getLastname());
        }

        return generateToken(claims, userDetails);
    }

    /**
     * Génère un token JWT avec des claims supplémentaires.
     * <p>
     * Cette méthode est utilisée en interne par {@link #generateToken(UserDetails)}
     * pour construire le token avec les claims enrichis.
     * </p>
     *
     * @param extraClaims Claims supplémentaires à inclure dans le token
     * @param userDetails Les détails de l'utilisateur
     * @return Le token JWT généré
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }
    /**
     * Génère un refresh token JWT (longue durée).
     * <p>
     * Le refresh token contient moins de claims que l'access token
     * car il est utilisé uniquement pour obtenir de nouveaux access tokens.
     * </p>
     *
     * @param userDetails Les détails de l'utilisateur
     * @return Le refresh token JWT généré
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }
    /**
     * Construit un token JWT avec les paramètres spécifiés.
     * <p>
     * Méthode interne qui construit le token en utilisant l'algorithme HS256
     * et la clé secrète configurée.
     * </p>
     *
     * @param extraClaims Claims supplémentaires
     * @param userDetails Les détails de l'utilisateur
     * @param expiration Durée de validité du token en millisecondes
     * @return Le token JWT construit
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
// ============================================================
    // VALIDATION DE TOKENS
    // ============================================================

    /**
     * Vérifie si un token JWT est valide pour un utilisateur donné.
     * <p>
     * Un token est considéré comme valide si :
     * <ul>
     *     <li>Le nom d'utilisateur extrait du token correspond à celui de l'utilisateur</li>
     *     <li>Le token n'est pas expiré</li>
     * </ul>
     * </p>
     *
     * @param token Le token JWT à valider
     * @param userDetails Les détails de l'utilisateur
     * @return {@code true} si le token est valide, {@code false} sinon
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    /**
     * Vérifie si un token JWT a expiré.
     *
     * @param token Le token JWT à vérifier
     * @return {@code true} si le token est expiré, {@code false} sinon
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    /**
     * Extrait la date d'expiration du token JWT.
     *
     * @param token Le token JWT
     * @return La date d'expiration
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    /**
     * Extrait toutes les claims du token JWT.
     * <p>
     * Cette méthode parse le token, vérifie sa signature et retourne
     * l'ensemble des claims qu'il contient.
     * </p>
     *
     * @param token Le token JWT
     * @return Les claims du token
     * @throws io.jsonwebtoken.ExpiredJwtException Si le token est expiré
     * @throws io.jsonwebtoken.MalformedJwtException Si le token est mal formé
     * @throws io.jsonwebtoken.security.SignatureException Si la signature est invalide
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    // ============================================================
    // GESTION DE LA CLÉ DE SIGNATURE
    // ============================================================

    /**
     * Récupère la clé de signature pour signer et valider les jetons JWT.
     * <p>
     * La clé secrète configurée (en Base64) est décodée et convertie en
     * objet {@link Key} utilisable par JJWT.
     * </p>
     *
     * @return La clé de signature HMAC-SHA256
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }



}
