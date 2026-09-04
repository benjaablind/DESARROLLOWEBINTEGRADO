package com.utp.odontologia;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.HistoriaClinica;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.HistoriaClinicaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion de la historia clinica (Integrante 4).
 *
 * Verifican sobre todo la regla central del modulo: la informacion historica no
 * se sobrescribe ni desaparece, solo se anula.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HistoriaClinicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    private Paciente ana;
    private Paciente luis;
    private Usuario odontologo;
    private Cita citaDeAna;
    private Cita citaDeLuis;
    private HistoriaClinica consultaAntigua;
    private HistoriaClinica consultaReciente;

    @BeforeEach
    void prepararDatos() {
        historiaClinicaRepository.limpiar();
        pacienteRepository.limpiar();
        usuarioRepository.limpiar();
        citaRepository.limpiar();

        ana = pacienteRepository.guardar(nuevoPaciente("12345678", "Ana Maria", "Torres Vega"));
        luis = pacienteRepository.guardar(nuevoPaciente("87654321", "Luis Alberto", "Ramirez Soto"));

        odontologo = usuarioRepository.guardar(nuevoOdontologo());

        citaDeAna = citaRepository.guardar(nuevaCita(ana.getId(), LocalDateTime.now().plusDays(1)));
        citaDeLuis = citaRepository.guardar(nuevaCita(luis.getId(), LocalDateTime.now().plusDays(2)));

        consultaAntigua = historiaClinicaRepository.guardar(nuevaConsulta(ana.getId(),
                "Dolor al masticar", "Caries oclusal en pieza 36",
                LocalDateTime.now().minusDays(30)));
        consultaReciente = historiaClinicaRepository.guardar(nuevaConsulta(ana.getId(),
                "Control de obturacion", "Obturacion en buen estado",
                LocalDateTime.now().minusDays(2)));
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

    private Cita nuevaCita(Long pacienteId, LocalDateTime fechaHora) {
        Cita cita = new Cita();
        cita.setPacienteId(pacienteId);
        cita.setOdontologoId(odontologo.getId());
        cita.setFechaHora(fechaHora);
        cita.setMotivo("Evaluacion");
        cita.setEstado(EstadoCita.PENDIENTE);
        return cita;
    }

    private HistoriaClinica nuevaConsulta(Long pacienteId, String motivo, String diagnostico,
            LocalDateTime fecha) {
        HistoriaClinica historia = new HistoriaClinica();
        historia.setPacienteId(pacienteId);
        historia.setOdontologoId(odontologo.getId());
        historia.setFecha(fecha);
        historia.setMotivoConsulta(motivo);
        historia.setAnamnesis("Sin antecedentes relevantes");
        historia.setExamenClinico("Examen intraoral sin hallazgos adicionales");
        historia.setDiagnostico(diagnostico);
        return historia;
    }

    /** Cuerpo JSON valido para POST /api/historias. La cita puede ir en null. */
    private String cuerpoCrear(Long pacienteId, Long odontologoId, Long citaId) {
        String cita = citaId == null ? "null" : citaId.toString();
        return "{"
                + "\"pacienteId\": " + pacienteId + ","
                + "\"odontologoId\": " + odontologoId + ","
                + "\"citaId\": " + cita + ","
                + "\"motivoConsulta\": \"Dolor agudo en molar inferior\","
                + "\"anamnesis\": \"Dolor de tres dias de evolucion\","
                + "\"examenClinico\": \"Caries profunda en pieza 46\","
                + "\"diagnostico\": \"Pulpitis irreversible\","
                + "\"procedimiento\": \"Apertura camaral\","
                + "\"tratamiento\": \"Endodoncia en dos sesiones\","
                + "\"medicamentos\": \"Ibuprofeno 400 mg cada 8 horas\","
                + "\"observaciones\": \"Paciente alergica a la penicilina\""
                + "}";
    }

    @Test
    @DisplayName("GET historias por paciente devuelve el historial del mas reciente al mas antiguo")
    void listarPorPacienteDevuelveHistorialOrdenado() throws Exception {
        mockMvc.perform(get("/api/historias/paciente/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(consultaReciente.getId()))
                .andExpect(jsonPath("$[1].id").value(consultaAntigua.getId()))
                .andExpect(jsonPath("$[0].pacienteNombre").value("Ana Maria Torres Vega"))
                .andExpect(jsonPath("$[0].odontologoNombre").value("Carlos Diaz Pena"));
    }

    @Test
    @DisplayName("GET historias por paciente devuelve 404 si el paciente no existe")
    void listarPorPacienteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/historias/paciente/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("paciente")));
    }

    @Test
    @DisplayName("GET historias por paciente devuelve lista vacia si aun no tiene consultas")
    void listarPorPacienteSinConsultasDevuelveListaVacia() throws Exception {
        mockMvc.perform(get("/api/historias/paciente/" + luis.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET historia por id devuelve el detalle de la consulta")
    void buscarPorIdDevuelveLaConsulta() throws Exception {
        mockMvc.perform(get("/api/historias/" + consultaAntigua.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivoConsulta").value("Dolor al masticar"))
                .andExpect(jsonPath("$.diagnostico").value("Caries oclusal en pieza 36"))
                .andExpect(jsonPath("$.anulada").value(false));
    }

    @Test
    @DisplayName("GET historia por id devuelve 404 si la consulta no existe")
    void buscarPorIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/historias/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("consulta de historia clinica")));
    }

    @Test
    @DisplayName("POST historias crea la consulta y devuelve 201 con la cabecera Location")
    void crearDevuelve201ConLocation() throws Exception {
        mockMvc.perform(post("/api/historias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoCrear(ana.getId(), odontologo.getId(), citaDeAna.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/historias/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.citaId").value(citaDeAna.getId()))
                .andExpect(jsonPath("$.diagnostico").value("Pulpitis irreversible"))
                .andExpect(jsonPath("$.anulada").value(false));

        mockMvc.perform(get("/api/historias/paciente/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("POST historias devuelve 400 si faltan campos obligatorios")
    void crearSinCamposObligatoriosDevuelve400() throws Exception {
        String cuerpo = "{"
                + "\"pacienteId\": " + ana.getId() + ","
                + "\"odontologoId\": " + odontologo.getId() + ","
                + "\"motivoConsulta\": \"   \","
                + "\"diagnostico\": \"\""
                + "}";

        mockMvc.perform(post("/api/historias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("motivoConsulta")));
    }

    @Test
    @DisplayName("POST historias devuelve 404 si el odontologo no existe")
    void crearConOdontologoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/historias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoCrear(ana.getId(), 9999L, null)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("odontologo")));
    }

    @Test
    @DisplayName("POST historias devuelve 400 si la cita pertenece a otro paciente")
    void crearConCitaDeOtroPacienteDevuelve400() throws Exception {
        mockMvc.perform(post("/api/historias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoCrear(ana.getId(), odontologo.getId(), citaDeLuis.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("no pertenece al paciente")));
    }

    @Test
    @DisplayName("PATCH anular marca la consulta como anulada y guarda el motivo")
    void anularMarcaLaConsulta() throws Exception {
        mockMvc.perform(patch("/api/historias/" + consultaReciente.getId() + "/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Se registro en el paciente equivocado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anulada").value(true))
                .andExpect(jsonPath("$.motivoAnulacion")
                        .value("Se registro en el paciente equivocado"))
                .andExpect(jsonPath("$.diagnostico").value("Obturacion en buen estado"));
    }

    @Test
    @DisplayName("PATCH anular devuelve 400 si la consulta ya estaba anulada")
    void anularDosVecesDevuelve400() throws Exception {
        String cuerpo = "{\"motivo\": \"Consulta duplicada\"}";

        mockMvc.perform(patch("/api/historias/" + consultaAntigua.getId() + "/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/historias/" + consultaAntigua.getId() + "/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La consulta ya fue anulada"));
    }

    @Test
    @DisplayName("PATCH anular devuelve 400 si no se envia el motivo")
    void anularSinMotivoDevuelve400() throws Exception {
        mockMvc.perform(patch("/api/historias/" + consultaAntigua.getId() + "/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("motivo")));
    }

    @Test
    @DisplayName("PATCH anular devuelve 404 si la consulta no existe")
    void anularConsultaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(patch("/api/historias/9999/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Error de registro\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Una consulta anulada sigue apareciendo en el historial del paciente")
    void consultaAnuladaSigueEnElHistorial() throws Exception {
        mockMvc.perform(patch("/api/historias/" + consultaReciente.getId() + "/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Registrada por error\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/historias/paciente/" + ana.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(consultaReciente.getId()))
                .andExpect(jsonPath("$[0].anulada").value(true))
                .andExpect(jsonPath("$[0].motivoAnulacion").value("Registrada por error"))
                .andExpect(jsonPath("$[0].motivoConsulta").value("Control de obturacion"))
                .andExpect(jsonPath("$[1].anulada").value(false));
    }
}
