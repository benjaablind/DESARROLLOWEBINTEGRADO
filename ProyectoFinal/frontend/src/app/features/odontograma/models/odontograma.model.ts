export type EstadoPieza =
  | 'sano'
  | 'caries'
  | 'obturado'
  | 'ausente'
  | 'corona'
  | 'endodoncia';

export interface RegistroPieza {
  fecha: string;
  estado: EstadoPieza;
  diagnostico: string;
  tratamiento: string;
  observaciones: string;
}

export interface PiezaDental {
  numero: number;
  estadoActual: EstadoPieza;
  historial: RegistroPieza[];
}
