package com.example.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

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




    };

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
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
                .authorizeHttpRequests(auth -> auth
                        // ================= WHITE LIST =================
                        .requestMatchers(WHITE_LIST_URL).permitAll()

                        // ================= USER =================
                        .requestMatchers(GET, "/api/v1/users/me").authenticated()
                        .requestMatchers(GET,  "/api/v1/users/project-emails").authenticated()
                        .requestMatchers(GET,"/api/v1/charge-sheets/stats").authenticated()
                        

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
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext())
                );

        return http.build();
    }

}
