import { Injectable, signal } from '@angular/core';

export type TipoNotificacion = 'exito' | 'error' | 'info';

export interface Notificacion {
  id: number;
  tipo: TipoNotificacion;
  mensaje: string;
}

/** Duracion en milisegundos antes de que la notificacion se descarte sola. */
const DURACION_MS = 4000;

/**
 * Cola de mensajes flotantes. Cualquier componente puede llamar a
 * exito(), error() o info() y NotificacionesComponent los pinta.
 */
@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly lista = signal<Notificacion[]>([]);
  private siguienteId = 1;

  /** Lista de solo lectura para la vista. */
  readonly notificaciones = this.lista.asReadonly();

  exito(mensaje: string): void {
    this.agregar('exito', mensaje);
  }

  error(mensaje: string): void {
    this.agregar('error', mensaje);
  }

  info(mensaje: string): void {
    this.agregar('info', mensaje);
  }

  cerrar(id: number): void {
    this.lista.update((actuales) => actuales.filter((n) => n.id !== id));
  }

  limpiar(): void {
    this.lista.set([]);
  }

  private agregar(tipo: TipoNotificacion, mensaje: string): void {
    const id = this.siguienteId++;
    this.lista.update((actuales) => [...actuales, { id, tipo, mensaje }]);

    // setTimeout tambien existe en Node, asi que el autodescarte no rompe el SSR.
    setTimeout(() => this.cerrar(id), DURACION_MS);
  }
}
