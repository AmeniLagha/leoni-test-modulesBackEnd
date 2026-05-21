package com.example.security.config;

import com.example.security.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
/**
 * Service de déconnexion qui révoque les jetons JWT lors de la déconnexion d'un utilisateur.
 * <p>
 * Cette classe implémente l'interface {@link LogoutHandler} de Spring Security.
 * Elle est appelée automatiquement lors d'une requête de déconnexion sur l'endpoint
 * configuré (généralement {@code POST /logout}). Son rôle est d'invalider le token
 * JWT de l'utilisateur pour empêcher toute réutilisation ultérieure.
 * </p>
 *
 * <p><strong>Fonctionnement :</strong></p>
 * <ol>
 *     <li>Extrait le token JWT du header {@code Authorization: Bearer &lt;token&gt;}</li>
 *     <li>Recherche le token dans la base de données</li>
 *     <li>Marque le token comme expiré ({@code expired = true})</li>
 *     <li>Marque le token comme révoqué ({@code revoked = true})</li>
 *     <li>Sauvegarde le token modifié en base</li>
 *     <li>Efface le contexte de sécurité Spring</li>
 * </ol>
 *
 * <p><strong>Sécurité :</strong>
 * La révocation des tokens en base de données garantit qu'un token déconnecté
 * ne pourra pas être réutilisé, même s'il n'a pas encore expiré naturellement.
 * Cela permet de mettre en œuvre une déconnexion sécurisée et immédiate.
 * </p>
 *
 * <p><strong>Configuration requise dans SecurityConfiguration :</strong></p>
 * <pre>
 * @Bean
 * public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 *     http
 *         .logout(logout -> logout
 *             .logoutUrl("/api/v1/auth/logout")
 *             .addLogoutHandler(logoutService)
 *             .logoutSuccessHandler((request, response, authentication) ->
 *                 response.setStatus(HttpServletResponse.SC_OK))
 *         );
 *     return http.build();
 * }
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see TokenRepository
 * @see org.springframework.security.web.authentication.logout.LogoutHandler
 * @see org.springframework.security.web.authentication.logout.LogoutFilter
 * @since Sprint 2
 */
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    /**
     * Repository pour l'accès aux tokens JWT en base de données.
     * Permet de rechercher, modifier et sauvegarder les tokens.
     */
    private final TokenRepository tokenRepository;
    /**
     * Gère la déconnexion d'un utilisateur en révoquant son token JWT.
     * <p>
     * Cette méthode est appelée automatiquement par Spring Security lorsque
     * l'utilisateur effectue une requête de déconnexion sur l'endpoint configuré.
     * </p>
     *
     * <p><strong>Processus détaillé :</strong></p>
     * <ol>
     *     <li>Extraction du token depuis le header {@code Authorization}</li>
     *     <li>Si le token est absent ou mal formé, la méthode s'arrête</li>
     *     <li>Recherche du token en base de données</li>
     *     <li>Si le token existe, il est marqué comme expiré et révoqué</li>
     *     <li>Sauvegarde du token mis à jour</li>
     *     <li>Nettoyage du contexte de sécurité Spring</li>
     * </ol>
     *
     * <p><strong>Note importante :</strong>
     * Cette méthode révoque le token JWT en base, mais n'invalide pas les tokens
     * qui auraient pu être copiés ou stockés ailleurs. Cependant, lors des requêtes
     * suivantes, le filtre {@link JwtAuthenticationFilter} vérifiera l'état du token
     * en base et le rejettera s'il est marqué comme révoqué ou expiré.
     * </p>
     *
     * @param request La requête HTTP contenant le token JWT dans le header Authorization
     * @param response La réponse HTTP (peut être utilisée pour définir des cookies, etc.)
     * @param authentication L'objet d'authentification de l'utilisateur courant (non utilisé dans cette implémentation)
     *
     * @see TokenRepository#findByToken(String)
     * @see SecurityContextHolder#clearContext()
     */
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,

            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        var storedToken = tokenRepository.findByToken(jwt)
                .orElse(null);

        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            SecurityContextHolder.clearContext();
        }
    }
}
