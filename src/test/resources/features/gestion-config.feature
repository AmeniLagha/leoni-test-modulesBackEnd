# src/test/resources/features/gestion-config.feature
Feature: Gestion des configurations (Projets et Sites)
  En tant qu'administrateur,
  Je veux gérer les projets et les sites
  Afin de configurer l'application

  Background:
    Given je suis connecté en tant qu'administrateur
    And je navigue vers la page "Gestion Configuration"

  # ==================== TESTS PROJETS ====================
  @projet-create
  Scenario: Créer un nouveau projet avec succès
    When je clique sur "Nouveau projet"
    And je saisis le nom du projet "PROJET_TEST_E2Ev4"
    And je saisis la description "Projet créé par test E2E"
    And je valide le formulaire
    Then un message de succès "Projet créé avec succès" s'affiche
    And le projet "PROJET_TEST_E2Ev4" apparaît dans la liste

  @projet-create-missing-name
  Scenario: Créer un projet sans nom doit échouer
    When je clique sur "Nouveau projet"
    And je laisse le nom du projet vide
    Then le bouton de validation est désactivé
    And un message d'erreur "Le nom est requis" s'affiche

  @projet-update
  Scenario: Modifier un projet existant
    Given un projet "PROJET_A_MODIFIER" existe
    When je clique sur le bouton "Modifier" du projet
    And je modifie le nom du projet en "PROJET_MODIFIE_E2E14"
    And je modifie la description du projet en "Description modifiée"
    And je valide le formulaire
    Then un message de succès "Projet modifié avec succès" s'affiche
    And le projet "PROJET_MODIFIE_E2E14" remplace l'ancien

  @projet-delete
  Scenario: Supprimer un projet
    Given un projet "PROJET_A_SUPPRIMER" existe
    When je clique sur le bouton "Supprimer" du projet
    And je confirme la suppression
    Then un message de succès "Projet supprimé" s'affiche
    And le projet "PROJET_A_SUPPRIMER" n'apparaît plus dans la liste

  # ==================== TESTS SITES ====================

  @site-create
  Scenario: Créer un nouveau site avec succès
    When je clique sur l'onglet "Sites"
    And je clique sur "Nouveau site"
    And je saisis le nom du site "SITE_TEST_E2Ev7"
    And je saisis la description du site "Site créé par test E2E"
    And je valide le formulaire
    Then un message de succès "Site créé avec succès" s'affiche
    And le site "SITE_TEST_E2Ev7" apparaît dans la liste

  @site-create-missing-name
  Scenario: Créer un site sans nom doit échouer
    When je clique sur l'onglet "Sites"
    And je clique sur "Nouveau site"
    And je laisse le nom du site vide
    And un message d'erreur "Le nom est requis" s'affiche

  @site-update
  Scenario: Modifier un site existant
    Given un site "SITE_A_MODIFIER" existe
    When je clique sur l'onglet "Sites"
    And je clique sur le bouton "Modifier" du site
    And je modifie le nom du site en "SITE_MODIFIE_E2E9"
    And je valide le formulaire
    Then un message de succès "Site modifié avec succès" s'affiche
    And le site "SITE_MODIFIE_E2E9" remplace l'ancien

  @site-delete
  Scenario: Supprimer un site
    Given un site "SITE_A_SUPPRIMER" existe
    When je clique sur l'onglet "Sites"
    And je clique sur le bouton "Supprimer" du site
    And je confirme la suppression
    Then un message de succès "Site supprimé" s'affiche
    And le site "SITE_A_SUPPRIMER" n'apparaît plus dans la liste

  # ==================== TESTS ASSOCIATION ====================

  @association
  Scenario: Associer des projets à un site
    Given un projet "PROJET_AA" existe
    And un projet "PROJET_BB" existe
    And un site "SITE_AVEC_PROJETS" existe
    When je clique sur l'onglet "Sites"
    And je clique sur le bouton "Associer" du site "SITE_AVEC_PROJETS"
    And je sélectionne le projet "PROJET_AA"
    And je sélectionne le projet "PROJET_BB"
    And je sauvegarde les associations
    Then un message de succès "Projets associés au site" s'affiche
    And les projets "PROJET_AA" et "PROJET_BB" sont associés au site

