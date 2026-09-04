package com.utp.odontologia;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.odontologia.dto.CitaEstadoRequest;
import com.utp.odontologia.dto.CitaReprogramarRequest;
import com.utp.odontologia.dto.CitaRequest;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del modulo de citas y agenda (Integrante 3).
 * Cada prueba parte de un escenario conocido creado en @BeforeEach.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Paciente paciente;
    private Usuario odontologo;
    private Cita citaManana9;
    private Cita citaManana11;
    private Cita citaPasadoManana10;

    private LocalDate manana;
    private LocalDate pasadoManana;

    @BeforeEach
    void prepararEscenario() {
        citaRepository.limpiar();
        pacienteRepository.limpiar();
        usuarioRepository.limpiar();

        manana = LocalDate.now().plusDays(1);
        pasadoManana = LocalDate.now().plusDays(2);

        paciente = new Paciente();
        paciente.setDni("12345678");
        paciente.setNombres("Ana");
        paciente.setApellidos("Torres");
        paciente.setTelefono("987654321");
        paciente = pacienteRepository.guardar(paciente);

        odontologo = new Usuario();
        odontologo.setNombres("Luis");
        odontologo.setApellidos("Ramos");
        odontologo.setEmail("lramos@clinica.com");
        odontologo.setUsuario("lramos");
        odontologo.setPassword("123456");
        odontologo.setRol(Rol.ODONTOLOGO);
        odontologo = usuarioRepository.guardar(odontologo);

        citaManana9 = guardarCita(manana.atTime(9, 0), EstadoCita.PENDIENTE, "Control");
        citaManana11 = guardarCita(manana.atTime(11, 0), EstadoCita.CONFIRMADA, "Limpieza");
        citaPasadoManana10 = guardarCita(pasadoManana.atTime(10, 0), EstadoCita.PENDIENTE,
                "Extraccion");
    }

    private Cita guardarCita(LocalDateTime fechaHora, EstadoCita estado, String motivo) {
        Cita cita = new Cita();
        cita.setPacienteId(paciente.getId());
        cita.setOdontologoId(odontologo.getId());
        cita.setFechaHora(fechaHora);
        cita.setDuracionMinutos(30);
        cita.setMotivo(motivo);
        cita.setEstado(estado);
        return citaRepository.guardar(cita);
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    @Test
    @DisplayName("GET /api/citas devuelve todas las citas ordenadas por fecha")
    void listarDevuelveTodasLasCitas() throws Exception {
        mockMvc.perform(get("/api/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].motivo").value("Control"))
                .andExpect(jsonPath("$[2].motivo").value("Extraccion"));
    }

    @Test
    @DisplayName("GET /api/citas?dia= filtra por dia")
    void listarFiltraPorDia() throws Exception {
        mockMvc.perform(get("/api/citas").param("dia", manana.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/citas?estado= filtra por estado")
    void listarFiltraPorEstado() throws Exception {
        mockMvc.perform(get("/api/citas").param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/citas").param("estado", "CONFIRMADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].motivo").value("Limpieza"));
    }

    @Test
    @DisplayName("GET /api/citas/{id} devuelve la cita con los nombres resueltos")
    void buscarPorIdDevuelveLaCita() throws Exception {
        mockMvc.perform(get("/api/citas/" + citaManana9.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(citaManana9.getId()))
                .andExpect(jsonPath("$.pacienteNombre").value("Ana Torres"))
                .andExpect(jsonPath("$.odontologoNombre").value("Luis Ramos"))
                .andExpect(jsonPath("$.duracionMinutos").value(30))
                .andExpect(jsonPath("$.fechaHoraFin").exists());
    }

    @Test
    @DisplayName("GET /api/citas/{id} devuelve 404 si la cita no existe")
    void buscarPorIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/citas/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    @DisplayName("POST /api/citas crea la cita y devuelve 201 con Location")
    void crearDevuelve201ConLocation() throws Exception {
        CitaRequest peticion = new CitaRequest(paciente.getId(), odontologo.getId(),
                manana.atTime(15, 0), null, "Evaluacion", null);

        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/citas/")))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.duracionMinutos").value(30));
    }

    @Test
    @DisplayName("POST /api/citas devuelve 400 si falta el motivo")
    void crearSinMotivoDevuelve400() throws Exception {
        String cuerpo = "{\"pacienteId\":" + paciente.getId()
                + ",\"odontologoId\":" + odontologo.getId()
                + ",\"fechaHora\":\"" + manana.atTime(16, 0) + "\"}";

        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/citas devuelve 400 si la fecha no es futura")
    void crearConFechaPasadaDevuelve400() throws Exception {
        CitaRequest peticion = new CitaRequest(paciente.getId(), odontologo.getId(),
                LocalDate.now().minusDays(1).atTime(9, 0), 30, "Control", null);

        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/citas devuelve 404 si el paciente no existe")
    void crearConPacienteInexistenteDevuelve404() throws Exception {
        CitaRequest peticion = new CitaRequest(9999L, odontologo.getId(),
                manana.atTime(17, 0), 30, "Control", null);

        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/citas devuelve 400 si el horario del odontologo se cruza")
    void crearConCruceDeHorarioDevuelve400() throws Exception {
        CitaRequest peticion = new CitaRequest(paciente.getId(), odontologo.getId(),
                manana.atTime(9, 15), 30, "Control", null);

        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("El odontologo ya tiene una cita en ese horario"));
    }

    @Test
    @DisplayName("PUT /api/citas/{id} actualiza sin considerar su propio horario un cruce")
    void actualizarDevuelve200() throws Exception {
        CitaRequest peticion = new CitaRequest(paciente.getId(), odontologo.getId(),
                manana.atTime(9, 0), 45, "Control reprogramado", "Trae radiografia");

        mockMvc.perform(put("/api/citas/" + citaManana9.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duracionMinutos").value(45))
                .andExpect(jsonPath("$.motivo").value("Control reprogramado"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/confirmar pasa la cita a CONFIRMADA")
    void confirmarCambiaElEstado() throws Exception {
        mockMvc.perform(patch("/api/citas/" + citaManana9.getId() + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        mockMvc.perform(patch("/api/citas/" + citaManana9.getId() + "/confirmar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/asistencia registra ATENDIDA y NO_ASISTIO")
    void registrarAsistenciaCambiaElEstado() throws Exception {
        mockMvc.perform(patch("/api/citas/" + citaManana9.getId() + "/asistencia")
                .param("asistio", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDA"));

        mockMvc.perform(patch("/api/citas/" + citaManana11.getId() + "/asistencia")
                .param("asistio", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("NO_ASISTIO"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/estado devuelve 400 si la cita ya esta CANCELADA")
    void cambiarEstadoDesdeCanceladaDevuelve400() throws Exception {
        mockMvc.perform(patch("/api/citas/" + citaManana9.getId() + "/cancelar"))
                .andExpect(status().isOk());

        CitaEstadoRequest peticion = new CitaEstadoRequest(EstadoCita.CONFIRMADA, null);

        mockMvc.perform(patch("/api/citas/" + citaManana9.getId() + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/estado cambia el estado de una cita vigente")
    void cambiarEstadoValidoDevuelve200() throws Exception {
        CitaEstadoRequest peticion = new CitaEstadoRequest(EstadoCita.CONFIRMADA,
                "Confirmada por telefono");

        mockMvc.perform(patch("/api/citas/" + citaManana9.getId() + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$.observaciones").value("Confirmada por telefono"));
    }

    @Test
    @DisplayName("PATCH /api/citas/{id}/cancelar guarda el motivo en observaciones")
    void cancelarGuardaElMotivo() throws Exception {
        mockMvc.perform(patch("/api/citas/" + citaManana9.getId() + "/cancelar")
                .param("motivo", "El paciente viajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.observaciones").value("El paciente viajo"));
    }

    @Test
    @DisplayName("POST /api/citas/{id}/reprogramar crea una cita nueva ligada a la original")
    void reprogramarCreaCitaNueva() throws Exception {
        CitaReprogramarRequest peticion = new CitaReprogramarRequest(
                pasadoManana.atTime(16, 0), "El odontologo tuvo una emergencia");

        mockMvc.perform(post("/api/citas/" + citaManana9.getId() + "/reprogramar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(peticion)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/citas/")))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.citaOriginalId").value(citaManana9.getId()))
                .andExpect(jsonPath("$.motivo").value("Control"));

        mockMvc.perform(get("/api/citas/" + citaManana9.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REPROGRAMADA"));
    }

    @Test
    @DisplayName("GET /api/citas/disponibilidad devuelve las horas ocupadas")
    void disponibilidadDevuelveHorasOcupadas() throws Exception {
        mockMvc.perform(get("/api/citas/disponibilidad")
                .param("odontologoId", String.valueOf(odontologo.getId()))
                .param("dia", manana.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("09:00"))
                .andExpect(jsonPath("$[1]").value("11:00"));
    }

    @Test
    @DisplayName("GET /api/citas/agenda devuelve la agenda del dia ordenada")
    void agendaDelDiaDevuelveLasCitasDelDia() throws Exception {
        mockMvc.perform(get("/api/citas/agenda").param("dia", manana.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].motivo").value("Control"));
    }

    @Test
    @DisplayName("DELETE /api/citas/{id} elimina la cita y luego devuelve 404")
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/citas/" + citaPasadoManana10.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/citas/" + citaPasadoManana10.getId()))
                .andExpect(status().isNotFound());
    }
}
