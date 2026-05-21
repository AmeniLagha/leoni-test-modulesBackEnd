package com.example.security.token;

import com.example.security.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

/**
 * Interface de repository pour la gestion des entités {@link Token}.
 * <p>
 * Cette interface étend {@link JpaRepository} et fournit les opérations d'accès
 * aux données pour les jetons JWT. Elle offre des méthodes personnalisées pour
 * la recherche, la validation et la suppression des jetons en base de données.
 * </p>
 *
 * <p>Les principales fonctionnalités offertes sont :</p>
 * <ul>
 *     <li>Recherche de tous les jetons valides (non expirés et non révoqués) d'un utilisateur</li>
 *     <li>Recherche d'un jeton par sa valeur</li>
 *     <li>Suppression de tous les jetons d'un utilisateur (déconnexion complète)</li>
 *     <li>Recherche de tous les jetons associés à un utilisateur</li>
 * </ul>
 *
 * <p>Cette interface est utilisée par les services d'authentification (comme
 * {@code JwtService} et {@code AuthenticationService}) pour gérer le cycle
 * de vie des jetons.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see Token
 * @see com.example.security.user.User
 * @see com.example.security.config.JwtService
 * @since Sprint 2
 */
public interface TokenRepository  extends JpaRepository<Token, Integer> {

    /**
     * Récupère tous les jetons valides (actifs) d'un utilisateur donné.
     * <p>
     * Un jeton est considéré comme valide s'il n'est pas expiré OU non révoqué.
     * Cette méthode est utilisée lors du processus d'authentification pour vérifier
     * qu'un utilisateur dispose encore de jetons actifs.
     * </p>
     *
     * <p>La requête utilise une jointure entre les tables {@code Token} et {@code User}
     * pour filtrer les jetons par l'identifiant de l'utilisateur.</p>
     *
     * @param id L'identifiant de l'utilisateur dont on souhaite récupérer les jetons valides
     * @return Une liste contenant tous les jetons valides (non expirés ou non révoqués)
     *         de l'utilisateur spécifié. La liste peut être vide si l'utilisateur n'a
     *         aucun jeton actif.
     *
     * @see Token#expired
     * @see Token#revoked
     */
    @Query(value = """    
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<Token> findAllValidTokenByUser(Integer id);
    /**
     * Recherche un jeton par sa valeur unique.
     * <p>
     * La valeur du jeton (token) étant unique en base de données, cette méthode
     * retourne au maximum un seul jeton. Elle est principalement utilisée par
     * le {@code JwtAuthenticationFilter} pour valider le token reçu dans
     * l'en-tête HTTP {@code Authorization}.
     * </p>
     *
     * <p>La recherche est optimisée grâce à la contrainte d'unicité
     * définie sur la colonne {@code token} dans l'entité {@link Token}.</p>
     *
     * @param token La valeur du jeton JWT à rechercher (chaîne encodée en Base64Url)
     * @return Un {@link Optional} contenant le jeton s'il existe, vide sinon
     *
     * @see Token#token
     */
    Optional<Token> findByToken(String token);
    /**
     * Supprime tous les jetons associés à un utilisateur donné.
     * <p>
     * Cette méthode est transactionnelle : les suppressions sont validées uniquement
     * si la méthode s'exécute sans erreur. En cas d'exception, toutes les suppressions
     * sont annulées (rollback).
     * </p>
     *
     * <p>Cette opération est typiquement utilisée lors de la déconnexion complète
     * d'un utilisateur (logout) pour invalider tous ses jetons actifs, y compris
     * les refresh tokens. Elle peut également être utilisée lors de la suppression
     * d'un compte utilisateur pour nettoyer les données associées.</p>
     *
     * @param userId L'identifiant de l'utilisateur dont on souhaite supprimer tous les jetons
     *
     * @see jakarta.transaction.Transactional
     * @see Token#user
     */
    @Transactional
    void deleteByUserId(Integer userId);
    /**
     * Recherche tous les jetons associés à un utilisateur donné.
     * <p>
     * Cette méthode retourne l'ensemble des jetons (valides, expirés ou révoqués)
     * appartenant à un utilisateur spécifique. Elle peut être utilisée pour :
     * </p>
     * <ul>
     *     <li>Auditer les sessions actives d'un utilisateur</li>
     *     <li>Vérifier le nombre de jetons actifs</li>
     *     <li>Nettoyer les jetons expirés ou révoqués</li>
     *     <li>Récupérer la liste des refresh tokens pour gestion des sessions multiples</li>
     * </ul>
     *
     * @param user L'utilisateur (objet {@link User}) dont on souhaite récupérer les jetons
     * @return Une liste contenant tous les jetons associés à l'utilisateur spécifié.
     *         La liste peut être vide si l'utilisateur n'a aucun jeton en base.
     *
     * @see Token#user
     * @see User
     */
    List<Token> findByUser(User user);

}
