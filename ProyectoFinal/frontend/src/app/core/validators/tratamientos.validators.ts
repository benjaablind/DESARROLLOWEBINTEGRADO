import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Tope de un importe en soles: @Digits(integer = 10, fraction = 2) de
 * TratamientoRequest.precio y PagoRequest.monto, igual que DECIMAL(12,2).
 */
export const ENTEROS_MAXIMOS_IMPORTE = 10;
export const DECIMALES_MAXIMOS_IMPORTE = 2;

/** Largos de columna de la entidad Tratamiento (@Column(length = ...)). */
export const NOMBRE_MAXIMO_TRATAMIENTO = 120;
export const DESCRIPCION_MAXIMA_TRATAMIENTO = 1000;
export const OBSERVACIONES_MAXIMAS_TRATAMIENTO = 1000;

/** Largos de columna de SesionTratamiento (@Column(length = ...)). */
export const DESCRIPCION_MAXIMA_SESION = 500;
export const OBSERVACIONES_MAXIMAS_SESION = 1000;

/**
 * Importe en soles con las reglas del backend: @Positive y
 * @Digits(integer = 10, fraction = 2). Los ceros finales no cuentan como
 * decimales, igual que en Hibernate Validator ("1.50" es valido).
 * El valor vacio lo resuelve Validators.required.
 */
export function importeValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value as number | string | null | undefined;
    const texto = typeof valor === 'string' ? valor.trim() : valor == null ? '' : String(valor);

    if (texto === '') {
      return null;
    }

    const numero = Number(texto);
    if (!Number.isFinite(numero)) {
      return { importeInvalido: true };
    }

    if (numero <= 0) {
      return { importeNoPositivo: true };
    }

    const { enteros, decimales } = contarDigitos(texto, numero);

    if (decimales > DECIMALES_MAXIMOS_IMPORTE) {
      return { importeDecimales: { maximo: DECIMALES_MAXIMOS_IMPORTE, actual: decimales } };
    }

    if (enteros > ENTEROS_MAXIMOS_IMPORTE) {
      return { importeEnteros: { maximo: ENTEROS_MAXIMOS_IMPORTE, actual: enteros } };
    }

    return null;
  };
}

/**
 * Equivalente a @NotBlank para un campo ya marcado como requerido:
 * rechaza el texto compuesto solo de espacios, que el formulario recorta
 * antes de enviarlo y el backend rechazaria con 400.
 * La cadena vacia la resuelve Validators.required.
 */
export const textoNoVacioValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const valor = control.value;
  if (typeof valor !== 'string' || valor === '') {
    return null;
  }
  return valor.trim() === '' ? { soloEspacios: true } : null;
};

/** Cuenta digitos enteros y decimales significativos de un importe positivo. */
function contarDigitos(texto: string, numero: number): { enteros: number; decimales: number } {
  // Notacion cientifica (1e-7, 1e+21): fuera de rango por un lado o por el otro.
  if (/e/i.test(texto)) {
    return numero >= 1
      ? { enteros: Math.floor(Math.log10(numero)) + 1, decimales: 0 }
      : { enteros: 0, decimales: DECIMALES_MAXIMOS_IMPORTE + 1 };
  }

  const [parteEntera, parteDecimal = ''] = texto.replace(/^\+/, '').split('.');
  return {
    enteros: parteEntera.replace(/^0+/, '').length,
    decimales: parteDecimal.replace(/0+$/, '').length,
  };
}
