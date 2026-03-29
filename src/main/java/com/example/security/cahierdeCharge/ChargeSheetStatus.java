package com.example.security.cahierdeCharge;

public enum ChargeSheetStatus {
    DRAFT,              // Créé par ING (non validé)
    VALIDATED_ING,      // Validé par ING après création
    TECH_FILLED,        // Rempli par PT (en cours)
    VALIDATED_PT,       // Validé par PT après remplissage
    SENT_TO_SUPPLIER,       // envoyé au fournisseur
    RECEIVED_FROM_SUPPLIER,
    COMPLETED
}
