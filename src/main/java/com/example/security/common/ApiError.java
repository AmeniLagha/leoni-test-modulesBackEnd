package com.example.security.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String field;
    private String message;
    private String code;

    // ✅ Constructeur pour field + message (utile pour les validations simples)
    public ApiError(String field, String message) {
        this.field = field;
        this.message = message;
        this.code = null;
    }
}