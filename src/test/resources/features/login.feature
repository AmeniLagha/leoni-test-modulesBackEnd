
Feature: Authentification sur l'application LEONI

  Background:
    Given je suis sur la page de connexion

  Scenario: Connexion réussie avec l'administrateur
    When je saisis l'email "admin@test.com"
    And je saisis le mot de passe "admin123"
    And je sélectionne le site ""
    And je clique sur le bouton "Se connecter"
    Then je suis redirigé vers le tableau de bord

  Scenario: Connexion échouée avec mot de passe incorrect
    When je saisis l'email "admin@leoni.com"
    And je saisis le mot de passe "wrongpassword"
    And je sélectionne le site "Manzel Hayet"
    And je clique sur le bouton "Se connecter"
    Then un message d'erreur s'affiche "Email ou mot de passe incorrect"

  Scenario: Connexion échouée avec email inexistant
    When je saisis l'email "inexistant@test.com"
    And je saisis le mot de passe "password123"
    And je sélectionne le site "Manzel Hayet"
    And je clique sur le bouton "Se connecter"
    Then un message d'erreur s'affiche "Email ou mot de passe incorrect"
