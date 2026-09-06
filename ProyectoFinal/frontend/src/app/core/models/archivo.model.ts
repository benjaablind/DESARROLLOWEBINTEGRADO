export type TipoArchivo =
  | 'RADIOGRAFIA'
  | 'FOTOGRAFIA'
  | 'INFORME'
  | 'CONSENTIMIENTO'
  | 'RECETA'
  | 'DOCUMENTO';

export const TIPOS_ARCHIVO: TipoArchivo[] = [
  'RADIOGRAFIA',
  'FOTOGRAFIA',
  'INFORME',
  'CONSENTIMIENTO',
  'RECETA',
  'DOCUMENTO',
];

export const ETIQUETAS_TIPO_ARCHIVO: Record<TipoArchivo, string> = {
  RADIOGRAFIA: 'Radiografia',
  FOTOGRAFIA: 'Fotografia',
  INFORME: 'Informe',
  CONSENTIMIENTO: 'Consentimiento',
  RECETA: 'Receta',
  DOCUMENTO: 'Documento',
};

/** Archivo clinico asociado a un paciente. */
export interface Archivo {
  id: number;
  pacienteId: number;
  /** Nombre completo del paciente, ya resuelto por el backend. */
  pacienteNombre: string;
  usuarioId: number | null;
  /** Nombre de quien subio el archivo, ya resuelto por el backend. */
  usuarioNombre: string | null;
  historiaClinicaId: number | null;
  tipo: TipoArchivo;
  nombre: string;
  nombreOriginal: string;
  extension: string;
  contentType: string;
  tamanoBytes: number;
  /** Tamano ya formateado por el backend, por ejemplo "1.4 MB". */
  tamanoLegible: string;
  descripcion: string | null;
  /** Fecha ISO con hora. */
  fechaSubida: string;
  /** Ruta relativa de descarga que arma el backend, por ejemplo "/api/archivos/3/descargar". */
  urlDescarga: string;
}

/** Filtros opcionales del listado de archivos. */
export interface FiltroArchivos {
  pacienteId?: number;
  tipo?: TipoArchivo;
  historiaClinicaId?: number;
}
