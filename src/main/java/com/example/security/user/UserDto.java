package com.example.security.user;


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
        private String email;
        private String projet;
        private String role;
    private List<String> permissions;
}


