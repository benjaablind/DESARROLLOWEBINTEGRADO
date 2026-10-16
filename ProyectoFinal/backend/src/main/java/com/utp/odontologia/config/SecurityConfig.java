package com.utp.odontologia.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.utp.odontologia.security.RespuestasDeSeguridad;

/**
 * Seguridad del sistema (semanas 8 y 9).
 *
 * - Autenticacion: login con usuario y contrasena (BCrypt) que emite un JWT;
 *   cada peticion posterior presenta "Authorization: Bearer <token>".
 * - Autorizacion: la matriz de permisos por rol que hasta la semana 5 solo
 *   informaba UsuarioService.permisosDe() ahora la aplica el servidor. Leer es
 *   GET; escribir es POST, PUT, PATCH o DELETE.
 *
 * Las reglas se evaluan en orden y gana la primera que coincide: por eso van de
 * la mas especifica a la mas general, terminando en anyRequest().
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ADMINISTRADOR = "ADMINISTRADOR";
    private static final String ODONTOLOGO = "ODONTOLOGO";
    private static final String RECEPCIONISTA = "RECEPCIONISTA";
    private static final String ASISTENTE = "ASISTENTE";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Valida usuario y contrasena en el login contra la base de datos.
     *
     * Por defecto Spring revisa si la cuenta esta deshabilitada ANTES de
     * comparar la contrasena, lo que le diria a cualquiera que un usuario
     * existe y esta inactivo. Aqui el estado de la cuenta se revisa DESPUES:
     * "usuario inactivo" solo se informa a quien ya demostro conocer la clave.
     */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder passwords) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarios);
        provider.setPasswordEncoder(passwords);
        provider.setPreAuthenticationChecks(usuario -> { });
        provider.setPostAuthenticationChecks(new AccountStatusUserDetailsChecker());
        return new ProviderManager(provider);
    }

    /**
     * Reconstruye los roles desde el claim "roles" del token. Prefijo vacio
     * porque el claim ya contiene ROLE_ADMINISTRADOR, ROLE_ODONTOLOGO...; con el
     * prefijo por defecto quedaria SCOPE_ROLE_... y ningun hasRole() coincidiria.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName("roles");
        autoridades.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(autoridades);
        return converter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter,
            RespuestasDeSeguridad respuestas) throws Exception {
        http
                // Sin cookies de sesion: el token viaja en el encabezado
                // Authorization, que el navegador no adjunta por su cuenta.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Usa la configuracion de CorsConfig (origen del frontend Angular).
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Publico: solo el login.
                        .requestMatchers(POST, "/api/auth/login").permitAll()

                        // Usuarios: administracion exclusiva del ADMINISTRADOR.
                        // Excepciones: cada uno cambia su contrasena y ve sus permisos
                        // (@PreAuthorize en UsuarioController), y todos pueden listar
                        // los odontologos (solo id y nombre) para agendar.
                        .requestMatchers(PATCH, "/api/usuarios/*/password").authenticated()
                        .requestMatchers(GET, "/api/usuarios/*/permisos", "/api/usuarios/odontologos").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole(ADMINISTRADOR)

                        // Pacientes y citas: todos consultan; el asistente no modifica.
                        .requestMatchers(GET, "/api/pacientes/**", "/api/citas/**")
                        .hasAnyRole(ADMINISTRADOR, ODONTOLOGO, RECEPCIONISTA, ASISTENTE)
                        .requestMatchers("/api/pacientes/**", "/api/citas/**")
                        .hasAnyRole(ADMINISTRADOR, ODONTOLOGO, RECEPCIONISTA)

                        // Informacion clinica: solo administrador y odontologo.
                        // Incluye los archivos (radiografias, consentimientos...),
                        // que ya no son accesibles para cualquiera que sepa el id.
                        .requestMatchers("/api/historias/**", "/api/odontograma/**",
                                "/api/tratamientos/**", "/api/archivos/**")
                        .hasAnyRole(ADMINISTRADOR, ODONTOLOGO)

                        // Caja: administrador y recepcion.
                        .requestMatchers("/api/pagos/**").hasAnyRole(ADMINISTRADOR, RECEPCIONISTA)

                        // Reportes. El historial consolidado de un paciente incluye
                        // consultas clinicas, asi que exige acceso clinico.
                        .requestMatchers(GET, "/api/reportes/historial/**")
                        .hasAnyRole(ADMINISTRADOR, ODONTOLOGO)
                        .requestMatchers(GET, "/api/reportes/**").hasAnyRole(ADMINISTRADOR, RECEPCIONISTA)

                        // Dashboard, perfil, logout y cualquier otra ruta: autenticado.
                        .anyRequest().authenticated())
                // Verifica el Bearer Token (firma, exp, iss) antes del controlador.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter))
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(respuestas)
                        .accessDeniedHandler(respuestas));

        return http.build();
    }
}
