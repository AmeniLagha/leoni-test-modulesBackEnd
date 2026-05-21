package com.example.security.token;

/**
 * Énumération des types de jetons supportés par le système d'authentification.
 * <p>
 * Cette énumération définit les différents types de jetons JWT qui peuvent être
 * utilisés dans l'application. Actuellement, seul le type {@code BEARER} est
 * supporté, conformément à la norme OAuth2.
 * </p>
 *
 * <p>Les jetons de type Bearer sont inclus dans l'en-tête HTTP {@code Authorization}
 * sous la forme : {@code Authorization: Bearer <token>}.</p>
 *
 * @author LAGHA AMENI
 * @version 1.0
 * @see Token
 * @since Sprint 2
 */
public enum TokenType {
    /**
     * Jeton de type Bearer (norme OAuth2).
     * <p>
     * C'est le type de jeton standard pour les API REST utilisant JWT.
     * Le jeton est transmis dans l'en-tête HTTP {@code Authorization}
     * avec le préfixe "Bearer ".
     * </p>
     * <p>Exemple d'utilisation :</p>
     * <pre>
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     */
    BEARER
}
