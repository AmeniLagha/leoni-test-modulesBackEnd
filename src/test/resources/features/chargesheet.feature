Feature: Gestion des Cahiers des Charges

  Background:
    Given je suis connecté en tant qu'ingénieur
    And je navigue vers la page des cahiers des charges

  Scenario: Création réussie d'un cahier des charges
    When je clique sur Nouveau cahier
    And je sélectionne le projet "FORD"
    And je saisis la référence harnais "DOORS-TEST-001"
    And je saisis le numéro de téléphone "12345678"
    And je saisis le numéro de commande "ORD-2026-001"
    And je saisis le centre de coût "CC-1001"
    And je saisis la date du jour
    And je saisis la date de livraison "2026-12-31"
    And je remplis l'item avec les caractéristiques suivantes:
      | samplesExist | ways | housingColour | testModuleExist | housingRefLeoni | housingRefSupplier | referenceSeals | quantity |
      | Yes          | 12   | Black         | Yes             | LEONI-CONN-001  | SUPPLIER-CONN-001  | SEAL-001       | 100      |
    And je soumets le cahier des charges
    Then le cahier des charges est créé avec succès
    And un message de confirmation s'affiche
  # =============================================
  # SCÉNARIO 2: CRÉATION AVEC MULTIPLES ITEMS
  # =============================================
  Scenario: Création d'un cahier avec plusieurs items
    When je clique sur Nouveau cahier
    And je sélectionne le projet "FORD"
    And je saisis la référence harnais "BMW-X5-001"
    And je saisis le numéro de téléphone "98765432"
    And je saisis le numéro de commande "ORD-2026-002"
    And je saisis le centre de coût "CC-1002"
    And je saisis la date du jour
    And je saisis la date de livraison "2026-10-15"
    And je remplis l'item avec les caractéristiques suivantes:
      | samplesExist | ways | housingColour | testModuleExist | housingRefLeoni | housingRefSupplier | referenceSeals | quantity |
      | Yes          | 12   | Black         | Yes             | LEONI-CONN-001  | SUPPLIER-CONN-001  | SEAL-001       | 100      |

    And j'ajoute un item avec les caractéristiques suivantes:
      | samplesExist | ways | housingColour | testModuleExist | housingRefLeoni | housingRefSupplier | referenceSeals | quantity |
      | Yes          | 24   | Blue          | Yes             | LEONI-CONN-002  | SUPPLIER-CONN-002  | SEAL-002       | 50       |
      | No           | 36   | Red           | No              | LEONI-CONN-003  | SUPPLIER-CONN-003  | SEAL-003       | 75       |
    And je soumets le cahier des charges
    Then le cahier des charges est créé avec succès

  # =============================================
  # SCÉNARIO 3: ÉCHEC - CHAMPS OBLIGATOIRES MANQUANTS
  # =============================================
  Scenario: Échec de création - projet manquant
    When je clique sur Nouveau cahier
    And je saisis la référence harnais "TEST-001"
    And je saisis le numéro de téléphone "12345678"
    And je saisis le numéro de commande "ORD-001"
    And je saisis le centre de coût "CC-001"
    And je saisis la date du jour
    And je saisis la date de livraison "2026-12-31"
    And je remplis l'item avec les caractéristiques suivantes:
      | samplesExist | ways | housingColour | testModuleExist | housingRefLeoni | housingRefSupplier | referenceSeals | quantity |
      | Yes          | 12   | Black         | Yes             | LEONI-CONN-001  | SUPPLIER-CONN-001  | SEAL-001       | 100      |
    Then le formulaire ne se soumet pas