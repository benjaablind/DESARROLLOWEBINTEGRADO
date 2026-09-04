/** Formato unico de error que devuelve la API para cualquier fallo. */
export interface ErrorApi {
  estado: number;
  mensaje: string;
  ruta: string;
  /** Fecha y hora ISO, por ejemplo "2026-09-04T10:00:00". */
  fechaHora: string;
}

/** Respuesta simple del backend cuando solo devuelve un texto. */
export interface MensajeApi {
  mensaje: string;
}

/** Opcion generica para poblar selects. */
export interface Opcion<T = string> {
  valor: T;
  etiqueta: string;
}
