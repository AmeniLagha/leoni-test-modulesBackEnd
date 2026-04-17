// SiteDto.java
package com.example.security.site;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteDto {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private List<String> projetNames;  // ✅ Noms des projets associés
    private List<Long> projetIds;
}