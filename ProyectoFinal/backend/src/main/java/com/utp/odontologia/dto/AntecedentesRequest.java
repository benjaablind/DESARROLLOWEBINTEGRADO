package com.utp.odontologia.dto;

import java.util.List;

/**
 * Datos de entrada de los antecedentes clinicos de un paciente (Integrante 2).
 * Todos los campos son opcionales: se usa tanto dentro de PacienteRequest como
 * en la actualizacion parcial PATCH /api/pacientes/{id}/antecedentes.
 */
public record AntecedentesRequest(
        List<String> enfermedades,
        List<String> alergias,
        List<String> medicamentos,
        List<String> habitos,
        String antecedentesOdontologicos,
        String observaciones) {
}
