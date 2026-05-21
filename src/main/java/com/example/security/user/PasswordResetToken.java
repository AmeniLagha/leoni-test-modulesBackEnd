package com.example.security.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un jeton de réinitialisation de mot de passe.
 * <p>
 * Cette classe modélise le jeton temporaire envoyé par email à un utilisateur
 * ayant oublié son mot de passe. Le jeton permet de sécuriser le processus
 * de réinitialisation en vérifiant que la personne qui demande le changement
 * est bien le propriétaire légitime du compte.
 * </p>
 *
 * <p>Caractéristiques principales :</p>
 * <ul>
 *     <li>Jeton unique généré de manière cryptographiquement sécurisée</li>
 *     <li>Durée de vie limitée (définie par {@code expiryDate})</li>
 *     <li>Usage unique (basculement du flag {@code used} à {@code true})</li>
 *     <li>Associé à un utilisateur spécifique via une relation ManyToOne</li>
 *     <li>Stockage persistant en base de données pour vérification ultérieure</li>
 * </ul>
 *
 * <p><strong>Durée de vie typique :</strong> 15 à 30 minutes après génération.
 * Au-delà, le jeton est considéré comme expiré et la réinitialisation est refusée.</p>
 *
 * <p><strong>Processus complet de réinitialisation :</strong></p>
 * <ol>
 *     <li>L'utilisateur demande une réinitialisation ({@link PasswordResetRequestDto})</li>
 *     <li>Le backend génère un {@code PasswordResetToken} persistant</li>
 *     <li>Le token est envoyé par email à l'utilisateur</li>
 *     <li>L'utilisateur clique sur le lien contenant le token</li>
 *     <li>Le backend vérifie la validité du token (existence, non expiré, non utilisé)</li>
 *     <li>Si valide, l'utilisateur peut définir un nouveau mot de passe</li>
 *     <li>Le token est marqué comme {@code used = true}</li>
 * </ol>
 *
 * @author Votre Nom
 * @version 1.0
 * @see User
 * @see PasswordResetRequestDto
 * @see PasswordResetDto
 * @since Sprint 2
 */
@Entity
@Table(name = "password_reset_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    /**
     * Identifiant unique du jeton de réinitialisation.
     * <p>
     * Clé primaire générée automatiquement par la base de données avec
     * une stratégie d'auto-incrémentation ({@link GenerationType#IDENTITY}).
     * Cet identifiant est utilisé comme clé étrangère dans les relations.
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Valeur du jeton de réinitialisation.
     * <p>
     * Chaîne de caractères unique générée de manière cryptographiquement sécurisée
     * (généralement un UUID ou une chaîne aléatoire). Ce token est envoyé par email
     * à l'utilisateur et doit être fourni pour prouver la légitimité de la demande
     * de réinitialisation.
     * </p>
     *
     * <p>Contraintes :</p>
     * <ul>
     *     <li>{@code nullable = false} : le token ne peut pas être nul</li>
     *     <li>{@code unique = true} : chaque token est unique en base de données</li>
     *     <li>Généralement stocké sous forme de chaîne hexadécimale ou UUID</li>
     * </ul>
     *
     * <p><strong>Exemple de token :</strong> {@code "a1b2c3d4-e5f6-7890-abcd-ef1234567890"}</p>
     *
     * <p><strong>Note de sécurité :</strong> Le token doit être suffisamment long
     * et aléatoire pour résister aux attaques par force brute (minimum 128 bits
     * d'entropie recommandés).</p>
     */
    @Column(nullable = false, unique = true)
    private String token;
    /**
     * Utilisateur associé à ce jeton de réinitialisation.
     * <p>
     * Relation Many-to-One entre {@code PasswordResetToken} et {@link User}.
     * Un utilisateur peut avoir plusieurs jetons de réinitialisation (demandes
     * multiples, chaque nouvelle demande invalide généralement les précédentes).
     * </p>
     *
     * <p>Caractéristiques de la relation :</p>
     * <ul>
     *     <li>Chargement paresseux ({@code FetchType.LAZY}) pour optimiser les performances</li>
     *     <li>La colonne de jointure s'appelle {@code user_id}</li>
     *     <li>La clé étrangère ne peut pas être nulle ({@code nullable = false})</li>
     *     <li>Un token est toujours associé à un utilisateur valide</li>
     * </ul>
     *
     * @see User
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
    /**
     * Date et heure d'expiration du jeton.
     * <p>
     * Définit la limite temporelle de validité du jeton. Au-delà de cette date,
     * le token est considéré comme expiré et ne peut plus être utilisé pour
     * réinitialiser le mot de passe.
     * </p>
     *
     * <p><strong>Calcul typique :</strong> {@code LocalDateTime.now().plusMinutes(30)}</p>
     *
     * <p><strong>Vérifications :</strong></p>
     * <ul>
     *     <li>Un token expiré est rejeté même si {@code used = false}</li>
     *     <li>La comparaison se fait avec {@link LocalDateTime#now()}</li>
     *     <li>Il est recommandé d'avoir une tâche planifiée pour nettoyer
     *         les tokens expirés de la base de données</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong></p>
     * <pre>
     * PasswordResetToken token = PasswordResetToken.builder()
     *     .token(UUID.randomUUID().toString())
     *     .user(user)
     *     .expiryDate(LocalDateTime.now().plusMinutes(30))
     *     .used(false)
     *     .build();
     * </pre>
     *
     * @see java.time.LocalDateTime
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    /**
     * Indique si le jeton a déjà été utilisé pour réinitialiser un mot de passe.
     * <p>
     * Un jeton est à usage unique pour des raisons de sécurité. Une fois qu'un
     * utilisateur a réinitialisé son mot de passe avec succès, le jeton est
     * marqué comme {@code used = true} et ne peut plus être réutilisé.
     * </p>
     *
     * <p><strong>Comportement :</strong></p>
     * <ul>
     *     <li>Valeur par défaut : {@code false} (jeton non utilisé)</li>
     *     <li>Passé à {@code true} après une réinitialisation réussie</li>
     *     <li>Un token expiré n'est pas utilisé, même si {@code used = false}</li>
     * </ul>
     *
     * <p><strong>Note de sécurité :</strong> L'usage unique empêche la réutilisation
     * d'un même token en cas d'interception (bien que l'expiration limite déjà
     * la fenêtre d'attaque). C'est une bonne pratique de sécurité.</p>
     */
    private boolean used;
}