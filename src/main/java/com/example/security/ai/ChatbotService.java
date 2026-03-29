package com.example.security.ai;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatbotService {

    private final Map<String, String> responses = new HashMap<>();

    public ChatbotService() {
        initResponses();
    }

    private void initResponses() {
        // Réponses pour les cahiers des charges
        responses.put("créer cahier",
                "📄 **CRÉER UN CAHIER DES CHARGES**\n\n" +
                        "1️⃣ Allez dans le menu 'Cahiers des charges'\n" +
                        "2️⃣ Cliquez sur 'Nouveau cahier'\n" +
                        "3️⃣ Remplissez les informations générales :\n" +
                        "   • Plant (Menzel Hayet, Menzel Bourguiba, Bizerte, Tunis)\n" +
                        "   • Projet (BMW, Mercedes, Audi, Volkswagen, Porsche)\n" +
                        "   • Référence Harness (ex: DOORS)\n" +
                        "4️⃣ Ajoutez les items avec leurs caractéristiques\n" +
                        "5️⃣ Cliquez sur 'Créer le cahier'\n\n" +
                        "🔗 Lien direct : /charge-sheets/create");

        responses.put("valider cahier",
                "✅ **VALIDER UN CAHIER DES CHARGES**\n\n" +
                        "Le workflow de validation :\n\n" +
                        "1️⃣ **ING** : Valide les informations générales → statut VALIDATED_ING\n" +
                        "2️⃣ **PT** : Remplit la partie technique → statut TECH_FILLED\n" +
                        "3️⃣ **PT** : Valide la partie technique → statut VALIDATED_PT\n" +
                        "4️⃣ **PT** : Envoie au fournisseur → statut SENT_TO_SUPPLIER\n" +
                        "5️⃣ **PT** : Confirme la réception → statut RECEIVED_FROM_SUPPLIER\n" +
                        "6️⃣ **PT** : Complète le cahier → statut COMPLETED\n\n" +
                        "📌 Seul le rôle correspondant peut effectuer chaque étape !");

        responses.put("item",
                "🔌 **GESTION DES ITEMS**\n\n" +
                        "Un item est un connecteur avec ses caractéristiques :\n\n" +
                        "• **Item Number** : Identifiant unique\n" +
                        "• **Samples Exist** : Échantillons disponibles (Oui/Non)\n" +
                        "• **Ways** : Nombre de voies\n" +
                        "• **Housing Colour** : Couleur du boîtier\n" +
                        "• **Quantity** : Quantité de modules de test\n\n" +
                        "📸 Vous pouvez aussi ajouter une photo du connecteur !");

        responses.put("conformité",
                "✅ **CRÉER UNE FICHE DE CONFORMITÉ**\n\n" +
                        "1️⃣ Allez dans le menu 'Conformité'\n" +
                        "2️⃣ Sélectionnez l'item concerné\n" +
                        "3️⃣ Remplissez les informations générales\n" +
                        "4️⃣ Saisissez les résultats des tests\n" +
                        "5️⃣ Indiquez le résultat (Qualifié/Conditionnel/Non qualifié)\n\n" +
                        "📊 La fiche sera enregistrée dans la liste des conformités !");

        responses.put("réception",
                "📦 **RÉCEPTIONNER DES ITEMS**\n\n" +
                        "1️⃣ Allez dans le menu 'Réception'\n" +
                        "2️⃣ Sélectionnez le cahier des charges\n" +
                        "3️⃣ Saisissez le N° de bon de livraison\n" +
                        "4️⃣ Pour chaque item, indiquez la quantité reçue\n" +
                        "5️⃣ Cliquez sur 'Confirmer la réception'\n\n" +
                        "💡 **Astuce** : Les réceptions partielles sont possibles !");

        responses.put("réclamation",
                "⚠️ **CRÉER UNE RÉCLAMATION**\n\n" +
                        "1️⃣ Allez dans le menu 'Réclamations'\n" +
                        "2️⃣ Cliquez sur 'Nouvelle réclamation'\n" +
                        "3️⃣ Remplissez le titre et la description\n" +
                        "4️⃣ Choisissez la priorité (Basse, Moyenne, Haute, Critique)\n" +
                        "5️⃣ Assignez à un technicien\n" +
                        "6️⃣ Validez la création\n\n" +
                        "📌 Suivez l'avancement dans la liste des réclamations !");

        responses.put("dossier technique",
                "📁 **CRÉER UN DOSSIER TECHNIQUE**\n\n" +
                        "1️⃣ Allez dans le menu 'Dossiers techniques'\n" +
                        "2️⃣ Cliquez sur 'Nouveau dossier'\n" +
                        "3️⃣ Ajoutez la référence du dossier\n" +
                        "4️⃣ Pour chaque item, saisissez les résultats des tests\n" +
                        "5️⃣ Enregistrez pour finaliser\n\n" +
                        "📊 Consultez l'historique des maintenances !");

        responses.put("statut",
                "📊 **STATUTS DES CAHIERS**\n\n" +
                        "• 🟡 **DRAFT** : Brouillon, en cours de création\n" +
                        "• 🔵 **VALIDATED_ING** : Validé par l'ingénieur\n" +
                        "• ⚙️ **TECH_FILLED** : Partie technique remplie\n" +
                        "• 🟠 **VALIDATED_PT** : Validé par PT\n" +
                        "• 🚚 **SENT_TO_SUPPLIER** : Envoyé au fournisseur\n" +
                        "• 📦 **RECEIVED_FROM_SUPPLIER** : Reçu du fournisseur\n" +
                        "• ✅ **COMPLETED** : Terminé");

        responses.put("permission",
                "🔐 **VOS PERMISSIONS**\n\n" +
                        "Les rôles et leurs droits :\n\n" +
                        "• **ADMIN** : Accès total (création utilisateurs, suppression)\n" +
                        "• **ING** : Création et modification des cahiers (partie ING)\n" +
                        "• **PT** : Remplissage et validation technique\n" +
                        "• **PP** : Consultation des cahiers validés\n" +
                        "• **MC/MP** : Maintenance (corrective/préventive)\n\n" +
                        "Contactez l'administrateur pour modifier vos droits.");
    }

    public String getChatResponse(String userMessage) {
        String message = userMessage.toLowerCase().trim();

        // Détection des mots-clés
        if (message.contains("créer") && message.contains("cahier")) {
            return responses.get("créer cahier");
        }
        if (message.contains("valider") && message.contains("cahier")) {
            return responses.get("valider cahier");
        }
        if (message.contains("item") || message.contains("connecteur")) {
            return responses.get("item");
        }
        if (message.contains("conformité") || message.contains("conformite")) {
            return responses.get("conformité");
        }
        if (message.contains("réception") || message.contains("reception")) {
            return responses.get("réception");
        }
        if (message.contains("réclamation") || message.contains("reclamation")) {
            return responses.get("réclamation");
        }
        if (message.contains("dossier") && message.contains("technique")) {
            return responses.get("dossier technique");
        }
        if (message.contains("statut") || message.contains("status")) {
            return responses.get("statut");
        }
        if (message.contains("permission") || message.contains("droit") || message.contains("role")) {
            return responses.get("permission");
        }

        // Messages d'accueil
        if (message.contains("bonjour") || message.contains("salut") || message.contains("hello")) {
            return "👋 **Bienvenue sur l'assistant LEONI !**\n\n" +
                    "Je peux vous aider avec :\n" +
                    "• 📄 Créer/valider un cahier des charges\n" +
                    "• 🔌 Gérer les items\n" +
                    "• ✅ Créer une fiche de conformité\n" +
                    "• 📦 Gérer une réception\n" +
                    "• ⚠️ Créer une réclamation\n" +
                    "• 📁 Créer un dossier technique\n" +
                    "• 📊 Voir les statuts\n" +
                    "• 🔐 Voir les permissions\n\n" +
                    "💡 **Posez votre question** ou tapez 'aide' !";
        }

        if (message.contains("aide") || message.contains("help")) {
            return "🤖 **AIDE - ASSISTANT LEONI**\n\n" +
                    "Posez-moi une question avec un de ces mots-clés :\n\n" +
                    "📄 **Cahiers des charges**\n" +
                    "   • 'créer cahier'\n" +
                    "   • 'valider cahier'\n\n" +
                    "🔌 **Items**\n" +
                    "   • 'item' ou 'connecteur'\n\n" +
                    "✅ **Conformité**\n" +
                    "   • 'conformité'\n\n" +
                    "📦 **Réception**\n" +
                    "   • 'réception'\n\n" +
                    "⚠️ **Réclamations**\n" +
                    "   • 'réclamation'\n\n" +
                    "📁 **Dossiers techniques**\n" +
                    "   • 'dossier technique'\n\n" +
                    "📊 **Statuts**\n" +
                    "   • 'statut'\n\n" +
                    "🔐 **Permissions**\n" +
                    "   • 'permission' ou 'droit'\n\n" +
                    "💬 Tapez 'bonjour' pour l'accueil !";
        }

        return "🤖 **Assistant LEONI**\n\n" +
                "Je ne connais pas encore la réponse à cette question.\n\n" +
                "💡 **Essayez plutôt** :\n" +
                "• 'créer cahier'\n" +
                "• 'valider cahier'\n" +
                "• 'conformité'\n" +
                "• 'réception'\n" +
                "• 'réclamation'\n" +
                "• 'dossier technique'\n" +
                "• 'statut'\n" +
                "• 'permission'\n\n" +
                "Ou tapez 'aide' pour plus d'options !";
    }
}