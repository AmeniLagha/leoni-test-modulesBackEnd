package com.example.security.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
 * Énumération des permissions granulaires du système.
 * <p>
 * Cette énumération définit l'ensemble des droits d'accès disponibles dans
 * l'application. Chaque permission est associée à une chaîne de caractères
 * unique qui sert d'identifiant pour le contrôle d'accès basé sur les rôles (RBAC).
 * </p>
 *
 * <p><strong>Structure des permissions :</strong></p>
 * <ul>
 *     <li><strong>Format :</strong> {@code module:action}</li>
 *     <li><strong>Module :</strong> charge_sheet, compliance, technical_file, claim, stock, admin, etc.</li>
 *     <li><strong>Action :</strong> create, read, write, delete, update, execute</li>
 * </ul>
 *
 * <p><strong>Classification des permissions par module :</strong></p>
 * <ul>
 *     <li><strong>Cahiers des charges (CHARGE_SHEET) :</strong> 7 permissions
 *         (basic create/read/write, tech read/write, all read)</li>
 *     <li><strong>Conformité (COMPLIANCE) :</strong> 3 permissions
 *         (create, read, write)</li>
 *     <li><strong>Dossiers techniques (TECHNICAL_FILE) :</strong> 3 permissions
 *         (create, read, write)</li>
 *     <li><strong>Maintenance corrective (MAINTENANCE_CORRECTIVE) :</strong> 2 permissions
 *         (read, write)</li>
 *     <li><strong>Maintenance préventive (MAINTENANCE_PREVENTIVE) :</strong> 2 permissions
 *         (read, write)</li>
 *     <li><strong>Réclamations (CLAIM) :</strong> 3 permissions
 *         (create, read, write)</li>
 *     <li><strong>Stock (STOCK) :</strong> 2 permissions
 *         (read, write)</li>
 *     <li><strong>Administration (ADMIN) :</strong> 5 permissions
 *         (read, update, create, delete, createuser, readuser)</li>
 *     <li><strong>Autres :</strong> search, reception</li>
 * </ul>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Dans un contrôleur Spring, utiliser l'annotation PreAuthorize
 * // avec la permission correspondante :
 * // &#64;PreAuthorize("hasAuthority('charge_sheet:basic:create')")
 * public ResponseEntity&lt;?&gt; createChargeSheet() { ... }
 *
 * // Vérification programmatique
 * if (hasPermission(Permission.CHARGE_SHEET_BASIC_READ)) {
 *     // L'utilisateur a le droit de lecture
 * }
 *
 * // Récupération de la chaîne de permission
 * String permission = Permission.CLAIM_CREATE.getPermission(); // "claim:create"
 * </pre>
 * <p>
 * <strong>Note :</strong> L'annotation {@code @PreAuthorize} est utilisée
 * par Spring Security pour vérifier les autorisations avant l'exécution
 * d'une méthode. Exemple : {@code @PreAuthorize("hasAuthority('charge_sheet:basic:read')")}
 * </p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see Role
 * @see com.example.security.config.SecurityConfiguration
 * @see org.springframework.security.access.prepost.PreAuthorize
 * @since Sprint 2
 */
@RequiredArgsConstructor
public enum Permission {
    // ============================================================
    // PERMISSIONS - CAHIERS DES CHARGES (CHARGE SHEET)
    // ============================================================

