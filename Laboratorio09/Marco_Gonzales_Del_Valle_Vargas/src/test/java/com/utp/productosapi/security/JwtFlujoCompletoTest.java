package com.utp.productosapi.security;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * Matriz de pruebas de la seccion 11 de la guia, automatizada de punta a
 * punta: login real contra la base (BCrypt + UserDetailsService), token
 * firmado de verdad y verificacion por el Resource Server.
 *
 * Usa su propia base H2 para no compartir datos con otras pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:jwt_flujo;DB_CLOSE_DELAY=-1")
class JwtFlujoCompletoTest {

    private static final String PRODUCTO_JSON = """
            {"nombre":"Audifonos USB","categoria":"Tecnologia","precio":190.00,"stock":7}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder encoder;

    // ---------------- Fila 01 ----------------

    @Test
    void f01_estadoPublico_sinToken_200() throws Exception {
        mockMvc.perform(get("/api/publico/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("API disponible"));
    }

    // ---------------- Filas 02 y 03: login ----------------

    @Test
    void f02_loginValido_200ConJwt() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"usuario\",\"password\":\"Usuario123*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresInSeconds").value(1800));
    }

    @Test
    void f03_loginConContrasenaErronea_401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"usuario\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales invalidas"));
    }

    @Test
    void loginConCamposVacios_400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------------- Filas 04 a 08: recursos protegidos ----------------

    @Test
    void f04_productosSinToken_401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void f05_productosConTokenUser_200() throws Exception {
        mockMvc.perform(get("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(tokenDe("usuario", "Usuario123*"))))
                .andExpect(status().isOk());
    }

    @Test
    void f06_crearProductoConTokenUser_403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenDe("usuario", "Usuario123*")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCTO_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void f07_crearProductoConTokenAdmin_201() throws Exception {
        mockMvc.perform(post("/api/productos")
                .header(HttpHeaders.AUTHORIZATION, bearer(tokenDe("admin", "Admin123*")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCTO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void f08_miPerfilConTokenAdmin_200ConRoles() throws Exception {
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(tokenDe("admin", "Admin123*"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_ADMIN", "ROLE_USER")));
    }

    // ---------------- Filas 09 y 10: tokens invalidos ----------------

    @Test
    void f09_tokenConFirmaModificada_401() throws Exception {
        String token = tokenDe("admin", "Admin123*");
        int inicioFirma = token.lastIndexOf('.') + 1;
        char original = token.charAt(inicioFirma);
        String alterado = token.substring(0, inicioFirma)
                + (original == 'A' ? 'B' : 'A')
                + token.substring(inicioFirma + 1);

        mockMvc.perform(get("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(alterado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void f10_tokenVencido_401() throws Exception {
        // Vencido hace 5 minutos: supera la tolerancia de reloj por defecto (60 s).
        Instant hace10 = Instant.now().minus(10, ChronoUnit.MINUTES);
        String vencido = firmar(JwtClaimsSet.builder()
                .issuer("productos-api")
                .subject("admin")
                .issuedAt(hace10)
                .expiresAt(hace10.plus(5, ChronoUnit.MINUTES))
                .claim("roles", List.of("ROLE_ADMIN"))
                .build());

        mockMvc.perform(get("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(vencido)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeOtroEmisor_401() throws Exception {
        Instant ahora = Instant.now();
        String ajeno = firmar(JwtClaimsSet.builder()
                .issuer("otra-api")
                .subject("admin")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(5, ChronoUnit.MINUTES))
                .claim("roles", List.of("ROLE_ADMIN"))
                .build());

        mockMvc.perform(get("/api/productos").header(HttpHeaders.AUTHORIZATION, bearer(ajeno)))
                .andExpect(status().isUnauthorized());
    }

    // ---------------- Reto 15.1: /api/admin/reporte ----------------

    @Test
    void reporteSinToken_401() throws Exception {
        mockMvc.perform(get("/api/admin/reporte"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reporteConTokenUser_403() throws Exception {
        mockMvc.perform(get("/api/admin/reporte").header(HttpHeaders.AUTHORIZATION, bearer(tokenDe("usuario", "Usuario123*"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void reporteConTokenAdmin_200() throws Exception {
        mockMvc.perform(get("/api/admin/reporte").header(HttpHeaders.AUTHORIZATION, bearer(tokenDe("admin", "Admin123*"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaServidor").isNotEmpty())
                .andExpect(jsonPath("$.totalProductos").value(greaterThanOrEqualTo(0)));
    }

    // ---------------- Apoyo ----------------

    private String tokenDe(String username, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.accessToken");
    }

    private String firmar(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
