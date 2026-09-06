package com.utp.odontologia.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.Tratamiento;

/** Repositorio de tratamientos (Integrante 5). */
@Repository
public class TratamientoRepository extends RepositorioEnMemoria<Tratamiento> {

    @Override
    protected Long obtenerId(Tratamiento entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Tratamiento entidad, Long id) {
        entidad.setId(id);
    }

    public List<Tratamiento> listarPorPaciente(Long pacienteId) {
        return listarSi(t -> pacienteId.equals(t.getPacienteId()));
    }

    public List<Tratamiento> listarPorEstado(EstadoTratamiento estado) {
        return listarSi(t -> t.getEstado() == estado);
    }

    /** Tratamientos pendientes, aprobados o en proceso. */
    public List<Tratamiento> listarActivos() {
        return listarSi(Tratamiento::estaActivo);
    }

    public List<Tratamiento> listarCompletadosEntre(LocalDate desde, LocalDate hasta) {
        return listarSi(t -> t.getEstado() == EstadoTratamiento.COMPLETADO
                && t.getFechaFin() != null
                && !t.getFechaFin().isBefore(desde)
                && !t.getFechaFin().isAfter(hasta));
    }
}
