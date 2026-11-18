import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Validadores propios del modulo de usuarios y autenticacion (Integrante 1).
 *
 * Reflejan en el navegador las reglas de los DTO del backend (LoginRequest,
 * UsuarioRequest, ActualizarUsuarioRequest y CambiarPasswordRequest) y de
 * UsuarioService, para avisar antes de enviar algo que el servidor rechazaria
 * con 400. El valor vacio siempre lo resuelve Validators.required.
 */

/**
 * Equivale a @NotBlank: rechaza el texto que solo tiene espacios.
 * Validators.required acepta "   ", pero el backend responde 400.
 */
export const noEnBlancoValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const valor = control.value;

  // Validators.required sera responsable del valor vacio.
  if (valor === null || valor === undefined || valor === '') {
    return null;
  }

  return String(valor).trim() === '' ? { enBlanco: true } : null;
};

/**
 * Equivale a @Size(min, max) sobre el valor recortado.
 * El formulario envia el texto con trim(), asi que " abc" mide 3 y no 4:
 * se valida lo que de verdad llega al servidor.
 */
export function longitudRecortadaValidator(minima: number, maxima: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const recortado = String(control.value ?? '').trim();

    // Vacio o solo espacios: lo resuelven Validators.required y noEnBlancoValidator.
    if (recortado === '') {
      return null;
    }

    const actual = recortado.length;
    if (actual < minima || actual > maxima) {
      return { longitudRecortada: { minima, maxima, actual } };
    }

    return null;
  };
}

/**
 * Validador de grupo: la confirmacion debe coincidir con la contrasena.
 * El backend no recibe la confirmacion; evita crear una cuenta o fijar una
 * contrasena nueva con un error de tipeo que nadie podria corregir despues,
 * porque cambiarla exige conocer la actual.
 */
export function passwordsIgualesValidator(campo: string, confirmacion: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const password = grupo.get(campo)?.value;
    const confirmar = grupo.get(confirmacion)?.value;

    if (!password || !confirmar) {
      return null;
    }

    return password === confirmar ? null : { passwordsNoCoinciden: true };
  };
}

/**
 * Validador de grupo: la contrasena nueva debe ser distinta de la actual.
 * Es la regla de UsuarioService.cambiarPassword, que responde 400 con
 * "La contrasena nueva debe ser distinta de la actual".
 */
export function passwordDistintaValidator(actual: string, nueva: string): ValidatorFn {
  return (grupo: AbstractControl): ValidationErrors | null => {
    const valorActual = grupo.get(actual)?.value;
    const valorNuevo = grupo.get(nueva)?.value;

    if (!valorActual || !valorNuevo) {
      return null;
    }

    return valorActual === valorNuevo ? { passwordSinCambio: true } : null;
  };
}