    /**
     * Permission de création de base pour un cahier des charges.
     * <p>
     * Permet à l'utilisateur de créer un nouveau cahier des charges
     * avec les informations générales (DRAFT). Cette permission est
     * généralement attribuée aux rôles ING et ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> création initiale d'un cahier</p>
     */
    CHARGE_SHEET_BASIC_CREATE("charge_sheet:basic:create"),
    /**
     * Permission de lecture de base pour un cahier des charges.
     * <p>
     * Permet à l'utilisateur de consulter les informations générales
     * d'un cahier des charges et la liste des items de base.
     * Cette permission est attribuée à la plupart des rôles (ING, PT, PP, MC, MP, ADMIN).
     * </p>
     * <p><strong>Action associée :</strong> consultation des informations générales</p>
     */
    CHARGE_SHEET_BASIC_READ("charge_sheet:basic:read"),
    /**
     * Permission de modification de base pour un cahier des charges.
     * <p>
     * Permet à l'utilisateur de modifier les informations générales
     * d'un cahier des charges (uniquement en statut DRAFT).
     * Cette permission est généralement attribuée au rôle ING.
     * </p>
     * <p><strong>Action associée :</strong> modification des champs de base</p>
     */
    CHARGE_SHEET_BASIC_WRITE("charge_sheet:basic:write"),
    /**
     * Permission de lecture des données techniques d'un cahier des charges.
     * <p>
     * Permet à l'utilisateur de consulter les 150+ attributs techniques
     * des items d'un cahier des charges. Cette permission est attribuée
     * aux rôles PT, ING, ADMIN pour le suivi technique.
     * </p>
     * <p><strong>Action associée :</strong> consultation des données techniques</p>
     */
    CHARGE_SHEET_TECH_READ("charge_sheet:tech:read"),
    /**
     * Permission de modification des données techniques d'un cahier des charges.
     * <p>
     * Permet à l'utilisateur de saisir et modifier les 150+ attributs techniques
     * des items via l'interface de type tableur. Cette permission est
     * exclusivement attribuée au rôle PT.
     * </p>
     * <p><strong>Action associée :</strong> saisie et modification des attributs techniques</p>
     */
    CHARGE_SHEET_TECH_WRITE("charge_sheet:tech:write"),
    /**
     * Permission de lecture complète sur tous les cahiers des charges.
     * <p>
     * Permet à l'utilisateur de consulter tous les cahiers des charges
     * sans restriction de projet ou de site. Cette permission est
     * réservée au rôle ADMIN pour la supervision globale.
     * </p>
     * <p><strong>Action associée :</strong> visionnage transverse de tous les cahiers</p>
     */
    CHARGE_SHEET_ALL_READ("charge_sheet:all:read"),

    // ============================================================
    // PERMISSIONS - CONFORMITÉ (COMPLIANCE)
    // ============================================================

    /**
     * Permission de création d'une fiche de conformité.
     * <p>
     * Permet à l'utilisateur de générer des fiches de conformité pour
     * les modules reçus. Cette permission est attribuée au rôle PP
     * qui est responsable des contrôles qualité.
     * </p>
     * <p><strong>Action associée :</strong> création de fiches de conformité</p>
     */
    COMPLIANCE_CREATE("compliance:create"),
    /**
     * Permission de lecture des fiches de conformité.
     * <p>
     * Permet à l'utilisateur de consulter les fiches de conformité
     * existantes. Cette permission est attribuée aux rôles PP, PT, ING, MC, MP et ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> consultation des fiches de conformité</p>
     */
    COMPLIANCE_READ("compliance:read"),
    /**
     * Permission de modification des fiches de conformité.
     * <p>
     * Permet à l'utilisateur de modifier les résultats de tests
     * sur les fiches de conformité. Cette permission est attribuée
     * au rôle PP pour la saisie des résultats.
     * </p>
     * <p><strong>Action associée :</strong> modification des résultats de tests</p>
     */
    COMPLIANCE_WRITE("compliance:write"),

    // ============================================================
    // PERMISSIONS - DOSSIERS TECHNIQUES (TECHNICAL FILE)
    // ============================================================

