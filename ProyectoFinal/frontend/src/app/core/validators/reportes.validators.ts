import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validador de grupo para el rango { desde, hasta } de los reportes.
 * Replica ReporteService.validarRango del backend: la fecha inicial no puede
 * ser posterior a la final. Cualquiera de los dos extremos puede ir vacio.
 * Las fechas llegan como "YYYY-MM-DD", que se comparan bien como texto.
 */
export const rangoFechasValidator: ValidatorFn = (
  grupo: AbstractControl,
): ValidationErrors | null => {
  const desde = grupo.get('desde')?.value as string | null | undefined;
  const hasta = grupo.get('hasta')?.value as string | null | undefined;

  if (!desde || !hasta) {
    return null;
  }

  return desde > hasta ? { rangoInvertido: true } : null;
};
