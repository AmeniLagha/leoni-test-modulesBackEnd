package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de repository pour la gestion des entités {@link PasswordResetToken}.
 * <p>
 * Cette interface étend {@link JpaRepository} et fournit les opérations d'accès
 * aux données pour les jetons de réinitialisation de mot de passe. Elle permet
 * la recherche, la validation et la suppression des jetons en base de données.
 * </p>
 *
 * <p>Les principales fonctionnalités offertes sont :</p>
 * <ul>
 *     <li>Recherche d'un jeton par sa valeur unique ({@code findByToken})</li>
 *     <li>Suppression de tous les jetons associés à un utilisateur ({@code deleteByUserId})</li>
 *     <li>Héritage des méthodes CRUD standard de {@link JpaRepository}</li>
 * </ul>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Créer un nouveau jeton
 * PasswordResetToken token = PasswordResetToken.builder()
 *     .token(UUID.randomUUID().toString())
 *     .user(user)
 *     .expiryDate(LocalDateTime.now().plusMinutes(30))
 *     .used(false)
 *     .build();
 * passwordResetTokenRepository.save(token);
 *
 * // Rechercher un jeton par sa valeur
 * Optional&lt;PasswordResetToken&gt; found = passwordResetTokenRepository.findByToken(tokenValue);
 *
 * // Nettoyer les jetons d'un utilisateur supprimé
 * passwordResetTokenRepository.deleteByUserId(userId);
 * </pre>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see PasswordResetToken
 * @see com.example.security.user.User
 * @since Sprint 2
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    /**
     * Recherche un jeton de réinitialisation par sa valeur unique.
     * <p>
     * Cette méthode permet de retrouver un jeton à partir de sa chaîne de caractères
     * (généralement un UUID). La recherche est optimisée grâce à la contrainte
     * d'unicité définie sur la colonne {@code token} dans l'entité
     * {@link PasswordResetToken}.
     * </p>
     *
     * <p>Cette méthode est principalement utilisée lors de la validation
     * du lien de réinitialisation cliqué par l'utilisateur. Elle intervient
     * dans les étapes suivantes :</p>
     * <ol>
     *     <li>Récupération du token depuis l'URL de la requête</li>
     *     <li>Recherche du token en base de données</li>
     *     <li>Vérification de la validité (non expiré, non utilisé)</li>
     *     <li>Récupération de l'utilisateur associé</li>
     *     <li>Autorisation de la réinitialisation du mot de passe</li>
     * </ol>
     *
     * <p><strong>Exemple d'utilisation :</strong></p>
     * <pre>
     * public void validateResetToken(String token) {
     *     PasswordResetToken resetToken = passwordResetTokenRepository
     *         .findByToken(token)
     *         .orElseThrow(() -> new RuntimeException("Token invalide"));
     *
     *     if (resetToken.isExpired()) {
     *         throw new RuntimeException("Token expiré");
     *     }
     *
     *     if (resetToken.isUsed()) {
     *         throw new RuntimeException("Token déjà utilisé");
     *     }
     *
     *     // Token valide, procéder à la réinitialisation
     * }
     * </pre>
     *
     * @param token La valeur unique du jeton de réinitialisation (généralement un UUID)
     * @return Un {@link Optional} contenant le jeton s'il existe en base de données,
     *         {@link Optional#empty()} sinon
     *
     */
    Optional<PasswordResetToken> findByToken(String token);
    /**
     * Supprime tous les jetons de réinitialisation associés à un utilisateur donné.
     * <p>
     * Cette méthode supprime en une seule opération toutes les entrées de la table
     * {@code password_reset_token} dont la clé étrangère {@code user_id} correspond
     * à l'identifiant fourni.
     * </p>
     *
     * <p><strong>Cas d'utilisation :</strong></p>
     * <ul>
     *     <li><strong>Nettoyage lors de la suppression d'un compte utilisateur :</strong>
     *         Lorsqu'un administrateur supprime un utilisateur, tous ses jetons
     *         de réinitialisation doivent être supprimés pour éviter les orphelins
     *         et maintenir l'intégrité référentielle.</li>
     *     <li><strong>Nettoyage des jetons expirés :</strong>
     *         Une tâche planifiée peut utiliser cette méthode via une requête
     *         personnalisée (avec {@code @Query}) pour supprimer les jetons
     *         associés à des utilisateurs inactifs.</li>
     *     <li><strong>Réinitialisation de la sécurité :</strong>
     *         En cas de compromission suspectée, on peut supprimer tous les jetons
     *         d'un utilisateur pour forcer une nouvelle demande.</li>
     * </ul>
     *
     * <p><strong>Note d'implémentation :</strong>
     * Cette méthode exécute une seule requête DELETE en base de données,
     * ce qui est plus performant que de récupérer d'abord tous les jetons
     * puis de les supprimer un par un.</p>
     *
     * <p><strong>Requête SQL générée :</strong></p>
     * <pre>
     * DELETE FROM password_reset_token WHERE user_id = ?
     * </pre>
     *
     * <p><strong>Exemple d'utilisation :</strong></p>
     * <pre>
     * // Suppression d'un utilisateur
     * public void deleteUser(Integer userId) {
     *     // 1. Supprimer d'abord ses jetons de réinitialisation
     *     passwordResetTokenRepository.deleteByUserId(userId);
     *
     *     // 2. Supprimer l'utilisateur
     *     userRepository.deleteById(userId);
     * }
     *
     * // Nettoyage programmé des jetons expirés
     *
     * public void cleanupExpiredTokens() {
     *     List&lt;PasswordResetToken&gt; expiredTokens = tokenRepository
     *         .findAllByExpiryDateBefore(LocalDateTime.now());
     *
     *     expiredTokens.forEach(token ->
     *         passwordResetTokenRepository.deleteByUserId(token.getUser().getId())
     *     );
     * }
     * </pre>
     *
     * @param userId L'identifiant de l'utilisateur dont on souhaite supprimer
     *               tous les jetons de réinitialisation
     * @see org.springframework.transaction.annotation.Transactional
     * @see org.springframework.scheduling.annotation.Scheduled
     */
    void deleteByUserId(Integer userId);
}