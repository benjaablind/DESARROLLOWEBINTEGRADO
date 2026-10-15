package com.utp.productosapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Contrato JSON del login. Es un record aparte para no exponer la entidad
 * Usuario ni su campo password almacenado.
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
