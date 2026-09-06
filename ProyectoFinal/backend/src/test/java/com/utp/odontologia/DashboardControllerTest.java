package com.utp.odontologia;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MockMvc;

import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Pago;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del tablero de control (Integrante 3).
 * El escenario se arma con datos conocidos para poder verificar cada indicador.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Paciente paciente1;
    private Paciente paciente2;
    private Usuario odontologo;
    private Tratamiento tratamientoActivo;

    @BeforeEach
    void prepararEscenario() {
        pacienteRepository.limpiar();
        citaRepository.limpiar();
        tratamientoRepository.limpiar();
        pagoRepository.limpiar();
        usuarioRepository.limpiar();

        LocalDate hoy = LocalDate.now();
        // Un dia del mes en curso distinto de hoy, para que los conteos no dependan
        // de la fecha en que se ejecuten las pruebas.
        LocalDate otroDiaDelMes = hoy.getDayOfMonth() == 1
                ? hoy.withDayOfMonth(2)
                : hoy.withDayOfMonth(1);

        paciente1 = new Paciente();
        paciente1.setDni("12345678");
        paciente1.setNombres("Ana");
        paciente1.setApellidos("Torres");
        paciente1.setTelefono("987654321");
        paciente1 = pacienteRepository.guardar(paciente1);

        paciente2 = new Paciente();
        paciente2.setDni("87654321");
        paciente2.setNombres("Juan");
        paciente2.setApellidos("Perez");
        paciente2.setTelefono("912345678");
        paciente2 = pacienteRepository.guardar(paciente2);

        odontologo = new Usuario();
        odontologo.setNombres("Luis");
        odontologo.setApellidos("Ramos");
        odontologo.setEmail("lramos@clinica.com");
        odontologo.setUsuario("lramos");
        odontologo.setPassword("123456");
        odontologo.setRol(Rol.ODONTOLOGO);
        odontologo = usuarioRepository.guardar(odontologo);

        guardarCita(hoy.atTime(9, 0), EstadoCita.PENDIENTE);
        guardarCita(hoy.atTime(15, 30), EstadoCita.CONFIRMADA);
        guardarCita(hoy.plusDays(3).atTime(10, 0), EstadoCita.PENDIENTE);
        guardarCita(otroDiaDelMes.atTime(12, 0), EstadoCita.ATENDIDA);
        guardarCita(otroDiaDelMes.atTime(13, 0), EstadoCita.CANCELADA);
        guardarCita(otroDiaDelMes.atTime(8, 0), EstadoCita.NO_ASISTIO);

        tratamientoActivo = new Tratamiento();
        tratamientoActivo.setPacienteId(paciente1.getId());
        tratamientoActivo.setOdontologoId(odontologo.getId());
        tratamientoActivo.setNombre("Ortodoncia");
        tratamientoActivo.setPrecio(1000);
        tratamientoActivo.setEstado(EstadoTratamiento.EN_PROCESO);
        tratamientoActivo = tratamientoRepository.guardar(tratamientoActivo);

        Tratamiento tratamientoCompletado = new Tratamiento();
        tratamientoCompletado.setPacienteId(paciente2.getId());
        tratamientoCompletado.setOdontologoId(odontologo.getId());
        tratamientoCompletado.setNombre("Profilaxis");
        tratamientoCompletado.setPrecio(500);
        tratamientoCompletado.setEstado(EstadoTratamiento.COMPLETADO);
        tratamientoCompletado.setFechaFin(hoy);
        tratamientoCompletado = tratamientoRepository.guardar(tratamientoCompletado);

        guardarPago(tratamientoActivo.getId(), paciente1.getId(), 400, LocalDateTime.now());
        // Pago del mes anterior: no debe sumar a los ingresos del mes en curso.
        guardarPago(tratamientoCompletado.getId(), paciente2.getId(), 200,
                LocalDateTime.now().minusMonths(1));
    }

    private void guardarCita(LocalDateTime fechaHora, EstadoCita estado) {
        Cita cita = new Cita();
        cita.setPacienteId(paciente1.getId());
        cita.setOdontologoId(odontologo.getId());
        cita.setFechaHora(fechaHora);
        cita.setDuracionMinutos(30);
        cita.setMotivo("Control");
        cita.setEstado(estado);
        citaRepository.guardar(cita);
    }

    private void guardarPago(Long tratamientoId, Long pacienteId, double monto,
            LocalDateTime fecha) {
        Pago pago = new Pago();
        pago.setTratamientoId(tratamientoId);
        pago.setPacienteId(pacienteId);
        pago.setMonto(monto);
        pago.setFecha(fecha);
        pagoRepository.guardar(pago);
    }

    @Test
    @DisplayName("GET /api/dashboard devuelve los contadores principales")
    void devuelveLosContadoresPrincipales() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacientesRegistrados").value(2))
                .andExpect(jsonPath("$.citasHoy").value(2))
                .andExpect(jsonPath("$.citasPendientes").value(2))
                .andExpect(jsonPath("$.tratamientosActivos").value(1));
    }

    @Test
    @DisplayName("GET /api/dashboard calcula el saldo pendiente de los tratamientos activos")
    void calculaElSaldoPendiente() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagosPendientes").value(600.0))
                .andExpect(jsonPath("$.tratamientosActivosDetalle.length()").value(1))
                .andExpect(jsonPath("$.tratamientosActivosDetalle[0].id")
                        .value(tratamientoActivo.getId()))
                .andExpect(jsonPath("$.tratamientosActivosDetalle[0].pacienteNombre")
                        .value("Ana Torres"))
                .andExpect(jsonPath("$.tratamientosActivosDetalle[0].saldoPendiente").value(600.0));
    }

    @Test
    @DisplayName("GET /api/dashboard suma solo los pagos del mes en curso")
    void sumaSoloLosIngresosDelMes() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresosDelMes").value(400.0));
    }

    @Test
    @DisplayName("GET /api/dashboard genera las alertas cuando corresponde")
    void generaLasAlertas() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertas", hasSize(3)))
                .andExpect(jsonPath("$.alertas", hasItem("Hay 1 cita sin confirmar para hoy")))
                .andExpect(jsonPath("$.alertas",
                        hasItem("Hay 1 tratamiento con saldo pendiente")))
                .andExpect(jsonPath("$.alertas",
                        hasItem("Hay 1 cita marcada como no asistio este mes")));
    }

    @Test
    @DisplayName("GET /api/dashboard resume la actividad del mes")
    void resumeLaActividadDelMes() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumenActividad.citasAtendidasMes").value(1))
                .andExpect(jsonPath("$.resumenActividad.citasCanceladasMes").value(1))
                .andExpect(jsonPath("$.resumenActividad.citasNoAsistioMes").value(1))
                .andExpect(jsonPath("$.resumenActividad.tratamientosCompletadosMes").value(1))
                .andExpect(jsonPath("$.resumenActividad.nuevosPacientesMes").value(2));
    }

    @Test
    @DisplayName("GET /api/dashboard incluye proximas citas y pacientes recientes")
    void incluyeLasListasDeDetalle() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proximasCitas", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.proximasCitas[0].pacienteNombre").value("Ana Torres"))
                .andExpect(jsonPath("$.pacientesRecientes", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/dashboard devuelve todo en cero cuando no hay datos")
    void devuelveCerosCuandoNoHayDatos() throws Exception {
        pacienteRepository.limpiar();
        citaRepository.limpiar();
        tratamientoRepository.limpiar();
        pagoRepository.limpiar();

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacientesRegistrados").value(0))
                .andExpect(jsonPath("$.citasHoy").value(0))
                .andExpect(jsonPath("$.pagosPendientes").value(0.0))
                .andExpect(jsonPath("$.ingresosDelMes").value(0.0))
                .andExpect(jsonPath("$.alertas", hasSize(0)));
    }
}