    /**
     * Permission de création de dossiers techniques.
     * <p>
     * Permet à l'utilisateur de créer des dossiers techniques regroupant
     * des items provenant de différents cahiers des charges.
     * Cette permission est attribuée au rôle PP.
     * </p>
     * <p><strong>Action associée :</strong> création de dossiers transverses</p>
     */
    TECHNICAL_FILE_CREATE("technical_file:create"),
    /**
     * Permission de lecture des dossiers techniques.
     * <p>
     * Permet à l'utilisateur de consulter les dossiers techniques
     * et leurs items associés. Cette permission est attribuée aux
     * rôles PP, PT, MC, MP et ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> consultation des dossiers techniques</p>
     */
    TECHNICAL_FILE_READ("technical_file:read"),
    /**
     * Permission de modification des dossiers techniques.
     * <p>
     * Permet à l'utilisateur d'ajouter ou supprimer des items dans
     * un dossier technique existant. Cette permission est attribuée
     * au rôle PP pour la gestion des dossiers.
     * </p>
     * <p><strong>Action associée :</strong> modification de la composition des dossiers</p>
     */
    TECHNICAL_FILE_WRITE("technical_file:write"),

    // ============================================================
    // PERMISSIONS - MAINTENANCE CORRECTIVE
    // ============================================================

    /**
     * Permission de lecture pour la maintenance corrective.
     * <p>
     * Permet à l'utilisateur de consulter les informations techniques
     * nécessaires à la maintenance corrective. Cette permission est
     * attribuée au rôle MC.
     * </p>
     * <p><strong>Action associée :</strong> consultation des données techniques</p>
     */
    MAINTENANCE_CORRECTIVE_READ("maintenance_corrective:read"),
    /**
     * Permission de modification pour la maintenance corrective.
     * <p>
     * Permet à l'utilisateur de modifier les informations techniques
     * pour corriger des anomalies. Cette permission est attribuée
     * au rôle MC.
     * </p>
     * <p><strong>Action associée :</strong> modification corrective des données</p>
     */
    MAINTENANCE_CORRECTIVE_WRITE("maintenance_corrective:write"),
    // ============================================================
    // PERMISSIONS - MAINTENANCE PRÉVENTIVE
    // ============================================================

    /**
     * Permission de lecture pour la maintenance préventive.
     * <p>
     * Permet à l'utilisateur de consulter les informations techniques
     * pour planifier les maintenances préventives. Cette permission
     * est attribuée au rôle MP.
     * </p>
     * <p><strong>Action associée :</strong> consultation des données techniques</p>
     */
    MAINTENANCE_PREVENTIVE_READ("maintenance_preventive:read"),
    /**
     * Permission de modification pour la maintenance préventive.
     * <p>
     * Permet à l'utilisateur de mettre à jour les informations
     * techniques dans le cadre de la maintenance préventive.
     * Cette permission est attribuée au rôle MP.
     * </p>
     * <p><strong>Action associée :</strong> mise à jour préventive des données</p>
     */
    MAINTENANCE_PREVENTIVE_WRITE("maintenance_preventive:write"),
    // ============================================================
    // PERMISSIONS - RÉCLAMATIONS (CLAIM)
    // ============================================================

    /**
     * Permission de lecture des réclamations.
     * <p>
     * Permet à l'utilisateur de consulter la liste des réclamations
     * et leurs détails. Cette permission est attribuée à tous les
     * rôles (PP, PT, MC, MP, ING, ADMIN).
     * </p>
     * <p><strong>Action associée :</strong> consultation des réclamations</p>
     */
    CLAIM_READ("claim:read"),
    /**
     * Permission de modification des réclamations.
     * <p>
     * Permet à l'utilisateur de modifier une réclamation existante
     * (ajout de commentaires, mise à jour des actions correctives).
     * Cette permission est attribuée au créateur de la réclamation
     * et à l'utilisateur assigné.
     * </p>
     * <p><strong>Action associée :</strong> modification des réclamations</p>
     */
    CLAIM_WRITE("claim:write"),
    /**
     * Permission de création de réclamations.
     * <p>
     * Permet à l'utilisateur d'ouvrir une nouvelle réclamation
     * pour signaler une anomalie. Cette permission est attribuée
     * aux rôles PP et PT.
     * </p>
     * <p><strong>Action associée :</strong> création de réclamations</p>
     */
    CLAIM_CREATE("claim:create"),
    // ============================================================
    // PERMISSIONS - STOCK
    // ============================================================

