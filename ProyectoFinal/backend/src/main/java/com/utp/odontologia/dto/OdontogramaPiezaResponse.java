package com.utp.odontologia.dto;

import java.time.LocalDateTime;

import com.utp.odontologia.model.EstadoPieza;
import com.utp.odontologia.model.RegistroPieza;

/**
 * Vista de salida de un registro de pieza dental (Integrante 4).
 *
 * En el odontograma actual representa el estado vigente de la pieza; dentro del
 * historial representa uno de los estados por los que paso.
 */
public record OdontogramaPiezaResponse(
        int numeroPieza,
        EstadoPieza estado,
        String superficie,
        String diagnostico,
        String tratamiento,
        String observaciones,
        LocalDateTime fecha,
        Long odontologoId,
        Long registroId) {

    /** Convierte la entidad del dominio en su vista de salida. */
    public static OdontogramaPiezaResponse desde(RegistroPieza registro) {
        return new OdontogramaPiezaResponse(
                registro.getNumeroPieza(),
                registro.getEstado(),
                registro.getSuperficie(),
                registro.getDiagnostico(),
                registro.getTratamiento(),
                registro.getObservaciones(),
                registro.getFecha(),
                registro.getOdontologoId(),
                registro.getId());
    }
}
