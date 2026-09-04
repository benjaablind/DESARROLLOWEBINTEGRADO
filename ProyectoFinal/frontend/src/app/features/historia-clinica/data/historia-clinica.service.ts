import { Injectable, signal } from '@angular/core';
import { HistoriaClinica, HistoriaClinicaRequest } from '../models/historia-clinica.model';

@Injectable({ providedIn: 'root' })
export class HistoriaClinicaService {
  private readonly _registros = signal<HistoriaClinica[]>([
    {
      id: 1,
      pacienteNombre: 'Ana Torres',
      odontologo: 'Dr. Carlos Ramírez',
      fecha: '2026-08-15',
      motivoConsulta: 'Dolor en molar inferior derecho',
      anamnesis: 'Paciente refiere dolor intermitente desde hace una semana.',
      examenClinico: 'Caries profunda en pieza 46, sensibilidad al frío.',
      diagnostico: 'Caries dental profunda - pieza 46',
      procedimiento: 'Evaluación clínica y radiografía periapical',
      tratamiento: 'Endodoncia programada',
      medicamentos: 'Ibuprofeno 400mg cada 8 horas por 3 días',
      observaciones: 'Paciente citado para próxima sesión de endodoncia.',
    },
  ]);

  private nextId = 2;

  readonly registros = this._registros.asReadonly();

  listar(): HistoriaClinica[] {
    return this._registros();
  }

  buscarPorId(id: number): HistoriaClinica | undefined {
    return this._registros().find((r) => r.id === id);
  }

  buscarPorPaciente(nombre: string): HistoriaClinica[] {
    const texto = nombre.trim().toLowerCase();
    if (!texto) {
      return this._registros();
    }
    return this._registros().filter((r) =>
      r.pacienteNombre.toLowerCase().includes(texto)
    );
  }

  crear(request: HistoriaClinicaRequest): HistoriaClinica {
    const nuevo: HistoriaClinica = { id: this.nextId++, ...request };
    // Las consultas anteriores nunca se sobrescriben: siempre se agrega un registro nuevo.
    this._registros.update((lista) => [...lista, nuevo]);
    return nuevo;
  }
}
