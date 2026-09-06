package com.utp.odontologia.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.HistoriaAnularRequest;
import com.utp.odontologia.dto.HistoriaClinicaRequest;
import com.utp.odontologia.dto.HistoriaClinicaResponse;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.HistoriaClinica;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.HistoriaClinicaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Reglas de negocio de la historia clinica (Integrante 4).
 *
 * Regla central del modulo: la informacion historica no se sobrescribe ni se
 * borra. Una consulta se crea una sola vez y, si se registro por error, se anula
 * dejando constancia del motivo. El registro permanece en el historial.
 *
 * Por esa razon esta clase NO tiene los metodos actualizar(...) ni eliminar(...)
 * que si existen en otros modulos: editar una consulta ya firmada destruiria
 * evidencia clinica y borrarla dejaria el historial del paciente incompleto.
 */
@Service
public class HistoriaClinicaService {

    private static final String RECURSO = "consulta de historia clinica";

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CitaRepository citaRepository;

    public HistoriaClinicaService(HistoriaClinicaRepository historiaClinicaRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository,
            CitaRepository citaRepository) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.citaRepository = citaRepository;
    }

    /**
     * Historial completo de un paciente, de la consulta mas reciente a la mas
     * antigua. Incluye a proposito las consultas anuladas: siguen formando parte
     * del historial clinico.
     *
     * @throws RecursoNoEncontradoException si el paciente no existe
     */
    public List<HistoriaClinicaResponse> listarPorPaciente(Long pacienteId) {
        validarPacienteExiste(pacienteId);

        return historiaClinicaRepository.listarPorPaciente(pacienteId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    /**
     * Busca una consulta por su id.
     *
     * @throws RecursoNoEncontradoException si la consulta no existe
     */
    public HistoriaClinicaResponse buscarPorId(Long id) {
        return aRespuesta(obtenerEntidad(id));
    }

    /**
     * Registra una consulta nueva.
     *
     * Valida que el paciente y el odontologo existan. Si se envia una cita,
     * valida que exista y que sea del mismo paciente, para que la consulta no
     * quede colgada de la cita de otra persona.
     */
    public HistoriaClinicaResponse crear(HistoriaClinicaRequest request) {
        validarPacienteExiste(request.pacienteId());
        validarOdontologoExiste(request.odontologoId());
        validarCita(request.citaId(), request.pacienteId());

        HistoriaClinica historia = new HistoriaClinica();
        historia.setPacienteId(request.pacienteId());
        historia.setOdontologoId(request.odontologoId());
        historia.setCitaId(request.citaId());
        historia.setMotivoConsulta(request.motivoConsulta());
        historia.setAnamnesis(request.anamnesis());
        historia.setExamenClinico(request.examenClinico());
        historia.setDiagnostico(request.diagnostico());
        historia.setProcedimiento(request.procedimiento());
        historia.setTratamiento(request.tratamiento());
        historia.setMedicamentos(request.medicamentos());
        historia.setObservaciones(request.observaciones());

        return aRespuesta(historiaClinicaRepository.guardar(historia));
    }

    /**
     * Anula una consulta registrada por error. Es la unica forma de corregir el
     * historial: el contenido clinico no se toca y la consulta sigue listandose.
     *
     * @throws ReglaNegocioException si la consulta ya estaba anulada
     */
    public HistoriaClinicaResponse anular(Long id, HistoriaAnularRequest request) {
        HistoriaClinica historia = obtenerEntidad(id);

        if (historia.isAnulada()) {
            throw new ReglaNegocioException("La consulta ya fue anulada");
        }

        historia.setAnulada(true);
        historia.setMotivoAnulacion(request.motivo());

        return aRespuesta(historiaClinicaRepository.guardar(historia));
    }

    // No existe actualizar(...) ni eliminar(...) en este servicio.
    // La historia clinica es un documento legal: una vez registrada una consulta
    // su contenido no se sobrescribe y el registro no se borra. La correccion de
    // un error se hace con anular(...), que conserva el dato original.

    private HistoriaClinica obtenerEntidad(Long id) {
        return historiaClinicaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(RECURSO, id));
    }

    private void validarPacienteExiste(Long pacienteId) {
        if (!pacienteRepository.existe(pacienteId)) {
            throw new RecursoNoEncontradoException("paciente", pacienteId);
        }
    }

    private void validarOdontologoExiste(Long odontologoId) {
        if (!usuarioRepository.existe(odontologoId)) {
            throw new RecursoNoEncontradoException("odontologo", odontologoId);
        }
    }

    /** La cita es opcional, pero si se envia debe existir y ser del paciente. */
    private void validarCita(Long citaId, Long pacienteId) {
        if (citaId == null) {
            return;
        }

        Cita cita = citaRepository.buscarPorId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("cita", citaId));

        if (!pacienteId.equals(cita.getPacienteId())) {
            throw new ReglaNegocioException(
                    "La cita " + citaId + " no pertenece al paciente " + pacienteId);
        }
    }

    /** Resuelve los nombres del paciente y del odontologo para la respuesta. */
    private HistoriaClinicaResponse aRespuesta(HistoriaClinica historia) {
        String pacienteNombre = pacienteRepository.buscarPorId(historia.getPacienteId())
                .map(p -> p.getNombreCompleto())
                .orElse(null);

        String odontologoNombre = usuarioRepository.buscarPorId(historia.getOdontologoId())
                .map(u -> u.getNombreCompleto())
                .orElse(null);

        return HistoriaClinicaResponse.desde(historia, pacienteNombre, odontologoNombre);
    }
}
