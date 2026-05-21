package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Interface de repository pour la gestion des entités {@link User}.
 * <p>
 * Cette interface étend {@link JpaRepository} et fournit les opérations d'accès
 * aux données pour les utilisateurs du système. Elle contient des méthodes
 * personnalisées pour la recherche, la validation d'unicité et les requêtes
 * complexes impliquant les relations ManyToMany avec les projets.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Vérification d'existence par email ou matricule</li>
 *     <li>Recherche d'utilisateur par email (pour authentification)</li>
 *     <li>Récupération des emails pour notifications (par projet, site, rôle)</li>
 *     <li>Recherche d'utilisateurs spécifiques (PP, MC, MP) par projet et site</li>
 *     <li>Filtrage des emails en excluant l'utilisateur courant</li>
 * </ul>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Vérifier si un email existe déjà
 * boolean exists = userRepository.existsByEmail("user@leoni.com");
 *
 * // Récupérer un utilisateur par son email (authentification)
 * Optional&lt;User&gt; user = userRepository.findByEmail("user@leoni.com");
 *
 * // Récupérer les emails des utilisateurs d'un projet spécifique
 * List&lt;String&gt; emails = userRepository.findActiveUserEmailsByProjet("Mercedes");
 *
 * // Récupérer les utilisateurs PP d'un projet et site
 * List&lt;User&gt; ppUsers = userRepository.findPpUsersByProjectAndSite("BMW", "Manzel Hayet");
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see User
 * @see com.example.security.user.Role
 * @see com.example.security.projet.Projet
 * @see com.example.security.site.Site
 * @since Sprint 2
 */
public interface UserRepository extends JpaRepository<User,Integer> {

    // ============================================================
    // MÉTHODES DE VÉRIFICATION D'EXISTENCE
    // ============================================================

    /**
     * Vérifie si un utilisateur avec l'adresse email spécifiée existe déjà.
     * <p>
     * Cette méthode est utilisée lors de la création ou modification d'un
     * compte utilisateur pour garantir l'unicité de l'email. Elle évite les
     * doublons et les conflits d'identification.
     * </p>
     *
     * @param email L'adresse email à vérifier
     * @return {@code true} si un utilisateur avec cet email existe,
     *         {@code false} sinon
     */
    boolean existsByEmail(String email);
    /**
     * Vérifie si un utilisateur avec le matricule spécifié existe déjà.
     * <p>
     * Le matricule étant un identifiant unique attribué par les RH, cette
     * méthode permet de s'assurer qu'un même employé n'est pas inscrit
     * plusieurs fois dans le système.
     * </p>
     *
     * @param matricule Le matricule à vérifier (entier unique)
     * @return {@code true} si un utilisateur avec ce matricule existe,
     *         {@code false} sinon
     */
    boolean existsByMatricule(Integer matricule);

    // ============================================================
    // MÉTHODES DE RECHERCHE D'EMAILS POUR NOTIFICATIONS
    // ============================================================

    /**
     * Récupère la liste de toutes les adresses email des utilisateurs.
     * <p>
     * Cette méthode est principalement utilisée par les administrateurs
     * pour envoyer des notifications à l'ensemble des utilisateurs du système.
     * </p>
     *
     * @return Liste contenant tous les emails des utilisateurs
     */
    @Query("SELECT u.email FROM User u")
    List<String> findAllUserEmails();
    /**
     * Récupère un utilisateur par son adresse email.
     * <p>
     * Cette méthode est cruciale pour le processus d'authentification :
     * Spring Security l'utilise pour charger les informations de l'utilisateur
     * lors de la connexion.
     * </p>
     *
     * @param email L'adresse email de l'utilisateur recherché
     * @return Un {@link Optional} contenant l'utilisateur s'il existe,
     *         {@link Optional#empty()} sinon
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    /**
     * Récupère tous les emails des utilisateurs actifs.
     * <p>
     * Méthode par défaut qui retourne simplement tous les emails.
     * Elle peut être surchargée ultérieurement pour filtrer uniquement
     * les utilisateurs actifs (non supprimés).
     * </p>
     *
     * @return Liste de tous les emails des utilisateurs
     */
    default List<String> findAllActiveUserEmails() {
        return findAllUserEmails();
    }

    // ============================================================
    // MÉTHODES DE RECHERCHE PAR PROJET
    // ============================================================

