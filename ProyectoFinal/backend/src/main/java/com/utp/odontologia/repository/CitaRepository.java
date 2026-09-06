package com.utp.odontologia.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;

/** Repositorio de citas (Integrante 3). */
@Repository
public class CitaRepository extends RepositorioEnMemoria<Cita> {

    @Override
    protected Long obtenerId(Cita entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Cita entidad, Long id) {
        entidad.setId(id);
    }

    /** Citas ordenadas cronologicamente. */
    public List<Cita> listarPorFecha() {
        return listar().stream()
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .toList();
    }

    public List<Cita> listarPorPaciente(Long pacienteId) {
        return listarSi(c -> pacienteId.equals(c.getPacienteId()));
    }

    public List<Cita> listarPorOdontologo(Long odontologoId) {
        return listarSi(c -> odontologoId.equals(c.getOdontologoId()));
    }

    public List<Cita> listarPorEstado(EstadoCita estado) {
        return listarSi(c -> c.getEstado() == estado);
    }

    public List<Cita> listarPorDia(LocalDate dia) {
        return listarSi(c -> c.getFechaHora().toLocalDate().equals(dia));
    }

    /** Citas cuyo inicio cae dentro del rango, inclusive en ambos extremos. */
    public List<Cita> listarEntre(LocalDateTime desde, LocalDateTime hasta) {
        return listar().stream()
                .filter(c -> !c.getFechaHora().isBefore(desde) && !c.getFechaHora().isAfter(hasta))
                .sorted(Comparator.comparing(Cita::getFechaHora))
                .toList();
    }
}
