package com.utp.odontologia.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.SesionTratamiento;
import com.utp.odontologia.model.Tratamiento;

/**
 * Vista de un tratamiento para el cliente (Integrante 5).
 * Trae resueltos los nombres del paciente y del odontologo y el resumen
 * economico (total pagado y saldo pendiente) para evitar llamadas extra.
 */
public record TratamientoResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long odontologoId,
        String odontologoNombre,
        String nombre,
        String descripcion,
        double precio,
        EstadoTratamiento estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String observaciones,
        List<SesionTratamiento> sesiones,
        double totalPagado,
        double saldoPendiente,
        LocalDateTime fechaRegistro) {

    /** Convierte el modelo interno en la respuesta que ve el cliente. */
    public static TratamientoResponse desde(Tratamiento tratamiento, String pacienteNombre,
            String odontologoNombre, double totalPagado, double saldoPendiente) {

        return new TratamientoResponse(
                tratamiento.getId(),
                tratamiento.getPacienteId(),
                pacienteNombre == null ? Textos.SIN_DATO : pacienteNombre,
                tratamiento.getOdontologoId(),
                odontologoNombre == null ? Textos.SIN_DATO : odontologoNombre,
                tratamiento.getNombre(),
                tratamiento.getDescripcion(),
                tratamiento.getPrecio(),
                tratamiento.getEstado(),
                tratamiento.getFechaInicio(),
                tratamiento.getFechaFin(),
                tratamiento.getObservaciones(),
                List.copyOf(tratamiento.getSesiones()),
                totalPagado,
                saldoPendiente,
                tratamiento.getFechaRegistro());
    }
}
