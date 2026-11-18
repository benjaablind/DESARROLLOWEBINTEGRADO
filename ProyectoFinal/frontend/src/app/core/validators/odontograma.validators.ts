import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

import { ESTADOS_PIEZA, EstadoPieza } from '../models/odontograma.model';

/**
 * Validadores del odontograma (Integrante 4).
 *
 * Replican en el formulario las reglas que el backend ya aplica:
 * - OdontogramaService.validarNumeroPieza: solo numeros de la notacion FDI.
 * - El enum EstadoPieza: cualquier otro estado no se puede leer y responde 400.
 * - Las longitudes de columna de la entidad RegistroPieza.
 *
 * Como en el laboratorio, un valor vacio devuelve null: la obligatoriedad la
 * resuelve Validators.required.
 */

/** Longitudes maximas de las columnas de la tabla odontograma (@Column(length)). */
export const LONGITUDES_ODONTOGRAMA = {
  superficie: 50,
  diagnostico: 500,
  tratamiento: 500,
  observaciones: 1000,
} as const;

/**
 * Misma regla que OdontogramaService.validarNumeroPieza.
 * Permanente: cuadrantes 1 a 4 y piezas 1 a 8 (11-18, 21-28, 31-38, 41-48).
 * Decidua: cuadrantes 5 a 8 y piezas 1 a 5 (51-55, 61-65, 71-75, 81-85).
 */
export function esPiezaFdi(numero: number): boolean {
  if (!Number.isInteger(numero)) {
    return false;
  }
  const cuadrante = Math.trunc(numero / 10);
  const pieza = numero % 10;

  const permanente = cuadrante >= 1 && cuadrante <= 4 && pieza >= 1 && pieza <= 8;
  const decidua = cuadrante >= 5 && cuadrante <= 8 && pieza >= 1 && pieza <= 5;
  return permanente || decidua;
}

/** El numero de pieza debe existir en la notacion FDI (19, 50 o 99 no existen). */
export function piezaFdi(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value;
    if (valor === null || valor === undefined || valor === '') {
      return null;
    }
    const numero = Number(valor);
    return esPiezaFdi(numero) ? null : { piezaFdi: { valor } };
  };
}

/** El estado debe ser uno de los valores del enum EstadoPieza del backend. */
export function estadoPiezaValido(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = control.value;
    if (valor === null || valor === undefined || valor === '') {
      return null;
    }
    return ESTADOS_PIEZA.includes(valor as EstadoPieza) ? null : { estadoPieza: { valor } };
  };
}
