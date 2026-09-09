package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.TipoArchivo;

/**
 * Vista de salida de un archivo clinico (Integrante 6).
 *
 * Nunca expone la ubicacion fisica del archivo: el cliente solo recibe la URL
 * logica de descarga, de modo que el binario siempre pasa por el controlador.
 */
public record ArchivoResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long usuarioId,
        String usuarioNombre,
        Long historiaClinicaId,
        TipoArchivo tipo,
        String nombre,
        String nombreOriginal,
        String extension,
        String contentType,
        long tamanoBytes,
        String tamanoLegible,
        String descripcion,
        LocalDateTime fechaSubida,
        String urlDescarga) {

    /**
     * Convierte la entidad en su representacion de salida.
     *
     * @param archivo        metadatos guardados
     * @param pacienteNombre nombre completo del paciente, puede ser null
     * @param usuarioNombre  nombre completo de quien subio el archivo, puede ser null
     */
    public static ArchivoResponse desde(Archivo archivo, String pacienteNombre,
            String usuarioNombre) {

        return new ArchivoResponse(
                archivo.getId(),
                archivo.getPacienteId(),
                pacienteNombre,
                archivo.getUsuarioId(),
                usuarioNombre,
                archivo.getHistoriaClinicaId(),
                archivo.getTipo(),
                archivo.getNombre(),
                archivo.getNombreOriginal(),
                archivo.getExtension(),
                archivo.getContentType(),
                archivo.getTamanoBytes(),
                archivo.getTamanoLegible(),
                archivo.getDescripcion(),
                archivo.getFechaSubida(),
                "/api/archivos/" + archivo.getId() + "/descargar");
    }
}
