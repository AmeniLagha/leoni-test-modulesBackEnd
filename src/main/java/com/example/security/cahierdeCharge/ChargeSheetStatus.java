package com.example.security.cahierdeCharge;

/**
 * Énumération des statuts possibles pour un cahier des charges.
 * <p>
 * Cette énumération définit l'ensemble des étapes du workflow de validation
 * d'un cahier des charges, depuis sa création jusqu'à sa finalisation.
 * Chaque statut représente une étape spécifique du processus métier et
 * détermine les actions possibles et les acteurs autorisés.
 * </p>
 *
 * <p><strong>Workflow complet (7 étapes) :</strong></p>
 * <pre>
 * DRAFT → VALIDATED_ING → TECH_FILLED → VALIDATED_PT → SENT_TO_SUPPLIER → RECEIVED_FROM_SUPPLIER → COMPLETED
 *    ↑_______________________|
 *    (retour possible par PT)
 * </pre>
 *
 * <p><strong>Rôles et actions par statut :</strong></p>
 * <table border="1">
 *     <caption>Actions autorisées par statut</caption>
 *     <tr>
 *         <th>Statut</th>
 *         <th>Rôle</th>
 *         <th>Actions possibles</th>
 *     </tr>
 *     <tr>
 *         <td>DRAFT</td>
 *         <td>ING</td>
 *         <td>Création, modification des infos générales, ajout/suppression d'items</td>
 *     </tr>
 *     <tr>
 *         <td>VALIDATED_ING</td>
 *         <td>PT</td>
 *         <td>Saisie des attributs techniques (150+ champs)</td>
 *     </tr>
 *     <tr>
 *         <td>TECH_FILLED</td>
 *         <td>PT</td>
 *         <td>Validation technique, ou retour à ING si corrections nécessaires</td>
 *     </tr>
 *     <tr>
 *         <td>VALIDATED_PT</td>
 *         <td>PT</td>
 *         <td>Envoi au fournisseur</td>
 *     </tr>
 *     <tr>
 *         <td>SENT_TO_SUPPLIER</td>
 *         <td>PT</td>
 *         <td>Enregistrement des réceptions</td>
 *     </tr>
 *     <tr>
 *         <td>RECEIVED_FROM_SUPPLIER</td>
 *         <td>PT</td>
 *         <td>Finalisation du traitement</td>
 *     </tr>
 *     <tr>
 *         <td>COMPLETED</td>
 *         <td>-</td>
 *         <td>Statut terminal, lecture seule</td>
 *     </tr>
 * </table>
 *
 * <p><strong>Règles de transition :</strong></p>
 * <ul>
 *     <li><strong>DRAFT → VALIDATED_ING</strong> : ING valide le cahier (tous champs obligatoires renseignés)</li>
 *     <li><strong>VALIDATED_ING → TECH_FILLED</strong> : PT saisit les 150+ attributs techniques</li>
 *     <li><strong>TECH_FILLED → VALIDATED_PT</strong> : PT valide les données techniques</li>
 *     <li><strong>VALIDATED_PT → SENT_TO_SUPPLIER</strong> : PT confirme l'envoi au fournisseur</li>
 *     <li><strong>SENT_TO_SUPPLIER → RECEIVED_FROM_SUPPLIER</strong> : PT confirme la réception</li>
 *     <li><strong>RECEIVED_FROM_SUPPLIER → COMPLETED</strong> : PT finalise le traitement</li>
 *     <li><strong>Retour possible</strong> : PT peut retourner un cahier au statut DRAFT depuis
 *         VALIDATED_ING ou TECH_FILLED (avec justification)</li>
 * </ul>
 *
 * <p><strong>Visibilité par rôle :</strong></p>
 * <ul>
 *     <li><strong>ING</strong> : DRAFT uniquement (ses créations en attente de validation)</li>
 *     <li><strong>PT</strong> : VALIDATED_ING, TECH_FILLED, VALIDATED_PT, SENT_TO_SUPPLIER, RECEIVED_FROM_SUPPLIER</li>
 *     <li><strong>PP</strong> : VALIDATED_PT, SENT_TO_SUPPLIER, RECEIVED_FROM_SUPPLIER, COMPLETED</li>
 *     <li><strong>MC/MP</strong> : COMPLETED uniquement</li>
 *     <li><strong>ADMIN</strong> : Tous les statuts</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheet
 * @see ChargeSheetService
 * @since Sprint 4
 */
