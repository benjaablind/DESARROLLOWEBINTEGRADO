package com.utp.odontologia.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.utp.odontologia.model.Antecedentes;
import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;

/**
 * Vista completa de un paciente (Integrante 2).
 * Expone tambien los datos derivados nombreCompleto y edad para que el
 * frontend no tenga que calcularlos.
 */
public record PacienteResponse(
        Long id,
        String dni,
        String nombres,
        String apellidos,
        String nombreCompleto,
        Integer edad,
        LocalDate fechaNacimiento,
        String sexo,
        String telefono,
        String email,
        String direccion,
        String distrito,
        String ciudad,
        EstadoPaciente estado,
        Antecedentes antecedentes,
        LocalDateTime fechaRegistro) {

    /** Convierte la entidad del dominio en su representacion de salida. */
    public static PacienteResponse desde(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getDni(),
                paciente.getNombres(),
                paciente.getApellidos(),
                paciente.getNombreCompleto(),
                paciente.getEdad(),
                paciente.getFechaNacimiento(),
                paciente.getSexo(),
                paciente.getTelefono(),
                paciente.getEmail(),
                paciente.getDireccion(),
                paciente.getDistrito(),
                paciente.getCiudad(),
                paciente.getEstado(),
                paciente.getAntecedentes(),
                paciente.getFechaRegistro());
    }
}
