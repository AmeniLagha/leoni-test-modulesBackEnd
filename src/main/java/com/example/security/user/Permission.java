package com.example.security.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {
    // Permissions par section du cahier des charges
    CHARGE_SHEET_BASIC_CREATE("charge_sheet:basic:create"),
    CHARGE_SHEET_BASIC_READ("charge_sheet:basic:read"),
    CHARGE_SHEET_BASIC_WRITE("charge_sheet:basic:write"),

    CHARGE_SHEET_TECH_READ("charge_sheet:tech:read"),
    CHARGE_SHEET_TECH_WRITE("charge_sheet:tech:write"),

    CHARGE_SHEET_ALL_READ("charge_sheet:all:read"),

    // Permissions pour la conformité
    COMPLIANCE_CREATE("compliance:create"),
    COMPLIANCE_READ("compliance:read"),
    COMPLIANCE_WRITE("compliance:write"),

    // Permissions pour le dossier technique
    TECHNICAL_FILE_CREATE("technical_file:create"),
    TECHNICAL_FILE_READ("technical_file:read"),
    TECHNICAL_FILE_WRITE("technical_file:write"),

    // Permissions générales
    MAINTENANCE_CORRECTIVE_READ("maintenance_corrective:read"),
    MAINTENANCE_CORRECTIVE_WRITE("maintenance_corrective:write"),

    MAINTENANCE_PREVENTIVE_READ("maintenance_preventive:read"),
    MAINTENANCE_PREVENTIVE_WRITE("maintenance_preventive:write"),

    CLAIM_READ("claim:read"),
    CLAIM_WRITE("claim:write"),
    CLAIM_CREATE("claim:create"),


    STOCK_READ("stock:read"),
    STOCK_WRITE("stock:write"),

    SEARCH("search:execute"),

    // Permissions admin
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),
    AJOUTE_USER("admin:createuser"),
    AJOUTE_USER_LISTE("admin:readuser"),
    RECEPTION_READ("reception:read");


    @Getter
    private final String permission;
}