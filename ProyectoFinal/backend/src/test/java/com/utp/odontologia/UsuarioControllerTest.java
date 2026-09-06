package com.utp.odontologia;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.odontologia.dto.ActualizarUsuarioRequest;
import com.utp.odontologia.dto.CambiarPasswordRequest;
import com.utp.odontologia.dto.LoginRequest;
import com.utp.odontologia.dto.UsuarioRequest;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del modulo de usuarios y autenticacion (Integrante 1).
 *
 * Cada prueba parte de un repositorio limpio y de los mismos tres usuarios, para
 * que el resultado no dependa del orden de ejecucion ni de otras pruebas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long idAdmin;
    private Long idOdontologo;
    private Long idInactivo;

    @BeforeEach
    void prepararDatos() {
        // Obligatorio: el repositorio en memoria es un singleton compartido.
        usuarioRepository.limpiar();

        idAdmin = usuarioRepository.guardar(new Usuario(null, "Ana", "Torres",
                "ana@clinica.com", "admin", "admin123", Rol.ADMINISTRADOR)).getId();

        idOdontologo = usuarioRepository.guardar(new Usuario(null, "Luis", "Ramos",
                "luis@clinica.com", "lramos", "clave123", Rol.ODONTOLOGO)).getId();

        Usuario inactivo = new Usuario(null, "Rosa", "Diaz",
                "rosa@clinica.com", "rdiaz", "clave456", Rol.RECEPCIONISTA);
        inactivo.setActivo(false);
        idInactivo = usuarioRepository.guardar(inactivo).getId();
    }

    @Test
    @DisplayName("GET /api/usuarios devuelve los usuarios registrados sin la contrasena")
    void listarDevuelveTodos() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].usuario").value("admin"))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Ana Torres"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/usuarios?rol= filtra por rol")
    void listarFiltraPorRol() throws Exception {
        mockMvc.perform(get("/api/usuarios").param("rol", "ODONTOLOGO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].usuario").value("lramos"))
                .andExpect(jsonPath("$[0].rol").value("ODONTOLOGO"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/usuarios?activo=false filtra por estado")
    void listarFiltraPorActivo() throws Exception {
        mockMvc.perform(get("/api/usuarios").param("activo", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].usuario").value("rdiaz"))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} devuelve el usuario solicitado")
    void buscarPorIdDevuelveUsuario() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + idAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idAdmin))
                .andExpect(jsonPath("$.email").value("ana@clinica.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} devuelve 404 si el id no existe")
    void buscarPorIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/usuarios/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    @DisplayName("POST /api/usuarios crea el usuario y devuelve 201 con Location")
    void crearDevuelve201ConLocation() throws Exception {
        UsuarioRequest nuevo = new UsuarioRequest("Carla", "Mejia", "carla@clinica.com",
                "cmejia", "secreta123", Rol.ASISTENTE);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevo)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/usuarios/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.usuario").value("cmejia"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/usuarios devuelve 400 cuando fallan las validaciones")
    void crearConDatosInvalidosDevuelve400() throws Exception {
        // Nombres vacios, email mal formado, usuario corto y contrasena corta.
        UsuarioRequest invalido = new UsuarioRequest("", "Mejia", "correo-invalido",
                "ab", "123", Rol.ASISTENTE);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    @DisplayName("POST /api/usuarios devuelve 400 si el nombre de usuario ya existe")
    void crearConUsuarioDuplicadoDevuelve400() throws Exception {
        UsuarioRequest duplicado = new UsuarioRequest("Otra", "Persona", "otra@clinica.com",
                "admin", "secreta123", Rol.RECEPCIONISTA);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("nombre de usuario")));
    }

    @Test
    @DisplayName("POST /api/usuarios devuelve 400 si el email ya existe")
    void crearConEmailDuplicadoDevuelve400() throws Exception {
        UsuarioRequest duplicado = new UsuarioRequest("Otra", "Persona", "ana@clinica.com",
                "operson", "secreta123", Rol.RECEPCIONISTA);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("email")));
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} actualiza los datos del usuario")
    void actualizarModificaDatos() throws Exception {
        ActualizarUsuarioRequest cambios = new ActualizarUsuarioRequest(
                "Luis Alberto", "Ramos Vega", "luis.ramos@clinica.com", Rol.ADMINISTRADOR);

        mockMvc.perform(put("/api/usuarios/" + idOdontologo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("Luis Alberto Ramos Vega"))
                .andExpect(jsonPath("$.email").value("luis.ramos@clinica.com"))
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                // El nombre de usuario no se modifica en este endpoint.
                .andExpect(jsonPath("$.usuario").value("lramos"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/usuarios/{id} devuelve 404 si el id no existe")
    void actualizarInexistenteDevuelve404() throws Exception {
        ActualizarUsuarioRequest cambios = new ActualizarUsuarioRequest(
                "Nadie", "Nadie", "nadie@clinica.com", Rol.ASISTENTE);

        mockMvc.perform(put("/api/usuarios/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/password cambia la contrasena cuando la actual coincide")
    void cambiarPasswordCorrecto() throws Exception {
        CambiarPasswordRequest cambio = new CambiarPasswordRequest("clave123", "nuevaClave123");

        mockMvc.perform(patch("/api/usuarios/" + idOdontologo + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());

        // La nueva contrasena debe servir para iniciar sesion.
        LoginRequest login = new LoginRequest("lramos", "nuevaClave123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true));
    }

    @Test
    @DisplayName("PATCH /api/usuarios/{id}/password devuelve 400 si la contrasena actual es incorrecta")
    void cambiarPasswordIncorrectoDevuelve400() throws Exception {
        CambiarPasswordRequest cambio = new CambiarPasswordRequest("equivocada", "nuevaClave123");

        mockMvc.perform(patch("/api/usuarios/" + idOdontologo + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambio)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("La contrasena actual no es correcta"));
    }

    @Test
    @DisplayName("PATCH activar y desactivar cambian el estado del usuario")
    void activarYDesactivarCambianEstado() throws Exception {
        mockMvc.perform(patch("/api/usuarios/" + idInactivo + "/activar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(patch("/api/usuarios/" + idAdmin + "/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} devuelve 204 y luego 404")
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/" + idOdontologo))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/usuarios/" + idOdontologo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} devuelve 404 si el id no existe")
    void eliminarInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/usuarios/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/usuarios/{id}/permisos devuelve los permisos del rol")
    void permisosPorRol() throws Exception {
        mockMvc.perform(get("/api/usuarios/" + idAdmin + "/permisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.permisos", hasItem("usuarios:escribir")))
                .andExpect(jsonPath("$.permisos", hasItem("pagos:escribir")));

        mockMvc.perform(get("/api/usuarios/" + idOdontologo + "/permisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ODONTOLOGO"))
                .andExpect(jsonPath("$.permisos", hasItem("odontograma:escribir")))
                // El odontologo no administra usuarios.
                .andExpect(jsonPath("$.permisos", not(hasItem("usuarios:escribir"))));

        mockMvc.perform(get("/api/usuarios/" + idInactivo + "/permisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("RECEPCIONISTA"))
                .andExpect(jsonPath("$.permisos", hasItem("pagos:escribir")));
    }

    @Test
    @DisplayName("POST /api/auth/login autentica con credenciales correctas")
    void loginCorrecto() throws Exception {
        LoginRequest login = new LoginRequest("admin", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.usuario.usuario").value("admin"))
                .andExpect(jsonPath("$.usuario.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve 400 con credenciales incorrectas")
    void loginConCredencialesMalasDevuelve400() throws Exception {
        LoginRequest login = new LoginRequest("admin", "claveEquivocada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contrasena incorrectos"));

        LoginRequest inexistente = new LoginRequest("noexiste", "admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inexistente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contrasena incorrectos"));
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve 400 si el usuario esta inactivo")
    void loginDeUsuarioInactivoDevuelve400() throws Exception {
        LoginRequest login = new LoginRequest("rdiaz", "clave456");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El usuario esta inactivo"));
    }

    @Test
    @DisplayName("GET /api/auth/perfil devuelve el perfil del usuario indicado")
    void perfilDevuelveUsuario() throws Exception {
        mockMvc.perform(get("/api/auth/perfil").param("usuarioId", String.valueOf(idAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario").value("admin"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/logout responde 200 con un mensaje")
    void logoutRespondeConMensaje() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());
    }
}
