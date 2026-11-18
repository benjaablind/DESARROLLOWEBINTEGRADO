import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validadores de la historia clinica (Integrante 4).
 *
 * Replican en el formulario las reglas que el backend ya aplica, para que el
 * usuario vea el error antes de enviar y no reciba un 400 del servidor:
 * - HistoriaClinicaRequest y HistoriaAnularRequest (@NotBlank).
 * - HistoriaClinicaService.validarCita: la cita debe ser del mismo paciente.
 * - Las longitudes de columna de la entidad HistoriaClinica.
 *
 * Como en el laboratorio, un valor vacio devuelve null: la obligatoriedad la
 * resuelve Validators.required y cada validador se ocupa de una sola regla.
 */

/** Longitudes maximas de las columnas de historias_clinicas (@Column(length)). */
export const LONGITUDES_HISTORIA = {
  motivoConsulta: 500,
  /** anamnesis, examen clinico, diagnostico, procedimiento, tratamiento, medicamentos y observaciones. */
  textoClinico: 1000,
  motivoAnulacion: 500,
} as const;

/**
 * Equivalente de @NotBlank: rechaza un texto formado solo por espacios.
 * Validators.required lo da por valido, pero el backend lo rechaza.
 */
export function sinSoloEspacios(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value;
    if (typeof valor !== 'string' || valor.length === 0) {
      return null;
    }
    return valor.trim().length === 0 ? { soloEspacios: true } : null;
  };
}

/** Lo minimo que el validador necesita saber de cada cita. */
export interface CitaDePaciente {
  id: number;
  pacienteId: number;
}

/**
 * Validador de grupo (regla entre campos): la cita asociada debe pertenecer al
 * paciente de la consulta, igual que HistoriaClinicaService.validarCita.
 *
 * Recibe una funcion y no la lista porque las citas llegan del servidor despues
 * de crear el formulario. Si la cita no esta en la lista no se puede comparar y
 * se deja la decision al servidor, que tambien verifica que exista.
 */
export function citaDelPaciente(citas: () => readonly CitaDePaciente[]): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const citaId = Number(grupo.get('citaId')?.value);
    const pacienteId = Number(grupo.get('pacienteId')?.value);
    if (!citaId || !pacienteId) {
      return null;
    }

    const cita = citas().find((c) => c.id === citaId);
    if (!cita) {
      return null;
    }
    return cita.pacienteId === pacienteId ? null : { citaDeOtroPaciente: true };
  };
}
