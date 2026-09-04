import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Pantalla para cualquier direccion que no corresponda a una ruta conocida. */
@Component({
  selector: 'app-no-encontrada',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <div class="contenedor">
      <p class="codigo">404</p>
      <h1>Pagina no encontrada</h1>
      <p class="texto-suave">
        La direccion que intenta abrir no existe o fue movida a otra seccion del sistema.
      </p>
      <a class="btn btn-primario" routerLink="/dashboard">Ir al dashboard</a>
    </div>
  `,
  styles: `
    .contenedor {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: var(--e-2);
      min-height: 60vh;
      padding: var(--e-6) var(--e-4);
      text-align: center;
    }

    .codigo {
      margin: 0;
      font-size: 3.4rem;
      font-weight: 800;
      line-height: 1;
      letter-spacing: -0.04em;
      color: var(--c-primario-200);
    }

    h1 {
      margin: 0;
    }

    p {
      max-width: 46ch;
    }

    .btn {
      margin-top: var(--e-3);
    }
  `,
})
export class NoEncontradaComponent {}
