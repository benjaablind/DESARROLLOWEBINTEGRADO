import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Marcador de posición del módulo de Historia clinica y odontograma.
 *
 * El Integrante 4 reemplaza este componente por la pantalla real en la rama
 * feature/historia-odontograma. Existe aquí para que el ruteo y la compilación
 * funcionen antes de que los módulos se integren.
 */
@Component({
  selector: 'app-odontograma',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="titulo-pagina">Odontograma</h1>
    <p class="subtitulo">Responsable: Integrante 4</p>

    <div class="card">
      <div class="estado-vacio">
        <strong>Pantalla en construcción</strong>
        <p class="texto-suave">
          Este módulo se implementa en la rama <span class="mono">feature/historia-odontograma</span>.
        </p>
      </div>
    </div>
  `,
})
export class OdontogramaComponent {}
