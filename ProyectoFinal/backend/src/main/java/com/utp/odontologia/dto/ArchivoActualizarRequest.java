package com.utp.odontologia.dto;

import com.utp.odontologia.model.TipoArchivo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos editables de un archivo clinico (Integrante 6).
 * Solo se corrigen los metadatos: el binario ya subido nunca se reemplaza.
 */
public record ArchivoActualizarRequest(

        @NotNull(message = "El tipo de archivo es obligatorio")
        TipoArchivo tipo,

        @Size(max = 300, message = "La descripcion no puede superar los 300 caracteres")
        String descripcion,

        Long historiaClinicaId) {
}
