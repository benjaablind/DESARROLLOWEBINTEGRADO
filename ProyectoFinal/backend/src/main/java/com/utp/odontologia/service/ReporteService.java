package com.utp.odontologia.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.ArchivoResponse;
import com.utp.odontologia.dto.ReporteCitaItem;
import com.utp.odontologia.dto.ReporteCitasResponse;
import com.utp.odontologia.dto.ReporteConsultaItem;
import com.utp.odontologia.dto.ReporteHistorialPacienteResponse;
import com.utp.odontologia.dto.ReporteIngresosResponse;
import com.utp.odontologia.dto.ReportePacienteItem;
import com.utp.odontologia.dto.ReportePacientesResponse;
import com.utp.odontologia.dto.ReportePagosPendientesResponse;
import com.utp.odontologia.dto.ReporteTratamientoItem;
import com.utp.odontologia.dto.ReporteTratamientosResponse;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.HistoriaClinica;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Pago;
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
 * Reportes gerenciales del sistema (Integrante 6).
 *
 * Este servicio solo lee: cruza la informacion que registraron los demas
 * modulos y la resume. Nunca modifica datos, por lo que puede consultarse en
 * cualquier momento sin efectos secundarios.
 *
 * Todos los reportes con periodo aceptan fechas opcionales. Si falta alguna se
 * completa con un rango amplio por defecto, para que el frontend pueda pedir el
 * reporte completo sin enviar parametros.
 */
@Service
public class ReporteService {

    /** Inicio del rango por defecto cuando el cliente no envia la fecha desde. */
    private static final LocalDate DESDE_POR_DEFECTO = LocalDate.of(2000, 1, 1);

    private static final DateTimeFormatter CLAVE_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final PacienteRepository pacienteRepository;
    private final CitaRepository citaRepository;
    private final TratamientoRepository tratamientoRepository;
    private final PagoRepository pagoRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final ArchivoRepository archivoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteService(PacienteRepository pacienteRepository,
            CitaRepository citaRepository,
            TratamientoRepository tratamientoRepository,
            PagoRepository pagoRepository,
            HistoriaClinicaRepository historiaClinicaRepository,
            ArchivoRepository archivoRepository,
            UsuarioRepository usuarioRepository) {

        this.pacienteRepository = pacienteRepository;
        this.citaRepository = citaRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.pagoRepository = pagoRepository;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.archivoRepository = archivoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Pacientes registrados dentro del periodo, con el conteo por estado. */
    public ReportePacientesResponse pacientesRegistrados(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = normalizarDesde(desde);
        LocalDate fin = normalizarHasta(hasta);
        validarRango(inicio, fin);

        List<Paciente> pacientes = pacienteRepository.listarPorRangoDeRegistro(inicio, fin);

        long activos = pacientes.stream()
                .filter(p -> p.getEstado() == EstadoPaciente.ACTIVO)
                .count();

        List<ReportePacienteItem> items = pacientes.stream()
                .map(ReportePacienteItem::desde)
                .toList();

        return new ReportePacientesResponse(inicio, fin, pacientes.size(), activos,
                pacientes.size() - activos, items);
    }

    /** Citas del periodo, con el conteo por estado y el detalle cronologico. */
    public ReporteCitasResponse citasPorPeriodo(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = normalizarDesde(desde);
        LocalDate fin = normalizarHasta(hasta);
        validarRango(inicio, fin);

        List<Cita> citas = citaRepository.listarEntre(
                inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));

        // Se incluyen todos los estados, incluso los que quedan en cero, para que
        // los graficos del frontend tengan siempre las mismas categorias.
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoCita estado : EstadoCita.values()) {
            porEstado.put(estado.name(),
                    citas.stream().filter(c -> c.getEstado() == estado).count());
        }

        List<ReporteCitaItem> items = citas.stream()
                .map(c -> new ReporteCitaItem(
                        c.getId(),
                        nombrePaciente(c.getPacienteId()),
                        nombreUsuario(c.getOdontologoId()),
                        c.getFechaHora(),
                        c.getMotivo(),
                        c.getEstado()))
                .toList();

