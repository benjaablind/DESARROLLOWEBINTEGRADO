package com.utp.odontologia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.odontologia.model.RegistroPieza;

/** Repositorio del odontograma (Integrante 4). Guarda el historial de cada pieza. */
public interface OdontogramaRepository extends RepositorioBase<RegistroPieza> {

    /** Todos los registros de un paciente, del mas reciente al mas antiguo. */
    @Query("""
            SELECT r
            FROM RegistroPieza r
            WHERE r.pacienteId = :pacienteId
            ORDER BY r.fecha DESC, r.id DESC
            """)
    List<RegistroPieza> listarPorPaciente(@Param("pacienteId") Long pacienteId);

    /** Historial completo de una pieza, del mas reciente al mas antiguo. */
    @Query("""
            SELECT r
            FROM RegistroPieza r
            WHERE r.pacienteId = :pacienteId AND r.numeroPieza = :numeroPieza
            ORDER BY r.fecha DESC, r.id DESC
            """)
    List<RegistroPieza> listarHistorialDePieza(@Param("pacienteId") Long pacienteId,
            @Param("numeroPieza") int numeroPieza);

    /** Ultimo registro de una pieza, que representa su estado actual. */
    default Optional<RegistroPieza> buscarUltimoDePieza(Long pacienteId, int numeroPieza) {
        return listarHistorialDePieza(pacienteId, numeroPieza).stream().findFirst();
    }
}
