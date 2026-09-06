package com.utp.odontologia.repository;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.TipoArchivo;

/** Repositorio de metadatos de archivos clinicos (Integrante 6). */
@Repository
public class ArchivoRepository extends RepositorioEnMemoria<Archivo> {

    @Override
    protected Long obtenerId(Archivo entidad) {
        return entidad.getId();
    }

    @Override
    protected void asignarId(Archivo entidad, Long id) {
        entidad.setId(id);
    }

    /** Archivos de un paciente, del mas reciente al mas antiguo. */
    public List<Archivo> listarPorPaciente(Long pacienteId) {
        return listar().stream()
                .filter(a -> pacienteId.equals(a.getPacienteId()))
                .sorted(Comparator.comparing(Archivo::getFechaSubida).reversed())
                .toList();
    }

    public List<Archivo> listarPorTipo(TipoArchivo tipo) {
        return listarSi(a -> a.getTipo() == tipo);
    }

    public List<Archivo> listarPorHistoriaClinica(Long historiaClinicaId) {
        return listarSi(a -> historiaClinicaId.equals(a.getHistoriaClinicaId()));
    }
}
