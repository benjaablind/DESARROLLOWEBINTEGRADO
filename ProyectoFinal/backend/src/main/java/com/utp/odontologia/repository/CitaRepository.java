package com.utp.odontologia.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;

/** Repositorio de citas (Integrante 3). */
public interface CitaRepository extends RepositorioBase<Cita> {

    List<Cita> findByEstadoOrderById(EstadoCita estado);

    /** Citas ordenadas cronologicamente. */
    @Query("SELECT c FROM Cita c ORDER BY c.fechaHora, c.id")
    List<Cita> listarPorFecha();

    @Query("SELECT c FROM Cita c WHERE c.pacienteId = :pacienteId ORDER BY c.id")
    List<Cita> listarPorPaciente(@Param("pacienteId") Long pacienteId);

    @Query("SELECT c FROM Cita c WHERE c.odontologoId = :odontologoId ORDER BY c.id")
    List<Cita> listarPorOdontologo(@Param("odontologoId") Long odontologoId);

    @Query("""
            SELECT c
            FROM Cita c
            WHERE c.fechaHora >= :inicio AND c.fechaHora < :fin
            ORDER BY c.id
            """)
    List<Cita> iniciadasEntre(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime finExclusivo);

    /** Citas cuyo inicio cae dentro del rango, inclusive en ambos extremos. */
    @Query("""
            SELECT c
            FROM Cita c
            WHERE c.fechaHora BETWEEN :desde AND :hasta
            ORDER BY c.fechaHora, c.id
            """)
    List<Cita> listarEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    default List<Cita> listarPorEstado(EstadoCita estado) {
        return findByEstadoOrderById(estado);
    }

    default List<Cita> listarPorDia(LocalDate dia) {
        return iniciadasEntre(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }
}
