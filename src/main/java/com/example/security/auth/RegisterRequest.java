package com.example.security.auth;

import com.example.security.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Le prénom est obligatoire")
    private String firstname;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastname;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    @NotNull(message = "Le matricule est obligatoire")
    private Integer matricule;

    @NotBlank(message = "Le projet est obligatoire")
    private List<String> projets;


    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @NotNull(message = "Le site est obligatoire")
    private String siteName;
}
