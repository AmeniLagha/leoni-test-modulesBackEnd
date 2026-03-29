// TechnicalFileItemStatus.java
package com.example.security.fichierTechnique;

public enum TechnicalFileItemStatus {
    DRAFT,                    // Créé, en attente validation PP
    VALIDATED_PP,             // Validé par PP, en attente MC
    VALIDATED_MC,             // Validé par MC, en attente MP
    VALIDATED_MP;             // Validé par MP, finalisé

    public boolean canBeValidatedBy(String role) {
        return switch (this) {
            case DRAFT -> "PP".equals(role);
            case VALIDATED_PP -> "MC".equals(role);
            case VALIDATED_MC -> "MP".equals(role);
            default -> false;
        };
    }

    public String getDisplayName() {
        return switch (this) {
            case DRAFT -> "Brouillon";
            case VALIDATED_PP -> "Validé PP";
            case VALIDATED_MC -> "Validé MC";
            case VALIDATED_MP -> "Validé MP";
        };
    }
}