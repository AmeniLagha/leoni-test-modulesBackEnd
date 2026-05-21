package com.example.security.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Configuration de la documentation OpenAPI (Swagger) pour l'API REST.
 * <p>
 * Cette classe configure les métadonnées de la documentation générée automatiquement
 * par Swagger/OpenAPI. Les informations définies ici apparaîtront dans l'interface
 * Swagger UI accessible à l'adresse {@code /swagger-ui/index.html}.
 * </p>
 *
 * <p><strong>Informations configurées :</strong></p>
 * <ul>
 *     <li><strong>Titre</strong> : "API Gestion Industrielle"</li>
 *     <li><strong>Version</strong> : "1.0"</li>
 *     <li><strong>Description</strong> : Description complète du périmètre de l'application</li>
 * </ul>
 *
 * <p><strong>Modules couverts par l'API :</strong></p>
 * <ul>
 *     <li>Gestion des cahiers des charges</li>
 *     <li>Gestion des items techniques (connecteurs, 150+ attributs)</li>
 *     <li>Gestion des réceptions fournisseur</li>
 *     <li>Gestion des fiches de conformité</li>
 *     <li>Gestion des dossiers techniques</li>
 *     <li>Gestion des réclamations</li>
 *     <li>Gestion du stock des modules testés</li>
 *     <li>Gestion des utilisateurs (authentification JWT, rôles, permissions)</li>
 *     <li>Gestion des sites et projets (isolation multi-sites)</li>
 * </ul>
 *
 * <p><strong>Accès à la documentation :</strong></p>
 * <ul>
 *     <li><strong>Swagger UI</strong> : {@code http://localhost:8080/swagger-ui/index.html}</li>
 *     <li><strong>Documentation JSON</strong> : {@code http://localhost:8080/api-docs}</li>
 * </ul>
 *
 * <p><strong>Annotation dans les contrôleurs :</strong>
 * Pour enrichir la documentation, les contrôleurs utilisent les annotations
 * {@code @Tag} (pour regrouper les endpoints) et {@code @Operation}
 * (pour décrire les opérations individuelles).
 * </p>
 *
 * <p><strong>Exemple dans un contrôleur :</strong></p>
 * <pre>
 * @RestController
 * @Tag(name = "Cahier de charges", description = "Gestion des cahiers de charges")
 * @RequestMapping("/api/v1/charge-sheets")
 * public class ChargeSheetController {
 *
 *     @GetMapping("/{id}")
 *     @Operation(summary = "Consulter un cahier",
 *                description = "Récupérer les détails complets d’un cahier de charges")
 *     public ResponseEntity&lt;ApiResponse&lt;ChargeSheetDto.CompleteDto&gt;&gt; getChargeSheet(@PathVariable Long id) {
 *         // ...
 *     }
 * }
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see io.swagger.v3.oas.annotations.tags.Tag
 * @see io.swagger.v3.oas.annotations.Operation
 * @since Sprint 3
 */
@Configuration
public class OpenApiConfig {
    /**
     * Crée et configure l'objet {@link OpenAPI} contenant les métadonnées
     * de la documentation de l'API.
     * <p>
     * Ce bean est automatiquement détecté par SpringDoc OpenAPI pour générer
     * la documentation Swagger/OpenAPI au format JSON, puis l'interface Swagger UI.
     * </p>
     *
     * <p><strong>Métadonnées configurées :</strong></p>
     * <ul>
     *     <li><strong>title</strong> : Nom de l'API (apparaît en haut de la page Swagger UI)</li>
     *     <li><strong>version</strong> : Version actuelle de l'API</li>
     *     <li><strong>description</strong> : Description détaillée du périmètre fonctionnel</li>
     * </ul>
     *
     * @return L'objet {@link OpenAPI} configuré avec les informations de l'API
     *
     * @see OpenAPI
     * @see Info
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gestion Industrielle")
                        .version("1.0")
                        .description("Documentation de l'application de gestion des cahiers de charges, maintenance, stock et utilisateurs"));
    }
}