import { Injectable, signal } from '@angular/core';

/** Actividad complementaria: cuenta las solicitudes HTTP que terminaron bien. */
@Injectable({
  providedIn: 'root'
})
export class ContadorSolicitudesService {
  private readonly exitosasInterno = signal(0);
  readonly exitosas = this.exitosasInterno.asReadonly();

  registrarExito(): void {
    this.exitosasInterno.update(total => total + 1);
  }
}
