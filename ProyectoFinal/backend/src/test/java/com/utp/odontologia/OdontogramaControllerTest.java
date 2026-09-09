package com.utp.odontologia;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.OdontogramaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del odontograma (Integrante 4).
 *
 * Verifican la regla central del modulo: cada cambio de estado de una pieza crea
 * un registro nuevo y los anteriores siguen disponibles en el historial.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OdontogramaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OdontogramaRepository odontogramaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Paciente ana;
    private Usuario odontologo;

    @BeforeEach
    void prepararDatos() {
        odontogramaRepository.limpiar();
        pacienteRepository.limpiar();
        usuarioRepository.limpiar();

        ana = pacienteRepository.guardar(nuevoPaciente("12345678", "Ana Maria", "Torres Vega"));
        odontologo = usuarioRepository.guardar(nuevoOdontologo());
    }

    private Paciente nuevoPaciente(String dni, String nombres, String apellidos) {
        Paciente paciente = new Paciente();
        paciente.setDni(dni);
        paciente.setNombres(nombres);
        paciente.setApellidos(apellidos);
        paciente.setFechaNacimiento(LocalDate.of(1990, 5, 12));
        paciente.setSexo("F");
        paciente.setTelefono("987654321");
        return paciente;
    }

    private Usuario nuevoOdontologo() {
        Usuario usuario = new Usuario();
        usuario.setNombres("Carlos");
        usuario.setApellidos("Diaz Pena");
        usuario.setEmail("carlos.diaz@clinica.com");
        usuario.setUsuario("cdiaz");
        usuario.setPassword("secreta123");
        usuario.setRol(Rol.ODONTOLOGO);
        return usuario;
    }

    /** Cuerpo JSON valido para POST /api/odontograma. */
    private String cuerpoRegistro(Long pacienteId, Long odontologoId, int numeroPieza,
            String estado) {
        return "{"
                + "\"pacienteId\": " + pacienteId + ","
                + "\"odontologoId\": " + odontologoId + ","
                + "\"numeroPieza\": " + numeroPieza + ","
                + "\"estado\": \"" + estado + "\","
                + "\"superficie\": \"oclusal\","
                + "\"diagnostico\": \"Hallazgo del examen clinico\","
                + "\"tratamiento\": \"Segun plan de tratamiento\","
                + "\"observaciones\": \"Registro de control\""
                + "}";
    }

    /** Registra una pieza y falla la prueba si el alta no devuelve 201. */
    private void registrar(int numeroPieza, String estado) throws Exception {
        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(ana.getId(), odontologo.getId(), numeroPieza,
                                estado)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET odontograma de un paciente nuevo devuelve un odontograma vacio")
    void odontogramaDePacienteNuevoEstaVacio() throws Exception {
        mockMvc.perform(get("/api/odontograma/paciente/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(ana.getId()))
                .andExpect(jsonPath("$.pacienteNombre").value("Ana Maria Torres Vega"))
                .andExpect(jsonPath("$.piezas", hasSize(0)))
                .andExpect(jsonPath("$.totalRegistros").value(0));
    }

    @Test
    @DisplayName("POST odontograma registra una pieza y devuelve 201 con Location")
    void registrarPiezaDevuelve201ConLocation() throws Exception {
        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(ana.getId(), odontologo.getId(), 36, "CARIES")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        containsString("/api/odontograma/paciente/" + ana.getId() + "/pieza/36")))
                .andExpect(jsonPath("$.registroId").isNumber())
                .andExpect(jsonPath("$.numeroPieza").value(36))
                .andExpect(jsonPath("$.estado").value("CARIES"))
                .andExpect(jsonPath("$.odontologoId").value(odontologo.getId()));
    }

    @Test
    @DisplayName("GET odontograma devuelve las piezas registradas ordenadas por numero")
    void odontogramaActualDevuelvePiezasOrdenadas() throws Exception {
        registrar(46, "OBTURADO");
        registrar(11, "SANO");
        registrar(36, "CARIES");

        mockMvc.perform(get("/api/odontograma/paciente/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.piezas", hasSize(3)))
                .andExpect(jsonPath("$.piezas[0].numeroPieza").value(11))
                .andExpect(jsonPath("$.piezas[1].numeroPieza").value(36))
                .andExpect(jsonPath("$.piezas[2].numeroPieza").value(46))
                .andExpect(jsonPath("$.totalRegistros").value(3));
    }

    @Test
    @DisplayName("Un segundo estado de la misma pieza deja el ultimo como estado actual")
    void segundoRegistroDeLaMismaPiezaActualizaElEstadoActual() throws Exception {
        registrar(36, "CARIES");
        registrar(36, "OBTURADO");

        mockMvc.perform(get("/api/odontograma/paciente/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.piezas", hasSize(1)))
                .andExpect(jsonPath("$.piezas[0].numeroPieza").value(36))
                .andExpect(jsonPath("$.piezas[0].estado").value("OBTURADO"))
                .andExpect(jsonPath("$.totalRegistros").value(2));
    }

    @Test
    @DisplayName("El historial de una pieza conserva todos sus registros, del mas nuevo al mas viejo")
    void historialDePiezaConservaTodosLosRegistros() throws Exception {
        registrar(36, "CARIES");
        registrar(36, "OBTURADO");

        mockMvc.perform(get("/api/odontograma/paciente/" + ana.getId() + "/pieza/36"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(ana.getId()))
                .andExpect(jsonPath("$.numeroPieza").value(36))
                .andExpect(jsonPath("$.registros", hasSize(2)))
                .andExpect(jsonPath("$.registros[0].estado").value("OBTURADO"))
                .andExpect(jsonPath("$.registros[1].estado").value("CARIES"));
    }

    @Test
    @DisplayName("POST odontograma devuelve 400 con la pieza 99, que no existe en la notacion FDI")
    void registrarPieza99Devuelve400() throws Exception {
        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(ana.getId(), odontologo.getId(), 99, "SANO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("numero de pieza")));
    }

    @Test
    @DisplayName("POST odontograma devuelve 400 con la pieza 50, que no es un cuadrante valido")
    void registrarPieza50Devuelve400() throws Exception {
        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(ana.getId(), odontologo.getId(), 50, "SANO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("El numero de pieza 50 no es valido en la notacion FDI"));
    }

    @Test
    @DisplayName("POST odontograma acepta la pieza decidua 51")
    void registrarPiezaDeciduaValida() throws Exception {
        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(ana.getId(), odontologo.getId(), 51, "SELLANTE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroPieza").value(51))
                .andExpect(jsonPath("$.estado").value("SELLANTE"));
    }

    @Test
    @DisplayName("GET historial de pieza devuelve 400 si el numero de pieza no es valido")
    void historialDePiezaInvalidaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/odontograma/paciente/" + ana.getId() + "/pieza/19"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("El numero de pieza 19 no es valido en la notacion FDI"));
    }

    @Test
    @DisplayName("GET odontograma devuelve 404 si el paciente no existe")
    void odontogramaDePacienteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/odontograma/paciente/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("paciente")));
    }

    @Test
    @DisplayName("POST odontograma devuelve 404 si el paciente no existe")
    void registrarConPacienteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(9999L, odontologo.getId(), 36, "CARIES")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("paciente")));
    }

    @Test
    @DisplayName("POST odontograma devuelve 404 si el odontologo no existe")
    void registrarConOdontologoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(ana.getId(), 9999L, 36, "CARIES")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("odontologo")));
    }

    @Test
    @DisplayName("GET resumen cuenta las piezas de cada estado del odontograma actual")
    void resumenCuentaPiezasPorEstado() throws Exception {
        registrar(11, "SANO");
        registrar(12, "SANO");
        registrar(36, "CARIES");
        registrar(36, "OBTURADO");

        mockMvc.perform(get("/api/odontograma/paciente/" + ana.getId() + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.SANO").value(2))
                .andExpect(jsonPath("$.OBTURADO").value(1))
                .andExpect(jsonPath("$.CARIES").doesNotExist());
    }

    @Test
    @DisplayName("POST odontograma devuelve 400 si faltan campos obligatorios")
    void registrarSinCamposObligatoriosDevuelve400() throws Exception {
        String cuerpo = "{\"superficie\": \"oclusal\"}";

        mockMvc.perform(post("/api/odontograma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("pacienteId")));
    }
}
