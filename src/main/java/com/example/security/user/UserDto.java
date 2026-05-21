package com.example.security.user;

import lombok.Data;
import java.util.List;

/**
 * Objet de transfert de données (DTO) pour l'entité {@link User}.
 * <p>
 * Cette classe est utilisée pour transporter les informations d'un utilisateur
 * entre le serveur (backend Spring Boot) et le client (frontend Angular).
 * Elle permet de contrôler précisément les données exposées par l'API REST,
 * contrairement à l'entité {@link User} qui contient des champs sensibles
 * comme le mot de passe.
 * </p>
 *
 * <p><strong>Objectifs du DTO :</strong></p>
 * <ul>
 *     <li>Sérialisation/désérialisation JSON pour les échanges API</li>
 *     <li>Masquage des champs sensibles (mot de passe, relations JPA complexes)</li>
 *     <li>Regroupement des données utilisateur avec leurs permissions</li>
 *     <li>Prévention des appels circulaires (évite les références User ↔ Role)</li>
 *     <li>Optimisation des transferts réseau (uniquement les champs nécessaires)</li>
 * </ul>
 *
 * <p><strong>Utilisation typique :</strong></p>
 * <pre>
 * // Conversion de l'entité User vers UserDto
 * User user = userRepository.findById(1);
 * UserDto userDto = UserDto.builder()
 *     .id(user.getId())
 *     .firstname(user.getFirstname())
 *     .lastname(user.getLastname())
 *     .email(user.getEmail())
 *     .matricule(user.getMatricule())
 *     .role(user.getRole().name())
 *     .site(user.getSite().getName())
 *     .projets(user.getProjetsNames())
 *     .permissions(user.getRole().getPermissions())
 *     .build();
 *
 * // Envoi au frontend (sans le mot de passe)
 * return ResponseEntity.ok(userDto);
 * </pre>
 *
 * <p><strong>Note de sécurité :</strong>
 * Le champ {@code password} n'est pas inclus dans la sérialisation JSON
 * car il n'est pas nécessaire côté frontend (l'utilisateur ne doit jamais
 * recevoir son mot de passe depuis l'API). Ce DTO est utilisé uniquement
 * pour les réponses de l'API.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see User
 * @see UserController
 * @see UserService
 * @since Sprint 2
 */
