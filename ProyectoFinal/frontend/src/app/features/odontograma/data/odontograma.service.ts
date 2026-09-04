import { Injectable, signal } from '@angular/core';
import { EstadoPieza, PiezaDental } from '../models/odontograma.model';

const NUMEROS_PIEZAS = [
  18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28,
  48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38,
];

@Injectable({ providedIn: 'root' })
export class OdontogramaService {
  private readonly _piezas = signal<PiezaDental[]>(
    NUMEROS_PIEZAS.map((numero) => ({
      numero,
      estadoActual: 'sano' as EstadoPieza,
      historial: [],
    }))
  );

  readonly piezas = this._piezas.asReadonly();

  listar(): PiezaDental[] {
    return this._piezas();
  }

  buscarPorNumero(numero: number): PiezaDental | undefined {
    return this._piezas().find((p) => p.numero === numero);
  }

  actualizarPieza(
    numero: number,
    datos: { estado: EstadoPieza; diagnostico: string; tratamiento: string; observaciones: string }
  ): void {
    this._piezas.update((lista) =>
      lista.map((pieza) => {
        if (pieza.numero !== numero) {
          return pieza;
        }
        const nuevoRegistro = {
          fecha: new Date().toISOString().slice(0, 10),
          ...datos,
        };
        return {
          ...pieza,
          estadoActual: datos.estado,
          historial: [nuevoRegistro, ...pieza.historial],
        };
      })
    );
  }
}