public enum ChargeSheetStatus {
    /**
     * Statut BROUILLON.
     * <p>
     * Statut initial d'un cahier des charges venant d'être créé par un ingénieur (ING).
     * À ce stade, le cahier est en cours d'élaboration :
     * <ul>
     *     <li>L'ING peut modifier les informations générales</li>
     *     <li>L'ING peut ajouter, modifier ou supprimer des items</li>
     *     <li>L'ING peut uploader des images pour les connecteurs</li>
     *     <li>Aucune donnée technique n'est encore saisie</li>
     * </ul>
     * </p>
     * <p><strong>Transition possible :</strong> {@code DRAFT → VALIDATED_ING} (par ING)</p>
     */
    DRAFT,              // Créé par ING (non validé)
    /**
     * Statut VALIDÉ PAR L'INGÉNIEUR.
     * <p>
     * Le cahier a été validé par l'ingénieur après vérification des informations
     * générales et des items de base. Il est maintenant prêt pour la saisie
     * des attributs techniques par le technicien (PT).
     * </p>
     * <p><strong>Actions possibles :</strong></p>
     * <ul>
     *     <li>Le PT peut commencer la saisie des 150+ attributs techniques</li>
     *     <li>Le PT peut retourner le cahier à ING (DRAFT) si des corrections sont nécessaires</li>
     * </ul>
     * <p><strong>Transitions possibles :</strong></p>
     * <ul>
     *     <li>{@code VALIDATED_ING → TECH_FILLED} (après saisie technique par PT)</li>
     *     <li>{@code VALIDATED_ING → DRAFT} (retour à ING par PT)</li>
     * </ul>
     */
    VALIDATED_ING,      // Validé par ING après création
    /**
     * Statut DONNÉES TECHNIQUES REMPLIES.
     * <p>
     * Le technicien (PT) a saisi l'ensemble des attributs techniques pour tous
     * les items du cahier. Le cahier est en attente de validation finale par le PT.
     * </p>
     * <p><strong>Note :</strong> Ce statut est atteint automatiquement lorsque
     * tous les items ont leur statut {@code TECH_FILLED}.</p>
     * <p><strong>Transitions possibles :</strong></p>
     * <ul>
     *     <li>{@code TECH_FILLED → VALIDATED_PT} (validation par PT)</li>
     *     <li>{@code TECH_FILLED → DRAFT} (retour à ING par PT)</li>
     * </ul>
     */
    TECH_FILLED,        // Rempli par PT (en cours)
    /**
     * Statut VALIDÉ PAR LE TECHNICIEN.
     * <p>
     * Le technicien a vérifié et validé l'ensemble des données techniques
     * saisies. Le cahier est maintenant prêt à être envoyé au fournisseur.
     * </p>
     * <p><strong>Actions possibles :</strong></p>
     * <ul>
     *     <li>Le PT peut envoyer la commande au fournisseur</li>
     *     <li>Le PP peut consulter le cahier et préparer les fiches de conformité</li>
     * </ul>
     * <p><strong>Transition possible :</strong> {@code VALIDATED_PT → SENT_TO_SUPPLIER} (par PT)</p>
     */
    VALIDATED_PT,       // Validé par PT après remplissage
    /**
     * Statut ENVOYÉ AU FOURNISSEUR.
     * <p>
     * Le cahier des charges a été transmis au fournisseur. Les modules
     * commandés sont en cours de fabrication ou de livraison.
     * </p>
     * <p><strong>Actions possibles :</strong></p>
     * <ul>
     *     <li>Le PT peut enregistrer les réceptions partielles ou complètes</li>
     *     <li>Le PP peut créer des fiches de conformité après réception</li>
     * </ul>
     * <p><strong>Transition possible :</strong> {@code SENT_TO_SUPPLIER → RECEIVED_FROM_SUPPLIER}
     * (lorsque la première réception est enregistrée)</p>
     */
    SENT_TO_SUPPLIER,       // envoyé au fournisseur
    /**
     * Statut RÉCEPTION CONFIRMÉE.
     * <p>
     * Au moins une réception a été enregistrée. Le statut passe automatiquement
     * à ce niveau lors de la première réception. Si des items restent à recevoir,
     * le statut reste {@code SENT_TO_SUPPLIER} jusqu'à réception complète.
     * </p>
     * <p><strong>Actions possibles :</strong></p>
     * <ul>
     *     <li>Le PT peut continuer à enregistrer des réceptions supplémentaires</li>
     *     <li>Le PP peut créer les fiches de conformité pour les unités reçues</li>
     * </ul>
     * <p><strong>Transition possible :</strong> {@code RECEIVED_FROM_SUPPLIER → COMPLETED}
     * (lorsque tous les traitements sont finalisés)</p>
     */
    RECEIVED_FROM_SUPPLIER,
    /**
     * Statut COMPLÉTÉ / TERMINÉ.
     * <p>
     * Statut terminal du workflow. Toutes les étapes du processus sont terminées :
     * <ul>
     *     <li>Création et validation ingénieur</li>
     *     <li>Saisie et validation technique</li>
     *     <li>Envoi au fournisseur</li>
     *     <li>Réception complète des modules</li>
     *     <li>Traitement finalisé</li>
     * </ul>
     * </p>
     * <p><strong>Actions possibles :</strong></p>
     * <ul>
     *     <li>Lecture seule (consultation historique)</li>
     *     <li>Consultation par les équipes de maintenance (MC/MP)</li>
     *     <li>Export des données pour archivage</li>
     * </ul>
     * <p><strong>Transition possible :</strong> Aucune (statut terminal)</p>
     */
    COMPLETED
}
