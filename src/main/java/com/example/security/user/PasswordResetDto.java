package com.example.security.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objet de transfert de données (DTO) pour la réinitialisation du mot de passe utilisateur.
 * <p>
 * Cette classe encapsule les informations nécessaires pour effectuer une opération
 * de réinitialisation de mot de passe. Elle est utilisée pour transporter les données
 * entre le client (frontend Angular) et le serveur (backend Spring Boot) lors
 * d'une demande de changement de mot de passe oublié.
 * </p>
 *
 * <p>Le processus de réinitialisation se déroule généralement en deux étapes :</p>
 * <ol>
 *     <li>L'utilisateur demande une réinitialisation → un token est envoyé par email</li>
 *     <li>L'utilisateur soumet ce DTO avec le token reçu et son nouveau mot de passe</li>
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
 * @author LAGHA AMENI
 * @version 1.0
 * @see com.example.security.user.User
 * @see com.example.security.auth.AuthenticationService
 * @see com.example.security.auth.AuthenticationController
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDto {
    /**
     * Adresse email de l'utilisateur demandant la réinitialisation.
     * <p>
     * Cet email est utilisé pour identifier le compte utilisateur concerné
     * par l'opération de réinitialisation. Il doit correspondre à un email
     * existant en base de données.
     * </p>
     * <p>Le champ est validé côté serveur pour s'assurer :</p>
     * <ul>
     *     <li>Qu'il n'est pas vide ou null</li>
     *     <li>Qu'il respecte le format d'une adresse email valide</li>
     *     <li>Qu'il correspond à un utilisateur existant</li>
     * </ul>
     *
     * <p>Exemple d'utilisation :</p>
     * <pre>
     * PasswordResetDto dto = PasswordResetDto.builder()
     *     .email("jean.dupont@leoni.com")
     *     .newPassword("NouveauMotDePasse123!")
     *     .resetToken("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
     *     .build();
     * </pre>
     */
    private String email;
    /**
     * Nouveau mot de passe choisi par l'utilisateur.
     * <p>
     * Ce champ contient le nouveau mot de passe que l'utilisateur souhaite
     * définir pour son compte. Le mot de passe doit respecter les règles
     * de sécurité définies par l'application (longueur minimale, complexité,
     * caractères spéciaux, etc.).
     * </p>
     *
     * <p>Recommandations de sécurité :</p>
     * <ul>
     *     <li>Longueur minimale : 8 caractères</li>
     *     <li>Au moins une lettre majuscule</li>
     *     <li>Au moins une lettre minuscule</li>
     *     <li>Au moins un chiffre</li>
     *     <li>Au moins un caractère spécial (@, #, $, !, etc.)</li>
     * </ul>
     *
     * <p>Le mot de passe est encodé avec BCrypt avant d'être stocké en base
     * de données, garantissant ainsi sa sécurité en cas de compromission
     * de la base.</p>
     *
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    private String newPassword;
    /**
     * Jeton de réinitialisation (reset token) envoyé par email à l'utilisateur.
     * <p>
     * Ce token est généré de manière unique lors de la demande de réinitialisation
     * et envoyé à l'adresse email de l'utilisateur. Il permet de vérifier que
     * la personne qui demande le changement de mot de passe est bien le
     * propriétaire légitime du compte.
     * </p>
     *
     * <p>Caractéristiques du token :</p>
     * <ul>
     *     <li>Généré de manière cryptographiquement sécurisée</li>
     *     <li>Durée de vie limitée (généralement 15 à 30 minutes)</li>
     *     <li>Usage unique (invalide après utilisation)</li>
     *     <li>Transmis uniquement par email (canal sécurisé)</li>
     * </ul>
     *
     * <p>Le token doit correspondre à celui stocké en base pour l'utilisateur
     * concerné. En cas de non-correspondance ou d'expiration, la réinitialisation
     * est refusée.</p>
     *
     * @see com.example.security.config.JwtService
     */
    private String resetToken;
}