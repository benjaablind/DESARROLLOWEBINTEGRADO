package com.utp.odontologia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.TipoArchivo;

/** Repositorio de metadatos de archivos clinicos (Integrante 6). */
public interface ArchivoRepository extends RepositorioBase<Archivo> {

    List<Archivo> findByTipoOrderById(TipoArchivo tipo);

    /** Archivos de un paciente, del mas reciente al mas antiguo. */
    @Query("""
            SELECT a
            FROM Archivo a
            WHERE a.pacienteId = :pacienteId
            ORDER BY a.fechaSubida DESC, a.id DESC
            """)
    List<Archivo> listarPorPaciente(@Param("pacienteId") Long pacienteId);

    @Query("SELECT a FROM Archivo a WHERE a.historiaClinicaId = :historiaClinicaId ORDER BY a.id")
    List<Archivo> listarPorHistoriaClinica(@Param("historiaClinicaId") Long historiaClinicaId);

    default List<Archivo> listarPorTipo(TipoArchivo tipo) {
        return findByTipoOrderById(tipo);
    }
}
