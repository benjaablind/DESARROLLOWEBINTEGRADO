import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Marcador de posición del módulo de Tratamientos y pagos.
 *
 * El Integrante 5 reemplaza este componente por la pantalla real en la rama
 * feature/tratamientos-pagos. Existe aquí para que el ruteo y la compilación
 * funcionen antes de que los módulos se integren.
 */
@Component({
  selector: 'app-tratamientos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="titulo-pagina">Tratamientos</h1>
    <p class="subtitulo">Responsable: Integrante 5</p>

    <div class="card">
      <div class="estado-vacio">
        <strong>Pantalla en construcción</strong>
        <p class="texto-suave">
          Este módulo se implementa en la rama <span class="mono">feature/tratamientos-pagos</span>.
        </p>
      </div>
    </div>
  `,
})
export class TratamientosComponent {}
