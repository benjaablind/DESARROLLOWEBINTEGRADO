package com.utp.odontologia.repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.Pago;

/** Repositorio de pagos (Integrante 5). */
@Repository
public class PagoRepository extends RepositorioEnMemoria<Pago> {

    @Override
    protected Long obtenerId(Pago entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Pago entidad, Long id) {
        entidad.setId(id);
    }

    public List<Pago> listarPorTratamiento(Long tratamientoId) {
        return listar().stream()
                .filter(p -> tratamientoId.equals(p.getTratamientoId()))
                .sorted(Comparator.comparing(Pago::getFecha))
                .toList();
    }

    public List<Pago> listarPorPaciente(Long pacienteId) {
        return listarSi(p -> pacienteId.equals(p.getPacienteId()));
    }

    public List<Pago> listarEntre(LocalDate desde, LocalDate hasta) {
        return listarSi(p -> {
            LocalDate fecha = p.getFecha().toLocalDate();
            return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
        });
    }

    /** Suma de todos los pagos registrados contra un tratamiento. */
    public double totalPagadoDeTratamiento(Long tratamientoId) {
        return listarPorTratamiento(tratamientoId).stream().mapToDouble(Pago::getMonto).sum();
    }
}
