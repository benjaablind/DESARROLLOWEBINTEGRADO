package com.utp.odontologia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Seguridad de punta a punta (semanas 8 y 9): login real contra la base con
 * BCrypt, JWT firmado de verdad y la matriz de permisos por rol aplicada por
 * el servidor.
 *
 * A diferencia del resto de pruebas, aqui no se usa @WithMockUser: cada
 * peticion lleva el token que devolvio el login.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiadorBaseDatos baseDeDatos;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    private Long idAdmin;
    private Long idOdontologo;
    private Long pacienteId;

    @BeforeEach
    void prepararUsuarios() {
        baseDeDatos.limpiar();
        idAdmin = crearUsuario("admin", "admin123", Rol.ADMINISTRADOR);
        idOdontologo = crearUsuario("lramos", "lramos123", Rol.ODONTOLOGO);
        crearUsuario("rdiaz", "rdiaz123", Rol.RECEPCIONISTA);
        crearUsuario("jpalma", "jpalma123", Rol.ASISTENTE);

        Paciente paciente = new Paciente();
        paciente.setDni("70111222");
        paciente.setNombres("Ana");
        paciente.setApellidos("Torres");
        pacienteId = pacienteRepository.guardar(paciente).getId();
    }

    // ------------------------------------------------------------------
    // Autenticacion: 401
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Sin token: 401 en JSON y encabezado WWW-Authenticate: Bearer")
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
                .andExpect(jsonPath("$.estado").value(401))
                .andExpect(jsonPath("$.ruta").value("/api/pacientes"));
    }

    @Test
    @DisplayName("Con el token del login: 200")
    void conTokenValidoResponde200() throws Exception {
        mockMvc.perform(get("/api/pacientes").header(HttpHeaders.AUTHORIZATION, bearer("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Token con la firma alterada: 401")
    void tokenAlteradoResponde401() throws Exception {
        String token = token("admin", "admin123");
        int inicioFirma = token.lastIndexOf('.') + 1;
        char original = token.charAt(inicioFirma);
        // Se altera el primer caracter de la firma: el ultimo puede caer en
        // bits de relleno de Base64URL y no cambiar la firma real.
        String alterado = token.substring(0, inicioFirma)
                + (original == 'A' ? 'B' : 'A')
                + token.substring(inicioFirma + 1);

        mockMvc.perform(get("/api/pacientes").header(HttpHeaders.AUTHORIZATION, "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Token vencido: 401")
    void tokenVencidoResponde401() throws Exception {
        // Vencido hace 5 minutos: supera la tolerancia de reloj del decodificador (60 s).
        Instant hace10 = Instant.now().minus(10, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("odontologia-api")
                .subject("admin")
                .issuedAt(hace10)
                .expiresAt(hace10.plus(5, ChronoUnit.MINUTES))
                .claim("roles", List.of("ROLE_ADMINISTRADOR"))
                .build();
        String vencido = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get("/api/pacientes").header(HttpHeaders.AUTHORIZATION, "Bearer " + vencido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Las contrasenas se guardan como hash BCrypt, nunca en texto plano")
    void contrasenaSeGuardaConBcrypt() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, bearer("admin", "admin123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombres":"Nuevo","apellidos":"Usuario","email":"nuevo@clinica.pe",
                         "usuario":"nuevo","password":"secreto123","rol":"ASISTENTE"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist());

        String guardada = usuarioRepository.buscarPorUsuario("nuevo").orElseThrow().getPassword();
        assertThat(guardada).startsWith("$2a$").isNotEqualTo("secreto123");
        assertThat(passwordEncoder.matches("secreto123", guardada)).isTrue();
    }

    // ------------------------------------------------------------------
    // Autorizacion por rol: 403
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("ASISTENTE: solo consulta pacientes y citas")
    class Asistente {

        @Test
        void consultaPacientesYCitas() throws Exception {
            String token = bearer("jpalma", "jpalma123");
            mockMvc.perform(get("/api/pacientes").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/citas").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk());
        }

        @Test
        void noRegistraPacientes() throws Exception {
            mockMvc.perform(post("/api/pacientes")
                    .header(HttpHeaders.AUTHORIZATION, bearer("jpalma", "jpalma123"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.estado").value(403));
        }

        @Test
        void noVeInformacionClinicaNiPagos() throws Exception {
            String token = bearer("jpalma", "jpalma123");
            mockMvc.perform(get("/api/historias/paciente/{id}", pacienteId).header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/pagos").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("RECEPCIONISTA: pacientes, citas, pagos y reportes; nada clinico")
    class Recepcionista {

        @Test
        void manejaCajaYReportes() throws Exception {
            String token = bearer("rdiaz", "rdiaz123");
            mockMvc.perform(get("/api/pagos").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/reportes/ingresos").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk());
        }

        @Test
        void noAccedeAArchivosClinicos() throws Exception {
            mockMvc.perform(get("/api/archivos").header(HttpHeaders.AUTHORIZATION, bearer("rdiaz", "rdiaz123")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void noVeElHistorialClinicoConsolidado() throws Exception {
            mockMvc.perform(get("/api/reportes/historial/{id}", pacienteId)
                    .header(HttpHeaders.AUTHORIZATION, bearer("rdiaz", "rdiaz123")))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("ODONTOLOGO: informacion clinica; no caja ni usuarios")
    class Odontologo {

        @Test
        void accedeAInformacionClinica() throws Exception {
            String token = bearer("lramos", "lramos123");
            mockMvc.perform(get("/api/tratamientos").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/archivos").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/reportes/historial/{id}", pacienteId).header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk());
        }

        @Test
        void noManejaCajaNiUsuarios() throws Exception {
            String token = bearer("lramos", "lramos123");
            mockMvc.perform(get("/api/pagos").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Datos propios: contrasena y permisos")
    class DatosPropios {

        @Test
        void cadaUsuarioCambiaSuPropiaContrasena() throws Exception {
            mockMvc.perform(patch("/api/usuarios/{id}/password", idOdontologo)
                    .header(HttpHeaders.AUTHORIZATION, bearer("lramos", "lramos123"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"passwordActual\":\"lramos123\",\"passwordNuevo\":\"nuevaClave456\"}"))
                    .andExpect(status().isOk());

            // La nueva contrasena sirve para iniciar sesion.
            assertThat(token("lramos", "nuevaClave456")).isNotBlank();
        }

        @Test
        void nadieCambiaLaContrasenaDeOtro() throws Exception {
            mockMvc.perform(patch("/api/usuarios/{id}/password", idAdmin)
                    .header(HttpHeaders.AUTHORIZATION, bearer("lramos", "lramos123"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"passwordActual\":\"admin123\",\"passwordNuevo\":\"robada123\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void cadaUsuarioVeSusPermisosPeroNoLosDeOtro() throws Exception {
            String token = bearer("lramos", "lramos123");
            mockMvc.perform(get("/api/usuarios/{id}/permisos", idOdontologo).header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rol").value("ODONTOLOGO"));
            mockMvc.perform(get("/api/usuarios/{id}/permisos", idAdmin).header(HttpHeaders.AUTHORIZATION, token))
                    .andExpect(status().isForbidden());
        }

        @Test
        void elPerfilSaleDelToken() throws Exception {
            mockMvc.perform(get("/api/auth/perfil").header(HttpHeaders.AUTHORIZATION, bearer("rdiaz", "rdiaz123")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.usuario").value("rdiaz"))
                    .andExpect(jsonPath("$.rol").value("RECEPCIONISTA"));
        }
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private Long crearUsuario(String usuario, String password, Rol rol) {
        return usuarioRepository.guardar(new Usuario(null, "Nombre", usuario,
                usuario + "@clinica.pe", usuario, passwordEncoder.encode(password), rol)).getId();
    }

    private String token(String usuario, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuario\":\"" + usuario + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.accessToken");
    }

    private String bearer(String usuario, String password) throws Exception {
        return "Bearer " + token(usuario, password);
    }
}
