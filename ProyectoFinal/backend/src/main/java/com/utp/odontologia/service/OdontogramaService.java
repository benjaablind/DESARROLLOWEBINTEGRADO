package com.utp.odontologia.service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.OdontogramaHistorialResponse;
import com.utp.odontologia.dto.OdontogramaPiezaResponse;
import com.utp.odontologia.dto.OdontogramaRegistroRequest;
import com.utp.odontologia.dto.OdontogramaResponse;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.EstadoPieza;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.RegistroPieza;
import com.utp.odontologia.repository.OdontogramaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Reglas de negocio del odontograma (Integrante 4).
 *
 * Regla central del modulo: cada cambio de estado de una pieza crea un registro
 * nuevo y nunca modifica los anteriores. El estado actual de una pieza es su
 * ultimo registro y el historial son todos sus registros. Por eso este servicio
 * no ofrece actualizar(...) ni eliminar(...).
 */
@Service
public class OdontogramaService {

    /**
     * Orden de mas reciente a mas antiguo. Se desempata por id descendente
     * porque dos registros creados en la misma milesima de segundo comparten
     * fecha, y en ese caso el ultimo insertado es el mas nuevo.
     */
    private static final Comparator<RegistroPieza> MAS_RECIENTE_PRIMERO =
            Comparator.comparing(RegistroPieza::getFecha)
                    .thenComparing(RegistroPieza::getId)
                    .reversed();

    private final OdontogramaRepository odontogramaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public OdontogramaService(OdontogramaRepository odontogramaRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository) {
        this.odontogramaRepository = odontogramaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Odontograma actual del paciente: el ultimo registro de cada pieza,
     * ordenado por numero de pieza.
     *
     * @throws RecursoNoEncontradoException si el paciente no existe
     */
    public OdontogramaResponse obtenerActual(Long pacienteId) {
        Paciente paciente = obtenerPaciente(pacienteId);
        List<RegistroPieza> registros = odontogramaRepository.listarPorPaciente(pacienteId);

        List<OdontogramaPiezaResponse> piezas = registros.stream()
                .collect(Collectors.groupingBy(RegistroPieza::getNumeroPieza))
                .values().stream()
                .map(historial -> historial.stream()
                        .sorted(MAS_RECIENTE_PRIMERO)
                        .findFirst()
                        .orElseThrow())
                .sorted(Comparator.comparingInt(RegistroPieza::getNumeroPieza))
                .map(OdontogramaPiezaResponse::desde)
                .toList();

        return new OdontogramaResponse(pacienteId, paciente.getNombreCompleto(), piezas,
                registros.size());
    }

    /**
     * Historial completo de una pieza, del registro mas reciente al mas antiguo.
     *
     * @throws RecursoNoEncontradoException si el paciente no existe
     * @throws ReglaNegocioException        si el numero de pieza no es FDI
     */
    public OdontogramaHistorialResponse historialDePieza(Long pacienteId, int numeroPieza) {
        obtenerPaciente(pacienteId);
        validarNumeroPieza(numeroPieza);

        List<OdontogramaPiezaResponse> registros =
                odontogramaRepository.listarHistorialDePieza(pacienteId, numeroPieza).stream()
                        .sorted(MAS_RECIENTE_PRIMERO)
                        .map(OdontogramaPiezaResponse::desde)
                        .toList();

        return new OdontogramaHistorialResponse(pacienteId, numeroPieza, registros);
    }

    /**
     * Registra un estado nuevo de una pieza. Siempre inserta: los registros
     * anteriores de la pieza quedan intactos y forman su historial.
     */
    public OdontogramaPiezaResponse registrar(OdontogramaRegistroRequest request) {
        obtenerPaciente(request.pacienteId());
        validarOdontologoExiste(request.odontologoId());
        validarNumeroPieza(request.numeroPieza());

        RegistroPieza registro = new RegistroPieza();
        registro.setPacienteId(request.pacienteId());
        registro.setOdontologoId(request.odontologoId());
        registro.setNumeroPieza(request.numeroPieza());
        registro.setEstado(request.estado());
        registro.setSuperficie(request.superficie());
        registro.setDiagnostico(request.diagnostico());
        registro.setTratamiento(request.tratamiento());
        registro.setObservaciones(request.observaciones());

        return OdontogramaPiezaResponse.desde(odontogramaRepository.guardar(registro));
    }

    /**
     * Cuenta cuantas piezas hay en cada estado segun el odontograma actual.
     * Solo aparecen los estados que tienen al menos una pieza.
     */
    public Map<EstadoPieza, Long> resumenEstados(Long pacienteId) {
        Map<EstadoPieza, Long> resumen = new EnumMap<>(EstadoPieza.class);

        for (OdontogramaPiezaResponse pieza : obtenerActual(pacienteId).piezas()) {
            resumen.merge(pieza.estado(), 1L, Long::sum);
        }

        return resumen;
    }

    private Paciente obtenerPaciente(Long pacienteId) {
        return pacienteRepository.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("paciente", pacienteId));
    }

    private void validarOdontologoExiste(Long odontologoId) {
        if (!usuarioRepository.existe(odontologoId)) {
            throw new RecursoNoEncontradoException("odontologo", odontologoId);
        }
    }

    /**
     * Acepta unicamente los numeros de la notacion FDI de dos digitos.
     *
     * Denticion permanente: cuadrantes 1 a 4 y piezas 1 a 8 (11-18, 21-28,
     * 31-38, 41-48). Denticion decidua: cuadrantes 5 a 8 y piezas 1 a 5 (51-55,
     * 61-65, 71-75, 81-85). Cualquier otro numero, como 19, 50 o 99, no
     * corresponde a ninguna pieza real.
     */
    private void validarNumeroPieza(int numeroPieza) {
        int cuadrante = numeroPieza / 10;
        int pieza = numeroPieza % 10;

        boolean permanente = cuadrante >= 1 && cuadrante <= 4 && pieza >= 1 && pieza <= 8;
        boolean decidua = cuadrante >= 5 && cuadrante <= 8 && pieza >= 1 && pieza <= 5;

        if (!permanente && !decidua) {
            throw new ReglaNegocioException(
                    "El numero de pieza " + numeroPieza + " no es valido en la notacion FDI");
        }
    }
}
