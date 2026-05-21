package com.example.security.config;


import com.example.security.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
/**
 * Configuration centrale de l'application pour Spring Security et autres composants transverses.
 * <p>
 * Cette classe définit les beans nécessaires à l'authentification et à la gestion
 * des utilisateurs dans l'application. Elle configure :
 * <ul>
 *     <li>Le service de chargement des utilisateurs ({@link UserDetailsService})</li>
 *     <li>Le fournisseur d'authentification ({@link AuthenticationProvider})</li>
 *     <li>Le gestionnaire d'authentification ({@link AuthenticationManager})</li>
 *     <li>L'encodeur de mot de passe ({@link PasswordEncoder})</li>
 * </ul>
 * </p>
 *
 * <p><strong>Composants configurés :</strong></p>
 * <ul>
 *     <li><strong>UserDetailsService</strong> : Charge les informations d'un utilisateur depuis la base de données
 *         en utilisant son email comme identifiant.</li>
 *     <li><strong>DaoAuthenticationProvider</strong> : Utilise le UserDetailsService et le PasswordEncoder
 *         pour valider les identifiants.</li>
 *     <li><strong>AuthenticationManager</strong> : Point d'entrée principal pour l'authentification,
 *         utilisé par Spring Security et par notre service d'authentification.</li>
 *     <li><strong>BCryptPasswordEncoder</strong> : Encode les mots de passe avec l'algorithme BCrypt,
 *         considéré comme une bonne pratique de sécurité.</li>
 * </ul>
 *
 * <p><strong>Flux d'authentification :</strong></p>
 * <ol>
 *     <li>L'utilisateur soumet ses identifiants (email, mot de passe, site)</li>
 *     <li>Spring Security délègue à l'{@link AuthenticationManager}</li>
 *     <li>L'{@link AuthenticationManager} utilise le {@link DaoAuthenticationProvider}</li>
 *     <li>Le {@link DaoAuthenticationProvider} appelle le {@link UserDetailsService}
 *         pour charger l'utilisateur par email</li>
 *     <li>Le {@link PasswordEncoder} compare le mot de passe saisi avec le hash stocké</li>
 *     <li>Si les identifiants sont valides, l'authentification réussit</li>
 * </ol>
 *
 * <p><strong>Sécurité :</strong>
 * L'utilisation de BCrypt garantit que les mots de passe sont stockés sous forme
 * de hashs salés, protégeant ainsi les données utilisateur en cas de compromission
 * de la base de données.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
 * @see SecurityConfiguration
 * @since Sprint 2
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    /**
     * Repository des utilisateurs, utilisé pour accéder aux données utilisateur
     * depuis la base de données.
     */
    private final UserRepository repository;
    /**
     * Crée et configure le service de chargement des détails d'un utilisateur.
     * <p>
     * Ce bean est utilisé par Spring Security pour charger les informations
     * d'un utilisateur (nom d'utilisateur, mot de passe, autorités) lors du
     * processus d'authentification.
     * </p>
     *
     * <p><strong>Fonctionnement :</strong></p>
     * <ul>
     *     <li>Reçoit l'email saisi par l'utilisateur comme nom d'utilisateur</li>
     *     <li>Recherche l'utilisateur dans la base de données via {@link UserRepository}</li>
     *     <li>Si l'utilisateur existe, retourne ses informations (email, mot de passe hashé, rôles)</li>
     *     <li>Si l'utilisateur n'existe pas, lance une exception {@link UsernameNotFoundException}</li>
     * </ul>
     *
     * @return Un {@link UserDetailsService} qui charge les utilisateurs par email
     *
     * @throws UsernameNotFoundException Si aucun utilisateur n'est trouvé avec l'email fourni
     *
     * @see UserDetailsService
     * @see UserRepository#findByEmail(String)
     */
    @Bean
    public UserDetailsService userDetailsService(){
        return username -> repository.findByEmail(username)
                .orElseThrow(() ->new UsernameNotFoundException("user not found"));

    }
    /**
     * Crée et configure le fournisseur d'authentification.
     * <p>
     * Le {@link DaoAuthenticationProvider} est le fournisseur d'authentification
     * standard basé sur une base de données. Il utilise le {@link UserDetailsService}
     * pour charger l'utilisateur et le {@link PasswordEncoder} pour vérifier le mot de passe.
     * </p>
     *
     * <p><strong>Configuration :</strong></p>
     * <ul>
     *     <li>Définit le {@link UserDetailsService} pour le chargement des utilisateurs</li>
     *     <li>Définit le {@link PasswordEncoder} pour la validation des mots de passe</li>
     * </ul>
     *
     * @return Un {@link AuthenticationProvider} configuré pour l'authentification par email/mot de passe
     *
     * @see DaoAuthenticationProvider
     * @see #userDetailsService()
     * @see #passwordEncoder()
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider= new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    /**
     * Crée et expose le gestionnaire d'authentification principal.
     * <p>
     * L'{@link AuthenticationManager} est le point d'entrée principal pour
     * authentifier un utilisateur. Il orchestre les différents fournisseurs
     * d'authentification pour valider les identifiants.
     * </p>
     *
     * <p><strong>Utilisation :</strong>
     * Ce bean est injecté dans notre service {@code AuthenticationService}
     * pour authentifier manuellement les utilisateurs.</p>
     *
     * @param config La configuration d'authentification Spring (injectée automatiquement)
     * @return Le gestionnaire d'authentification principal
     * @throws Exception Si la configuration ne peut pas être récupérée
     *
     * @see AuthenticationManager
     * @see AuthenticationConfiguration#getAuthenticationManager()
     * @see com.example.security.auth.AuthenticationService#authenticate
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
    /**
     * Crée l'encodeur de mot de passe utilisant l'algorithme BCrypt.
     * <p>
     * BCrypt est un algorithme de hachage adapté au stockage des mots de passe :
     * <ul>
     *     <li>Intègre automatiquement un "salt" (sel) pour chaque mot de passe</li>
     *     <li>Est intrinsèquement lent (configurable), ce qui rend les attaques par force brute difficiles</li>
     *     <li>Produit des hashs de longueur fixe (60 caractères)</li>
     *     <li>Est considéré comme une pratique standard de sécurité</li>
     * </ul>
     * </p>
     *
     * <p><strong>Utilisation :</strong></p>
     * <ul>
     *     <li>Lors de l'inscription : encode le mot de passe avant stockage</li>
     *     <li>Lors de l'authentification : compare le mot de passe saisi avec le hash stocké</li>
     * </ul>
     *
     * @return Un {@link PasswordEncoder} implémenté avec BCrypt
     *
     * @see BCryptPasswordEncoder
     * @see org.springframework.security.crypto.password.PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return  new BCryptPasswordEncoder();
    }


}

