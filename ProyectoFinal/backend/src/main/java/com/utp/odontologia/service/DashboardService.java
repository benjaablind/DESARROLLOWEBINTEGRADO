package com.utp.odontologia.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.CitaResponse;
import com.utp.odontologia.dto.DashboardPacienteResumen;
import com.utp.odontologia.dto.DashboardResponse;
import com.utp.odontologia.dto.DashboardResumenActividad;
import com.utp.odontologia.dto.DashboardTratamientoResumen;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Pago;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Indicadores del tablero de control (Integrante 3).
 *
 * Consulta los repositorios de los demas modulos en modo lectura y arma una
 * unica respuesta para la pantalla de inicio del sistema.
 */
@Service
public class DashboardService {

    /** Cantidad maxima de filas que se envian en cada lista de detalle. */
    private static final int MAXIMO_DETALLE = 5;

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final TratamientoRepository tratamientoRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardService(PacienteRepository pacienteRepository, CitaRepository citaRepository,
            TratamientoRepository tratamientoRepository, PagoRepository pagoRepository,
            UsuarioRepository usuarioRepository) {
        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Calcula todos los indicadores en una sola pasada. */
    public DashboardResponse obtener() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        LocalDateTime ahora = LocalDateTime.now();

        List<Tratamiento> activos = tratamientoRepository.listarActivos();
        List<Cita> citasDelMesActual = citasDelMes(inicioMes, finMes);

        long citasHoy = citaRepository.listarPorDia(hoy).size();
        long citasPendientes = citaRepository.listarPorEstado(EstadoCita.PENDIENTE).size();

        double pagosPendientes = 0;
        List<DashboardTratamientoResumen> detalleTratamientos = new ArrayList<>();
        long tratamientosConSaldo = 0;

        for (Tratamiento tratamiento : activos) {
            double saldo = saldoDe(tratamiento);
            pagosPendientes += saldo;
            if (saldo > 0) {
                tratamientosConSaldo++;
            }
            if (detalleTratamientos.size() < MAXIMO_DETALLE) {
                detalleTratamientos.add(DashboardTratamientoResumen.desde(tratamiento,
                        nombreDePaciente(tratamiento.getPacienteId()), redondear(saldo)));
            }
        }

        double ingresosDelMes = pagoRepository.listarEntre(inicioMes, finMes).stream()
                .mapToDouble(Pago::getMonto)
                .sum();

        List<CitaResponse> proximasCitas = citaRepository.listar().stream()
                .filter(c -> c.getFechaHora().isAfter(ahora))
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .limit(MAXIMO_DETALLE)
                .map(this::aRespuesta)
                .toList();

        List<DashboardPacienteResumen> pacientesRecientes = pacienteRepository.listar().stream()
                .sorted(Comparator.comparing((Paciente p) -> p.getFechaRegistro()).reversed())
                .limit(MAXIMO_DETALLE)
                .map(DashboardPacienteResumen::desde)
                .toList();

        DashboardResumenActividad resumen = new DashboardResumenActividad(
                contarPorEstado(citasDelMesActual, EstadoCita.ATENDIDA),
                contarPorEstado(citasDelMesActual, EstadoCita.CANCELADA),
                contarPorEstado(citasDelMesActual, EstadoCita.NO_ASISTIO),
                tratamientoRepository.listarCompletadosEntre(inicioMes, finMes).size(),
                pacienteRepository.listarPorRangoDeRegistro(inicioMes, finMes).size());

        List<String> alertas = construirAlertas(hoy, tratamientosConSaldo,
                resumen.citasNoAsistioMes());

        return new DashboardResponse(
                pacienteRepository.contar(),
                citasHoy,
                citasPendientes,
                activos.size(),
                redondear(pagosPendientes),
                redondear(ingresosDelMes),
                proximasCitas,
                pacientesRecientes,
                detalleTratamientos,
                alertas,
                resumen);
    }

    // ------------------------------------------------------------- privados

    /** Alertas operativas; solo se incluyen cuando el conteo es mayor que cero. */
    private List<String> construirAlertas(LocalDate hoy, long tratamientosConSaldo,
            long citasNoAsistioMes) {

        List<String> alertas = new ArrayList<>();

        long sinConfirmarHoy = citaRepository.listarPorDia(hoy).stream()
                .filter(c -> c.getEstado() == EstadoCita.PENDIENTE)
                .count();

        if (sinConfirmarHoy > 0) {
            alertas.add(texto(sinConfirmarHoy, "cita sin confirmar para hoy",
                    "citas sin confirmar para hoy"));
        }
        if (tratamientosConSaldo > 0) {
            alertas.add(texto(tratamientosConSaldo, "tratamiento con saldo pendiente",
                    "tratamientos con saldo pendiente"));
        }
        if (citasNoAsistioMes > 0) {
            alertas.add(texto(citasNoAsistioMes, "cita marcada como no asistio este mes",
                    "citas marcadas como no asistio este mes"));
        }
        return alertas;
    }

    /** Arma el texto de una alerta concordando el singular y el plural. */
    private String texto(long cantidad, String singular, String plural) {
        return "Hay " + cantidad + " " + (cantidad == 1 ? singular : plural);
    }

    private List<Cita> citasDelMes(LocalDate inicioMes, LocalDate finMes) {
        return citaRepository.listarEntre(inicioMes.atStartOfDay(), finMes.atTime(23, 59, 59));
    }

    private long contarPorEstado(List<Cita> citas, EstadoCita estado) {
        return citas.stream().filter(c -> c.getEstado() == estado).count();
    }

    /** Saldo del tratamiento sin permitir valores negativos por pagos en exceso. */
    private double saldoDe(Tratamiento tratamiento) {
        double pagado = pagoRepository.totalPagadoDeTratamiento(tratamiento.getId());
        return Math.max(0, tratamiento.getPrecio() - pagado);
    }

    private String nombreDePaciente(Long pacienteId) {
        return pacienteRepository.buscarPorId(pacienteId)
                .map(p -> p.getNombreCompleto())
                .orElse(CitaResponse.SIN_DATO);
    }

    private CitaResponse aRespuesta(Cita cita) {
        String paciente = nombreDePaciente(cita.getPacienteId());
        String odontologo = usuarioRepository.buscarPorId(cita.getOdontologoId())
                .map(u -> u.getNombreCompleto())
                .orElse(CitaResponse.SIN_DATO);
        return CitaResponse.desde(cita, paciente, odontologo);
    }

    /** Evita que los decimales de coma flotante lleguen al frontend. */
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
