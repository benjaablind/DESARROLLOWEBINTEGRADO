package com.utp.odontologia.repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.HistoriaClinica;

/** Repositorio de consultas de historia clinica (Integrante 4). */
@Repository
public class HistoriaClinicaRepository extends RepositorioEnMemoria<HistoriaClinica> {

    @Override
    protected Long obtenerId(HistoriaClinica entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(HistoriaClinica entidad, Long id) {
        entidad.setId(id);
    }

    /** Consultas de un paciente, de la mas reciente a la mas antigua. */
    public List<HistoriaClinica> listarPorPaciente(Long pacienteId) {
        return listar().stream()
                .filter(h -> pacienteId.equals(h.getPacienteId()))
                .sorted(Comparator.comparing(HistoriaClinica::getFecha).reversed())
                .toList();
    }

    public List<HistoriaClinica> listarPorOdontologo(Long odontologoId) {
        return listarSi(h -> odontologoId.equals(h.getOdontologoId()));
    }

    public List<HistoriaClinica> listarEntre(LocalDateTime desde, LocalDateTime hasta) {
        return listar().stream()
                .filter(h -> !h.getFecha().isBefore(desde) && !h.getFecha().isAfter(hasta))
                .sorted(Comparator.comparing(HistoriaClinica::getFecha))
                .toList();
    }
}
