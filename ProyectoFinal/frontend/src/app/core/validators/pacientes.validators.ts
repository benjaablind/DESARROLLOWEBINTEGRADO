import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validadores propios del modulo de pacientes (Integrante 2).
 *
 * Cada uno replica una regla del backend (dto/PacienteRequest.java) para que el
 * usuario vea el error antes de enviar. Igual que en el laboratorio, un valor
 * vacio devuelve null: de la obligatoriedad se encarga Validators.required.
 */

/**
 * Longitudes maximas de las columnas en la base (model/Paciente.java y
 * model/Antecedentes.java). Si el texto las supera, la base rechaza el registro.
 */
export const LONGITUD_MAXIMA_PACIENTE = {
  nombres: 80,
  apellidos: 80,
  email: 120,
  direccion: 200,
  distrito: 80,
  ciudad: 80,
  /** Cada elemento de enfermedades, alergias, medicamentos y habitos. */
  elementoAntecedente: 200,
  /** antecedentesOdontologicos y observaciones. */
  textoAntecedente: 1000,
} as const;

/** Valor del control como texto; null y undefined cuentan como vacio. */
function textoDe(control: AbstractControl): string {
  const valor: unknown = control.value;
  return valor === null || valor === undefined ? '' : String(valor);
}

/**
 * @NotBlank: Validators.required acepta "   ", el backend no.
 * Error: { soloEspacios: true }.
 */
export function sinSoloEspacios(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = textoDe(control);
    if (valor.length === 0) {
      return null;
    }
    return valor.trim().length === 0 ? { soloEspacios: true } : null;
  };
}

/**
 * @Pattern(regexp = "\\d{8}") del DNI.
 * Se evalua el valor recortado porque es el que se envia al backend.
 * Error: { dniInvalido: true }.
 */
export function dniValido(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = textoDe(control);
    if (valor.length === 0) {
      return null;
    }
    return /^\d{8}$/.test(valor.trim()) ? null : { dniInvalido: true };
  };
}

/**
 * Telefono: solo digitos, entre 6 y 15.
 * El backend solo exige @NotBlank (y la columna admite 20 caracteres); el
 * formato es una regla del frontend que ya tenia el formulario.
 * Error: { telefonoInvalido: true }.
 */
export function telefonoValido(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = textoDe(control);
    if (valor.length === 0) {
      return null;
    }
    return /^\d{6,15}$/.test(valor.trim()) ? null : { telefonoInvalido: true };
  };
}

/** Fecha local como numero aaaammdd, para comparar sin horas ni zona horaria. */
function claveDia(anio: number, mes: number, dia: number): number {
  return anio * 10000 + mes * 100 + dia;
}

/**
 * @Past de la fecha de nacimiento: tiene que ser anterior a hoy (hoy no vale).
 * Recibe el reloj como parametro para que las pruebas no dependan de la fecha real.
 * Errores: { fechaInvalida: true } si el texto no es una fecha "aaaa-mm-dd" real,
 * { fechaNoPasada: true } si es hoy o una fecha futura.
 */
export function fechaPasada(hoy: () => Date = () => new Date()): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const valor = textoDe(control);
    if (valor.length === 0) {
      return null;
    }

    const partes = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor);
    if (!partes) {
      return { fechaInvalida: true };
    }

    const anio = Number(partes[1]);
    const mes = Number(partes[2]);
    const dia = Number(partes[3]);

    // Descarta fechas que no existen, como 2023-02-30.
    const fecha = new Date(2000, 0, 1);
    fecha.setFullYear(anio, mes - 1, dia);
    if (fecha.getFullYear() !== anio || fecha.getMonth() !== mes - 1 || fecha.getDate() !== dia) {
      return { fechaInvalida: true };
    }

    const ahora = hoy();
    const claveHoy = claveDia(ahora.getFullYear(), ahora.getMonth() + 1, ahora.getDate());

    return claveDia(anio, mes, dia) < claveHoy ? null : { fechaNoPasada: true };
  };
}
