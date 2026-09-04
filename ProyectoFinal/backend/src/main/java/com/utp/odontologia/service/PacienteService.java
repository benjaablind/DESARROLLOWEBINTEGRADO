package com.utp.odontologia.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.utp.odontologia.dto.AntecedentesRequest;
import com.utp.odontologia.dto.PacienteRequest;
import com.utp.odontologia.exception.RecursoNoEncontradoException;
import com.utp.odontologia.exception.ReglaNegocioException;
import com.utp.odontologia.model.Antecedentes;
import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.repository.PacienteRepository;

/**
 * Reglas de negocio del modulo de pacientes (Integrante 2).
 * El controlador solo traduce HTTP: toda la validacion de negocio vive aqui.
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Lista pacientes aplicando filtros opcionales.
     *
     * @param texto  coincidencia parcial en nombre completo o DNI; puede ser null
     * @param estado estado exacto del paciente; puede ser null
     */
    public List<Paciente> listar(String texto, EstadoPaciente estado) {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);

        return pacienteRepository.listarSi(paciente -> {
            boolean coincideEstado = estado == null || paciente.getEstado() == estado;
            boolean coincideTexto = buscado.isEmpty()
                    || paciente.getNombreCompleto().toLowerCase(Locale.ROOT).contains(buscado)
                    || (paciente.getDni() != null
                            && paciente.getDni().toLowerCase(Locale.ROOT).contains(buscado));
            return coincideEstado && coincideTexto;
        });
    }

    /** Busca un paciente por id o falla con 404. */
    public Paciente buscarPorId(Long id) {
        return pacienteRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("paciente", id));
    }

    /** Busca un paciente por DNI o falla con 404. */
    public Paciente buscarPorDni(String dni) {
        return pacienteRepository.buscarPorDni(dni)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe paciente con DNI: " + dni));
    }

    /** Registra un paciente nuevo. El DNI no puede repetirse. */
    public Paciente crear(PacienteRequest request) {
        validarDniDisponible(request.dni(), null);

        Paciente paciente = new Paciente();
        copiarDatos(request, paciente);
        return pacienteRepository.guardar(paciente);
    }

    /** Actualiza todos los datos del paciente. Permite cambiar el DNI si esta libre. */
    public Paciente actualizar(Long id, PacienteRequest request) {
        Paciente paciente = buscarPorId(id);
        validarDniDisponible(request.dni(), id);

        copiarDatos(request, paciente);
        return pacienteRepository.guardar(paciente);
    }

    /** Actualizacion parcial: solo reemplaza los antecedentes clinicos. */
    public Paciente actualizarAntecedentes(Long id, AntecedentesRequest request) {
        Paciente paciente = buscarPorId(id);
        paciente.setAntecedentes(convertir(request));
        return pacienteRepository.guardar(paciente);
    }

    /** Activa o desactiva al paciente sin borrar su informacion. */
    public Paciente cambiarEstado(Long id, EstadoPaciente estado) {
        if (estado == null) {
            throw new ReglaNegocioException("El estado es obligatorio");
        }
        Paciente paciente = buscarPorId(id);
        paciente.setEstado(estado);
        return pacienteRepository.guardar(paciente);
    }

    /** Elimina al paciente o falla con 404 si no existe. */
    public void eliminar(Long id) {
        if (!pacienteRepository.eliminar(id)) {
            throw new RecursoNoEncontradoException("paciente", id);
        }
    }

    /** Total de pacientes registrados. Lo usa el tablero de indicadores. */
    public long contar() {
        return pacienteRepository.contar();
    }

    /** Rechaza el DNI si ya pertenece a otro paciente distinto del indicado. */
    private void validarDniDisponible(String dni, Long idPropio) {
        Optional<Paciente> existente = pacienteRepository.buscarPorDni(dni);
        if (existente.isPresent() && !existente.get().getId().equals(idPropio)) {
            throw new ReglaNegocioException(
                    "Ya existe un paciente registrado con el DNI " + dni);
        }
    }

    /** Vuelca los datos del request sobre la entidad, sin tocar id ni fechaRegistro. */
    private void copiarDatos(PacienteRequest request, Paciente paciente) {
        paciente.setDni(request.dni());
        paciente.setNombres(request.nombres());
        paciente.setApellidos(request.apellidos());
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setSexo(request.sexo());
        paciente.setTelefono(request.telefono());
        paciente.setEmail(request.email());
        paciente.setDireccion(request.direccion());
        paciente.setDistrito(request.distrito());
        paciente.setCiudad(request.ciudad());

        if (request.antecedentes() != null) {
            paciente.setAntecedentes(convertir(request.antecedentes()));
        }
    }

    /** Convierte el DTO de antecedentes al objeto del modelo, nunca devuelve null. */
    private Antecedentes convertir(AntecedentesRequest request) {
        Antecedentes antecedentes = new Antecedentes();
        if (request == null) {
            return antecedentes;
        }
        antecedentes.setEnfermedades(request.enfermedades());
        antecedentes.setAlergias(request.alergias());
        antecedentes.setMedicamentos(request.medicamentos());
        antecedentes.setHabitos(request.habitos());
        antecedentes.setAntecedentesOdontologicos(request.antecedentesOdontologicos());
        antecedentes.setObservaciones(request.observaciones());
        return antecedentes;
    }
}
