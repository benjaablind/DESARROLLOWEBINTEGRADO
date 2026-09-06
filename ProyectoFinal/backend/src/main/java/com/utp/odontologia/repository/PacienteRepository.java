package com.utp.odontologia.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;

/** Repositorio de pacientes (Integrante 2). */
@Repository
public class PacienteRepository extends RepositorioEnMemoria<Paciente> {

    @Override
    protected Long obtenerId(Paciente entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Paciente entidad, Long id) {
        entidad.setId(id);
    }

    public Optional<Paciente> buscarPorDni(String dni) {
        return listar().stream()
                .filter(p -> p.getDni() != null && p.getDni().equals(dni))
                .findFirst();
    }

    /** Busca por coincidencia parcial en nombres, apellidos o DNI. */
    public List<Paciente> buscarPorTexto(String texto) {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return listarSi(p -> p.getNombreCompleto().toLowerCase(Locale.ROOT).contains(buscado)
                || (p.getDni() != null && p.getDni().contains(buscado)));
    }

    public List<Paciente> listarPorEstado(EstadoPaciente estado) {
        return listarSi(p -> p.getEstado() == estado);
    }

    /** Pacientes registrados dentro de un rango de fechas, inclusive. */
    public List<Paciente> listarPorRangoDeRegistro(LocalDate desde, LocalDate hasta) {
        return listarSi(p -> {
            LocalDate fecha = p.getFechaRegistro().toLocalDate();
            return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
        });
    }
}
