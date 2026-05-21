package com.example.security.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * Interface de repository pour la gestion des entités {@link VerificationCode}.
 * <p>
 * Cette interface étend {@link JpaRepository} et fournit les opérations d'accès
 * aux données pour les codes de vérification email. Elle permet la recherche,
 * la validation et la suppression des codes en base de données.
 * </p>
 *
 * <p><strong>Fonctionnalités principales :</strong></p>
 * <ul>
 *     <li>Recherche d'un code par email et valeur (non utilisé uniquement)</li>
 *     <li>Suppression de tous les codes associés à un email</li>
 *     <li>Marquage d'un code comme utilisé après validation réussie</li>
 * </ul>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Envoyer un code de vérification
 * VerificationCode code = VerificationCode.builder()
 *     .email("user@leoni.com")
 *     .code("123456")
 *     .expiryDate(LocalDateTime.now().plusMinutes(15))
 *     .used(false)
 *     .build();
 * verificationCodeRepository.save(code);
 *
 * // Vérifier un code saisi par l'utilisateur
 * Optional&lt;VerificationCode&gt; found = verificationCodeRepository
 *     .findByEmailAndCodeAndUsedFalse("user@leoni.com", "123456");
 *
 * // Nettoyer les anciens codes d'un utilisateur
 * verificationCodeRepository.deleteByEmail("user@leoni.com");
 *
 * // Marquer un code comme utilisé
 * verificationCodeRepository.markAsUsed("user@leoni.com", "123456");
 * </pre>
 *
 * <p><strong>Note sur les transactions :</strong>
 * Les méthodes de modification ({@code @Modifying}) sont annotées avec
 * {@code @Transactional} pour garantir l'atomicité des opérations.
 * </p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see VerificationCode
 * @see com.example.security.user.VerificationCodeService
 * @since Sprint 2
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    /**
     * Recherche un code de vérification non utilisé par email et par valeur.
     * <p>
     * Cette méthode permet de retrouver un code de vérification à partir
     * de l'adresse email de l'utilisateur et du code qu'il a saisi.
     * Elle ne retourne que les codes qui n'ont pas encore été utilisés
     * (flag {@code used = false}).
     * </p>
     *
     * <p><strong>Cas d'utilisation :</strong></p>
     * <ol>
     *     <li>L'utilisateur saisit le code reçu par email</li>
     *     <li>Le backend recherche un code correspondant non utilisé</li>
     *     <li>Si trouvé, on vérifie également qu'il n'est pas expiré</li>
     *     <li>Si tout est valide, la vérification est réussie</li>
     * </ol>
     *
     * <p><strong>Requête SQL générée :</strong></p>
     * <pre>
     * SELECT * FROM verification_codes
     * WHERE email = ? AND code = ? AND used = false
     * </pre>
     *
     * @param email L'adresse email de l'utilisateur
     * @param code Le code de vérification saisi par l'utilisateur (6 chiffres)
     * @return Un {@link Optional} contenant le code de vérification s'il existe
     *         et n'a pas été utilisé, {@link Optional#empty()} sinon
     */
    Optional<VerificationCode> findByEmailAndCodeAndUsedFalse(String email, String code);
    /**
     * Supprime tous les codes de vérification associés à une adresse email.
     * <p>
     * Cette méthode supprime en une seule opération toutes les entrées de la
     * table {@code verification_codes} dont l'email correspond à la valeur
     * fournie. Elle est utile pour :
     * </p>
     *
     * <p><strong>Cas d'utilisation :</strong></p>
     * <ul>
     *     <li><strong>Nettoyage avant nouvelle demande :</strong>
     *         Lorsqu'un utilisateur demande un nouveau code, on supprime
     *         tous ses anciens codes (expirés ou non) pour éviter les conflits
     *         et l'accumulation de données inutiles.</li>
     *     <li><strong>Nettoyage après validation réussie :</strong>
     *         Une fois qu'un code a été validé avec succès, on peut supprimer
     *         tous les autres codes associés à cet email.</li>
     *     <li><strong>Suppression d'un compte utilisateur :</strong>
     *         Lorsqu'un utilisateur est supprimé du système, tous ses codes
     *         de vérification doivent également être supprimés.</li>
     * </ul>
     *
     * <p><strong>Requête SQL générée :</strong></p>
     * <pre>
     * DELETE FROM verification_codes WHERE email = ?
     * </pre>
     *
     * <p><strong>Note de performance :</strong>
     * Cette méthode exécute une seule requête DELETE en base de données,
     * ce qui est plus performant que de récupérer d'abord tous les codes
     * puis de les supprimer un par un.</p>
     *
     * @param email L'adresse email dont on souhaite supprimer tous les codes
     *
     * @see Modifying
     * @see Transactional
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode v WHERE v.email = :email")
    void deleteByEmail(String email);

}