@Data
    public class UserDto {
    /**
     * Identifiant unique de l'utilisateur.
     * <p>
     * Correspond à la clé primaire de l'entité {@link User}.
     * Cet identifiant est utilisé pour référencer l'utilisateur
     * dans les opérations CRUD (modification, suppression, etc.).
     * </p>
     *
     * <p><strong>Valeurs possibles :</strong> entier positif unique</p>
     */
        private Integer id;
    /**
     * Prénom de l'utilisateur.
     * <p>
     * Correspond au prénom réel de la personne (ex: "Jean", "Marie").
     * Utilisé pour l'affichage dans l'interface utilisateur et
     * pour la personnalisation des notifications par email.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être vide ou null</li>
     *     <li>Longueur généralement limitée à 50 caractères</li>
     *     <li>Peut contenir des lettres, tirets, apostrophes</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "Jean", "Marie-Claire"</p>
     */
        private String firstname;
    /**
     * Nom de famille de l'utilisateur.
     * <p>
     * Correspond au nom réel de la personne (ex: "Dupont", "Martin").
     * Utilisé pour l'affichage dans l'interface utilisateur et
     * pour la personnalisation des notifications par email.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Ne peut pas être vide ou null</li>
     *     <li>Longueur généralement limitée à 50 caractères</li>
     *     <li>Peut contenir des lettres, tirets, espaces</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "Dupont", "El Amrani"</p>
     */
        private String lastname;
    /**
     * Matricule unique de l'utilisateur au sein de l'entreprise LEONI.
     * <p>
     * Le matricule est un identifiant numérique unique attribué à chaque
     * employé par les ressources humaines. Il est utilisé comme second
     * identifiant en complément de l'email.
     * </p>
     *
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *     <li>Valeur entière unique</li>
     *     <li>Ne peut pas être null</li>
     *     <li>Généralement composé de 4 à 6 chiffres</li>
     *     <li>Ne change pas pendant toute la carrière de l'employé</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> 12345</p>
     */
        private Integer matricule;
    /**
     * Mot de passe de l'utilisateur.
     * <p>
     * <strong>⚠️ Note de sécurité importante :</strong>
     * Ce champ est présent dans la classe DTO mais n'est pas utilisé
     * dans les réponses API. Il est conservé pour d'éventuelles opérations
     * de modification de mot de passe en requête.
     * </p>
     *
     * <p>Le mot de passe n'est jamais inclus dans les réponses JSON
     * du backend vers le frontend pour des raisons de sécurité évidentes.</p>
     *
     * <p><strong>Recommandations de sécurité :</strong></p>
     * <ul>
     *     <li>Longueur minimale de 8 caractères</li>
     *     <li>Combinaison de majuscules, minuscules, chiffres et caractères spéciaux</li>
     *     <li>Stocké uniquement sous forme hashée (BCrypt) dans la base</li>
     *     <li>Jamais affiché ou loggé</li>
     * </ul>
     *
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
        private String password;
    /**
     * Adresse email de l'utilisateur.
     * <p>
     * L'email est l'identifiant principal utilisé pour l'authentification
     * dans le système. Il doit être unique et correspondre généralement
     * à l'adresse email professionnelle de l'employé.
     * </p>
     *
     * <p><strong>Contraintes :</strong></p>
     * <ul>
     *     <li>Doit être unique dans le système</li>
     *     <li>Doit respecter le format d'une adresse email valide</li>
     *     <li>Ne peut pas être modifié après création (sauf par ADMIN)</li>
     *     <li>Utilisé pour les notifications et la réinitialisation de mot de passe</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> "jean.dupont@leoni.com"</p>
     */
        private String email;
    /**
     * Liste des noms des projets auxquels l'utilisateur est associé.
     * <p>
     * Cette liste représente l'ensemble des projets clients (ex: "Mercedes",
     * "BMW", "Audi") pour lesquels l'utilisateur a des droits d'accès.
     * Les données visibles par l'utilisateur sont filtrées selon ces projets.
     * </p>
     *
     * <p><strong>Fonctionnalités :</strong></p>
     * <ul>
     *     <li>Isolation des données par projet</li>
     *     <li>Filtrage des cahiers des charges, réceptions, etc.</li>
     *     <li>Utilisé pour les notifications ciblées</li>
     *     <li>Relation Many-to-Many avec l'entité {@code Projet}</li>
     * </ul>
     *
     * <p><strong>Exemple :</strong> ["Mercedes", "BMW"]</p>
     *
     * @see User#getProjetsNames()
     */
    private List<String> projets;
    /**
     * Nom du rôle de l'utilisateur.
     * <p>
     * Le rôle définit le périmètre fonctionnel de l'utilisateur dans
     * l'application. Il détermine l'ensemble des permissions disponibles.
     * </p>
     *
     * <p><strong>Valeurs possibles :</strong></p>
     * <ul>
     *     <li><strong>ING</strong> : Ingénieur (création et validation des cahiers)</li>
     *     <li><strong>PT</strong> : Technicien (saisie technique, réceptions)</li>
     *     <li><strong>PP</strong> : Responsable préparation (conformités, dossiers techniques)</li>
     *     <li><strong>MC</strong> : Maintenance corrective</li>
     *     <li><strong>MP</strong> : Maintenance préventive</li>
     *     <li><strong>ADMIN</strong> : Administrateur (gestion complète)</li>
     * </ul>
     *
     * @see Role
     * @see User#getRole()
     */
        private String role;
    /**
     * Nom du site de production de l'utilisateur.
     * <p>
     * Chaque utilisateur est rattaché à un site géographique unique.
     * Cette attribution détermine l'isolation des données :
     * un utilisateur ne voit que les données de son propre site.
     * </p>
     *
     * <p><strong>Sites disponibles chez LEONI Tunisia :</strong></p>
     * <ul>
     *     <li><strong>Manzel Hayet</strong> : Site principal du projet MEB</li>
     *     <li><strong>LTN1</strong> : Ligne technique 1</li>
     *     <li><strong>LTN2</strong> : Ligne technique 2</li>
     *     <li><strong>Mateur</strong> : Site secondaire</li>
     * </ul>
     * @see User#getSite()
     */
        private String site;
    /**
     * Liste des permissions granulaires de l'utilisateur.
     * <p>
     * Les permissions définissent précisément ce que l'utilisateur peut
     * faire dans l'application (CREATE, READ, UPDATE, DELETE, VALIDATE).
     * Elles sont dérivées du rôle de l'utilisateur.
     * </p>
     *
     * <p><strong>Format des permissions :</strong> {@code module:action}</p>
     * <ul>
     *     <li>{@code charge_sheet:basic:create} → Créer un cahier des charges</li>
     *     <li>{@code charge_sheet:tech:write} → Modifier les données techniques</li>
     *     <li>{@code claim:create} → Créer une réclamation</li>
     *     <li>{@code stock:read} → Consulter le stock</li>
     *     <li>{@code admin:createuser} → Créer un utilisateur</li>
     * </ul>
     *
     * <p><strong>Utilisation :</strong> Ces permissions sont vérifiées par
     * Spring Security via l'annotation {@code @PreAuthorize}.</p>
     *
     * @see Permission
     * @see org.springframework.security.access.prepost.PreAuthorize
     */
    private List<String> permissions;
}


