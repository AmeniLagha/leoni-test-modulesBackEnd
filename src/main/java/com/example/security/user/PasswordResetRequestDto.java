package com.example.security.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objet de transfert de données (DTO) pour la demande de réinitialisation du mot de passe.
 * <p>
 * Cette classe encapsule l'adresse email nécessaire pour initier une procédure
 * de réinitialisation de mot de passe. Elle est utilisée pour transporter la
 * demande du client (frontend Angular) vers le serveur (backend Spring Boot)
 * lors de la première étape du processus "Mot de passe oublié".
 * </p>
 *
 * <p>Le processus de demande de réinitialisation se déroule comme suit :</p>
 * <ol>
 *     <li>L'utilisateur saisit son adresse email sur la page "Mot de passe oublié"</li>
 *     <li>Le client envoie un objet {@code PasswordResetRequestDto} contenant cet email</li>
 *     <li>Le serveur vérifie l'existence de l'email en base de données</li>
 *     <li>Si l'email existe, un token de réinitialisation est généré et envoyé par email</li>
 *     <li>L'utilisateur reçoit un lien contenant le token pour définir un nouveau mot de passe</li>
 * </ol>
 *
 * <p>Ce DTO est annoté avec Lombok pour réduire le code boilerplate :</p>
 * <ul>
 *     <li>{@code @Data} : génère les getters, setters, toString, equals et hashCode</li>
 *     <li>{@code @Builder} : fournit un pattern builder pour une construction fluide</li>
 *     <li>{@code @NoArgsConstructor} : génère un constructeur sans paramètres</li>
 *     <li>{@code @AllArgsConstructor} : génère un constructeur avec tous les paramètres</li>
 * </ul>
 *
 * <p><strong>Note de sécurité :</strong> Le serveur ne doit pas révéler si un email
 * existe ou non dans la base de données pour éviter l'énumération des comptes.
 * La réponse doit être identique dans les deux cas (email existant ou inexistant).</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see PasswordResetDto
 * @see com.example.security.user.User
 * @see com.example.security.auth.AuthenticationService
 * @see com.example.security.auth.AuthenticationController
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {
    /**
     * Adresse email de l'utilisateur demandant la réinitialisation du mot de passe.
     * <p>
     * Cet email est utilisé pour identifier le compte utilisateur à qui envoyer
     * le lien de réinitialisation. Il doit correspondre à un email existant
     * dans la base de données des utilisateurs.
     * </p>
     *
     * <p><strong>Validation côté serveur :</strong></p>
     * <ul>
     *     <li>Ne doit pas être {@code null} ou vide</li>
     *     <li>Doit respecter le format d'une adresse email valide</li>
     *     <li>Doit contenir un "@" et un domaine valide</li>
     *     <li>Longueur maximale généralement limitée à 255 caractères</li>
     * </ul>
     *
     * <p><strong>Exemple d'email valide :</strong> {@code jean.dupont@leoni.com}</p>
     *
     * <p><strong>Note de sécurité importante :</strong>
     * Pour des raisons de sécurité, le serveur doit toujours retourner une réponse
     * de succès, même si l'email n'existe pas. Cela empêche un attaquant de
     * découvrir quels emails sont enregistrés dans le système par des appels
     * répétés à cet endpoint.</p>
     *
     * <p>Exemple d'utilisation :</p>
     * <pre>
     * // Création de la requête
     * PasswordResetRequestDto request = PasswordResetRequestDto.builder()
     *     .email("jean.dupont@leoni.com")
     *     .build();
     *
     * // Envoi au backend
     * // POST /api/v1/auth/forgot-password
     * </pre>
     *
     * @see jakarta.validation.constraints.Email
     * @see jakarta.validation.constraints.NotBlank
     * @see jakarta.validation.constraints.Size
     */
    private String email;
}