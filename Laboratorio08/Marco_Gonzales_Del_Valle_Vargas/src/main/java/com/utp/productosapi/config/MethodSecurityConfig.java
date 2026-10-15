package com.utp.productosapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Reto avanzado (seccion 18): habilita @PreAuthorize en la capa de servicio.
 * Complementa, no sustituye, las reglas por URL del SecurityFilterChain:
 * aunque alguien agregara una ruta nueva hacia eliminar(), seguiria exigiendo
 * ROLE_ADMIN.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
