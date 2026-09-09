package com.utp.odontologia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.MetodoPago;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Pago;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del modulo de tratamientos (Integrante 5).
 * Cada prueba parte de repositorios limpios y de un juego de datos conocido.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TratamientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long pacienteUnoId;
    private Long pacienteDosId;
    private Long odontologoId;
    private Long pendienteId;
    private Long enProcesoId;
    private Long completadoId;
    private Long canceladoId;

    @BeforeEach
    void prepararDatos() {
        tratamientoRepository.limpiar();
        pagoRepository.limpiar();
        pacienteRepository.limpiar();
        usuarioRepository.limpiar();

        pacienteUnoId = pacienteRepository.guardar(paciente("70111222", "Ana", "Torres")).getId();
        pacienteDosId = pacienteRepository.guardar(paciente("70333444", "Luis", "Rojas")).getId();

        Usuario odontologo = new Usuario(null, "Carlos", "Diaz", "carlos.diaz@clinica.pe",
                "cdiaz", "clave123", Rol.ODONTOLOGO);
        odontologoId = usuarioRepository.guardar(odontologo).getId();

        pendienteId = tratamientoRepository.guardar(tratamiento(pacienteUnoId, "Limpieza dental",
                300.0, EstadoTratamiento.PENDIENTE)).getId();

        Tratamiento enProceso = tratamiento(pacienteUnoId, "Ortodoncia", 1200.0,
                EstadoTratamiento.EN_PROCESO);
        enProceso.setFechaInicio(LocalDate.now().minusDays(10));
        enProcesoId = tratamientoRepository.guardar(enProceso).getId();

        Tratamiento completado = tratamiento(pacienteDosId, "Extraccion", 200.0,
                EstadoTratamiento.COMPLETADO);
        completado.setFechaFin(LocalDate.now().minusDays(1));
        completadoId = tratamientoRepository.guardar(completado).getId();

        canceladoId = tratamientoRepository.guardar(tratamiento(pacienteDosId, "Blanqueamiento",
                400.0, EstadoTratamiento.CANCELADO)).getId();
    }

    @Test
    @DisplayName("GET /api/tratamientos devuelve todos los tratamientos registrados")
    void listarDevuelveTodos() throws Exception {
        mockMvc.perform(get("/api/tratamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    @DisplayName("GET /api/tratamientos?pacienteId= filtra por paciente")
    void listarFiltraPorPaciente() throws Exception {
        mockMvc.perform(get("/api/tratamientos").param("pacienteId", pacienteUnoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pacienteNombre").value("Ana Torres"));
    }

    @Test
    @DisplayName("GET /api/tratamientos?estado= filtra por estado")
    void listarFiltraPorEstado() throws Exception {
        mockMvc.perform(get("/api/tratamientos").param("estado", "COMPLETADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Extraccion"));
    }

    @Test
    @DisplayName("GET /api/tratamientos?soloActivos=true excluye completados y cancelados")
    void listarSoloActivos() throws Exception {
        mockMvc.perform(get("/api/tratamientos").param("soloActivos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/tratamientos/{id} devuelve el detalle con su saldo")
    void buscarPorIdDevuelveDetalle() throws Exception {
        mockMvc.perform(get("/api/tratamientos/" + pendienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pendienteId))
                .andExpect(jsonPath("$.nombre").value("Limpieza dental"))
                .andExpect(jsonPath("$.odontologoNombre").value("Carlos Diaz"))
                .andExpect(jsonPath("$.totalPagado").value(0.0))
                .andExpect(jsonPath("$.saldoPendiente").value(300.0));
    }

    @Test
    @DisplayName("GET /api/tratamientos/{id} devuelve 404 si el tratamiento no existe")
    void buscarPorIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/tratamientos/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/tratamientos crea el tratamiento y devuelve 201 con Location")
    void crearDevuelve201ConLocation() throws Exception {
        String cuerpo = """
                {
                  "pacienteId": %d,
                  "odontologoId": %d,
                  "nombre": "Profilaxis",
                  "descripcion": "Limpieza general",
                  "precio": 250.5,
                  "fechaInicio": "%s",
                  "observaciones": "Paciente sin alergias"
                }
                """.formatted(pacienteUnoId, odontologoId, LocalDate.now());

        mockMvc.perform(post("/api/tratamientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.saldoPendiente").value(250.5));
    }

    @Test
    @DisplayName("POST /api/tratamientos devuelve 400 si el precio no es positivo")
    void crearConPrecioInvalidoDevuelve400() throws Exception {
        String cuerpo = """
                {
                  "pacienteId": %d,
                  "odontologoId": %d,
                  "nombre": "Profilaxis",
                  "precio": 0
                }
                """.formatted(pacienteUnoId, odontologoId);

        mockMvc.perform(post("/api/tratamientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tratamientos devuelve 404 si el paciente no existe")
    void crearConPacienteInexistenteDevuelve404() throws Exception {
        String cuerpo = """
                {
                  "pacienteId": 9999,
                  "odontologoId": %d,
                  "nombre": "Profilaxis",
                  "precio": 100.0
                }
                """.formatted(odontologoId);

        mockMvc.perform(post("/api/tratamientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/tratamientos/{id} actualiza un tratamiento abierto")
    void actualizarModificaLosDatos() throws Exception {
        String cuerpo = """
                {
                  "pacienteId": %d,
                  "odontologoId": %d,
                  "nombre": "Limpieza dental profunda",
                  "descripcion": "Incluye destartraje",
                  "precio": 350.0,
                  "observaciones": "Reprogramado"
                }
                """.formatted(pacienteUnoId, odontologoId);

        mockMvc.perform(put("/api/tratamientos/" + pendienteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Limpieza dental profunda"))
                .andExpect(jsonPath("$.precio").value(350.0))
                .andExpect(jsonPath("$.saldoPendiente").value(350.0));
    }

    @Test
    @DisplayName("PUT /api/tratamientos/{id} devuelve 400 si el tratamiento esta COMPLETADO")
    void actualizarCompletadoDevuelve400() throws Exception {
        String cuerpo = """
                {
                  "pacienteId": %d,
                  "odontologoId": %d,
                  "nombre": "Extraccion revisada",
                  "precio": 220.0
                }
                """.formatted(pacienteDosId, odontologoId);

        mockMvc.perform(put("/api/tratamientos/" + completadoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/tratamientos/{id}/estado a COMPLETADO asigna la fecha de fin")
    void cambiarEstadoACompletadoAsignaFechaFin() throws Exception {
        mockMvc.perform(patch("/api/tratamientos/" + pendienteId + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\": \"COMPLETADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.fechaFin").value(LocalDate.now().toString()));
    }

    @Test
    @DisplayName("PATCH /api/tratamientos/{id}/estado a EN_PROCESO asigna la fecha de inicio")
    void cambiarEstadoAEnProcesoAsignaFechaInicio() throws Exception {
        mockMvc.perform(patch("/api/tratamientos/" + pendienteId + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\": \"EN_PROCESO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PROCESO"))
                .andExpect(jsonPath("$.fechaInicio").value(LocalDate.now().toString()));
    }

    @Test
    @DisplayName("PATCH /api/tratamientos/{id}/estado devuelve 400 si el tratamiento esta CANCELADO")
    void cambiarEstadoDesdeCanceladoDevuelve400() throws Exception {
        mockMvc.perform(patch("/api/tratamientos/" + canceladoId + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\": \"EN_PROCESO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/tratamientos/{id}/sesiones numera las sesiones de forma correlativa")
    void agregarSesionAsignaCorrelativo() throws Exception {
        mockMvc.perform(post("/api/tratamientos/" + enProcesoId + "/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sesion("Colocacion de brackets")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sesiones.length()").value(1))
                .andExpect(jsonPath("$.sesiones[0].id").value(1))
                .andExpect(jsonPath("$.sesiones[0].numero").value(1))
                .andExpect(jsonPath("$.sesiones[0].realizada").value(false));

        mockMvc.perform(post("/api/tratamientos/" + enProcesoId + "/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sesion("Primer control")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sesiones.length()").value(2))
                .andExpect(jsonPath("$.sesiones[1].id").value(2))
                .andExpect(jsonPath("$.sesiones[1].numero").value(2));
    }

    @Test
    @DisplayName("POST /api/tratamientos/{id}/sesiones devuelve 400 si el tratamiento esta cerrado")
    void agregarSesionATratamientoCerradoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/tratamientos/" + completadoId + "/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sesion("Control tardio")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/tratamientos/{id}/sesiones/{sesionId} marca la sesion como realizada")
    void actualizarSesionLaMarcaRealizada() throws Exception {
        mockMvc.perform(post("/api/tratamientos/" + enProcesoId + "/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sesion("Colocacion de brackets")))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/tratamientos/" + enProcesoId + "/sesiones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realizada\": true, \"observaciones\": \"Sin complicaciones\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sesiones[0].realizada").value(true))
                .andExpect(jsonPath("$.sesiones[0].observaciones").value("Sin complicaciones"));
    }

    @Test
    @DisplayName("PATCH sobre una sesion inexistente devuelve 404")
    void actualizarSesionInexistenteDevuelve404() throws Exception {
        mockMvc.perform(patch("/api/tratamientos/" + enProcesoId + "/sesiones/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realizada\": true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tratamientos/{id}/sesiones/{sesionId} quita la sesion")
    void eliminarSesionDevuelve204() throws Exception {
        mockMvc.perform(post("/api/tratamientos/" + enProcesoId + "/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(sesion("Colocacion de brackets")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/tratamientos/" + enProcesoId + "/sesiones/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tratamientos/" + enProcesoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sesiones.length()").value(0));
    }

    @Test
    @DisplayName("DELETE /api/tratamientos/{id} devuelve 400 si el tratamiento tiene pagos")
    void eliminarConPagosDevuelve400() throws Exception {
        Pago pago = new Pago();
        pago.setTratamientoId(pendienteId);
        pago.setPacienteId(pacienteUnoId);
        pago.setMonto(100.0);
        pago.setMetodo(MetodoPago.EFECTIVO);
        pagoRepository.guardar(pago);

        mockMvc.perform(delete("/api/tratamientos/" + pendienteId))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/tratamientos/" + pendienteId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/tratamientos/{id} elimina un tratamiento sin pagos")
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/tratamientos/" + pendienteId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tratamientos/" + pendienteId))
                .andExpect(status().isNotFound());
    }

    /** Cuerpo JSON reutilizable para dar de alta una sesion. */
    private String sesion(String descripcion) {
        return """
                {
                  "fecha": "%s",
                  "descripcion": "%s",
                  "observaciones": "Sesion programada"
                }
                """.formatted(LocalDate.now(), descripcion);
    }

    private Paciente paciente(String dni, String nombres, String apellidos) {
        Paciente paciente = new Paciente();
        paciente.setDni(dni);
        paciente.setNombres(nombres);
        paciente.setApellidos(apellidos);
        paciente.setFechaNacimiento(LocalDate.of(1995, 3, 20));
        paciente.setTelefono("999888777");
        return paciente;
    }

    private Tratamiento tratamiento(Long pacienteId, String nombre, double precio,
            EstadoTratamiento estado) {
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setPacienteId(pacienteId);
        tratamiento.setOdontologoId(odontologoId);
        tratamiento.setNombre(nombre);
        tratamiento.setDescripcion("Tratamiento de prueba");
        tratamiento.setPrecio(precio);
        tratamiento.setEstado(estado);
        return tratamiento;
    }
}
