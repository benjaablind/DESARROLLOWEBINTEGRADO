package com.utp.odontologia.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.RegistroPieza;

/** Repositorio del odontograma (Integrante 4). Guarda el historial de cada pieza. */
@Repository
public class OdontogramaRepository extends RepositorioEnMemoria<RegistroPieza> {

    @Override
    protected Long obtenerId(RegistroPieza entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(RegistroPieza entidad, Long id) {
        entidad.setId(id);
    }

    /** Todos los registros de un paciente, del mas reciente al mas antiguo. */
    public List<RegistroPieza> listarPorPaciente(Long pacienteId) {
        return listar().stream()
                .filter(r -> pacienteId.equals(r.getPacienteId()))
                .sorted(Comparator.comparing(RegistroPieza::getFecha).reversed())
                .toList();
    }

    /** Historial completo de una pieza, del mas reciente al mas antiguo. */
    public List<RegistroPieza> listarHistorialDePieza(Long pacienteId, int numeroPieza) {
        return listarPorPaciente(pacienteId).stream()
                .filter(r -> r.getNumeroPieza() == numeroPieza)
                .toList();
    }

    /** Ultimo registro de una pieza, que representa su estado actual. */
    public Optional<RegistroPieza> buscarUltimoDePieza(Long pacienteId, int numeroPieza) {
        return listarHistorialDePieza(pacienteId, numeroPieza).stream().findFirst();
    }
}
