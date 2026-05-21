package com.example.security.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objet de transfert de données (DTO) pour une requête d'authentification.
 * <p>
 * Cette classe encapsule les identifiants nécessaires à un utilisateur pour
 * s'authentifier auprès du système. Elle est utilisée pour transporter les
 * données de connexion du client (frontend Angular) vers le serveur
 * (backend Spring Boot) lors d'une tentative d'authentification.
 * </p>
 *
 * <p><strong>Champs requis :</strong></p>
 * <ul>
 *     <li>{@code email} : Adresse email de l'utilisateur (identifiant principal)</li>
 *     <li>{@code password} : Mot de passe de l'utilisateur (sera vérifié par BCrypt)</li>
 *     <li>{@code siteName} : Nom du site de production de l'utilisateur</li>
 * </ul>
 *
 * <p><strong>Processus d'authentification :</strong></p>
 * <ol>
 *     <li>L'utilisateur saisit son email, son mot de passe et son site</li>
 *     <li>Le client envoie un objet {@code AuthenticationRequest} au backend</li>
 *     <li>Le backend vérifie que l'email et le mot de passe correspondent</li>
 *     <li>Le backend vérifie que l'utilisateur a accès au site spécifié</li>
 *     <li>Si tout est valide, un token JWT est généré et retourné</li>
 * </ol>
 *
 * <p><strong>Exemple de requête JSON :</strong></p>
 * <pre>
 * {
 *     "email": "jean.dupont@leoni.com",
 *     "password": "monMotDePasse123",
 *     "siteName": "Manzel Hayet"
 * }
 * </pre>
 *
 * <p><strong>Note de sécurité :</strong>
 * Le mot de passe est transmis en clair dans la requête mais doit être
 * envoyé uniquement via HTTPS pour garantir la confidentialité.
 * Le backend ne stocke jamais le mot de passe en clair, seulement son hash BCrypt.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see AuthenticationResponse
 * @see AuthenticationController
 * @see AuthenticationService
 * @since Sprint 2
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequest {
    /**
     * Adresse email de l'utilisateur.
     * <p>
     * L'email est l'identifiant principal utilisé pour l'authentification.
     * Il doit correspondre à un email existant dans la base de données
     * des utilisateurs.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être {@code null} ou vide</li>
     *     <li>Doit respecter le format d'une adresse email valide</li>
     *     <li>Doit exister dans la table {@code user}</li>
     *     <li>La casse n'est pas prise en compte (stocké en minuscule)</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "jean.dupont@leoni.com"</p>
     */
    private String email;
    /**
     * Mot de passe de l'utilisateur.
     * <p>
     * Le mot de passe est saisi par l'utilisateur dans le formulaire de connexion.
     * Il est transmis au backend pour vérification, où il est comparé au hash
     * BCrypt stocké en base de données.
     * </p>
     *
     * <p><strong>Recommandations :</strong></p>
     * <ul>
     *     <li>Ne jamais afficher ou logger le mot de passe</li>
     *     <li>Toujours utiliser HTTPS pour la transmission</li>
     *     <li>Le mot de passe n'est jamais stocké en clair dans le système</li>
     *     <li>Longueur minimale recommandée : 8 caractères</li>
     *     <li>Doit contenir une combinaison de caractères (majuscules, minuscules, chiffres, spéciaux)</li>
     * </ul>
     *
     * <p><strong>Note :</strong>
     * Ce champ n'est pas inclus dans la réponse API pour des raisons de sécurité.</p>
     *
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    private String password;
    /**
     * Nom du site de production de l'utilisateur.
     * <p>
     * Le site est un critère supplémentaire d'authentification qui garantit
     * l'isolation multi-sites. L'utilisateur ne peut se connecter que sur
     * le site auquel il est rattaché dans son profil.
     * </p>
     *
     * <p><strong>Sites disponibles chez LEONI Tunisia :</strong></p>
     * <ul>
     *     <li><strong>Manzel Hayet</strong> : Site principal du projet MEB</li>
     *     <li><strong>LTN1</strong> : Ligne technique 1</li>
     *     <li><strong>LTN2</strong> : Ligne technique 2</li>
     *     <li><strong>Mateur</strong> : Site secondaire</li>
     * </ul>
     *
     * <p><strong>Fonction :</strong></p>
     * <ul>
     *     <li>Vérifie que l'utilisateur a bien le droit d'accéder au site</li>
     *     <li>Détermine le contexte d'isolation des données</li>
     *     <li>Filtre les données visibles par l'utilisateur après connexion</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "Manzel Hayet", "LTN1", "Mateur"</p>
     *
     * @see com.example.security.site.Site
     */
    private String siteName;
}
