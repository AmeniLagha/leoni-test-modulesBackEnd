package com.example.security.auth;

import com.example.security.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Objet de transfert de données (DTO) pour une demande d'inscription.
 * <p>
 * Cette classe encapsule l'ensemble des informations nécessaires à la création
 * d'un nouveau compte utilisateur dans le système. Elle est utilisée pour
 * transporter les données du client (frontend Angular) vers le serveur
 * (backend Spring Boot) lors de l'inscription d'un nouvel utilisateur par
 * l'administrateur.
 * </p>
 *
 * <p><strong>Champs requis selon le rôle :</strong></p>
 * <ul>
 *     <li><strong>Tous les rôles :</strong> firstname, lastname, email, password, matricule, role</li>
 *     <li><strong>Rôle ADMIN :</strong> aucun champ supplémentaire (siteName et projets peuvent être null)</li>
 *     <li><strong>Autres rôles (ING, PT, PP, MC, MP) :</strong> siteName et projets sont obligatoires</li>
 * </ul>
 *
 * <p><strong>Validations :</strong></p>
 * <ul>
 *     <li>Les annotations Jakarta Validation assurent la vérification des champs obligatoires</li>
 *     <li>L'email doit respecter le format standard (annotation {@code @Email})</li>
 *     <li>Le matricule est un entier unique ne pouvant pas être null</li>
 * </ul>
 *
 * <p><strong>Exemple de requête JSON pour un ingénieur :</strong></p>
 * <pre>
 * {
 *     "firstname": "Jean",
 *     "lastname": "Dupont",
 *     "email": "jean.dupont@leoni.com",
 *     "password": "password123",
 *     "matricule": 12345,
 *     "projets": ["Mercedes", "BMW"],
 *     "role": "ING",
 *     "siteName": "Manzel Hayet"
 * }
 * </pre>
 *
 * <p><strong>Exemple de requête JSON pour un administrateur :</strong></p>
 * <pre>
 * {
 *     "firstname": "Admin",
 *     "lastname": "System",
 *     "email": "admin@leoni.com",
 *     "password": "admin123",
 *     "matricule": 99999,
 *     "role": "ADMIN"
 * }
 * </pre>
 *
 * <p><strong>Note :</strong>
 * Les règles de validation supplémentaires (site obligatoire pour non-ADMIN,
 * projets obligatoires pour non-ADMIN) sont implémentées dans le service
 * {@link AuthenticationService}.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see AuthenticationService#register(RegisterRequest)
 * @see com.example.security.user.User
 * @see com.example.security.user.Role
 * @since Sprint 2
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    // ============================================================
    // INFORMATIONS PERSONNELLES
    // ============================================================

    /**
     * Prénom de l'utilisateur.
     * <p>
     * Correspond au prénom réel de la personne (ex: "Jean", "Marie", "Ahmed").
     * Ce champ est obligatoire pour tous les utilisateurs.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être {@code null}, vide ou composé uniquement d'espaces</li>
     *     <li>Longueur généralement limitée à 50 caractères</li>
     *     <li>Peut contenir des lettres, tirets, apostrophes et espaces</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "Jean", "Marie-Claire", "Ahmed"</p>
     */
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstname;
    /**
     * Nom de famille de l'utilisateur.
     * <p>
     * Correspond au nom réel de la personne (ex: "Dupont", "Martin", "El Amrani").
     * Ce champ est obligatoire pour tous les utilisateurs.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être {@code null}, vide ou composé uniquement d'espaces</li>
     *     <li>Longueur généralement limitée à 50 caractères</li>
     *     <li>Peut contenir des lettres, tirets, apostrophes et espaces</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "Dupont", "El Amrani", "van der Berg"</p>
     */
    @NotBlank(message = "Le nom est obligatoire")
    private String lastname;
    // ============================================================
    // IDENTIFIANTS DE CONNEXION
    // ============================================================

    /**
     * Adresse email de l'utilisateur.
     * <p>
     * L'email est l'identifiant principal utilisé pour l'authentification.
     * Il doit être unique dans le système.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être {@code null}, vide ou composé uniquement d'espaces</li>
     *     <li>Doit respecter le format standard d'une adresse email</li>
     *     <li>Doit être unique (vérifié dans {@link AuthenticationService})</li>
     *     <li>Généralement l'email professionnel de l'employé</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "jean.dupont@leoni.com"</p>
     *
     * @see AuthenticationService#register(RegisterRequest)
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;
    /**
     * Mot de passe de l'utilisateur.
     * <p>
     * Le mot de passe est saisi par l'utilisateur lors de la création du compte.
     * Il sera encodé avec BCrypt avant d'être stocké en base de données.
     * </p>
     *
     * <p><strong>Recommandations de sécurité :</strong></p>
     * <ul>
     *     <li>Longueur minimale recommandée : 8 caractères</li>
     *     <li>Doit contenir au moins une majuscule, une minuscule, un chiffre</li>
     *     <li>Doit contenir au moins un caractère spécial (@, #, $, !, etc.)</li>
     *     <li>Ne jamais afficher ou logger le mot de passe</li>
     *     <li>Toujours transmis via HTTPS</li>
     * </ul>
     *
     * <p><strong>Note :</strong>
     * Le mot de passe est transmis en clair dans la requête mais est immédiatement
     * encodé côté serveur. Il n'est jamais stocké en clair.</p>
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
// ============================================================
    // IDENTIFIANTS PROFESSIONNELS
    // ============================================================

    /**
     * Matricule unique de l'utilisateur au sein de l'entreprise LEONI.
     * <p>
     * Le matricule est un identifiant numérique unique attribué par les
     * ressources humaines. Il sert de second identifiant en complément de l'email.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être {@code null}</li>
     *     <li>Doit être unique (vérifié dans {@link AuthenticationService})</li>
     *     <li>Généralement composé de 4 à 6 chiffres</li>
     *     <li>Ne change pas pendant toute la carrière de l'employé</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> 12345, 67890</p>
     */
    @NotNull(message = "Le matricule est obligatoire")
    private Integer matricule;
    // ============================================================
    // PROJETS (OPTIONNEL POUR ADMIN, OBLIGATOIRE POUR AUTRES)
    // ============================================================

    /**
     * Liste des noms des projets auxquels l'utilisateur est associé.
     * <p>
     * Cette liste représente l'ensemble des projets clients (ex: "Mercedes",
     * "BMW", "Audi") pour lesquels l'utilisateur aura des droits d'accès.
     * </p>
     *
     * <p><strong>Règles de validation :</strong></p>
     * <ul>
     *     <li><strong>Pour ADMIN :</strong> Peut être {@code null} ou vide</li>
     *     <li><strong>Pour les autres rôles (ING, PT, PP, MC, MP) :</strong>
     *         Au moins un projet est obligatoire (validé dans le service)</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> ["Mercedes", "BMW", "Audi"]</p>
     *
     * @see com.example.security.projet.Projet
     */
    private List<String> projets;
    // ============================================================
    // RÔLE
    // ============================================================

    /**
     * Rôle de l'utilisateur dans le système.
     * <p>
     * Le rôle définit le périmètre fonctionnel de l'utilisateur et détermine
     * l'ensemble des permissions disponibles. Ce champ est obligatoire.
     * </p>
     *
     * <p><strong>Valeurs possibles :</strong></p>
     * <ul>
     *     <li><strong>ING</strong> : Ingénieur (création et validation des cahiers)</li>
     *     <li><strong>PT</strong> : Technicien (saisie technique, réceptions)</li>
     *     <li><strong>PP</strong> : Responsable préparation (conformités, dossiers techniques)</li>
     *     <li><strong>MC</strong> : Maintenance corrective</li>
     *     <li><strong>MP</strong> : Maintenance préventive</li>
     *     <li><strong>ADMIN</strong> : Administrateur (gestion complète du système)</li>
     * </ul>
     *
     * @see Role
     */
    @NotNull(message = "Le rôle est obligatoire")
    private Role role;
    // ============================================================
    // SITE (OPTIONNEL POUR ADMIN, OBLIGATOIRE POUR AUTRES)
    // ============================================================

    /**
     * Nom du site de production de l'utilisateur.
     * <p>
     * Le site détermine l'isolation des données : un utilisateur ne voit
     * que les données de son propre site.
     * </p>
     *
     * <p><strong>Règles de validation :</strong></p>
     * <ul>
     *     <li><strong>Pour ADMIN :</strong> Peut être {@code null} ou vide</li>
     *     <li><strong>Pour les autres rôles (ING, PT, PP, MC, MP) :</strong>
     *         Un site est obligatoire (validé dans le service)</li>
     * </ul>
     *
     * <p><strong>Sites disponibles chez LEONI Tunisia :</strong></p>
     * <ul>
     *     <li><strong>Manzel Hayet</strong> : Site principal du projet MEB</li>
     *     <li><strong>LTN1</strong> : Ligne technique 1</li>
     *     <li><strong>LTN2</strong> : Ligne technique 2</li>
     *     <li><strong>Mateur</strong> : Site secondaire</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "Manzel Hayet", "LTN1", "Mateur"</p>
     *
     * @see com.example.security.site.Site
     */
    private String siteName;
}
