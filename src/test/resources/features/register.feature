Feature: Gestion des utilisateurs - Inscription

  Background:
    Given je suis connecté en tant qu'administrateur
    And je navigue vers la page d'inscription

  # =============================================
  # SCÉNARIO 1: INSCRIPTION RÉUSSIE
  # =============================================
  Scenario: Création réussie d'un nouvel utilisateur
    When je saisis le prénom "Ameni"
    And je saisis le nom "Ben Ahmed"
    And je saisis l'email professionnel "AmeniiIiii.benahmed@leoni.com"
    And je saisis le matricule "123460113"
    And je passe à l'étape sécurité
    And je saisis le mot de passe "Password123"
    And je passe à l'étape affectation
    And je sélectionne le rôle "Ingénieur"
    And je sélectionne le projet "FORD"
    And je sélectionne le site "MH2"
    And je soumets le formulaire
    Then l'utilisateur est créé avec succès
    And un message de succès s'affiche

  # =============================================
  # SCÉNARIO 2: EMAIL DÉJÀ UTILISÉ
  # =============================================
  Scenario: Échec d'inscription avec email déjà utilisé
    When je remplis le formulaire avec email existant "admin@test.com"
    And je soumets le formulaire
    Then un message d'erreur "Erreur lors de l'inscription" s'affiche

  # =============================================
  # SCÉNARIO 3: MATRICULE DÉJÀ UTILISÉ
  # =============================================
  Scenario: Échec d'inscription avec matricule déjà utilisé
    When je remplis le formulaire avec matricule existant "123460111"
    And je soumets le formulaire
    Then un message d'erreur "Erreur lors de l'inscription" s'affiche

  # =============================================
  # SCÉNARIO 4: CHAMPS OBLIGATOIRES MANQUANTS
  # =============================================
  Scenario: Échec d'inscription avec prénom manquant
    When je saisis le prénom ""
    And je saisis le nom "Ben Ahmed"
    And je saisis l'email professionnel "test@leoni.com"
    And je saisis le matricule "99999"
    And je passe à l'étape sécurité
    And je saisis le mot de passe "Password123!"
    And je passe à l'étape affectation
    And je sélectionne le rôle "Ingénieur"
    And je sélectionne le projet "FORD"
    And je sélectionne le site "MH1"
    Then le formulaire ne se soumet pas

  Scenario: Échec d'inscription avec nom manquant
    When je saisis le prénom "Mohamed"
    And je saisis le nom ""
    And je saisis l'email professionnel "test@leoni.com"
    And je saisis le matricule "99998"
    And je passe à l'étape sécurité
    And je saisis le mot de passe "Password123!"
    And je passe à l'étape affectation
    And je sélectionne le rôle "Ingénieur"
    And je sélectionne le projet "FORD"
    And je sélectionne le site "MH1"
    Then le formulaire ne se soumet pas