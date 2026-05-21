package com.example.security.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un code de vérification temporaire pour la validation d'email.
 * <p>
 * Cette classe modélise le code de vérification à 6 chiffres envoyé par email
 * à un utilisateur pour confirmer son identité avant une opération sensible
 * (réinitialisation de mot de passe, modification d'email, etc.).
 * </p>
 *
 * <p><strong>Caractéristiques principales :</strong></p>
 * <ul>
 *     <li>Code à usage unique (basculement du flag {@code used} à {@code true})</li>
 *     <li>Durée de vie limitée (définie par {@code expiryDate})</li>
 *     <li>Associé à une adresse email spécifique</li>
 *     <li>Stockage persistant en base de données pour vérification ultérieure</li>
 *     <li>Horodatage de création pour traçabilité</li>
 * </ul>
 *
 * <p><strong>Durée de vie typique :</strong> 15 minutes après génération.
 * Au-delà, le code est considéré comme expiré et la validation est refusée.</p>
 *
 * <p><strong>Processus complet de vérification :</strong></p>
 * <ol>
 *     <li>L'utilisateur saisit son email sur le formulaire "Mot de passe oublié"</li>
 *     <li>Le backend génère un code aléatoire à 6 chiffres et persiste un {@code VerificationCode}</li>
 *     <li>Le code est envoyé par email à l'utilisateur</li>
 *     <li>L'utilisateur saisit le code reçu dans l'interface</li>
 *     <li>Le backend vérifie la validité du code (existence, non expiré, non utilisé, correspondance email)</li>
 *     <li>Si valide, le code est marqué comme {@code used = true}</li>
 *     <li>L'utilisateur peut alors procéder à l'opération sensible (réinitialisation, etc.)</li>
 * </ol>
 *
 * <p><strong>Différence avec {@link PasswordResetToken} :</strong></p>
 * <ul>
 *     <li>Le {@code VerificationCode} est un code court à 6 chiffres (plus facile à saisir)</li>
 *     <li>Le {@code PasswordResetToken} est un token UUID long (utilisé dans l'URL)</li>
 *     <li>Le code de vérification est utilisé pour la première étape de validation</li>
 *     <li>Le token de réinitialisation est utilisé pour l'étape finale de changement de mot de passe</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see PasswordResetToken
 * @see VerificationCodeService
 * @since Sprint 2
 */
@Entity
@Table(name = "verification_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    /**
     * Identifiant unique du code de vérification.
     * <p>
     * Clé primaire générée automatiquement par la base de données avec
     * une stratégie d'auto-incrémentation ({@link GenerationType#IDENTITY}).
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Adresse email de l'utilisateur demandant la vérification.
     * <p>
     * Cette adresse email est celle à laquelle le code de vérification
     * a été envoyé. Lors de la validation, le système vérifie que le
     * code est associé à l'email saisi par l'utilisateur.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être {@code null}</li>
     *     <li>Doit correspondre à une adresse email valide</li>
     *     <li>Doit exister dans la table {@code user}</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "jean.dupont@leoni.com"</p>
     */
    @Column(nullable = false)
    private String email;
    /**
     * Code de vérification à 6 chiffres envoyé par email.
     * <p>
     * Ce code est généré de manière aléatoire (généralement 6 chiffres)
     * et doit être saisi par l'utilisateur pour prouver qu'il a bien
     * accès à l'adresse email déclarée.
     * </p>
     *
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *     <li>Généré aléatoirement (entre 100000 et 999999)</li>
     *     <li>Code court facile à recopier</li>
     *     <li>Usage unique (flag {@code used})</li>
     *     <li>Durée de vie limitée (15 minutes typiquement)</li>
     * </ul>
     *
     * <p><strong>Exemple de code :</strong> "123456", "789012"</p>
     *
     * <p><strong>Note de sécurité :</strong> Le code ne doit pas être
     * stocké en clair dans les logs. Les codes expirés doivent être
     * nettoyés régulièrement par une tâche programmée.</p>
     */
    @Column(nullable = false)
    private String code;
    /**
     * Date et heure d'expiration du code de vérification.
     * <p>
     * Définit la limite temporelle de validité du code. Au-delà de cette
     * date, le code est considéré comme expiré et ne peut plus être utilisé
     * pour valider l'email.
     * </p>
     *
     * <p><strong>Calcul typique :</strong> {@code LocalDateTime.now().plusMinutes(15)}</p>
     *
     * <p><strong>Vérifications :</strong></p>
     * <ul>
     *     <li>Un code expiré est rejeté même si {@code used = false}</li>
     *     <li>La comparaison se fait avec {@link LocalDateTime#now()}</li>
     *     <li>Il est recommandé d'avoir une tâche planifiée pour nettoyer
     *         les codes expirés de la base de données</li>
     * </ul>
     *
     * @see java.time.LocalDateTime
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    /**
     * Indique si le code de vérification a déjà été utilisé.
     * <p>
     * Un code est à usage unique pour des raisons de sécurité. Une fois
     * que l'utilisateur a validé son code avec succès, celui-ci est marqué
     * comme {@code used = true} et ne peut plus être réutilisé.
     * </p>
     *
     * <p><strong>Comportement :</strong></p>
     * <ul>
     *     <li>Valeur par défaut : {@code false} (code non utilisé)</li>
     *     <li>Passé à {@code true} après une validation réussie</li>
     *     <li>Un code expiré n'est pas utilisé, même si {@code used = false}</li>
     * </ul>
     *
     * <p><strong>Note de sécurité :</strong> L'usage unique empêche la
     * réutilisation d'un même code en cas d'interception (bien que
     * l'expiration limite déjà la fenêtre d'attaque).</p>
     */
    private boolean used;
    /**
     * Date et heure de création du code de vérification.
     * <p>
     * Cette colonne enregistre le moment exact où le code a été généré.
     * Elle est utilisée pour la traçabilité et l'audit, permettant
     * de connaître quand la demande de vérification a été initiée.
     * </p>
     *
     * <p><strong>Valeur par défaut :</strong> Généralement définie à
     * {@code LocalDateTime.now()} lors de la création de l'entité.</p>
     *
     * <p><strong>Utilisations :</strong></p>
     * <ul>
     *     <li>Traçabilité des demandes de vérification</li>
     *     <li>Statistiques sur les demandes (pics, tendances)</li>
     *     <li>Détection des abus (trop de demandes dans un court intervalle)</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> 2024-01-15T14:30:00</p>
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}