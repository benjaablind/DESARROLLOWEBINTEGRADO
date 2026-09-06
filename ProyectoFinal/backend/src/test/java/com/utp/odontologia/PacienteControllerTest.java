package com.utp.odontologia;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.odontologia.model.Antecedentes;
import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.repository.PacienteRepository;

/**
 * Pruebas de integracion del modulo de pacientes (Integrante 2).
 * Cada prueba parte de un repositorio limpio con tres pacientes conocidos.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    private Paciente ana;
    private Paciente luis;
    private Paciente carla;

    @BeforeEach
    void prepararDatos() {
        pacienteRepository.limpiar();

        ana = pacienteRepository.guardar(nuevo("12345678", "Ana Maria", "Torres Vega",
                LocalDate.of(1990, 5, 12), "F", "987654321", EstadoPaciente.ACTIVO));
        luis = pacienteRepository.guardar(nuevo("87654321", "Luis Alberto", "Ramirez Soto",
                LocalDate.of(1985, 1, 30), "M", "912345678", EstadoPaciente.ACTIVO));
        carla = pacienteRepository.guardar(nuevo("11223344", "Carla", "Mendoza Rios",
                LocalDate.of(2000, 9, 3), "F", "955555555", EstadoPaciente.INACTIVO));
    }

    /** Arma un paciente de prueba completo, con antecedentes no vacios. */
    private Paciente nuevo(String dni, String nombres, String apellidos, LocalDate nacimiento,
            String sexo, String telefono, EstadoPaciente estado) {
        Paciente paciente = new Paciente();
        paciente.setDni(dni);
        paciente.setNombres(nombres);
        paciente.setApellidos(apellidos);
        paciente.setFechaNacimiento(nacimiento);
        paciente.setSexo(sexo);
        paciente.setTelefono(telefono);
        paciente.setEmail("paciente" + dni + "@correo.com");
        paciente.setDireccion("Av. Siempre Viva 123");
        paciente.setDistrito("Lima");
        paciente.setCiudad("Lima");
        paciente.setEstado(estado);

        Antecedentes antecedentes = new Antecedentes();
        antecedentes.setEnfermedades(List.of("Ninguna"));
        antecedentes.setObservaciones("Sin observaciones");
        paciente.setAntecedentes(antecedentes);
        return paciente;
    }

    /** Cuerpo JSON valido de un paciente, parametrizado por DNI. */
    private String cuerpoPaciente(String dni) {
        return "{"
                + "\"dni\": \"" + dni + "\","
                + "\"nombres\": \"Pedro\","
                + "\"apellidos\": \"Gonzales Diaz\","
                + "\"fechaNacimiento\": \"1995-03-15\","
                + "\"sexo\": \"M\","
                + "\"telefono\": \"900111222\","
                + "\"email\": \"pedro@correo.com\","
                + "\"direccion\": \"Jr. Union 456\","
                + "\"distrito\": \"Brena\","
                + "\"ciudad\": \"Lima\""
                + "}";
    }

    @Test
    @DisplayName("GET /api/pacientes devuelve los tres pacientes registrados")
    void listarDevuelveTodosLosPacientes() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Ana Maria Torres Vega"))
                .andExpect(jsonPath("$[0].dni").value("12345678"));
    }

    @Test
    @DisplayName("GET /api/pacientes?texto= filtra por coincidencia parcial en el nombre")
    void listarFiltraPorTextoParcialEnNombre() throws Exception {
        mockMvc.perform(get("/api/pacientes").param("texto", "ramirez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(luis.getId()));
    }

    @Test
    @DisplayName("GET /api/pacientes?texto= filtra tambien por parte del DNI")
    void listarFiltraPorTextoParcialEnDni() throws Exception {
        mockMvc.perform(get("/api/pacientes").param("texto", "1122"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].dni").value("11223344"));
    }

    @Test
    @DisplayName("GET /api/pacientes?estado=INACTIVO devuelve solo los inactivos")
    void listarFiltraPorEstado() throws Exception {
        mockMvc.perform(get("/api/pacientes").param("estado", "INACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(carla.getId()))
                .andExpect(jsonPath("$[0].estado").value("INACTIVO"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} devuelve la ficha completa")
    void buscarPorIdDevuelvePaciente() throws Exception {
        mockMvc.perform(get("/api/pacientes/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ana.getId()))
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.nombreCompleto").value("Ana Maria Torres Vega"))
                .andExpect(jsonPath("$.antecedentes.enfermedades[0]").value("Ninguna"));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} inexistente devuelve 404")
    void buscarPorIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/pacientes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    @DisplayName("GET /api/pacientes/dni/{dni} devuelve el paciente correcto")
    void buscarPorDniDevuelvePaciente() throws Exception {
        mockMvc.perform(get("/api/pacientes/dni/87654321"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(luis.getId()))
                .andExpect(jsonPath("$.nombres").value("Luis Alberto"));
    }

    @Test
    @DisplayName("GET /api/pacientes/dni/{dni} inexistente devuelve 404")
    void buscarPorDniInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/pacientes/dni/00000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/pacientes crea el paciente y devuelve 201 con header Location")
    void crearDevuelve201YHeaderLocation() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("55667788")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/api/pacientes/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.dni").value("55667788"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.antecedentes").exists());

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    @DisplayName("POST /api/pacientes con DNI de formato invalido devuelve 400")
    void crearConDniInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("DNI")));
    }

    @Test
    @DisplayName("POST /api/pacientes con campos obligatorios vacios devuelve 400")
    void crearConCamposObligatoriosVaciosDevuelve400() throws Exception {
        String cuerpo = "{"
                + "\"dni\": \"\","
                + "\"nombres\": \"\","
                + "\"apellidos\": \"\","
                + "\"fechaNacimiento\": null,"
                + "\"sexo\": \"\","
                + "\"telefono\": \"\""
                + "}";

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    @DisplayName("POST /api/pacientes con DNI duplicado devuelve 400")
    void crearConDniDuplicadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("12345678")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje",
                        containsString("Ya existe un paciente registrado con el DNI")));
    }

    @Test
    @DisplayName("PUT /api/pacientes/{id} actualiza todos los datos del paciente")
    void actualizarModificaLosDatos() throws Exception {
        mockMvc.perform(put("/api/pacientes/" + ana.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("12345678")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ana.getId()))
                .andExpect(jsonPath("$.nombres").value("Pedro"))
                .andExpect(jsonPath("$.apellidos").value("Gonzales Diaz"))
                .andExpect(jsonPath("$.telefono").value("900111222"));
    }

    @Test
    @DisplayName("PUT /api/pacientes/{id} con el DNI de otro paciente devuelve 400")
    void actualizarConDniDeOtroPacienteDevuelve400() throws Exception {
        mockMvc.perform(put("/api/pacientes/" + ana.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("87654321")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/pacientes/{id} inexistente devuelve 404")
    void actualizarPacienteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(put("/api/pacientes/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoPaciente("55667788")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH antecedentes cambia solo los antecedentes y conserva el resto")
    void actualizarAntecedentesNoAlteraLosDemasCampos() throws Exception {
        String cuerpo = "{"
                + "\"enfermedades\": [\"Diabetes\"],"
                + "\"alergias\": [\"Penicilina\"],"
                + "\"medicamentos\": [\"Metformina\"],"
                + "\"habitos\": [\"Fuma\"],"
                + "\"antecedentesOdontologicos\": \"Extraccion de terceras molares\","
                + "\"observaciones\": \"Control cada seis meses\""
                + "}";

        mockMvc.perform(patch("/api/pacientes/" + ana.getId() + "/antecedentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.antecedentes.enfermedades[0]").value("Diabetes"))
                .andExpect(jsonPath("$.antecedentes.alergias[0]").value("Penicilina"))
                .andExpect(jsonPath("$.antecedentes.habitos[0]").value("Fuma"))
                .andExpect(jsonPath("$.antecedentes.observaciones")
                        .value("Control cada seis meses"))
                .andExpect(jsonPath("$.dni").value("12345678"))
                .andExpect(jsonPath("$.nombres").value("Ana Maria"))
                .andExpect(jsonPath("$.apellidos").value("Torres Vega"))
                .andExpect(jsonPath("$.telefono").value("987654321"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("PATCH antecedentes sobre un paciente inexistente devuelve 404")
    void actualizarAntecedentesInexistenteDevuelve404() throws Exception {
        mockMvc.perform(patch("/api/pacientes/9999/antecedentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observaciones\": \"Sin datos\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH estado desactiva al paciente")
    void cambiarEstadoActualizaElEstado() throws Exception {
        mockMvc.perform(patch("/api/pacientes/" + luis.getId() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        mockMvc.perform(get("/api/pacientes").param("estado", "INACTIVO"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PATCH estado sin valor devuelve 400")
    void cambiarEstadoSinValorDevuelve400() throws Exception {
        mockMvc.perform(patch("/api/pacientes/" + luis.getId() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/pacientes/{id} devuelve 204 y luego la ficha ya no existe")
    void eliminarDevuelve204YLuego404() throws Exception {
        mockMvc.perform(delete("/api/pacientes/" + carla.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pacientes/" + carla.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("DELETE /api/pacientes/{id} inexistente devuelve 404")
    void eliminarInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/api/pacientes/9999"))
                .andExpect(status().isNotFound());
    }
}