    /**
     * Récupère les emails des utilisateurs actifs d'un projet donné.
     * <p>
     * Cette méthode retourne les emails des utilisateurs associés au projet
     * spécifié, ainsi que les administrateurs (qui ont accès à tous les projets).
     * Utilisée pour les notifications ciblées par projet.
     * </p>
     *
     * @param projet Le nom du projet (ex: "Mercedes", "BMW", "Audi")
     * @return Liste des adresses email des utilisateurs concernés
     */
    @Query("""
        SELECT DISTINCT u.email FROM User u
        JOIN u.projets p
        WHERE p.name = :projet OR u.role = 'ADMIN'
    """)
    List<String> findActiveUserEmailsByProjet(@Param("projet") String projet);

    /**
     * Récupère les emails des utilisateurs d'un projet, en excluant l'utilisateur courant.
     * <p>
     * Cette méthode est utile pour envoyer des notifications à tous les
     * membres d'un projet sauf la personne qui a déclenché l'action,
     * évitant ainsi les auto-notifications.
     * </p>
     *
     * @param projet Le nom du projet
     * @param currentEmail L'email de l'utilisateur à exclure
     * @return Liste des adresses email des utilisateurs concernés (sans l'utilisateur courant)
     */
    @Query("""
        SELECT DISTINCT u.email FROM User u
        JOIN u.projets p
        WHERE (p.name = :projet OR u.role = 'ADMIN')
        AND u.email <> :currentEmail
    """)
    List<String> findActiveUserEmailsByProjetExcludingCurrent(
            @Param("projet") String projet,
            @Param("currentEmail") String currentEmail);

    // ============================================================
    // MÉTHODES DE RECHERCHE PAR RÔLE (MC, MP, PP)
    // ============================================================

    /**
     * Récupère les utilisateurs ayant les rôles MC ou MP pour un projet donné.
     * <p>
     * Cette méthode est utilisée pour notifier les équipes de maintenance
     * lorsqu'un item technique est déplacé vers le stock ou modifié.
     * </p>
     *
     * @param projet Le nom du projet
     * @return Liste des utilisateurs avec les rôles MC (Maintenance Corrective)
     *         ou MP (Maintenance Préventive) associés au projet
     */
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.projets p
        WHERE p.name = :projet 
        AND (u.role = 'MC' OR u.role = 'MP')
    """)
    List<User> findMcAndMpByProject(@Param("projet") String projet);
    /**
     * Récupère les emails des utilisateurs filtrés par projet et site.
     * <p>
     * Cette méthode combine deux critères de filtrage : l'appartenance à
     * l'un des projets spécifiés ET au site indiqué. Les administrateurs
     * sont également inclus.
     * </p>
     *
     * @param projets Liste des noms de projets
     * @param site Nom du site de production
     * @return Liste des adresses email correspondant aux critères
     */
    @Query("""
        SELECT DISTINCT u.email FROM User u
        JOIN u.projets p
        WHERE p.name IN :projets
        AND (u.site.name = :site OR u.role = 'ADMIN')
    """)
    List<String> findEmailsByProjectsAndSite(
            @Param("projets") List<String> projets,
            @Param("site") String site);

    /**
     * Récupère les utilisateurs ayant le rôle PP (Responsable Préparation)
     * pour un projet et un site spécifiques.
     * <p>
     * Cette méthode est utilisée pour envoyer des notifications aux
     * responsables préparation lorsqu'une réception est enregistrée
     * ou lorsqu'une fiche de conformité doit être créée.
     * </p>
     *
     * @param projet Le nom du projet
     * @param site Le nom du site de production
     * @return Liste des utilisateurs PP correspondant aux critères
     */
    @Query("""
    SELECT DISTINCT u FROM User u
    JOIN u.projets p
    WHERE u.role = 'PP' 
    AND p.name = :projet
    AND u.site.name = :site
""")
    List<User> findPpUsersByProjectAndSite(@Param("projet") String projet, @Param("site") String site);

    /**
     * Récupère les emails des utilisateurs pour un projet et un site spécifiques.
     * <p>
     * Cette méthode est utilisée pour les notifications ciblées qui doivent
     * atteindre tous les membres d'un projet sur un site particulier,
     * à l'exclusion des administrateurs (qui sont déjà inclus via d'autres
     * mécanismes).
     * </p>
     *
     * @param projet Le nom du projet
     * @param site Le nom du site de production
     * @return Liste des adresses email correspondant aux critères
     */
    @Query("""
    SELECT DISTINCT u.email FROM User u
    JOIN u.projets p
    WHERE p.name = :projet 
    AND u.site.name = :site
""")
    List<String> findEmailsByProjectAndSite(@Param("projet") String projet, @Param("site") String site);


}