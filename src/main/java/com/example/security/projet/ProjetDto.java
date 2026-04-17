// ProjetDto.java
package com.example.security.projet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjetDto {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private List<String> siteNames;  // ✅ Noms des sites associés
    private List<Long> siteIds;
}