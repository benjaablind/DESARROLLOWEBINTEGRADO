import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Largos de columna de la entidad Pago (@Column(length = ...)). */
export const COMPROBANTE_MAXIMO_PAGO = 50;
export const OBSERVACIONES_MAXIMAS_PAGO = 500;

/** Pasa un importe a centimos para comparar sin errores de coma flotante. */
function aCentimos(valor: number): number {
  return Math.round(valor * 100);
}

/**
 * Validador de grupo: el monto no puede superar el saldo pendiente del
 * tratamiento elegido, la misma regla que PagoService.registrar en el backend.
 *
 * El saldo depende del tratamiento seleccionado, por eso la regla cruza dos
 * campos y vive en el FormGroup. `saldoDe` devuelve el saldo del tratamiento
 * o null si no se conoce. Sin tratamiento, sin monto o con un monto no
 * numerico no opina: esos casos los cubren los validadores de cada campo.
 */
export function montoDentroDelSaldoValidator(
  saldoDe: (tratamientoId: number) => number | null,
  campoTratamiento = 'tratamientoId',
  campoMonto = 'monto',
): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const tratamientoId = Number(grupo.get(campoTratamiento)?.value);
    const valorMonto = grupo.get(campoMonto)?.value;

    if (!tratamientoId || valorMonto === null || valorMonto === undefined || valorMonto === '') {
      return null;
    }

    const monto = Number(valorMonto);
    const saldo = saldoDe(tratamientoId);
    if (!Number.isFinite(monto) || saldo === null) {
      return null;
    }

    return aCentimos(monto) > aCentimos(saldo) ? { montoExcedeSaldo: { saldo, monto } } : null;
  };
}
