package com.utp.odontologia.dto;

import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.Tratamiento;

/** Fila compacta de tratamiento con su saldo pendiente (Integrante 3). */
public record DashboardTratamientoResumen(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        String nombre,
        EstadoTratamiento estado,
        double precio,
        double saldoPendiente) {

    public static DashboardTratamientoResumen desde(Tratamiento tratamiento, String pacienteNombre,
            double saldoPendiente) {
        return new DashboardTratamientoResumen(
                tratamiento.getId(),
                tratamiento.getPacienteId(),
                pacienteNombre == null ? Textos.SIN_DATO : pacienteNombre,
                tratamiento.getNombre(),
                tratamiento.getEstado(),
                tratamiento.getPrecio(),
                saldoPendiente);
    }
}