    /**
     * Permission de lecture du stock.
     * <p>
     * Permet à l'utilisateur de consulter l'état du stock des modules
     * testés (quantités, statuts, fournisseurs). Cette permission est
     * attribuée aux rôles PP, PT, MC, MP et ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> consultation du stock</p>
     */

    STOCK_READ("stock:read"),
    /**
     * Permission de modification du stock.
     * <p>
     * Permet à l'utilisateur d'effectuer des opérations sur le stock
     * (prélèvement, retour, changement de statut). Cette permission
     * est attribuée aux rôles PP, MC et MP.
     * </p>
     * <p><strong>Action associée :</strong> modification du stock (prélèvement/retour)</p>
     */
    STOCK_WRITE("stock:write"),
    // ============================================================
    // PERMISSIONS - RECHERCHE
    // ============================================================

    /**
     * Permission d'exécution de recherche globale.
     * <p>
     * Permet à l'utilisateur d'utiliser la fonctionnalité de recherche
     * avancée sur l'ensemble des modules de l'application.
     * Cette permission est attribuée à tous les rôles actifs.
     * </p>
     * <p><strong>Action associée :</strong> recherche globale</p>
     */
    SEARCH("search:execute"),

    // ============================================================
    // PERMISSIONS - ADMINISTRATION
    // ============================================================

    /**
     * Permission de lecture des données d'administration.
     * <p>
     * Permet à l'utilisateur de consulter les tableaux de bord,
     * les statistiques et les rapports d'audit. Cette permission
     * est réservée au rôle ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> consultation des données administratives</p>
     */
    ADMIN_READ("admin:read"),
    /**
     * Permission de modification des paramètres d'administration.
     * <p>
     * Permet à l'utilisateur de modifier les configurations système,
     * les paramètres généraux et les constantes applicatives.
     * Cette permission est réservée au rôle ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> modification des paramètres système</p>
     */
    ADMIN_UPDATE("admin:update"),
    /**
     * Permission de création de ressources administratives.
     * <p>
     * Permet à l'utilisateur de créer des sites, des projets
     * et d'autres ressources nécessitant des droits élevés.
     * Cette permission est réservée au rôle ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> création de ressources système</p>
     */
    ADMIN_CREATE("admin:create"),
    /**
     * Permission de suppression de ressources administratives.
     * <p>
     * Permet à l'utilisateur de supprimer définitivement des données
     * (cahiers des charges, utilisateurs, etc.). Cette permission
     * est réservée au rôle ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> suppression de données sensibles</p>
     */
    ADMIN_DELETE("admin:delete"),
    /**
     * Permission de création d'utilisateurs.
     * <p>
     * Permet à l'utilisateur d'ajouter de nouveaux comptes utilisateurs
     * dans le système. Cette permission est réservée au rôle ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> création de comptes utilisateurs</p>
     */
    AJOUTE_USER("admin:createuser"),
    /**
     * Permission de consultation de la liste des utilisateurs.
     * <p>
     * Permet à l'utilisateur de visualiser l'ensemble des comptes
     * utilisateurs du système. Cette permission est réservée au rôle ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> consultation de la liste des utilisateurs</p>
     */
    AJOUTE_USER_LISTE("admin:readuser"),
    /**
     * Permission de lecture des réceptions.
     * <p>
     * Permet à l'utilisateur de consulter l'historique des réceptions
     * fournisseur. Cette permission est attribuée aux rôles PT, PP et ADMIN.
     * </p>
     * <p><strong>Action associée :</strong> consultation des réceptions</p>
     */
    RECEPTION_READ("reception:read");

    /**
     * Chaîne de caractères représentant la permission.
     * <p>
     * Format : {@code module:action} (ex: "charge_sheet:basic:create").
     * Cette valeur est utilisée par Spring Security pour vérifier
     * les autorisations via l'annotation {@code @PreAuthorize}.
     * </p>
     *
     * @see org.springframework.security.access.prepost.PreAuthorize
     */
    @Getter
    private final String permission;
}