package com.example.security.cahierdeCharge;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un cahier des charges dans le système.
 * <p>
 * Cette classe modélise l'ensemble des informations relatives à un cahier
 * des charges client. Elle constitue le document principal du processus
 * de gestion des spécifications techniques. Chaque cahier des charges
 * contient des informations générales, une liste d'items techniques
 * (connecteurs) et suit un workflow de validation à travers son statut.
 * </p>
 *
 * <p><strong>Workflow des statuts :</strong></p>
 * <ul>
 *     <li><strong>DRAFT</strong> : Brouillon, en cours de création par l'ingénieur</li>
 *     <li><strong>VALIDATED_ING</strong> : Validé par l'ingénieur, en attente de saisie technique</li>
 *     <li><strong>TECH_FILLED</strong> : Données techniques saisies, en attente validation PT</li>
 *     <li><strong>VALIDATED_PT</strong> : Validé par le technicien, prêt pour envoi fournisseur</li>
 *     <li><strong>SENT_TO_SUPPLIER</strong> : Envoyé au fournisseur</li>
 *     <li><strong>RECEIVED_FROM_SUPPLIER</strong> : Réception confirmée</li>
 *     <li><strong>COMPLETED</strong> : Cahier terminé et clôturé</li>
 * </ul>
 *
 * <p><strong>Relations :</strong></p>
 * <ul>
 *     <li>Un cahier des charges contient plusieurs {@link ChargeSheetItem}</li>
 *     <li>La relation est en cascade : suppression d'un cahier = suppression de ses items</li>
 *     <li>Chargement paresseux (LAZY) pour optimiser les performances</li>
 * </ul>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheetItem
 * @see ChargeSheetStatus
 * @since Sprint 4
 */
@Entity
@Table(name = "charge_sheet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeSheet {
    // ============================================================
    // IDENTIFIANT
    // ============================================================

    /**
     * Identifiant unique du cahier des charges.
     * <p>
     * Clé primaire générée automatiquement par la base de données avec
     * une stratégie d'auto-incrémentation ({@link GenerationType#IDENTITY}).
     * </p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // INFORMATIONS GÉNÉRALES
    // ============================================================

    /**
     * Site de production (plant) concerné par le cahier des charges.
     * <p>
     * Correspond au site LEONI où les modules seront produits.
     * </p>
     *
     * <p><strong>Exemple :</strong> "Manzel Hayet", "LTN1", "LTN2", "Mateur"</p>
     */
    @Column(name = "plant", length = 50)
    private String plant;
    /**
     * Projet client associé au cahier des charges.
     * <p>
     * Nom du projet client (constructeur automobile) pour lequel
     * le cahier des charges est créé.
     * </p>
     *
     * <p><strong>Exemple :</strong> "Mercedes", "BMW", "Audi", "Volkswagen"</p>
     */
    @Column(name = "project", length = 50)
    private String project;
    /**
     * Référence du faisceau (harness reference).
     * <p>
     * Identifiant technique du faisceau de câbles concerné par le cahier.
     * </p>
     */
    @Column(name = "harness_ref", length = 50)
    private String harnessRef;
    /**
     * Personne ou entité ayant émis le cahier des charges.
     * <p>
     * Nom de l'ingénieur ou du service ayant créé le document.
     * </p>
     */
    @Column(name = "issued_by", length = 100)
    private String issuedBy;
    /**
     * Adresse email de contact pour le cahier des charges.
     * <p>
     * Email de la personne référente pour ce cahier.
     * </p>
     */
    @Column(name = "email_address", length = 100)
    private String emailAddress;
    /**
     * Numéro de téléphone de contact.
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    /**
     * Numéro de commande associé au cahier des charges.
     * <p>
     * Identifiant unique de la commande client.
     * </p>
     */
    @Column(name = "order_number", length = 50)
    private String orderNumber;
    /**
     * Numéro du centre de coût pour la facturation.
     */
    @Column(name = "cost_center_number", length = 50)
    private String costCenterNumber;
    /**
     * Date de création du cahier des charges.
     */
    @Column(name = "date")
    private LocalDate date;
    /**
     * Date de livraison souhaitée par le client.
     */
    @Column(name = "preferred_delivery_date")
    private LocalDate preferredDeliveryDate;

    // ============================================================
    // CHAMPS DE STATUT ET AUDIT
    // ============================================================

    /**
     * Statut actuel du cahier des charges dans le workflow de validation.
     * <p>
     * Le statut détermine l'étape du processus et les actions possibles.
     * </p>
     *
     * @see ChargeSheetStatus
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ChargeSheetStatus status;
    /**
     * Nom de l'utilisateur ayant créé le cahier des charges.
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;
    /**
     * Date de création du cahier des charges.
     */
    @Column(name = "created_at")
    private LocalDate createdAt;
    /**
     * Nom du dernier utilisateur ayant modifié le cahier des charges.
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    /**
     * Date de dernière modification du cahier des charges.
     */
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    // ============================================================
    // RELATION AVEC LES ITEMS
    // ============================================================

    /**
     * Liste des items techniques (connecteurs) associés au cahier des charges.
     * <p>
     * Relation One-to-Many avec {@link ChargeSheetItem}. Un cahier peut
     * contenir plusieurs items. La cascade {@link CascadeType#ALL} assure
     * que les opérations (persist, merge, remove) sont propagées aux items.
     * </p>
     *
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *     <li>Cascade ALL : suppression d'un cahier = suppression de ses items</li>
     *     <li>orphanRemoval = true : suppression des items orphelins</li>
     *     <li>Chargement paresseux (LAZY) pour optimiser les performances</li>
     *     <li>Annotation {@code @JsonIgnore} pour éviter les cycles JSON</li>
     * </ul>
     *
     * @see ChargeSheetItem
     */
    @OneToMany(mappedBy = "chargeSheet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<ChargeSheetItem> items = new ArrayList<>();

    // ============================================================
    // MÉTHODES UTILITAIRES
    // ============================================================

    /**
     * Ajoute un item technique au cahier des charges.
     * <p>
     * Cette méthode maintient la cohérence de la relation bidirectionnelle
     * en définissant également le parent ({@code item.setChargeSheet(this)}).
     * </p>
     *
     * @param item L'item technique à ajouter
     */
    public void addItem(ChargeSheetItem item) {
        items.add(item);
        item.setChargeSheet(this);
    }
    /**
     * Supprime un item technique du cahier des charges.
     * <p>
     * Cette méthode maintient la cohérence de la relation bidirectionnelle
     * en supprimant également la référence au parent
     * ({@code item.setChargeSheet(null)}).
     * </p>
     *
     * @param item L'item technique à supprimer
     */
    public void removeItem(ChargeSheetItem item) {
        items.remove(item);
        item.setChargeSheet(null);
    }
}