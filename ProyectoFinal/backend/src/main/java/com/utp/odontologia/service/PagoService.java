package com.utp.odontologia.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

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
 */
@Service
public class PagoService {

    /** Tolerancia para comparar importes en coma flotante (un centimo). */
    private static final double TOLERANCIA = 0.001;

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
    public List<PagoResponse> listar(Long pacienteId, Long tratamientoId) {
        List<Pago> encontrados = pagoRepository.listarSi(pago -> {
            boolean coincidePaciente = pacienteId == null
                    || pacienteId.equals(pago.getPacienteId());
            boolean coincideTratamiento = tratamientoId == null
                    || tratamientoId.equals(pago.getTratamientoId());
            return coincidePaciente && coincideTratamiento;
        });

        return encontrados.stream().map(this::aRespuesta).toList();
    }

    /** Busca un pago por id o falla con 404. */
    public Pago buscarPorId(Long id) {
        return pagoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("pago", id));
    }

    /** Devuelve la vista de un pago, o falla con 404. */
    public PagoResponse detalle(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    /**
     * Registra un pago contra un tratamiento.
     * El paciente se toma del tratamiento y el monto no puede exceder el saldo.
     */
    public PagoResponse registrar(PagoRequest request) {
        Tratamiento tratamiento = buscarTratamiento(request.tratamientoId());

        if (tratamiento.getEstado() == EstadoTratamiento.CANCELADO) {
            throw new ReglaNegocioException(
                    "No se puede registrar un pago de un tratamiento CANCELADO");
        }

        double saldo = saldoDe(tratamiento);
        double monto = request.monto();

        if (monto - saldo > TOLERANCIA) {
            throw new ReglaNegocioException(
                    "El monto excede el saldo pendiente del tratamiento: saldo actual S/ "
                            + String.format(Locale.ROOT, "%.2f", saldo));
        }

        Pago pago = new Pago();
        pago.setTratamientoId(tratamiento.getId());
        pago.setPacienteId(tratamiento.getPacienteId());
        pago.setMonto(redondear(monto));
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
    public EstadoCuentaResponse estadoDeCuenta(Long pacienteId) {
        Paciente paciente = pacienteRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("paciente", pacienteId));

        List<Tratamiento> vigentes = tratamientoRepository.listarPorPaciente(pacienteId).stream()
                .filter(tratamiento -> tratamiento.getEstado() != EstadoTratamiento.CANCELADO)
                .toList();

        double totalTratamientos = 0;
        double totalPagado = 0;
        double saldoPendiente = 0;

        for (Tratamiento tratamiento : vigentes) {
            totalTratamientos += tratamiento.getPrecio();
            totalPagado += pagoRepository.totalPagadoDeTratamiento(tratamiento.getId());
            saldoPendiente += saldoDe(tratamiento);
        }

        List<TratamientoResponse> detalle = vigentes.stream()
                .map(this::aRespuestaTratamiento)
                .toList();

        return new EstadoCuentaResponse(
                paciente.getId(),
                paciente.getNombreCompleto(),
                redondear(totalTratamientos),
                redondear(totalPagado),
                redondear(saldoPendiente),
                detalle);
    }

    private Tratamiento buscarTratamiento(Long tratamientoId) {
        return tratamientoRepository.buscarPorId(tratamientoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("tratamiento", tratamientoId));
    }

    /** Saldo del tratamiento: precio menos lo pagado, nunca negativo. */
    private double saldoDe(Tratamiento tratamiento) {
        double pagado = pagoRepository.totalPagadoDeTratamiento(tratamiento.getId());
        return redondear(Math.max(0, tratamiento.getPrecio() - pagado));
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
        double pagado = redondear(pagoRepository.totalPagadoDeTratamiento(tratamiento.getId()));
        String nombrePaciente = pacienteRepository.buscarPorId(tratamiento.getPacienteId())
                .map(Paciente::getNombreCompleto)
                .orElse(null);

        return TratamientoResponse.desde(tratamiento, nombrePaciente, null, pagado,
                saldoDe(tratamiento));
    }

    /** Redondeo monetario a 2 decimales, comun a todos los importes del modulo. */
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
