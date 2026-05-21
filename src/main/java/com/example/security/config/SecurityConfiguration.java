package com.example.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

import static com.example.security.user.Permission.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
/**
 * Configuration principale de la sécurité de l'application.
 * <p>
 * Cette classe configure l'ensemble des règles de sécurité pour l'application,
 * incluant la gestion des autorisations d'accès (endpoints publics vs protégés),
 * la configuration CORS, la politique de session (stateless), l'authentification
 * par token JWT et la déconnexion.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li><strong>Liste blanche (WHITE_LIST_URL)</strong> : Endpoints accessibles sans authentification</li>
 *     <li><strong>Configuration CORS</strong> : Autorisation des requêtes depuis les frontends Angular</li>
 *     <li><strong>Protection CSRF</strong> : Désactivée (API REST stateless)</li>
 *     <li><strong>Session management</strong> : Stateless (aucune session HTTP)</li>
 *     <li><strong>Filtre JWT</strong> : Interception et validation des tokens avant authentification</li>
 *     <li><strong>Autorisations par endpoint</strong> : Contrôle d'accès basé sur les permissions</li>
 *     <li><strong>Déconnexion</strong> : Révocation des tokens JWT</li>
 * </ul>
 *
 * <p><strong>Endpoints publics (WHITE_LIST_URL) :</strong></p>
 * <ul>
 *     <li>Authentification : /api/v1/auth/**</li>
 *     <li>Swagger/OpenAPI : /v3/api-docs/**, /swagger-ui/**</li>
 *     <li>Réinitialisation mot de passe : forgot-password, reset-password, validate-reset-token</li>
 *     <li>Vérification email : check-email, check-matricule</li>
 *     <li>Codes de vérification : send-verification-code, verify-code, send-reset-link</li>
 *     <li>Ressources statiques : /uploads/**</li>
 *     <li>Santé et debug : /actuator/**, /api/v1/health</li>
 * </ul>
 *
 * <p><strong>Permissions utilisées :</strong></p>
 * <ul>
 *     <li>CHARGE_SHEET_BASIC_* : Gestion basique des cahiers des charges (ING)</li>
 *     <li>CHARGE_SHEET_TECH_* : Saisie technique (PT)</li>
 *     <li>CHARGE_SHEET_ALL_READ : Consultation (tous rôles)</li>
 *     <li>COMPLIANCE_* : Gestion des fiches de conformité (PP)</li>
 *     <li>TECHNICAL_FILE_* : Gestion des dossiers techniques (PP)</li>
 *     <li>MAINTENANCE_CORRECTIVE_* : Maintenance corrective (MC)</li>
 *     <li>MAINTENANCE_PREVENTIVE_* : Maintenance préventive (MP)</li>
 *     <li>CLAIM_* : Gestion des réclamations (PP, PT)</li>
 *     <li>STOCK_* : Gestion du stock (PP, MC, MP)</li>
 *     <li>SEARCH : Recherche globale</li>
 *     <li>AJOUTE_USER, ADMIN_* : Administration (ADMIN)</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see JwtAuthenticationFilter
 * @see AuthenticationProvider
 * @see LogoutHandler
 * @since Sprint 2
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {
    /**
     * Liste des URLs accessibles sans authentification (public).
     * <p>
     * Ces endpoints sont exclus du filtre JWT et de toute vérification
     * d'authentification.
     * </p>
     */
    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",

            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/error",
            "/api/v1/users/forgot-password",
            "/api/v1/users/validate-reset-token",
            "/api/v1/users/reset-password",
            "/api/v1/users/check-email",
            "/api/v1/users/send-verification-code",
            "/api/v1/users/verify-email",
            "/api/v1/users/verify-code",
            "/api/v1/users/send-reset-link",
            "/api/v1/chatbot/ask",
            "/uploads/**",
            "/api/v1/sites",           // ✅ AJOUTER CETTE LIGNE
            "/api/v1/health",
            "/api/v1/compliance/test-mail",
            "/api/v1/claims/debug-all-images",
            "/api/v1/users/check-email",      // ← AJOUTE CETTE LIGNE
            "/api/v1/users/check-matricule",
            "/actuator/**"


    };

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    /**
     * Configure la chaîne de filtres de sécurité Spring Security.
     * <p>
     * Cette méthode définit l'ensemble des règles de sécurité :
     * <ul>
     *     <li>Désactivation de CSRF (API REST stateless)</li>
     *     <li>Configuration CORS pour autoriser les frontends Angular</li>
     *     <li>Définition des autorisations par endpoint (liste blanche / permissions)</li>
     *     <li>Politique de session sans état (STATELESS)</li>
     *     <li>Ajout du filtre JWT avant le filtre d'authentification standard</li>
     *     <li>Configuration de la déconnexion (logout)</li>
     * </ul>
     * </p>
     *
     * @param http L'objet {@link HttpSecurity} pour configurer la sécurité
     * @return La chaîne de filtres {@link SecurityFilterChain} configurée
     * @throws Exception En cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ============================================================
                // DÉSACTIVATION CSRF (API REST stateless)
                // ============================================================
                .csrf(AbstractHttpConfigurer::disable)
                // ============================================================
                // CONFIGURATION CORS (autoriser les frontends)
                // ============================================================
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.setAllowedOrigins(Arrays.asList("http://localhost",
                            "http://localhost:80",
                            "http://localhost:4200",
                            "http://localhost:5000",
                            "http://127.0.0.1",
                            "http://127.0.0.1:80",
                            "http://127.0.0.1:4200","https://leoni-test-modulesfrontend.onrender.com"));
                    corsConfig.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
                    corsConfig.setAllowedHeaders(Arrays.asList("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                // ============================================================
                // CONFIGURATION DES AUTORISATIONS PAR ENDPOINT
                // ============================================================
                .authorizeHttpRequests(auth -> auth
                        // ================= WHITE LIST =================
                        .requestMatchers(WHITE_LIST_URL).permitAll()

                        // ================= USER =================
                        .requestMatchers(GET, "/api/v1/users/me").authenticated()
                        .requestMatchers(GET,  "/api/v1/users/project-emails").authenticated()
                        .requestMatchers(GET,"/api/v1/charge-sheets/stats").authenticated()
                        .requestMatchers(GET,"api/v1/users/check-matricule").authenticated()

                        .requestMatchers(GET, "/api/v1/users/getUsers").hasAuthority(AJOUTE_USER_LISTE.getPermission())
                        .requestMatchers(POST, "/api/v1/auth/register").hasAuthority(AJOUTE_USER.getPermission())
                        .requestMatchers(PUT, "/api/v1/users/**")
                        .hasAuthority(AJOUTE_USER.getPermission())

                        .requestMatchers(DELETE, "/api/v1/users/**")
                        .hasAuthority(AJOUTE_USER.getPermission())
                        // ================= CAHIER DES CHARGES =================
                        // ING: Créer un nouveau cahier (champs basiques)
                        .requestMatchers(POST, "/api/v1/charge-sheets").hasAuthority(CHARGE_SHEET_BASIC_CREATE.getPermission())
                        // ING: Modifier les champs basiques
                        .requestMatchers(PUT, "/api/v1/charge-sheets/{id}/basic").hasAuthority(CHARGE_SHEET_BASIC_WRITE.getPermission())
                        // ING: Lire les champs basiques
                        .requestMatchers(GET, "/api/v1/charge-sheets/{id}/basic").hasAuthority(CHARGE_SHEET_BASIC_READ.getPermission())
                        // PT: Modifier les champs techniques
                        .requestMatchers(PUT, "/api/v1/charge-sheets/{id}/tech").hasAuthority(CHARGE_SHEET_TECH_WRITE.getPermission())
                        // PT: Lire les champs techniques
                        .requestMatchers(GET, "/api/v1/charge-sheets/{id}/tech").hasAuthority(CHARGE_SHEET_TECH_READ.getPermission())
                        // Tous: Lire un cahier COMPLET (après les endpoints spécifiques)
                        .requestMatchers(GET, "/api/v1/charge-sheets/{id}").hasAuthority(CHARGE_SHEET_ALL_READ.getPermission())
                        // Tous: Lister tous les cahiers
                        .requestMatchers(GET, "/api/v1/charge-sheets").hasAuthority(CHARGE_SHEET_ALL_READ.getPermission())
                        .requestMatchers(GET, "/api/v1/charge-sheets/*/image").hasAuthority(CHARGE_SHEET_ALL_READ.getPermission())
                        .requestMatchers(POST, "/api/v1/charge-sheets/*/upload-image").hasAnyAuthority(
                                CHARGE_SHEET_BASIC_WRITE.getPermission(),
                                CHARGE_SHEET_TECH_WRITE.getPermission(),
                                CHARGE_SHEET_BASIC_CREATE.getPermission()
                        )

                        // ================= CONFORMITÉ =================
                        .requestMatchers(POST, "/api/v1/compliance").hasAuthority(COMPLIANCE_CREATE.getPermission())
                        .requestMatchers(PUT, "/api/v1/compliance/**").hasAuthority(COMPLIANCE_WRITE.getPermission())
                        .requestMatchers(GET, "/api/v1/compliance/**").hasAuthority(COMPLIANCE_READ.getPermission())

                        // ================= DOSSIER TECHNIQUE =================
                        .requestMatchers(POST, "/api/v1/technical-files").hasAuthority(TECHNICAL_FILE_CREATE.getPermission())
                        .requestMatchers(PUT, "/api/v1/technical-files/**").hasAuthority(TECHNICAL_FILE_WRITE.getPermission())
                        .requestMatchers(GET, "/api/v1/technical-files/**").hasAuthority(TECHNICAL_FILE_READ.getPermission())

                        // ================= MAINTENANCE CORRECTIVE =================
                        .requestMatchers(POST, "/api/v1/maintenance/corrective/**").hasAuthority(MAINTENANCE_CORRECTIVE_WRITE.getPermission())
                        .requestMatchers(GET, "/api/v1/maintenance/corrective/**").hasAuthority(MAINTENANCE_CORRECTIVE_READ.getPermission())

                        // ================= MAINTENANCE PRÉVENTIVE =================
                        .requestMatchers(POST, "/api/v1/maintenance/preventive/**").hasAuthority(MAINTENANCE_PREVENTIVE_WRITE.getPermission())
                        .requestMatchers(GET, "/api/v1/maintenance/preventive/**").hasAuthority(MAINTENANCE_PREVENTIVE_READ.getPermission())

                        // ================= RÉCLAMATIONS =================
                        .requestMatchers(POST, "/api/v1/claims/**").hasAnyAuthority(CLAIM_CREATE.getPermission(), CLAIM_WRITE.getPermission())
                        .requestMatchers(GET, "/api/v1/claims/**").hasAuthority(CLAIM_READ.getPermission())

                        // ================= STOCK =================
                        .requestMatchers(POST, "/api/v1/stock/**").hasAuthority(STOCK_WRITE.getPermission())
                        .requestMatchers(GET, "/api/v1/stock/**").hasAuthority(STOCK_READ.getPermission())

                        // ================= RECHERCHE =================
                        .requestMatchers("/api/v1/search/**").hasAuthority(SEARCH.getPermission())

                        // Toute autre requête doit être authentifiée
                        .anyRequest().authenticated()
                )
                // ============================================================
                // POLITIQUE DE SESSION (STATELESS)
                // ============================================================
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                // ============================================================
                // FOURNISSEUR D'AUTHENTIFICATION
                // ============================================================
                .authenticationProvider(authenticationProvider)
                // ============================================================
                // AJOUT DU FILTRE JWT
                // ============================================================
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // ============================================================
                // CONFIGURATION DE LA DÉCONNEXION
                // ============================================================
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext())
                );

        return http.build();
    }

}
