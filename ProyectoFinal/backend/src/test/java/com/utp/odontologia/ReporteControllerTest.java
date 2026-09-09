package com.utp.odontologia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.HistoriaClinica;
import com.utp.odontologia.model.MetodoPago;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Pago;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.TipoArchivo;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.ArchivoRepository;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.HistoriaClinicaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del modulo de reportes (Integrante 6).
 *
 * Todas las pruebas trabajan sobre el mismo escenario conocido, que se arma
 * antes de cada prueba sobre repositorios limpios: dos pacientes (uno activo y
 * uno inactivo), tres citas, tres tratamientos, dos pagos, dos consultas de
 * historia clinica y un archivo.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReporteControllerTest {

    private static final DateTimeFormatter CLAVE_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private HistoriaClinicaRepository historiaClinicaRepository;

    @Autowired
    private ArchivoRepository archivoRepository;

    private Long idJuan;
    private Long idMaria;
    private Long idOdontologo;

    @BeforeEach
    void prepararEscenario() {
        // Obligatorio: los repositorios en memoria son singletons compartidos.
        pacienteRepository.limpiar();
        usuarioRepository.limpiar();
        citaRepository.limpiar();
        tratamientoRepository.limpiar();
        pagoRepository.limpiar();
        historiaClinicaRepository.limpiar();
        archivoRepository.limpiar();

        LocalDate hoy = LocalDate.now();

        idOdontologo = usuarioRepository.guardar(new Usuario(null, "Luis", "Ramos",
                "luis@clinica.com", "lramos", "clave123", Rol.ODONTOLOGO)).getId();

        idJuan = pacienteRepository.guardar(
                nuevoPaciente("70123456", "Juan", "Perez", EstadoPaciente.ACTIVO)).getId();
        idMaria = pacienteRepository.guardar(
                nuevoPaciente("70999999", "Maria", "Lopez", EstadoPaciente.INACTIVO)).getId();

        guardarCita(hoy.atTime(9, 0), EstadoCita.ATENDIDA, "Control");
        guardarCita(hoy.atTime(10, 0), EstadoCita.ATENDIDA, "Limpieza");
        guardarCita(hoy.atTime(11, 0), EstadoCita.PENDIENTE, "Evaluacion");

        Long idEndodoncia = guardarTratamiento(idJuan, "Endodoncia", 500,
                EstadoTratamiento.COMPLETADO, hoy.minusDays(10), hoy);
        Long idOrtodoncia = guardarTratamiento(idJuan, "Ortodoncia", 300,
                EstadoTratamiento.EN_PROCESO, hoy, null);
        guardarTratamiento(idMaria, "Profilaxis", 200,
                EstadoTratamiento.PENDIENTE, hoy, null);

        guardarPago(idEndodoncia, idJuan, 500, MetodoPago.EFECTIVO);
        guardarPago(idOrtodoncia, idJuan, 100, MetodoPago.TARJETA);

        guardarConsulta("Dolor en molar", "Caries profunda", false);
        guardarConsulta("Registro duplicado", "Sin diagnostico", true);

        Archivo archivo = new Archivo();
        archivo.setPacienteId(idJuan);
        archivo.setUsuarioId(idOdontologo);
        archivo.setTipo(TipoArchivo.RADIOGRAFIA);
        archivo.setNombre("uuid-generado.png");
        archivo.setNombreOriginal("panoramica.png");
        archivo.setExtension("png");
        archivo.setContentType("image/png");
        archivo.setTamanoBytes(2048);
        archivo.setDescripcion("Radiografia panoramica");
        archivo.setUbicacion("uploads/uuid-generado.png");
        archivoRepository.guardar(archivo);
    }

    @Test
    @DisplayName("GET /api/reportes/pacientes devuelve los conteos por estado")
    void reportePacientesConteos() throws Exception {
        mockMvc.perform(get("/api/reportes/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.activos").value(1))
                .andExpect(jsonPath("$.inactivos").value(1))
                .andExpect(jsonPath("$.pacientes[0].nombreCompleto").value("Juan Perez"))
                .andExpect(jsonPath("$.pacientes[0].dni").value("70123456"))
                .andExpect(jsonPath("$.pacientes[1].estado").value("INACTIVO"));
    }

    @Test
    @DisplayName("GET /api/reportes/pacientes respeta el rango de fechas recibido")
    void reportePacientesConRangoAntiguoQuedaVacio() throws Exception {
        mockMvc.perform(get("/api/reportes/pacientes")
                        .param("desde", "2000-01-01")
                        .param("hasta", "2000-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.desde").value("2000-01-01"))
                .andExpect(jsonPath("$.hasta").value("2000-12-31"))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.pacientes").isEmpty());
    }

    @Test
    @DisplayName("GET /api/reportes/citas agrupa las citas por estado")
    void reporteCitasAgrupadoPorEstado() throws Exception {
        mockMvc.perform(get("/api/reportes/citas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.porEstado.ATENDIDA").value(2))
                .andExpect(jsonPath("$.porEstado.PENDIENTE").value(1))
                .andExpect(jsonPath("$.porEstado.CANCELADA").value(0))
                .andExpect(jsonPath("$.citas[0].pacienteNombre").value("Juan Perez"))
                .andExpect(jsonPath("$.citas[0].odontologoNombre").value("Luis Ramos"))
                .andExpect(jsonPath("$.citas[0].motivo").value("Control"));
    }

    @Test
    @DisplayName("GET /api/reportes/tratamientos separa realizados de pendientes")
    void reporteTratamientosSeparaRealizadosYPendientes() throws Exception {
        mockMvc.perform(get("/api/reportes/tratamientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRealizados").value(1))
                .andExpect(jsonPath("$.totalPendientes").value(2))
                .andExpect(jsonPath("$.montoRealizados").value(500.0))
                .andExpect(jsonPath("$.montoPendientes").value(500.0))
                .andExpect(jsonPath("$.tratamientos[0].nombre").value("Endodoncia"))
                .andExpect(jsonPath("$.tratamientos[0].estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.tratamientos[0].totalPagado").value(500.0))
                .andExpect(jsonPath("$.tratamientos[0].saldoPendiente").value(0.0));
    }

    @Test
    @DisplayName("GET /api/reportes/ingresos suma el total y agrupa por metodo de pago")
    void reporteIngresosPorMetodo() throws Exception {
        mockMvc.perform(get("/api/reportes/ingresos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(600.0))
                .andExpect(jsonPath("$.cantidadPagos").value(2))
                .andExpect(jsonPath("$.porMetodo.EFECTIVO").value(500.0))
                .andExpect(jsonPath("$.porMetodo.TARJETA").value(100.0));
    }

    @Test
    @DisplayName("GET /api/reportes/ingresos agrupa por mes con la clave yyyy-MM")
    void reporteIngresosPorMes() throws Exception {
        String mes = LocalDate.now().format(CLAVE_MES);

        mockMvc.perform(get("/api/reportes/ingresos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['porMes']['" + mes + "']").value(600.0));
    }

    @Test
    @DisplayName("GET /api/reportes/pagos-pendientes lista la deuda de los tratamientos activos")
    void reportePagosPendientes() throws Exception {
        mockMvc.perform(get("/api/reportes/pagos-pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPendiente").value(400.0))
                .andExpect(jsonPath("$.cantidadTratamientos").value(2))
                .andExpect(jsonPath("$.items[0].saldoPendiente").value(200.0))
                .andExpect(jsonPath("$.items[1].saldoPendiente").value(200.0));
    }

    @Test
    @DisplayName("GET /api/reportes/historial/{id} arma el consolidado del paciente")
    void reporteHistorialDePaciente() throws Exception {
        mockMvc.perform(get("/api/reportes/historial/" + idJuan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paciente.nombreCompleto").value("Juan Perez"))
                .andExpect(jsonPath("$.totalConsultas").value(2))
                .andExpect(jsonPath("$.totalCitas").value(3))
                .andExpect(jsonPath("$.totalTratamientos").value(2))
                .andExpect(jsonPath("$.totalArchivos").value(1))
                .andExpect(jsonPath("$.totalPagado").value(600.0))
                .andExpect(jsonPath("$.saldoPendiente").value(200.0))
                .andExpect(jsonPath("$.consultas[0].motivoConsulta").exists())
                .andExpect(jsonPath("$.citas[0].estado").exists())
                .andExpect(jsonPath("$.archivos[0].nombreOriginal").value("panoramica.png"))
                // El consolidado tampoco expone la ruta fisica del archivo.
                .andExpect(jsonPath("$.archivos[0].ubicacion").doesNotExist())
                .andExpect(jsonPath("$.archivos[0].urlDescarga").exists());
    }

    @Test
    @DisplayName("GET /api/reportes/historial/{id} devuelve 404 si el paciente no existe")
    void historialDePacienteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/reportes/historial/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    @DisplayName("Los reportes devuelven 400 si desde es posterior a hasta")
    void rangoInvertidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/reportes/pacientes")
                        .param("desde", "2025-12-31")
                        .param("hasta", "2025-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje")
                        .value("La fecha inicial no puede ser posterior a la fecha final"));

        mockMvc.perform(get("/api/reportes/ingresos")
                        .param("desde", "2025-12-31")
                        .param("hasta", "2025-01-01"))
                .andExpect(status().isBadRequest());
    }

    /** Paciente minimo pero completo para los reportes. */
    private Paciente nuevoPaciente(String dni, String nombres, String apellidos,
            EstadoPaciente estado) {

        Paciente paciente = new Paciente();
        paciente.setDni(dni);
        paciente.setNombres(nombres);
        paciente.setApellidos(apellidos);
        paciente.setTelefono("9" + dni);
        paciente.setFechaNacimiento(LocalDate.now().minusYears(30));
        paciente.setEstado(estado);
        paciente.setFechaRegistro(LocalDateTime.now());
        return paciente;
    }

    private void guardarCita(LocalDateTime fechaHora, EstadoCita estado, String motivo) {
        Cita cita = new Cita();
        cita.setPacienteId(idJuan);
        cita.setOdontologoId(idOdontologo);
        cita.setFechaHora(fechaHora);
        cita.setEstado(estado);
        cita.setMotivo(motivo);
        citaRepository.guardar(cita);
    }

    private Long guardarTratamiento(Long pacienteId, String nombre, double precio,
            EstadoTratamiento estado, LocalDate inicio, LocalDate fin) {

        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setPacienteId(pacienteId);
        tratamiento.setOdontologoId(idOdontologo);
        tratamiento.setNombre(nombre);
        tratamiento.setPrecio(precio);
        tratamiento.setEstado(estado);
        tratamiento.setFechaInicio(inicio);
        tratamiento.setFechaFin(fin);
        return tratamientoRepository.guardar(tratamiento).getId();
    }

    private void guardarPago(Long tratamientoId, Long pacienteId, double monto,
            MetodoPago metodo) {

        Pago pago = new Pago();
        pago.setTratamientoId(tratamientoId);
        pago.setPacienteId(pacienteId);
        pago.setMonto(monto);
        pago.setMetodo(metodo);
        pago.setFecha(LocalDateTime.now());
        pagoRepository.guardar(pago);
    }

    private void guardarConsulta(String motivo, String diagnostico, boolean anulada) {
        HistoriaClinica consulta = new HistoriaClinica();
        consulta.setPacienteId(idJuan);
        consulta.setOdontologoId(idOdontologo);
        consulta.setFecha(LocalDateTime.now());
        consulta.setMotivoConsulta(motivo);
        consulta.setDiagnostico(diagnostico);
        consulta.setAnulada(anulada);
        historiaClinicaRepository.guardar(consulta);
    }
}
