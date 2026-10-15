package com.utp.productosapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Politica de acceso de la semana 8. Se usa HTTP Basic para concentrarse en
 * autenticacion, roles y permisos; en la semana 9 se reemplaza por JWT.
 *
 * El orden importa: las reglas especificas van primero y anyRequest() al final.
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless consumida desde Postman con Basic: no hay cookie
                // de sesion que un sitio externo pueda reutilizar, por eso se
                // desactiva CSRF. No es una regla general.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/publico/**").permitAll()

                        // Consultas: cualquier usuario autenticado con rol.
                        .requestMatchers(HttpMethod.GET, "/api/productos/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/inventario/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Escritura de inventario: solo administrador.
                        .requestMatchers(HttpMethod.POST, "/api/inventario/ajustes")
                        .hasRole("ADMIN")

                        // Escritura de productos: solo administrador.
                        .requestMatchers(HttpMethod.POST, "/api/productos/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