        return new ReporteCitasResponse(inicio, fin, citas.size(), porEstado, items);
    }

    /**
     * Tratamientos del periodo separando lo realizado de lo que sigue en curso.
     *
     * Realizados: completados con fecha de fin dentro del rango.
     * Pendientes: los que siguen activos y empezaron dentro del rango, o que
     * todavia no tienen fecha de inicio asignada.
     */
    public ReporteTratamientosResponse tratamientos(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = normalizarDesde(desde);
        LocalDate fin = normalizarHasta(hasta);
        validarRango(inicio, fin);

        List<Tratamiento> realizados = tratamientoRepository.listarCompletadosEntre(inicio, fin);

        List<Tratamiento> pendientes = tratamientoRepository.listarActivos().stream()
                .filter(t -> t.getFechaInicio() == null
                        || (!t.getFechaInicio().isBefore(inicio) && !t.getFechaInicio().isAfter(fin)))
                .toList();

        double montoRealizados = realizados.stream().mapToDouble(Tratamiento::getPrecio).sum();
        double montoPendientes = pendientes.stream().mapToDouble(Tratamiento::getPrecio).sum();

        List<ReporteTratamientoItem> items = new ArrayList<>();
        realizados.forEach(t -> items.add(aItem(t)));
        pendientes.forEach(t -> items.add(aItem(t)));

        return new ReporteTratamientosResponse(inicio, fin,
                realizados.size(), pendientes.size(),
                redondear(montoRealizados), redondear(montoPendientes), items);
    }

    /** Ingresos cobrados en el periodo, agrupados por metodo de pago y por mes. */
    public ReporteIngresosResponse ingresos(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = normalizarDesde(desde);
        LocalDate fin = normalizarHasta(hasta);
        validarRango(inicio, fin);

        List<Pago> pagos = pagoRepository.listarEntre(inicio, fin);

        double total = pagos.stream().mapToDouble(Pago::getMonto).sum();

        Map<String, Double> porMetodo = new LinkedHashMap<>();
        Map<String, Double> porMes = new TreeMap<>();

        for (Pago pago : pagos) {
            String metodo = pago.getMetodo() == null ? "SIN_METODO" : pago.getMetodo().name();
            porMetodo.merge(metodo, pago.getMonto(), Double::sum);

            String mes = pago.getFecha().format(CLAVE_MES);
            porMes.merge(mes, pago.getMonto(), Double::sum);
        }

        porMetodo.replaceAll((clave, monto) -> redondear(monto));
        porMes.replaceAll((clave, monto) -> redondear(monto));

        return new ReporteIngresosResponse(inicio, fin, redondear(total), pagos.size(),
                porMetodo, porMes);
    }

    /** Tratamientos activos que todavia tienen saldo por cobrar. */
    public ReportePagosPendientesResponse pagosPendientes() {
        List<ReporteTratamientoItem> items = tratamientoRepository.listarActivos().stream()
                .map(this::aItem)
                .filter(item -> item.saldoPendiente() > 0)
                .sorted(Comparator.comparingDouble(ReporteTratamientoItem::saldoPendiente)
                        .reversed())
                .toList();

        double total = items.stream()
                .mapToDouble(ReporteTratamientoItem::saldoPendiente)
                .sum();

        return new ReportePagosPendientesResponse(redondear(total), items.size(), items);
    }

    /** Historial consolidado de un paciente: consultas, citas, tratamientos y archivos. */
    public ReporteHistorialPacienteResponse historialDePaciente(Long pacienteId) {
        Paciente paciente = pacienteRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("paciente", pacienteId));

        List<HistoriaClinica> consultas = historiaClinicaRepository.listarPorPaciente(pacienteId);

        List<Cita> citas = citaRepository.listarPorPaciente(pacienteId).stream()
                .sorted(Comparator.comparing(Cita::getFechaHora).reversed())
                .toList();

        List<Tratamiento> tratamientos = tratamientoRepository.listarPorPaciente(pacienteId);
        List<Archivo> archivos = archivoRepository.listarPorPaciente(pacienteId);

        double totalPagado = pagoRepository.listarPorPaciente(pacienteId).stream()
                .mapToDouble(Pago::getMonto)
                .sum();

        List<ReporteTratamientoItem> itemsTratamiento = tratamientos.stream()
                .map(this::aItem)
                .toList();

        // El saldo del paciente solo considera los tratamientos que siguen vigentes.
        double saldoPendiente = tratamientos.stream()
                .filter(Tratamiento::estaActivo)
                .mapToDouble(t -> Math.max(0,
                        t.getPrecio() - pagoRepository.totalPagadoDeTratamiento(t.getId())))
                .sum();

        List<ReporteConsultaItem> itemsConsulta = consultas.stream()
                .map(ReporteConsultaItem::desde)
                .toList();

        List<ReporteCitaItem> itemsCita = citas.stream()
                .map(c -> new ReporteCitaItem(
                        c.getId(),
                        paciente.getNombreCompleto(),
                        nombreUsuario(c.getOdontologoId()),
                        c.getFechaHora(),
                        c.getMotivo(),
                        c.getEstado()))
                .toList();

        List<ArchivoResponse> itemsArchivo = archivos.stream()
                .map(a -> ArchivoResponse.desde(a, paciente.getNombreCompleto(),
                        nombreUsuario(a.getUsuarioId())))
                .toList();

        return new ReporteHistorialPacienteResponse(
                ReportePacienteItem.desde(paciente),
                consultas.size(),
                citas.size(),
                tratamientos.size(),
                archivos.size(),
                redondear(totalPagado),
                redondear(saldoPendiente),
                itemsConsulta,
                itemsCita,
                itemsTratamiento,
                itemsArchivo);
    }

    /** Arma la fila de tratamiento calculando lo pagado y el saldo por cobrar. */
    private ReporteTratamientoItem aItem(Tratamiento tratamiento) {
        double pagado = pagoRepository.totalPagadoDeTratamiento(tratamiento.getId());
        double saldo = Math.max(0, tratamiento.getPrecio() - pagado);

        return new ReporteTratamientoItem(
                tratamiento.getId(),
                nombrePaciente(tratamiento.getPacienteId()),
                tratamiento.getNombre(),
                tratamiento.getEstado(),
                redondear(tratamiento.getPrecio()),
                redondear(pagado),
                redondear(saldo),
                tratamiento.getFechaInicio(),
                tratamiento.getFechaFin());
    }

    /** Nombre completo del paciente, o null si el registro ya no existe. */
    private String nombrePaciente(Long pacienteId) {
        return pacienteRepository.buscarPorId(pacienteId)
                .map(Paciente::getNombreCompleto)
                .orElse(null);
    }

    /** Nombre completo del usuario, o null si el registro ya no existe. */
    private String nombreUsuario(Long usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .map(Usuario::getNombreCompleto)
                .orElse(null);
    }

    private LocalDate normalizarDesde(LocalDate desde) {
        return desde == null ? DESDE_POR_DEFECTO : desde;
    }

    private LocalDate normalizarHasta(LocalDate hasta) {
        return hasta == null ? LocalDate.now() : hasta;
    }

    /** El rango debe estar bien formado antes de consultar los repositorios. */
    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException(
                    "La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    /** Redondea importes a dos decimales para que el frontend no muestre colas largas. */
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
