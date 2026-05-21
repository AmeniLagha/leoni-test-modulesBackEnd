package com.example.security.cahierdeCharge;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * Objet de transfert de données (DTO) pour les statistiques mensuelles de création des cahiers des charges.
 * <p>
 * Cette classe encapsule les indicateurs statistiques permettant d'analyser
 * l'évolution de la création des cahiers des charges sur une période donnée.
 * Elle est utilisée pour alimenter les graphiques et indicateurs du tableau de bord.
 * </p>
 *
 * <p><strong>Indicateurs fournis :</strong></p>
 * <ul>
 *     <li>Nombre de cahiers créés par mois</li>
 *     <li>Pourcentage de variation entre les mois consécutifs</li>
 *     <li>Variation entre les deux derniers mois</li>
 *     <li>Tendance générale (hausse, baisse, stable)</li>
 *     <li>Détail du calcul (formule utilisée)</li>
 * </ul>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Appel du service
 * MonthlyStatsDto stats = chargeSheetService.getMonthlyCreationStats("Mercedes", 6);
 *
 * // Affichage des résultats
 * System.out.println("Variation: " + stats.getVariationPercentage() + "%");
 * System.out.println("Tendance: " + stats.getTrend());
 * System.out.println("Nombre de cahiers en Mars: " + stats.getMonthlyCounts().get("2024-03"));
 * </pre>
 *
 * <p><strong>Exemple de données :</strong></p>
 * <pre>
 * monthlyCounts = {
 *     "2024-03": 15,
 *     "2024-02": 12,
 *     "2024-01": 10
 * }
 * monthlyVariations = {
 *     "2024-03": 25.0,   // (15-12)/12*100 = +25%
 *     "2024-02": 20.0    // (12-10)/10*100 = +20%
 * }
 * variationPercentage = 25.0
 * trend = "hausse"
 * currentMonth = "2024-03"
 * previousMonth = "2024-02"
 * formula = "((15 - 12) / 12) × 100 = 25.0%"
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see ChargeSheetService#getMonthlyCreationStats(String, int)
 * @see ChargeSheetService#getLastTwoMonthsVariation(String)
 * @since Sprint 4
 */
@Data
@Builder
public class MonthlyStatsDto {
    /**
     * Carte associant chaque mois au nombre de cahiers créés.
     * <p>
     * La clé est une chaîne au format "YYYY-MM" (ex: "2024-03" pour mars 2024).
     * La valeur est le nombre total de cahiers créés durant ce mois.
     * </p>
     *
     * <p><strong>Format de la clé :</strong> {@code YYYY-MM}
     * <ul>
     *     <li>{@code YYYY} : Année sur 4 chiffres (ex: 2024)</li>
     *     <li>{@code MM} : Mois sur 2 chiffres (de 01 à 12)</li>
     * </ul>
     * </p>
     *
     * <p><strong>Exemple :</strong></p>
     * <pre>
     * monthlyCounts = {
     *     "2024-01": 10,   // 10 cahiers créés en janvier 2024
     *     "2024-02": 12,   // 12 cahiers créés en février 2024
     *     "2024-03": 15    // 15 cahiers créés en mars 2024
     * }
     * </pre>
     *
     * @see #monthlyVariations
     */
    private Map<String, Long> monthlyCounts; // Mois -> Nombre de cahiers
    /**
     * Carte associant chaque mois au pourcentage de variation par rapport au mois précédent.
     * <p>
     * La clé est une chaîne au format "YYYY-MM" représentant le mois courant.
     * La valeur est le pourcentage d'évolution (positif ou négatif) par rapport
     * au mois précédent.
     * </p>
     *
     * <p><strong>Formule de calcul :</strong></p>
     * <pre>
     * variation = ((countMoisCourant - countMoisPrécédent) / countMoisPrécédent) × 100
     * </pre>
     *
     * <p><strong>Exemple :</strong></p>
     * <pre>
     * monthlyVariations = {
     *     "2024-02": 20.0,   // +20% par rapport à janvier
     *     "2024-03": 25.0    // +25% par rapport à février
     * }
     * </pre>
     *
     * @see #monthlyCounts
     */
    private Map<String, Double> monthlyVariations; // Mois -> Pourcentage de variation
    /**
     * Mois le plus récent analysé (mois courant).
     * <p>
     * Correspond au premier élément de la série temporelle, généralement
     * le mois le plus récent avec des données disponibles.
     * </p>
     * <p><strong>Format :</strong> "YYYY-MM" (ex: "2024-03")</p>
     *
     * @see #previousMonth
     */
    private String currentMonth;
    /**
     * Mois précédant le mois courant.
     * <p>
     * Utilisé conjointement avec {@code currentMonth} pour calculer la
     * variation entre les deux derniers mois disponibles.
     * </p>
     * <p><strong>Format :</strong> "YYYY-MM" (ex: "2024-02")</p>
     *
     * @see #currentMonth
     */
    private String previousMonth;
    /**
     * Pourcentage de variation entre le mois courant et le mois précédent.
     * <p>
     * Valeur décimale pouvant être positive (hausse), négative (baisse)
     * ou nulle (stable). Les valeurs sont arrondies à une décimale.
     * </p>
     *
     * <p><strong>Interprétation :</strong></p>
     * <ul>
     *     <li>{@code > 0} : Augmentation (ex: 25.0 = +25%)</li>
     *     <li>{@code < 0} : Diminution (ex: -15.5 = -15.5%)</li>
     *     <li>{@code = 0} : Stabilité (aucune variation)</li>
     * </ul>
     *
     * @see #trend
     * @see #formula
     */
    private double variationPercentage;
    /**
     * Tendance générale de l'évolution.
     * <p>
     * Chaîne de caractères indiquant la direction de la variation.
     * </p>
     *
     * <p><strong>Valeurs possibles :</strong></p>
     * <ul>
     *     <li><strong>"hausse"</strong> : Augmentation (variation > 0)</li>
     *     <li><strong>"baisse"</strong> : Diminution (variation < 0)</li>
     *     <li><strong>"stable"</strong> : Pas de variation (variation = 0)</li>
     * </ul>
     *
     * @see #variationPercentage
     */
    private String trend; // "hausse", "baisse", "stable"
    /**
     * Formule détaillée du calcul de la variation.
     * <p>
     * Chaîne de caractères expliquant le calcul effectué, utile pour la
     * transparence des indicateurs et le débogage.
     * </p>
     *
     * <p><strong>Exemples :</strong></p>
     * <ul>
     *     <li>{@code "((15 - 12) / 12) × 100 = 25.0%"}</li>
     *     <li>{@code "((5 - 8) / 8) × 100 = -37.5%"}</li>
     *     <li>{@code "((10 - 0) / 1) × 100 = 100% (création depuis zéro)"}</li>
     *     <li>{@code "Aucune création sur les deux mois"}</li>
     * </ul>
     */
    private String formula;
}