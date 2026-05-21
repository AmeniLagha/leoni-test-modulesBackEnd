package com.example.security.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objet de transfert de données (DTO) pour la réponse d'authentification.
 * <p>
 * Cette classe encapsule les jetons JWT retournés au client après une
 * authentification réussie ou une inscription. Elle contient les deux types
 * de jetons nécessaires pour maintenir une session utilisateur sécurisée.
 * </p>
 *
 * <p><strong>Types de jetons :</strong></p>
 * <ul>
 *     <li><strong>Access Token</strong> : Jeton à courte durée de vie (généralement 15-30 minutes)
 *         utilisé pour authentifier les requêtes API. Il est inclus dans le header
 *         {@code Authorization: Bearer &lt;access_token&gt;}.</li>
 *     <li><strong>Refresh Token</strong> : Jeton à longue durée de vie (généralement 7-30 jours)
 *         utilisé pour obtenir un nouvel access token sans que l'utilisateur ait besoin
 *         de se reconnecter. Il est stocké côté client (localStorage ou cookie sécurisé).</li>
 * </ul>
 *
 * <p><strong>Format de réponse JSON :</strong></p>
 * <pre>
 * {
 *     "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *     "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
 * }
 * </pre>
 *
 * <p><strong>Cycle de vie des tokens :</strong></p>
 * <ol>
 *     <li>L'utilisateur se connecte → reçoit access_token + refresh_token</li>
 *     <li>Pour chaque requête API, il envoie l'access_token</li>
 *     <li>Quand l'access_token expire, il utilise le refresh_token pour en obtenir un nouveau</li>
 *     <li>Si le refresh_token expire, l'utilisateur doit se reconnecter</li>
 * </ol>
 *
 * <p><strong>Annotations Jackson :</strong>
 * Les annotations {@code @JsonProperty} garantissent que les noms de propriétés
 * dans la réponse JSON respectent la convention snake_case
 * ({@code access_token} et {@code refresh_token}) au lieu de camelCase
 * ({@code accessToken} et {@code refreshToken}).</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see AuthenticationRequest
 * @see AuthenticationController
 * @see AuthenticationService
 * @since Sprint 2
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    /**
     * Jeton d'accès JWT (access token).
     * <p>
     * Ce jeton est utilisé pour authentifier les requêtes API de l'utilisateur.
     * Il a une durée de vie limitée (généralement 15 à 30 minutes) pour des
     * raisons de sécurité. Il doit être inclus dans le header HTTP
     * {@code Authorization} de chaque requête protégée.
     * </p>
     *
     * <p><strong>Structure JWT :</strong></p>
     * <ul>
     *     <li><strong>Header</strong> : Algorithme de signature (HS256) et type de token</li>
     *     <li><strong>Payload</strong> : Claims (userId, email, rôle, permissions, site, projets)</li>
     *     <li><strong>Signature</strong> : Vérification de l'intégrité du token</li>
     * </ul>
     *
     * <p><strong>Utilisation côté client :</strong></p>
     * <pre>
     * // Stockage (localStorage)
     * localStorage.setItem('access_token', response.access_token);
     *
     * // Envoi dans les requêtes
     * headers: {
     *     'Authorization': `Bearer ${localStorage.getItem('access_token')}`
     * }
     * </pre>
     *
     * <p><strong>Note de sécurité :</strong>
     * L'access token doit être stocké de manière sécurisée côté client.
     * Évitez de le stocker dans des variables globales ou dans l'URL.</p>
     *
     * @see #refreshToken
     * @see io.jsonwebtoken.Jwts
     */
    @JsonProperty("access_token")
    private String accessToken;
    /**
     * Jeton de rafraîchissement JWT (refresh token).
     * <p>
     * Ce jeton est utilisé pour obtenir un nouvel access token lorsque celui-ci
     * a expiré, sans que l'utilisateur ait besoin de fournir à nouveau ses
     * identifiants. Il a une durée de vie plus longue que l'access token
     * (généralement 7 à 30 jours).
     * </p>
     *
     * <p><strong>Processus de rafraîchissement :</strong></p>
     * <ol>
     *     <li>L'access token expire (le serveur retourne une erreur 401 Unauthorized)</li>
     *     <li>Le client appelle l'endpoint {@code POST /api/v1/auth/refresh-token}</li>
     *     <li>Le client envoie le refresh token (généralement dans le body ou un cookie)</li>
     *     <li>Le serveur vérifie la validité du refresh token</li>
     *     <li>Le serveur retourne un nouvel access token</li>
     *     <li>Le client reprend ses requêtes avec le nouvel access token</li>
     * </ol>
     *
     * <p><strong>Différence avec l'access token :</strong></p>
     * <ul>
     *     <li>Durée de vie plus longue (jours vs minutes)</li>
     *     <li>Utilisé uniquement pour obtenir de nouveaux access tokens</li>
     *     <li>Stocké de manière plus sécurisée (peut être en cookie httpOnly)</li>
     *     <li>Peut être révoqué à distance (déconnexion, changement de mot de passe)</li>
     * </ul>
     *
     * <p><strong>Note de sécurité :</strong>
     * Le refresh token doit être stocké dans un endroit sécurisé. Une bonne pratique
     * est de le stocker dans un cookie httpOnly pour le protéger des attaques XSS.</p>
     *
     * @see #accessToken
     * @see AuthenticationService#refreshToken
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
}
