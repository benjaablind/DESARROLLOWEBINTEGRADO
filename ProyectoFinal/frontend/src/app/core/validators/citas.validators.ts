import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/*
 * Validadores de los formularios de citas (Integrante 3).
 *
 * Cada regla replica una del backend para que el usuario vea el error antes de
 * enviar la peticion. Como en el laboratorio, un valor vacio devuelve null:
 * de la obligatoriedad se encarga Validators.required.
 */

/** Devuelve la fecha y hora actual. Las pruebas pasan uno fijo para no depender del reloj. */
export type Reloj = () => Date;

const relojDelSistema: Reloj = () => new Date();

/* --------------------------- Limites del backend --------------------------- */

/** CitaRequest.duracionMinutos: @Min(10) y @Max(240). */
export const DURACION_MINIMA = 10;
export const DURACION_MAXIMA = 240;
/** CitaRequest.duracionEfectiva(): si no llega duracion se aplican 30 minutos. */
export const DURACION_POR_DEFECTO = 30;

/** Largo de las columnas motivo (255) y observaciones (1000) de la entidad Cita. */
export const MAX_MOTIVO = 255;
export const MAX_OBSERVACIONES = 1000;

/**
 * Al reprogramar, CitaService guarda en las observaciones de la cita original
 * "Reprogramada en la cita {id}. Motivo: {motivo}". Se descuenta ese prefijo,
 * con el id mas largo posible (19 digitos de un Long), para no pasar de 1000.
 */
export const MAX_MOTIVO_REPROGRAMACION =
  MAX_OBSERVACIONES - 'Reprogramada en la cita '.length - '. Motivo: '.length - 19;

/** Formatos que arman el LocalDateTime "AAAA-MM-DDTHH:mm:00" que espera el backend. */
export const PATRON_FECHA = /^\d{4}-\d{2}-\d{2}$/;
export const PATRON_HORA = /^([01]\d|2[0-3]):[0-5]\d$/;

/* ------------------------------ De un campo ------------------------------- */

/**
 * Equivale a @NotBlank: Validators.required acepta "   ", el backend no.
 * Error: { soloEspacios: true }.
 */
export const textoNoVacio: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;
  if (typeof valor !== 'string' || valor.length === 0) {
    return null;
  }
  return valor.trim().length > 0 ? null : { soloEspacios: true };
};

/**
 * duracionMinutos es un Integer en CitaRequest: no admite fracciones de minuto.
 * Error: { entero: true }.
 */
export const numeroEntero: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const valor = control.value;
  if (valor === null || valor === undefined || valor === '') {
    return null;
  }
  return Number.isInteger(Number(valor)) ? null : { entero: true };
};

/* ------------------------- De grupo (fecha + hora) ------------------------- */

/**
 * Convierte "AAAA-MM-DD" y "HH:mm" a una fecha local. Devuelve null si alguna
 * parte no existe, por ejemplo 2026-02-30 o 25:00, que el backend rechazaria.
 */
export function aFechaLocal(fecha: string, hora: string): Date | null {
  if (!PATRON_FECHA.test(fecha) || !PATRON_HORA.test(hora)) {
    return null;
  }
  const [anio, mes, dia] = fecha.split('-').map(Number);
  const [horas, minutos] = hora.split(':').map(Number);
  const valor = new Date(anio, mes - 1, dia, horas, minutos, 0, 0);
  const coincide =
    valor.getFullYear() === anio && valor.getMonth() === mes - 1 && valor.getDate() === dia;
  return coincide ? valor : null;
}

/**
 * Equivale a @Future en CitaRequest.fechaHora y CitaReprogramarRequest.nuevaFechaHora.
 * Se aplica al FormGroup porque la fecha y la hora viven en controles distintos
 * y solo juntas forman el instante que valida el backend.
 * Errores: { fechaInvalida: true } o { fechaPasada: true }.
 */
export function fechaHoraFutura(reloj: Reloj = relojDelSistema): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const fecha = grupo.get('fecha')?.value as string | null | undefined;
    const hora = grupo.get('hora')?.value as string | null | undefined;
    if (!fecha || !hora) {
      return null;
    }
    const valor = aFechaLocal(fecha, hora);
    if (!valor) {
      return { fechaInvalida: true };
    }
    // @Future es estricto: el instante actual ya no cuenta como futuro.
    return valor.getTime() > reloj().getTime() ? null : { fechaPasada: true };
  };
}

/**
 * Reprogramar crea una cita nueva y marca la actual como REPROGRAMADA, asi que
 * hacerlo a la misma fecha y hora solo duplicaria la cita.
 * Recibe una funcion porque la cita a reprogramar cambia cada vez que se abre el panel.
 * Error: { mismaFechaHora: true }.
 */
export function fechaHoraDistinta(original: () => string | null | undefined): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const fecha = grupo.get('fecha')?.value as string | null | undefined;
    const hora = grupo.get('hora')?.value as string | null | undefined;
    const actual = original();
    if (!fecha || !hora || !actual) {
      return null;
    }
    // "2026-09-04T10:00:00" -> "2026-09-04T10:00"
    return `${fecha}T${hora}` === actual.slice(0, 16) ? { mismaFechaHora: true } : null;
  };
}
