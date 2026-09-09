package com.utp.odontologia.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.SesionEstadoRequest;
import com.utp.odontologia.dto.SesionRequest;
import com.utp.odontologia.dto.TratamientoRequest;
import com.utp.odontologia.dto.TratamientoResponse;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.SesionTratamiento;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Reglas de negocio del modulo de tratamientos (Integrante 5).
 * Controla el ciclo de vida del tratamiento, sus sesiones y el calculo del
 * saldo pendiente a partir de los pagos registrados.
 */
@Service
public class TratamientoService {

    private final TratamientoRepository tratamientoRepository;
    private final PagoRepository pagoRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public TratamientoService(TratamientoRepository tratamientoRepository,
            PagoRepository pagoRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository) {
        this.tratamientoRepository = tratamientoRepository;
        this.pagoRepository = pagoRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista tratamientos aplicando filtros opcionales y combinables.
     *
     * @param pacienteId  solo los tratamientos de ese paciente; puede ser null
     * @param estado      estado exacto del tratamiento; puede ser null
     * @param soloActivos true para excluir los completados y cancelados; puede ser null
     */
    public List<TratamientoResponse> listar(Long pacienteId, EstadoTratamiento estado,
            Boolean soloActivos) {

        List<Tratamiento> encontrados = tratamientoRepository.listarSi(tratamiento -> {
            boolean coincidePaciente = pacienteId == null
                    || pacienteId.equals(tratamiento.getPacienteId());
            boolean coincideEstado = estado == null || tratamiento.getEstado() == estado;
            boolean coincideActivo = !Boolean.TRUE.equals(soloActivos) || tratamiento.estaActivo();
            return coincidePaciente && coincideEstado && coincideActivo;
        });

        return encontrados.stream().map(this::aRespuesta).toList();
    }

    /** Busca la entidad tratamiento por id o falla con 404. */
    public Tratamiento buscarPorId(Long id) {
        return tratamientoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("tratamiento", id));
    }

    /** Devuelve la vista completa de un tratamiento, o falla con 404. */
    public TratamientoResponse detalle(Long id) {
        return aRespuesta(buscarPorId(id));
    }

    /** Registra un tratamiento nuevo. El paciente y el odontologo deben existir. */
    public TratamientoResponse crear(TratamientoRequest request) {
        validarPaciente(request.pacienteId());
        validarOdontologo(request.odontologoId());

        Tratamiento tratamiento = new Tratamiento();
        copiarDatos(request, tratamiento);
        tratamiento.setEstado(EstadoTratamiento.PENDIENTE);

        return aRespuesta(tratamientoRepository.guardar(tratamiento));
    }

    /** Actualiza los datos del tratamiento. Uno cerrado ya no se modifica. */
    public TratamientoResponse actualizar(Long id, TratamientoRequest request) {
        Tratamiento tratamiento = buscarPorId(id);
        validarModificable(tratamiento);
        validarPaciente(request.pacienteId());
        validarOdontologo(request.odontologoId());

        copiarDatos(request, tratamiento);
        return aRespuesta(tratamientoRepository.guardar(tratamiento));
    }

    /**
     * Cambia el estado del tratamiento validando la transicion.
     * Un tratamiento COMPLETADO o CANCELADO ya no cambia de estado.
     */
    public TratamientoResponse cambiarEstado(Long id, EstadoTratamiento estado) {
        if (estado == null) {
            throw new ReglaNegocioException("El estado es obligatorio");
        }

        Tratamiento tratamiento = buscarPorId(id);
        if (esEstadoFinal(tratamiento.getEstado())) {
            throw new ReglaNegocioException(
                    "No se puede cambiar el estado de un tratamiento "
                            + tratamiento.getEstado() + ": ya es un estado final");
        }

        tratamiento.setEstado(estado);

        if (estado == EstadoTratamiento.COMPLETADO) {
            tratamiento.setFechaFin(LocalDate.now());
        }
        if (estado == EstadoTratamiento.EN_PROCESO && tratamiento.getFechaInicio() == null) {
            tratamiento.setFechaInicio(LocalDate.now());
        }

        return aRespuesta(tratamientoRepository.guardar(tratamiento));
    }

    /** Agrega una sesion al tratamiento con numero correlativo desde 1. */
    public TratamientoResponse agregarSesion(Long tratamientoId, SesionRequest request) {
        Tratamiento tratamiento = buscarPorId(tratamientoId);
        validarModificable(tratamiento);

        SesionTratamiento sesion = new SesionTratamiento();
        sesion.setId(siguienteIdDeSesion(tratamiento));
        sesion.setNumero(siguienteNumeroDeSesion(tratamiento));
        sesion.setFecha(request.fecha());
        sesion.setDescripcion(request.descripcion());
        sesion.setObservaciones(request.observaciones());
        sesion.setRealizada(false);

        tratamiento.getSesiones().add(sesion);
        return aRespuesta(tratamientoRepository.guardar(tratamiento));
    }

    /** Marca una sesion como realizada o pendiente y actualiza sus observaciones. */
    public TratamientoResponse actualizarSesion(Long tratamientoId, Long sesionId,
            SesionEstadoRequest request) {

        Tratamiento tratamiento = buscarPorId(tratamientoId);
        SesionTratamiento sesion = buscarSesion(tratamiento, sesionId);

        sesion.setRealizada(Boolean.TRUE.equals(request.realizada()));
        if (request.observaciones() != null) {
            sesion.setObservaciones(request.observaciones());
        }

        return aRespuesta(tratamientoRepository.guardar(tratamiento));
    }

