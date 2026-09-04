import { Component, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OdontogramaService } from '../../data/odontograma.service';
import { EstadoPieza } from '../../models/odontograma.model';

const ARCADA_SUPERIOR = [18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28];
const ARCADA_INFERIOR = [48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38];

const COLORES: Record<EstadoPieza, string> = {
  sano: '#d1fae5',
  caries: '#fecaca',
  obturado: '#bfdbfe',
  ausente: '#e5e7eb',
  corona: '#fde68a',
  endodoncia: '#ddd6fe',
};

@Component({
  selector: 'app-odontograma-view',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './odontograma-view.html',
  styleUrl: './odontograma-view.scss',
})
export class OdontogramaView {
  private readonly service = inject(OdontogramaService);

  protected readonly arcadaSuperior = ARCADA_SUPERIOR;
  protected readonly arcadaInferior = ARCADA_INFERIOR;
  protected readonly estados: EstadoPieza[] = [
    'sano',
    'caries',
    'obturado',
    'ausente',
    'corona',
    'endodoncia',
  ];

  protected readonly piezas = this.service.piezas;
  protected readonly piezaSeleccionada = signal<number | null>(null);

  protected readonly pieza = computed(() => {
    const numero = this.piezaSeleccionada();
    return numero !== null ? this.service.buscarPorNumero(numero) : undefined;
  });

  protected estadoForm: EstadoPieza = 'sano';
  protected diagnosticoForm = '';
  protected tratamientoForm = '';
  protected observacionesForm = '';

  protected seleccionar(numero: number): void {
    this.piezaSeleccionada.set(numero);
    const pieza = this.service.buscarPorNumero(numero);
    this.estadoForm = pieza?.estadoActual ?? 'sano';
    this.diagnosticoForm = '';
    this.tratamientoForm = '';
    this.observacionesForm = '';
  }

  protected estadoDe(numero: number): EstadoPieza {
    return this.service.buscarPorNumero(numero)?.estadoActual ?? 'sano';
  }

  protected colorDe(estado: EstadoPieza): string {
    return COLORES[estado];
  }

  protected guardar(): void {
    const numero = this.piezaSeleccionada();
    if (numero === null) {
      return;
    }
    this.service.actualizarPieza(numero, {
      estado: this.estadoForm,
      diagnostico: this.diagnosticoForm,
      tratamiento: this.tratamientoForm,
      observaciones: this.observacionesForm,
    });
    this.diagnosticoForm = '';
    this.tratamientoForm = '';
    this.observacionesForm = '';
  }
}
