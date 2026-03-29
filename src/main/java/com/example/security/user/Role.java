package com.example.security.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.security.user.Permission.*;

@RequiredArgsConstructor
public enum Role {
    ING(
            Set.of(
                    // Peut créer la partie "basique" du cahier des charges
                    CHARGE_SHEET_BASIC_CREATE,
                    CHARGE_SHEET_BASIC_READ,
                    CHARGE_SHEET_BASIC_WRITE,
                    CHARGE_SHEET_ALL_READ


            )
    ),

    PT(
            Set.of(
                    // Peut lire tout le cahier des charges
                    CHARGE_SHEET_ALL_READ,

                    // Peut écrire la partie technique du cahier des charges
                    CHARGE_SHEET_TECH_WRITE,
                    CHARGE_SHEET_TECH_READ,

                    CHARGE_SHEET_BASIC_READ,

                    // Réclamations
                    CLAIM_CREATE,
                    CLAIM_READ,
                    CLAIM_WRITE
            )
    ),

    PP(
            Set.of(
                    // Peut lire tout le cahier des charges
                    CHARGE_SHEET_ALL_READ,

                    // Peut gérer la conformité
                    COMPLIANCE_CREATE,
                    COMPLIANCE_READ,
                    COMPLIANCE_WRITE,

                    // Peut gérer le dossier technique
                    TECHNICAL_FILE_READ,
                    TECHNICAL_FILE_WRITE,
                    TECHNICAL_FILE_CREATE,



                    // Peut gérer le stock
                    STOCK_READ,
                    STOCK_WRITE,



                    // Réclamations
                    CLAIM_CREATE,
                    CLAIM_READ,
                    CLAIM_WRITE
            )
    ),

    MC(
            Set.of(
                    // Peut lire tout le cahier des charges
                    CHARGE_SHEET_ALL_READ,

                    // Peut lire et écrire le dossier technique
                    TECHNICAL_FILE_READ,
                    TECHNICAL_FILE_WRITE,
                    TECHNICAL_FILE_CREATE,

                    // Maintenance corrective
                    MAINTENANCE_CORRECTIVE_READ,
                    MAINTENANCE_CORRECTIVE_WRITE,

                    // Permissions de lecture
                    COMPLIANCE_READ,

                    // Réclamations
                    CLAIM_CREATE,
                    CLAIM_READ,
                    CLAIM_WRITE
            )
    ),

    MP(
            Set.of(
                    // Peut lire tout le cahier des charges
                    CHARGE_SHEET_ALL_READ,

                    // Peut lire et écrire le dossier technique
                    TECHNICAL_FILE_READ,
                    TECHNICAL_FILE_WRITE,
                    TECHNICAL_FILE_CREATE,
                    // Maintenance préventive
                    MAINTENANCE_PREVENTIVE_READ,
                    MAINTENANCE_PREVENTIVE_WRITE,

                    // Permissions de lecture seule pour tout le reste

                    COMPLIANCE_READ,
                    MAINTENANCE_CORRECTIVE_READ,

                    // Réclamations
                    CLAIM_CREATE,
                    CLAIM_READ,
                    CLAIM_WRITE,

                    // Stock
                    STOCK_READ
            )
    ),

    ADMIN(
            Set.of(

                    ADMIN_READ,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,
                    ADMIN_CREATE,
                    CHARGE_SHEET_BASIC_CREATE,
                    CHARGE_SHEET_BASIC_READ,
                    CHARGE_SHEET_BASIC_WRITE,
                    CHARGE_SHEET_TECH_READ,
                    CHARGE_SHEET_TECH_WRITE,
                    CHARGE_SHEET_ALL_READ,
                    COMPLIANCE_CREATE,
                    COMPLIANCE_READ,
                    COMPLIANCE_WRITE,
                    TECHNICAL_FILE_READ,
                    TECHNICAL_FILE_WRITE,
                    TECHNICAL_FILE_CREATE,
                    MAINTENANCE_CORRECTIVE_READ,
                    MAINTENANCE_CORRECTIVE_WRITE,
                    MAINTENANCE_PREVENTIVE_READ,
                    MAINTENANCE_PREVENTIVE_WRITE,
                    CLAIM_CREATE,
                    CLAIM_READ,
                    CLAIM_WRITE,
                    STOCK_READ,
                    STOCK_WRITE,
                    SEARCH,
                    AJOUTE_USER,
                    AJOUTE_USER_LISTE
            )
    );

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}