package com.example.security.user;


import com.example.security.site.Site;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Data
    public class UserDto {
        private Integer id;
        private String firstname;
        private String lastname;
        private Integer matricule;
        private String password;
        private String email;
    private List<String> projets;
        private String role;
        private String site;
    private List<String> permissions;
}


