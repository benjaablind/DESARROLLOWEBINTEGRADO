package com.utp.odontologia.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.CitaEstadoRequest;
import com.utp.odontologia.dto.CitaReprogramarRequest;
import com.utp.odontologia.dto.CitaRequest;
import com.utp.odontologia.dto.CitaResponse;
import com.utp.odontologia.dto.Textos;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Reglas de negocio de las citas y la agenda (Integrante 3).
 *
 * Concentra la validacion de cruces de horario y las transiciones de estado para
 * que el controlador solo se ocupe del protocolo HTTP.
 */
@Service
public class CitaService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaService(CitaRepository citaRepository, PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista las citas aplicando los filtros recibidos. Todos son opcionales y
     * combinables: un filtro en null simplemente no se aplica.
     */
    public List<CitaResponse> listar(LocalDate dia, Long pacienteId, Long odontologoId,
            EstadoCita estado) {

        return citaRepository.listar().stream()
                .filter(c -> dia == null || dia.equals(c.getFechaHora().toLocalDate()))
                .filter(c -> pacienteId == null || pacienteId.equals(c.getPacienteId()))
                .filter(c -> odontologoId == null || odontologoId.equals(c.getOdontologoId()))
                .filter(c -> estado == null || estado == c.getEstado())
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .map(this::aRespuesta)
                .toList();
    }

    public CitaResponse buscarPorId(Long id) {
        return aRespuesta(obtener(id));
    }

    /** Registra una cita nueva validando que no se cruce con otra del mismo odontologo. */
    public CitaResponse crear(CitaRequest peticion) {
        validarReferencias(peticion);
        validarCruce(peticion.odontologoId(), peticion.fechaHora(), peticion.duracionEfectiva(),
                null);

        Cita cita = new Cita();
        copiarDatos(peticion, cita);
        cita.setEstado(EstadoCita.PENDIENTE);
        return aRespuesta(citaRepository.guardar(cita));
    }

    /** Actualiza una cita existente; su propio horario no cuenta como cruce. */
    public CitaResponse actualizar(Long id, CitaRequest peticion) {
        Cita cita = obtener(id);
        validarReferencias(peticion);
        validarCruce(peticion.odontologoId(), peticion.fechaHora(), peticion.duracionEfectiva(), id);

        copiarDatos(peticion, cita);
        return aRespuesta(citaRepository.guardar(cita));
    }

    /** Cambio manual de estado. Una cita cancelada o atendida ya no se mueve. */
    public CitaResponse cambiarEstado(Long id, CitaEstadoRequest peticion) {
        Cita cita = obtener(id);
        validarEstadoModificable(cita);

        cita.setEstado(peticion.estado());
        if (peticion.observaciones() != null && !peticion.observaciones().isBlank()) {
            cita.setObservaciones(peticion.observaciones());
        }
        return aRespuesta(citaRepository.guardar(cita));
    }

    /** Solo una cita pendiente puede confirmarse. */
    public CitaResponse confirmar(Long id) {
        Cita cita = obtener(id);
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new ReglaNegocioException("Solo se puede confirmar una cita en estado PENDIENTE");
        }
        cita.setEstado(EstadoCita.CONFIRMADA);
        return aRespuesta(citaRepository.guardar(cita));
    }

    /** Cierra la cita como ATENDIDA o como NO_ASISTIO segun lo ocurrido. */
    public CitaResponse registrarAsistencia(Long id, boolean asistio) {
        Cita cita = obtener(id);
        if (cita.getEstado() == EstadoCita.CANCELADA
                || cita.getEstado() == EstadoCita.REPROGRAMADA) {
            throw new ReglaNegocioException(
                    "No se puede registrar la asistencia de una cita " + cita.getEstado());
        }
        cita.setEstado(asistio ? EstadoCita.ATENDIDA : EstadoCita.NO_ASISTIO);
        return aRespuesta(citaRepository.guardar(cita));
    }

    /**
     * Marca la cita original como REPROGRAMADA y crea una cita nueva con los
     * mismos datos y la fecha indicada. Devuelve la cita nueva.
     */
    public CitaResponse reprogramar(Long id, CitaReprogramarRequest peticion) {
        Cita original = obtener(id);
        validarEstadoModificable(original);
        validarCruce(original.getOdontologoId(), peticion.nuevaFechaHora(),
                original.getDuracionMinutos(), original.getId());

        Cita nueva = new Cita();
        nueva.setPacienteId(original.getPacienteId());
        nueva.setOdontologoId(original.getOdontologoId());
        nueva.setFechaHora(peticion.nuevaFechaHora());
        nueva.setDuracionMinutos(original.getDuracionMinutos());
        nueva.setMotivo(original.getMotivo());
        nueva.setEstado(EstadoCita.PENDIENTE);
        nueva.setCitaOriginalId(original.getId());
        if (peticion.motivo() != null && !peticion.motivo().isBlank()) {
            nueva.setObservaciones(peticion.motivo());
        }
        Cita guardada = citaRepository.guardar(nueva);

        original.setEstado(EstadoCita.REPROGRAMADA);
        original.setObservaciones(textoDeReprogramacion(peticion.motivo(), guardada.getId()));
        citaRepository.guardar(original);

        return aRespuesta(guardada);
    }

    /** Cancela la cita y deja el motivo en las observaciones. */
    public CitaResponse cancelar(Long id, String motivo) {
        Cita cita = obtener(id);
        validarEstadoModificable(cita);

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setObservaciones(motivo == null || motivo.isBlank() ? "Cita cancelada" : motivo);
        return aRespuesta(citaRepository.guardar(cita));
    }

    public void eliminar(Long id) {
        obtener(id);
        citaRepository.eliminar(id);
    }

    /** Agenda de un dia completo, ordenada por hora. */
    public List<CitaResponse> agendaDelDia(LocalDate dia) {
        return citaRepository.listarPorDia(dia).stream()
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .map(this::aRespuesta)
                .toList();
    }

    /**
     * Horas de inicio ya ocupadas por un odontologo en un dia, en formato HH:mm.
     * Las citas canceladas y reprogramadas liberan el horario, por eso no cuentan.
     */
    public List<String> disponibilidad(Long odontologoId, LocalDate dia) {
        return citaRepository.listarPorDia(dia).stream()
                .filter(c -> odontologoId != null && odontologoId.equals(c.getOdontologoId()))
                .filter(CitaService::ocupaHorario)
                .map(Cita::getFechaHora)
                .sorted()
                .map(fecha -> fecha.format(HORA))
                .distinct()
                .toList();
    }

    // ------------------------------------------------------------- privados

    private Cita obtener(Long id) {
        return citaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("cita", id));
    }

    private void validarReferencias(CitaRequest peticion) {
        if (!pacienteRepository.existe(peticion.pacienteId())) {
            throw new RecursoNoEncontradoException("paciente", peticion.pacienteId());
        }
        if (!usuarioRepository.existe(peticion.odontologoId())) {
            throw new RecursoNoEncontradoException("odontologo", peticion.odontologoId());
        }
    }

    /**
     * Dos intervalos se cruzan si inicioA es anterior a finB e inicioB es anterior
     * a finA. Las citas canceladas o reprogramadas no bloquean la agenda.
     */
    private void validarCruce(Long odontologoId, LocalDateTime inicio, int duracionMinutos,
            Long idExcluido) {

        LocalDateTime fin = inicio.plusMinutes(duracionMinutos);

        boolean cruce = citaRepository.listarPorOdontologo(odontologoId).stream()
                .filter(c -> idExcluido == null || !idExcluido.equals(c.getId()))
                .filter(CitaService::ocupaHorario)
                .anyMatch(c -> inicio.isBefore(c.getFechaHoraFin())
                        && c.getFechaHora().isBefore(fin));

        if (cruce) {
            throw new ReglaNegocioException("El odontologo ya tiene una cita en ese horario");
        }
    }

    private void validarEstadoModificable(Cita cita) {
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.ATENDIDA) {
            throw new ReglaNegocioException(
                    "No se puede cambiar el estado de una cita " + cita.getEstado());
        }
    }

    private void copiarDatos(CitaRequest peticion, Cita cita) {
        cita.setPacienteId(peticion.pacienteId());
        cita.setOdontologoId(peticion.odontologoId());
        cita.setFechaHora(peticion.fechaHora());
        cita.setDuracionMinutos(peticion.duracionEfectiva());
        cita.setMotivo(peticion.motivo());
        cita.setObservaciones(peticion.observaciones());
    }

    private String textoDeReprogramacion(String motivo, Long nuevaCitaId) {
        String base = "Reprogramada en la cita " + nuevaCitaId;
        return motivo == null || motivo.isBlank() ? base : base + ". Motivo: " + motivo;
    }

    /** Una cita ocupa la agenda mientras no este cancelada ni reprogramada. */
    private static boolean ocupaHorario(Cita cita) {
        return cita.getEstado() != EstadoCita.CANCELADA
                && cita.getEstado() != EstadoCita.REPROGRAMADA;
    }

    /** Resuelve los nombres del paciente y del odontologo para la respuesta. */
    private CitaResponse aRespuesta(Cita cita) {
        String paciente = pacienteRepository.buscarPorId(cita.getPacienteId())
                .map(p -> p.getNombreCompleto())
                .orElse(Textos.SIN_DATO);

        String odontologo = usuarioRepository.buscarPorId(cita.getOdontologoId())
                .map(u -> u.getNombreCompleto())
                .orElse(Textos.SIN_DATO);

        return CitaResponse.desde(cita, paciente, odontologo);
    }
}
