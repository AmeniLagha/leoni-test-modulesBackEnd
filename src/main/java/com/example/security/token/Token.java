package com.example.security.token;
import com.example.security.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente un jeton d'authentification JWT stocké en base de données.
 * <p>
 * Cette entité permet de gérer le cycle de vie des jetons JWT (access tokens et refresh tokens)
 * en les persistant en base de données. Cela offre la possibilité de révoquer des jetons,
 * de vérifier leur validité et d'assurer la traçabilité des sessions utilisateur.
 * </p>
 *
 * <p>Les principales fonctionnalités offertes par cette entité sont :</p>
 * <ul>
 *     <li>Stockage persistant des jetons JWT générés</li>
 *     <li>Suivi de l'état de révocation ({@code revoked})</li>
 *     <li>Suivi de l'expiration ({@code expired})</li>
 *     <li>Association avec l'utilisateur propriétaire du jeton</li>
 *     <li>Support des différents types de jetons via {@link TokenType}</li>
 * </ul>
 *
 * <p>Cette approche sans état (stateless) est complétée par une couche de persistance
 * permettant une révocation proactive des jetons en cas de déconnexion ou de compromission.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see TokenType
 * @see com.example.security.user.User
 * @see com.example.security.config.JwtService
 * @since Sprint 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {

    /**
     * Identifiant unique du jeton.
     * <p>
     * Généré automatiquement par la base de données. Cet identifiant est utilisé
     * comme clé primaire pour référencer le jeton dans les relations.
     * </p>
     */
    @Id
    @GeneratedValue
    public Integer id;
    /**
     * Valeur du jeton JWT.
     * <p>
     * Chaîne de caractères contenant le token JWT signé. Ce champ est unique en base
     * de données pour éviter les doublons. La longueur est augmentée (10000 caractères)
     * pour supporter les jetons JWT qui peuvent être relativement longs, notamment
     * lorsqu'ils contiennent de nombreuses claims.
     * </p>
     * <p>Le token est stocké sous sa forme encodée (Base64Url).</p>
     */
    @Column(unique = true, length = 10000)
    public String token;
    /**
     * Type du jeton.
     * <p>
     * Définit le type de jeton selon l'énumération {@link TokenType}.
     * Par défaut, la valeur est {@code BEARER} conformément à la norme OAuth2.
     * </p>
     * <p>Les types possibles sont définis dans l'énumération {@link TokenType}.</p>
     *
     * @see TokenType
     */
    @Enumerated(EnumType.STRING)
    public TokenType tokenType = TokenType.BEARER;
    /**
     * Indique si le jeton a été révoqué.
     * <p>
     * Un jeton révoqué ne peut plus être utilisé pour authentifier des requêtes.
     * La révocation intervient généralement lors de la déconnexion de l'utilisateur
     * ou en cas de détection d'une activité suspecte.
     * </p>
     * <p>Valeur par défaut : {@code false} (jeton actif non révoqué).</p>
     */
    public boolean revoked;
    /**
     * Indique si le jeton a expiré.
     * <p>
     * Un jeton expiré ne peut plus être utilisé pour authentifier des requêtes.
     * Cette information est redondante avec la claim {@code exp} du JWT, mais
     * son stockage en base permet des requêtes de nettoyage plus efficaces.
     * </p>
     * <p>Valeur par défaut : {@code false} (jeton non expiré).</p>
     */
    public boolean expired;
//mapping
    /**
     * Utilisateur associé à ce jeton.
     * <p>
     * Relation Many-to-One entre {@link Token} et {@link User}.
     * Un utilisateur peut posséder plusieurs jetons (multiples sessions,
     * refresh tokens, etc.). La relation est chargée de manière paresseuse
     * (LAZY) pour optimiser les performances.
     * </p>
     * <p>La colonne de jointure s'appelle {@code user_id} dans la table {@code token}.</p>
     *
     * @see User
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User user;
}
