// SiteProjetDto.java
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
public class SiteProjetDto {
    private Long siteId;
    private String siteName;
    private List<Long> projetIds;
    private List<String> projetNames;
}