package com.utp.odontologia.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.odontologia.model.HistoriaClinica;

/** Repositorio de consultas de historia clinica (Integrante 4). */
public interface HistoriaClinicaRepository extends RepositorioBase<HistoriaClinica> {

    /** Consultas de un paciente, de la mas reciente a la mas antigua. */
    @Query("""
            SELECT h
            FROM HistoriaClinica h
            WHERE h.pacienteId = :pacienteId
            ORDER BY h.fecha DESC, h.id DESC
            """)
    List<HistoriaClinica> listarPorPaciente(@Param("pacienteId") Long pacienteId);

    @Query("SELECT h FROM HistoriaClinica h WHERE h.odontologoId = :odontologoId ORDER BY h.id")
    List<HistoriaClinica> listarPorOdontologo(@Param("odontologoId") Long odontologoId);

    @Query("""
            SELECT h
            FROM HistoriaClinica h
            WHERE h.fecha BETWEEN :desde AND :hasta
            ORDER BY h.fecha, h.id
            """)
    List<HistoriaClinica> listarEntre(@Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
