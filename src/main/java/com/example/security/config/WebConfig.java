package com.example.security.config;

import com.example.security.monitoring.MetricsInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * Configuration des composants web de l'application.
 * <p>
 * Cette classe implémente {@link WebMvcConfigurer} pour configurer les aspects
 * web de l'application, incluant la gestion des ressources statiques, les règles
 * CORS (Cross-Origin Resource Sharing) et les intercepteurs de requêtes.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li><strong>Ressources statiques (Resource Handlers)</strong> : Exposition du dossier {@code uploads/}
 *         pour servir les images stockées.</li>
 *     <li><strong>CORS (Cross-Origin Resource Sharing)</strong> : Autorisation des requêtes provenant
 *         des frontends Angular (développement et production).</li>
 *     <li><strong>Intercepteurs (Interceptors)</strong> : Enregistrement de l'intercepteur de métriques
 *         pour collecter les données de performance des requêtes API.</li>
 * </ul>
 *
 * <p><strong>Ressources servies :</strong></p>
 * <ul>
 *     <li><strong>/uploads/**</strong> → Correspond au dossier physique {@code uploads/} à la racine du projet</li>
 *     <li>Utilisé pour servir les images des connecteurs, réclamations, etc.</li>
 * </ul>
 *
 * <p><strong>Configurations CORS :</strong></p>
 * <ul>
 *     <li><strong>Origines autorisées</strong> :
 *         <ul>
 *             <li>Développement : {@code http://localhost:4200} (Angular CLI)</li>
 *             <li>Production : {@code https://leoni-test-modulesfrontend.onrender.com}</li>
 *         </ul>
 *     </li>
 *     <li><strong>Méthodes HTTP autorisées</strong> : GET, POST, PUT, PATCH, DELETE, OPTIONS</li>
 *     <li><strong>Headers autorisés</strong> : Tous les headers (*)</li>
 *     <li><strong>Credentials</strong> : Autorisation d'envoi des cookies/tokens d'authentification</li>
 * </ul>
 *
 * <p><strong>Intercepteurs :</strong></p>
 * <ul>
 *     <li><strong>MetricsInterceptor</strong> : Intercepte les requêtes vers {@code /api/v1/**}</li>
 *     <li><strong>Paths exclus</strong> : Actuator, Swagger UI et API docs (pour éviter le bruit dans les métriques)</li>
 *     <li><strong>Utilité</strong> : Mesure des temps de réponse, comptage des requêtes, etc.</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see MetricsInterceptor
 * @see WebMvcConfigurer
 * @since Sprint 3
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * Intercepteur de métriques injecté automatiquement.
     * <p>
     * Cet intercepteur est responsable de la collecte des données de performance
     * pour chaque requête API (temps de réponse, compteurs, etc.).
     * </p>
     */
    @Autowired
    private MetricsInterceptor metricsInterceptor;
    // ============================================================
    // CONFIGURATION DES RESSOURCES STATIQUES
    // ============================================================

    /**
     * Configure les gestionnaires de ressources statiques.
     * <p>
     * Cette méthode expose le dossier {@code uploads/} à la racine du projet
     * pour servir les fichiers images téléchargés (connecteurs, réclamations, etc.).
     * </p>
     *
     * <p><strong>Mapping :</strong></p>
     * <ul>
     *     <li><strong>URL publique</strong> : {@code /uploads/**}</li>
     *     <li><strong>Emplacement physique</strong> : {@code file:uploads/}</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong></p>
     * <pre>
     * // Fichier physique : uploads/charge-sheets/abc123.jpg
     * // URL d'accès : http://localhost:8080/uploads/charge-sheets/abc123.jpg
     * </pre>
     *
     * @param registry Le registre des gestionnaires de ressources
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir le dossier uploads depuis la racine du projet
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
// ============================================================
    // CONFIGURATION CORS (Cross-Origin Resource Sharing)
    // ============================================================

    /**
     * Configure les règles CORS pour l'application.
     * <p>
     * Cette méthode définit les origines autorisées à accéder à l'API,
     * permettant ainsi aux frontends Angular (développement et production)
     * de communiquer avec le backend.
     * </p>
     *
     * <p><strong>Configuration :</strong></p>
     * <ul>
     *     <li><strong>Mapping</strong> : Tous les endpoints ({@code /**})</li>
     *     <li><strong>Origines autorisées</strong> :
     *         <ul>
     *             <li>{@code https://leoni-test-modulesfrontend.onrender.com} (production)</li>
     *             <li>{@code http://localhost:4200} (développement Angular)</li>
     *         </ul>
     *     </li>
     *     <li><strong>Méthodes autorisées</strong> : GET, POST, PUT, PATCH, DELETE, OPTIONS</li>
     *     <li><strong>Headers autorisés</strong> : Tous (*)</li>
     *     <li><strong>Credentials</strong> : {@code true} (permet l'envoi des cookies/tokens)</li>
     * </ul>
     *
     * @return Un {@link WebMvcConfigurer} avec la configuration CORS
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://leoni-test-modulesfrontend.onrender.com","http://localhost:4200")
                        .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }



// ============================================================
    // CONFIGURATION DES INTERCEPTEURS
    // ============================================================

    /**
     * Enregistre les intercepteurs de requêtes HTTP.
     * <p>
     * Cette méthode ajoute l'{@link MetricsInterceptor} pour collecter
     * les métriques de performance (temps de réponse, compteurs de requêtes)
     * sur l'ensemble des endpoints API.
     * </p>
     *
     * <p><strong>Configuration :</strong></p>
     * <ul>
     *     <li><strong>Paths interceptés</strong> : Tous les endpoints {@code /api/v1/**}</li>
     *     <li><strong>Paths exclus</strong> :
     *         <ul>
     *             <li>{@code /actuator/**} (métriques système)</li>
     *             <li>{@code /swagger-ui/**} (documentation API)</li>
     *             <li>{@code /v3/api-docs/**} (spécification OpenAPI)</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param registry Le registre des intercepteurs
     *
     * @see MetricsInterceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
    }

}
