import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Marcador de posición del módulo de Archivos clinicos y reportes.
 *
 * El Integrante 6 reemplaza este componente por la pantalla real en la rama
 * feature/archivos-reportes. Existe aquí para que el ruteo y la compilación
 * funcionen antes de que los módulos se integren.
 */
@Component({
  selector: 'app-reportes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="titulo-pagina">Reportes</h1>
    <p class="subtitulo">Responsable: Integrante 6</p>

    <div class="card">
      <div class="estado-vacio">
        <strong>Pantalla en construcción</strong>
        <p class="texto-suave">
          Este módulo se implementa en la rama <span class="mono">feature/archivos-reportes</span>.
        </p>
      </div>
    </div>
  `,
})
export class ReportesComponent {}
