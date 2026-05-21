package com.example.security.cahierdeCharge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
/**
 * Interface de repository pour la gestion des entités {@link ChargeSheetItem}.
 * <p>
 * Cette interface étend {@link JpaRepository} et fournit les opérations d'accès
 * aux données pour les items techniques (connecteurs) d'un cahier des charges.
 * Elle permet la recherche, la sauvegarde, la modification et la suppression
 * des items en base de données.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Héritage des méthodes CRUD standard de {@link JpaRepository}</li>
 *     <li>Recherche de tous les items appartenant à un cahier des charges spécifique</li>
 * </ul>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Récupérer tous les items d'un cahier
 * List&lt;ChargeSheetItem&gt; items = chargeSheetItemRepository.findByChargeSheetId(chargeSheetId);
 *
 * // Sauvegarder un nouvel item
 * ChargeSheetItem newItem = ChargeSheetItem.builder()
 *     .itemNumber("ITEM-001")
 *     .quantityOfTestModules(10)
 *     .chargeSheet(chargeSheet)
 *     .build();
 * chargeSheetItemRepository.save(newItem);
 *
 * // Supprimer un item
 * chargeSheetItemRepository.deleteById(itemId);
 * </pre>
 *
 * <p><strong>Note :</strong>
 * Cette interface est utilisée par {@link ChargeSheetService} pour toutes les
 * opérations liées aux items techniques, notamment la saisie des 150+ attributs
 * techniques, l'upload d'images et la gestion des réceptions.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheetItem
 * @see ChargeSheetService
 * @since Sprint 5
 */

@Repository
public interface ChargeSheetItemRepository extends JpaRepository<ChargeSheetItem, Long> {
    /**
     * Récupère tous les items techniques (connecteurs) associés à un cahier des charges spécifique.
     * <p>
     * Cette méthode retourne la liste complète des items appartenant au cahier
     * identifié par l'ID fourni. Les résultats sont retournés dans l'ordre
     * de création (par défaut, selon l'ID croissant).
     * </p>
     *
     * <p><strong>Cas d'utilisation :</strong></p>
     * <ul>
     *     <li>Affichage de la liste des items dans l'interface de saisie technique</li>
     *     <li>Vérification de l'existence d'items avant validation du cahier</li>
     *     <li>Calcul des quantités totales commandées par cahier</li>
     *     <li>Génération des fiches de conformité après réception</li>
     * </ul>
     *
     * <p><strong>Requête SQL générée :</strong></p>
     * <pre>
     * SELECT * FROM charge_sheet_item WHERE charge_sheet_id = ?
     * </pre>
     *
     * @param chargeSheetId L'identifiant du cahier des charges parent
     * @return Une liste contenant tous les items associés au cahier.
     *         Retourne une liste vide si le cahier n'a pas d'items.
     *
     * @see ChargeSheet#getId()
     */
    List<ChargeSheetItem> findByChargeSheetId(Long chargeSheetId);
}