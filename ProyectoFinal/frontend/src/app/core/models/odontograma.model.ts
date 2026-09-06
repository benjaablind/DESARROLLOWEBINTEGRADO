export type EstadoPieza =
  | 'SANO'
  | 'CARIES'
  | 'OBTURADO'
  | 'ENDODONCIA'
  | 'CORONA'
  | 'IMPLANTE'
  | 'PROTESIS'
  | 'FRACTURADO'
  | 'SELLANTE'
  | 'EXTRACCION_INDICADA'
  | 'AUSENTE';

export const ESTADOS_PIEZA: EstadoPieza[] = [
  'SANO',
  'CARIES',
  'OBTURADO',
  'ENDODONCIA',
  'CORONA',
  'IMPLANTE',
  'PROTESIS',
  'FRACTURADO',
  'SELLANTE',
  'EXTRACCION_INDICADA',
  'AUSENTE',
];

export const ETIQUETAS_ESTADO_PIEZA: Record<EstadoPieza, string> = {
  SANO: 'Sano',
  CARIES: 'Caries',
  OBTURADO: 'Obturado',
  ENDODONCIA: 'Endodoncia',
  CORONA: 'Corona',
  IMPLANTE: 'Implante',
  PROTESIS: 'Protesis',
  FRACTURADO: 'Fracturado',
  SELLANTE: 'Sellante',
  EXTRACCION_INDICADA: 'Extraccion indicada',
  AUSENTE: 'Ausente',
};

/** Color sugerido para pintar cada estado en el odontograma. */
export const COLORES_ESTADO_PIEZA: Record<EstadoPieza, string> = {
  SANO: '#e2e8ea',
  CARIES: '#c4362f',
  OBTURADO: '#2563a8',
  ENDODONCIA: '#7b4bb5',
  CORONA: '#d98324',
  IMPLANTE: '#4d6167',
  PROTESIS: '#8a6d3b',
  FRACTURADO: '#b5341f',
  SELLANTE: '#1f8a54',
  EXTRACCION_INDICADA: '#e0603c',
  AUSENTE: '#9aa8ac',
};

/** Registro historico del estado de una pieza dental. */
export interface RegistroPieza {
  id: number;
  pacienteId: number;
  odontologoId: number;
  numeroPieza: number;
  estado: EstadoPieza;
  superficie: string | null;
  diagnostico: string | null;
  tratamiento: string | null;
  observaciones: string | null;
  /** Fecha ISO con hora. */
  fecha: string;
}

/** Estado vigente de una pieza tal como lo devuelve la API. */
export interface OdontogramaPieza {
  numeroPieza: number;
  estado: EstadoPieza;
  superficie: string | null;
  diagnostico: string | null;
  tratamiento: string | null;
  observaciones: string | null;
  fecha: string;
  odontologoId: number;
  registroId: number;
}

/** Odontograma vigente completo de un paciente. */
export interface Odontograma {
  pacienteId: number;
  pacienteNombre: string;
  piezas: OdontogramaPieza[];
  totalRegistros: number;
}

/** Historial de una sola pieza. */
export interface OdontogramaHistorial {
  pacienteId: number;
  numeroPieza: number;
  registros: OdontogramaPieza[];
}

export interface OdontogramaRegistroRequest {
  pacienteId: number;
  odontologoId: number;
  numeroPieza: number;
  estado: EstadoPieza;
  superficie?: string | null;
  diagnostico?: string | null;
  tratamiento?: string | null;
  observaciones?: string | null;
}

/** Conteo de piezas por estado: { CARIES: 3, OBTURADO: 5, ... }. */
export type ResumenOdontograma = Partial<Record<EstadoPieza, number>>;

/** Numeracion FDI de la denticion permanente, agrupada por cuadrante. */
export const PIEZAS_FDI = {
  superiorDerecho: [18, 17, 16, 15, 14, 13, 12, 11],
  superiorIzquierdo: [21, 22, 23, 24, 25, 26, 27, 28],
  inferiorIzquierdo: [31, 32, 33, 34, 35, 36, 37, 38],
  inferiorDerecho: [48, 47, 46, 45, 44, 43, 42, 41],
} as const;
