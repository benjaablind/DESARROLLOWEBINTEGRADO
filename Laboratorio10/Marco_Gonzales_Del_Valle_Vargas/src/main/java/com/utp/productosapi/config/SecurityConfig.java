package com.utp.productosapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Semana 9: se reemplaza HTTP Basic por JWT. Se conservan BCrypt, el
 * UserDetailsService de MySQL y la misma politica por roles de la semana 8;
 * lo unico que cambia es como viaja la identidad en cada peticion.
 */
@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Valida usuario y contrasena en el login: busca al usuario con
     * CustomUserDetailsService y compara el hash con BCrypt.
     */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder passwords) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(passwords);
        return new ProviderManager(provider);
    }

    /**
     * Reconstruye las autoridades desde el claim "roles". Prefijo vacio porque
     * los claims ya contienen ROLE_USER / ROLE_ADMIN; con el prefijo por
     * defecto quedaria SCOPE_ROLE_ADMIN y hasRole("ADMIN") fallaria con 403.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter)
            throws Exception {
        http
                // Sin cookies de sesion para autenticar: el token viaja en el
                // encabezado Authorization, que el navegador no adjunta solo.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Semana 10: politica final, de lo mas especifico a lo mas
                        // general. La primera regla que coincide es la que se aplica.

                        // Publico: login (valida credenciales por dentro) y estado.
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/publico/**").permitAll()

                        // Administracion: reporte (semana 9) y resumen (semana 10).
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Inventario: USER consulta; toda escritura (ajustes y la
                        // salida de la semana 10) es solo de ADMIN.
                        .requestMatchers(HttpMethod.GET, "/api/inventario/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/inventario/**").hasRole("ADMIN")

                        // Productos: misma politica de la semana 8.
                        .requestMatchers(HttpMethod.GET, "/api/productos/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated())
                // Verifica el Bearer Token (firma, exp, iss) antes del Controller.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));

        return http.build();
    }
}
