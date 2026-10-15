package com.utp.odontologia.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.odontologia.dto.EstadoCuentaResponse;
import com.utp.odontologia.dto.PagoRequest;
import com.utp.odontologia.dto.PagoResponse;
import com.utp.odontologia.dto.TratamientoResponse;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Pago;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;

/**
 * Reglas de negocio del modulo de pagos (Integrante 5).
 * Un pago siempre se registra contra un tratamiento y nunca puede superar el
 * saldo pendiente de ese tratamiento.
 *
 * La frontera transaccional vive aqui (semana 7): las consultas se marcan
 * readOnly y registrar() es una unidad de trabajo que bloquea el tratamiento.
 */
@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final TratamientoRepository tratamientoRepository;
    private final PacienteRepository pacienteRepository;

    public PagoService(PagoRepository pagoRepository,
            TratamientoRepository tratamientoRepository,
            PacienteRepository pacienteRepository) {
        this.pagoRepository = pagoRepository;
        this.tratamientoRepository = tratamientoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Lista pagos aplicando filtros opcionales y combinables.
     *
     * @param pacienteId     solo los pagos de ese paciente; puede ser null
     * @param tratamientoId  solo los pagos de ese tratamiento; puede ser null
     */
    @Transactional(readOnly = true)
    public List<PagoResponse> listar(Long pacienteId, Long tratamientoId) {
        return pagoRepository.filtrar(pacienteId, tratamientoId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    /** Busca un pago por id o falla con 404. */
    @Transactional(readOnly = true)
    public Pago buscarPorId(Long id) {
        return pagoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("pago", id));
    }

    /** Devuelve la vista de un pago, o falla con 404. */
    @Transactional(readOnly = true)
    public PagoResponse detalle(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    /**
     * Registra un pago contra un tratamiento.
     * El paciente se toma del tratamiento y el monto no puede exceder el saldo.
     *
     * Leer el saldo y guardar el pago son dos pasos: sin control, dos cobros
     * simultaneos podrian leer el mismo saldo y pasar ambos la validacion. Por
     * eso el tratamiento se lee con bloqueo de fila (buscarParaActualizar): el
     * segundo cobro espera a que el primero confirme y ve el saldo ya reducido.
     */
    public PagoResponse registrar(PagoRequest request) {
        Tratamiento tratamiento = tratamientoRepository.buscarParaActualizar(request.tratamientoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("tratamiento", request.tratamientoId()));

        if (tratamiento.getEstado() == EstadoTratamiento.CANCELADO) {
            throw new ReglaNegocioException(
                    "No se puede registrar un pago de un tratamiento CANCELADO");
        }

        BigDecimal saldo = saldoDe(tratamiento);
        BigDecimal monto = Montos.redondear(request.monto());

        if (monto.compareTo(saldo) > 0) {
            throw new ReglaNegocioException(
                    "El monto excede el saldo pendiente del tratamiento: saldo actual S/ " + saldo);
        }

        Pago pago = new Pago();
        pago.setTratamientoId(tratamiento.getId());
        pago.setPacienteId(tratamiento.getPacienteId());
        pago.setMonto(monto);
        pago.setMetodo(request.metodo());
        pago.setFecha(LocalDateTime.now());
        pago.setComprobante(request.comprobante());
        pago.setObservaciones(request.observaciones());

        return aRespuesta(pagoRepository.guardar(pago));
    }

    /** Anula un pago. El saldo del tratamiento vuelve a subir. */
    public void eliminar(Long id) {
        buscarPorId(id);
        pagoRepository.eliminar(id);
    }

    /** Historial de pagos de un tratamiento, ordenado por fecha. */
    @Transactional(readOnly = true)
    public List<PagoResponse> historialDeTratamiento(Long tratamientoId) {
        buscarTratamiento(tratamientoId);
        return pagoRepository.listarPorTratamiento(tratamientoId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    /**
     * Estado de cuenta del paciente: suma sus tratamientos no cancelados con lo
     * pagado y lo que aun debe.
     */
    @Transactional(readOnly = true)
    public EstadoCuentaResponse estadoDeCuenta(Long pacienteId) {
        Paciente paciente = pacienteRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("paciente", pacienteId));

        List<Tratamiento> vigentes = tratamientoRepository.listarPorPaciente(pacienteId).stream()
                .filter(tratamiento -> tratamiento.getEstado() != EstadoTratamiento.CANCELADO)
                .toList();

        BigDecimal totalTratamientos = Montos.CERO;
        BigDecimal totalPagado = Montos.CERO;
        BigDecimal saldoPendiente = Montos.CERO;

        for (Tratamiento tratamiento : vigentes) {
            totalTratamientos = Montos.sumar(totalTratamientos, tratamiento.getPrecio());
            totalPagado = Montos.sumar(totalPagado, pagadoDe(tratamiento));
            saldoPendiente = Montos.sumar(saldoPendiente, saldoDe(tratamiento));
        }

        List<TratamientoResponse> detalle = vigentes.stream()
                .map(this::aRespuestaTratamiento)
                .toList();

        return new EstadoCuentaResponse(
                paciente.getId(),
                paciente.getNombreCompleto(),
                totalTratamientos,
                totalPagado,
                saldoPendiente,
                detalle);
    }

    private Tratamiento buscarTratamiento(Long tratamientoId) {
        return tratamientoRepository.buscarPorId(tratamientoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("tratamiento", tratamientoId));
    }

    private BigDecimal pagadoDe(Tratamiento tratamiento) {
        return Montos.redondear(pagoRepository.totalPagadoDeTratamiento(tratamiento.getId()));
    }

    /** Saldo del tratamiento: precio menos lo pagado, nunca negativo. */
    private BigDecimal saldoDe(Tratamiento tratamiento) {
        return Montos.saldo(tratamiento.getPrecio(), pagadoDe(tratamiento));
    }

    private PagoResponse aRespuesta(Pago pago) {
        String nombreTratamiento = tratamientoRepository.buscarPorId(pago.getTratamientoId())
                .map(Tratamiento::getNombre)
                .orElse(null);
        String nombrePaciente = pacienteRepository.buscarPorId(pago.getPacienteId())
                .map(Paciente::getNombreCompleto)
                .orElse(null);

        return PagoResponse.desde(pago, nombreTratamiento, nombrePaciente);
    }

    /**
     * Fila de tratamiento dentro del estado de cuenta. El nombre del odontologo
     * no se resuelve aqui porque este servicio solo mira la parte economica;
     * el detalle completo esta en GET /api/tratamientos/{id}.
     */
    private TratamientoResponse aRespuestaTratamiento(Tratamiento tratamiento) {
        String nombrePaciente = pacienteRepository.buscarPorId(tratamiento.getPacienteId())
                .map(Paciente::getNombreCompleto)
                .orElse(null);

        return TratamientoResponse.desde(tratamiento, nombrePaciente, null, pagadoDe(tratamiento),
                saldoDe(tratamiento));
    }
}