    /** Elimina una sesion del tratamiento. Los numeros ya asignados no se reordenan. */
    public void eliminarSesion(Long tratamientoId, Long sesionId) {
        Tratamiento tratamiento = buscarPorId(tratamientoId);
        SesionTratamiento sesion = buscarSesion(tratamiento, sesionId);

        tratamiento.getSesiones().remove(sesion);
        tratamientoRepository.guardar(tratamiento);
    }

    /** Elimina el tratamiento siempre que no tenga pagos registrados. */
    public void eliminar(Long id) {
        Tratamiento tratamiento = buscarPorId(id);

        if (!pagoRepository.listarPorTratamiento(tratamiento.getId()).isEmpty()) {
            throw new ReglaNegocioException(
                    "No se puede eliminar un tratamiento con pagos registrados");
        }

        tratamientoRepository.eliminar(tratamiento.getId());
    }

    /** Suma de los pagos registrados contra el tratamiento, redondeada a 2 decimales. */
    public double totalPagado(Long tratamientoId) {
        return redondear(pagoRepository.totalPagadoDeTratamiento(tratamientoId));
    }

    /** Precio menos lo pagado; nunca devuelve un valor negativo. */
    public double saldoPendiente(Long tratamientoId) {
        Tratamiento tratamiento = buscarPorId(tratamientoId);
        return redondear(Math.max(0, tratamiento.getPrecio() - totalPagado(tratamientoId)));
    }

    /** Cantidad de tratamientos activos. Sirve al tablero de indicadores. */
    public long contarActivos() {
        return tratamientoRepository.listarActivos().size();
    }

    /** Arma la respuesta enriquecida con nombres y con el resumen economico. */
    public TratamientoResponse aRespuesta(Tratamiento tratamiento) {
        double pagado = redondear(pagoRepository.totalPagadoDeTratamiento(tratamiento.getId()));
        double saldo = redondear(Math.max(0, tratamiento.getPrecio() - pagado));

        return TratamientoResponse.desde(tratamiento,
                nombreDePaciente(tratamiento.getPacienteId()),
                nombreDeOdontologo(tratamiento.getOdontologoId()),
                pagado,
                saldo);
    }

    /** Un tratamiento cerrado no admite cambios de datos ni de sesiones. */
    private void validarModificable(Tratamiento tratamiento) {
        if (esEstadoFinal(tratamiento.getEstado())) {
            throw new ReglaNegocioException(
                    "No se puede modificar un tratamiento " + tratamiento.getEstado());
        }
    }

    private boolean esEstadoFinal(EstadoTratamiento estado) {
        return estado == EstadoTratamiento.COMPLETADO || estado == EstadoTratamiento.CANCELADO;
    }

    private void validarPaciente(Long pacienteId) {
        if (!pacienteRepository.existe(pacienteId)) {
            throw new RecursoNoEncontradoException("paciente", pacienteId);
        }
    }

    private void validarOdontologo(Long odontologoId) {
        if (!usuarioRepository.existe(odontologoId)) {
            throw new RecursoNoEncontradoException("odontologo", odontologoId);
        }
    }

    /** Vuelca los datos del request sobre la entidad, sin tocar estado ni sesiones. */
    private void copiarDatos(TratamientoRequest request, Tratamiento tratamiento) {
        tratamiento.setPacienteId(request.pacienteId());
        tratamiento.setOdontologoId(request.odontologoId());
        tratamiento.setNombre(request.nombre());
        tratamiento.setDescripcion(request.descripcion());
        tratamiento.setPrecio(request.precio());
        tratamiento.setFechaInicio(request.fechaInicio());
        tratamiento.setObservaciones(request.observaciones());
    }

    private SesionTratamiento buscarSesion(Tratamiento tratamiento, Long sesionId) {
        return new ArrayList<>(tratamiento.getSesiones()).stream()
                .filter(sesion -> sesion.getId() != null && sesion.getId().equals(sesionId))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("sesion", sesionId));
    }

    /** El id de sesion es local al tratamiento: el maximo existente mas uno, desde 1. */
    private Long siguienteIdDeSesion(Tratamiento tratamiento) {
        long maximo = tratamiento.getSesiones().stream()
                .filter(sesion -> sesion.getId() != null)
                .mapToLong(SesionTratamiento::getId)
                .max()
                .orElse(0L);
        return maximo + 1;
    }

    /** El numero de sesion tambien es correlativo dentro del tratamiento, desde 1. */
    private int siguienteNumeroDeSesion(Tratamiento tratamiento) {
        int maximo = tratamiento.getSesiones().stream()
                .mapToInt(SesionTratamiento::getNumero)
                .max()
                .orElse(0);
        return maximo + 1;
    }

    private String nombreDePaciente(Long pacienteId) {
        return pacienteRepository.buscarPorId(pacienteId)
                .map(Paciente::getNombreCompleto)
                .orElse(null);
    }

    private String nombreDeOdontologo(Long odontologoId) {
        return usuarioRepository.buscarPorId(odontologoId)
                .map(Usuario::getNombreCompleto)
                .orElse(null);
    }

    /** Redondeo monetario a 2 decimales, comun a todos los importes del modulo. */
    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
