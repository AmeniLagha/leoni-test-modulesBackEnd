package com.example.security.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Représente une erreur détaillée pour une réponse API.
 * <p>
 * Cette classe est utilisée dans {@link ApiResponse} pour fournir des informations
 * précises sur les erreurs de validation ou les problèmes sur des champs spécifiques.
 * Elle permet au frontend d'afficher des messages d'erreur ciblés directement
 * sous les champs de formulaire concernés.
 * </p>
 *
 * <p><strong>Structure d'une erreur :</strong></p>
 * <ul>
 *     <li><strong>field</strong> : Le nom du champ concerné par l'erreur</li>
 *     <li><strong>message</strong> : Le message d'erreur explicatif</li>
 *     <li><strong>code</strong> : Code d'erreur optionnel pour traitement programmatique</li>
 * </ul>
 *
 * <p><strong>Exemple d'utilisation dans une réponse API :</strong></p>
 * <pre>
 * {
 *     "success": false,
 *     "message": "Erreur de validation des données",
 *     "statusCode": 400,
 *     "errors": [
 *         {"field": "email", "message": "L'email est obligatoire"},
 *         {"field": "password", "message": "Le mot de passe doit contenir au moins 6 caractères"}
 *     ]
 * }
 * </pre>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Dans un validateur personnalisé
 * List&lt;ApiError&gt; errors = new ArrayList<>();
 * if (email == null || email.isEmpty()) {
 *     errors.add(new ApiError("email", "L'email est obligatoire"));
 * }
 * if (password == null || password.length() < 6) {
 *     errors.add(new ApiError("password", "Le mot de passe doit contenir au moins 6 caractères"));
 * }
 * return ApiResponse.error("Validation échouée", 400, errors);
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ApiResponse
 * @see GlobalExceptionHandler
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    /**
     * Nom du champ concerné par l'erreur.
     * <p>
     * Correspond au nom de la propriété dans le DTO ou l'entité qui a causé l'erreur.
     * Permet au frontend d'associer l'erreur au champ correspondant dans le formulaire.
     * </p>
     *
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *     <li>"email" - pour une erreur sur le champ email</li>
     *     <li>"password" - pour une erreur sur le mot de passe</li>
     *     <li>"firstname" - pour une erreur sur le prénom</li>
     *     <li>"siteName" - pour une erreur sur le site</li>
     * </ul>
     */
    private String field;
    /**
     * Message d'erreur explicatif.
     * <p>
     * Description lisible par l'utilisateur de l'erreur survenue.
     * Doit être clair et actionnable (expliquer ce qui ne va pas et comment corriger).
     * </p>
     *
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *     <li>"L'email est obligatoire"</li>
     *     <li>"Le mot de passe doit contenir au moins 6 caractères"</li>
     *     <li>"Le matricule doit être un nombre positif"</li>
     *     <li>"Email déjà utilisé dans le système"</li>
     * </ul>
     */
    private String message;
    /**
     * Code d'erreur optionnel pour traitement programmatique.
     * <p>
     * Peut contenir un code spécifique permettant au frontend de traiter
     * l'erreur de manière programmatique (affichage d'icônes spécifiques,
     * actions particulières, etc.). Ce champ est optionnel (peut être null).
     * </p>
     *
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *     <li>"REQUIRED" - champ obligatoire non renseigné</li>
     *     <li>"INVALID_FORMAT" - format de champ invalide</li>
     *     <li>"DUPLICATE" - valeur déjà existante</li>
     *     <li>"TOO_SHORT" - valeur trop courte</li>
     *     <li>"TOO_LONG" - valeur trop longue</li>
     * </ul>
     */
    private String code;

    /**
     * Constructeur simplifié pour une erreur sans code.
     * <p>
     * Ce constructeur est utile pour les validations simples où seul le champ
     * et le message sont nécessaires, sans code d'erreur spécifique.
     * </p>
     *
     * @param field Le nom du champ concerné par l'erreur
     * @param message Le message d'erreur explicatif
     *
     * @example
     * <pre>
     * ApiError error = new ApiError("email", "L'email est obligatoire");
     * </pre>
     */
    public ApiError(String field, String message) {
        this.field = field;
        this.message = message;
        this.code = null;
    }
}