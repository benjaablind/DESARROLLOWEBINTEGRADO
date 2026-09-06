import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { NotificacionService } from '../core/services/notificacion.service';

/**
 * Pila de mensajes flotantes en la esquina inferior derecha.
 * Se monta una sola vez en app.html y escucha al NotificacionService.
 */
@Component({
  selector: 'app-notificaciones',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pila" role="status" aria-live="polite">
      @for (n of servicio.notificaciones(); track n.id) {
        <div class="aviso" [class]="'aviso-' + n.tipo">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            @if (n.tipo === 'exito') {
              <path d="M20 6 9 17l-5-5" />
            } @else if (n.tipo === 'error') {
              <path d="M12 8v5M12 17h.01M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z" />
            } @else {
              <path d="M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20zM12 16v-4M12 8h.01" />
            }
          </svg>
          <span class="texto">{{ n.mensaje }}</span>
          <button type="button" class="cerrar" (click)="servicio.cerrar(n.id)" aria-label="Cerrar aviso">
            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 6 6 18M6 6l12 12" /></svg>
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .pila {
      position: fixed;
      right: var(--e-4);
      bottom: var(--e-4);
      z-index: 900;
      display: flex;
      flex-direction: column;
      gap: var(--e-2);
      width: min(360px, calc(100vw - 2rem));
      pointer-events: none;
    }

    .aviso {
      display: flex;
      align-items: flex-start;
      gap: var(--e-2);
      padding: var(--e-3);
      border: 1px solid var(--c-borde);
      border-left: 4px solid var(--c-texto-suave);
      border-radius: var(--r-md);
      background: var(--c-superficie);
      box-shadow: var(--s-3);
      font-size: 0.88rem;
      pointer-events: auto;
      animation: entrar 0.2s ease-out;
    }

    .aviso svg {
      width: 18px;
      height: 18px;
      flex: none;
      margin-top: 1px;
      fill: none;
      stroke: currentColor;
      stroke-width: 2;
      stroke-linecap: round;
      stroke-linejoin: round;
    }

    .texto {
      flex: 1;
      color: var(--c-texto);
      overflow-wrap: anywhere;
    }

    .aviso-exito {
      border-left-color: var(--c-exito);
      color: var(--c-exito);
    }
    .aviso-error {
      border-left-color: var(--c-peligro);
      color: var(--c-peligro);
    }
    .aviso-info {
      border-left-color: var(--c-info);
      color: var(--c-info);
    }

    .cerrar {
      flex: none;
      padding: 0;
      border: 0;
      background: none;
      color: var(--c-texto-suave);
      cursor: pointer;
      line-height: 0;
    }
    .cerrar:hover {
      color: var(--c-texto);
    }
    .cerrar svg {
      width: 15px;
      height: 15px;
    }

    @keyframes entrar {
      from {
        opacity: 0;
        transform: translateY(8px);
      }
      to {
        opacity: 1;
        transform: none;
      }
    }
  `,
})
export class NotificacionesComponent {
  protected readonly servicio = inject(NotificacionService);
}
