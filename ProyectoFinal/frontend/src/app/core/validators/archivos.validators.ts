import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Lista blanca de extensiones, la misma de ArchivoService en el backend. */
export const EXTENSIONES_ARCHIVO = [
  'jpg',
  'jpeg',
  'png',
  'gif',
  'bmp',
  'webp',
  'pdf',
  'doc',
  'docx',
  'txt',
];

export const EXTENSIONES_IMAGEN = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];

/** Tope de 10 MB, el de spring.servlet.multipart.max-file-size. */
export const TAMANO_MAXIMO_ARCHIVO = 10 * 1024 * 1024;

/** Largo maximo de la descripcion: @Size(max = 300) de ArchivoActualizarRequest. */
export const DESCRIPCION_MAXIMA_ARCHIVO = 300;

/** Extension en minusculas y sin punto; cadena vacia si no tiene. */
export function extensionDe(nombre: string | null | undefined): string {
  const limpio = (nombre ?? '').trim().toLowerCase();
  const punto = limpio.lastIndexOf('.');
  return punto >= 0 ? limpio.slice(punto + 1) : '';
}

/**
 * Valida un FormControl<File | null> con las mismas reglas que aplica el
 * backend al subir: archivo no vacio, extension permitida y peso maximo.
 * El valor nulo lo resuelve Validators.required.
 */
export function archivoPermitidoValidator(
  extensiones: readonly string[] = EXTENSIONES_ARCHIVO,
  tamanoMaximo: number = TAMANO_MAXIMO_ARCHIVO,
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const archivo = control.value as File | null;
    if (!archivo) {
      return null;
    }

    // Mismo orden que ArchivoService.subir: primero vacio, luego extension.
    if (archivo.size === 0) {
      return { archivoVacio: true };
    }

    const extension = extensionDe(archivo.name);
    if (!extensiones.includes(extension)) {
      return { extensionNoPermitida: { extension, permitidas: extensiones } };
    }

    if (archivo.size > tamanoMaximo) {
      return { archivoMuyGrande: { tamano: archivo.size, maximo: tamanoMaximo } };
    }

    return null;
  };
}
