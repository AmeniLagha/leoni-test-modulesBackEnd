package com.example.security.config;

import com.example.security.token.TokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * Filtre d'authentification JWT pour l'application.
 * <p>
 * Ce filtre intercepte chaque requête HTTP entrante pour extraire, valider
 * et authentifier les jetons JWT. Il s'exécute une fois par requête
 * (d'où l'extension de {@link OncePerRequestFilter}) et s'intègre dans
 * la chaîne de filtres Spring Security avant le filtre d'authentification
 * standard.
 * </p>
 *
 * <p><strong>Fonctionnement :</strong></p>
 * <ol>
 *     <li>Vérifie si la requête concerne un endpoint public (/api/v1/auth/*)
 *         → Si oui, laisse passer sans authentification</li>
 *     <li>Extrait le token JWT du header {@code Authorization: Bearer &lt;token&gt;}</li>
 *     <li>Extrait l'email de l'utilisateur depuis le token</li>
 *     <li>Charge les informations de l'utilisateur depuis la base de données</li>
 *     <li>Vérifie la validité du token (signature, expiration, existence en base)</li>
 *     <li>Si valide, crée un contexte d'authentification Spring Security</li>
 *     <li>Continue la chaîne de filtres</li>
 * </ol>
 *
 * <p><strong>Mécanismes de sécurité :</strong></p>
 * <ul>
 *     <li>Vérification de la présence du token dans la table {@code token}
 *         pour s'assurer qu'il n'a pas été révoqué</li>
 *     <li>Vérification que le token n'est ni expiré ni révoqué</li>
 *     <li>Validation de la signature JWT pour prévenir les falsifications</li>
 *     <li>Gestion explicite des exceptions JWT (expiration, signature invalide, etc.)</li>
 * </ul>
 *
 * <p><strong>Codes de réponse en cas d'erreur :</strong></p>
 * <ul>
 *     <li><strong>401 Unauthorized</strong> : Token expiré → message "Token expired"</li>
 *     <li><strong>401 Unauthorized</strong> : Token invalide → message "Invalid token"</li>
 * </ul>
 *
 * @author Votre Nom
 * @version 1.0
 * @see JwtService
 * @see SecurityConfiguration
 * @see OncePerRequestFilter
 * @since Sprint 2
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    /**
     * Méthode principale du filtre, exécutée pour chaque requête HTTP.
     * <p>
     * Cette méthode intercepte la requête, vérifie la présence d'un token JWT valide,
     * authentifie l'utilisateur si le token est valide, ou laisse passer sans
     * authentification pour les endpoints publics.
     * </p>
     *
     * @param request La requête HTTP entrante
     * @param response La réponse HTTP sortante
     * @param filterChain La chaîne de filtres à continuer
     * @throws ServletException En cas d'erreur de traitement de la requête
     * @throws IOException En cas d'erreur d'entrée/sortie
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ============================================================
        // ÉTAPE 1 : Autoriser les endpoints d'authentification
        // ============================================================

        /**
         * Laisse passer les requêtes vers les endpoints publics d'authentification
         * (inscription, connexion, rafraîchissement de token) sans vérification de token.
         */
        if (request.getServletPath().contains("/api/v1/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }
        // ============================================================
        // ÉTAPE 2 : Extraction du token JWT
        // ============================================================
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        /**
         * Vérifie la présence et le format du header Authorization.
         * Le header doit être au format : {@code Authorization: Bearer <token>}
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Extraire le token (supprimer le préfixe "Bearer ")
        jwt = authHeader.substring(7);
        // ============================================================
        // ÉTAPE 3 : Validation du token et authentification
        // ============================================================
        try {
            userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                /**
                 * Vérification supplémentaire : le token doit exister en base
                 * et ne pas être expiré ou révoqué.
                 * Cela permet de révoquer des tokens (ex: déconnexion).
                 */
                var isTokenValid = tokenRepository.findByToken(jwt)
                        .map(t -> !t.isExpired() && !t.isRevoked())
                        .orElse(false);

                if (jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
                    /**
                     * Création du token d'authentification Spring Security
                     * avec les détails de l'utilisateur (email, autorités).
                     */
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Log pour déboguer
                    log.debug("User {} authenticated with roles: {}",
                            userEmail,
                            userDetails.getAuthorities());
                }
            }
        } catch (ExpiredJwtException e) {
            /**
             * Token expiré : retourne une erreur 401 Unauthorized.
             * Le client doit utiliser le refresh token pour obtenir un nouveau token.
             */
            log.warn("JWT token expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired");
            return;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            /**
             * Token invalide (mauvaise signature, mal formé, type non supporté, paramètre invalide) :
             * retourne une erreur 401 Unauthorized.
             */
            log.warn("Invalid JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }
        // ============================================================
        // ÉTAPE 4 : Continuer la chaîne de filtres
        // ============================================================
        filterChain.doFilter(request, response);
    }
}