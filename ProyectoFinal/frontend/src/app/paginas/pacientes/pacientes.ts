import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Marcador de posición del módulo de Gestion de pacientes.
 *
 * El Integrante 2 reemplaza este componente por la pantalla real en la rama
 * feature/pacientes. Existe aquí para que el ruteo y la compilación
 * funcionen antes de que los módulos se integren.
 */
@Component({
  selector: 'app-pacientes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="titulo-pagina">Pacientes</h1>
    <p class="subtitulo">Responsable: Integrante 2</p>

    <div class="card">
      <div class="estado-vacio">
        <strong>Pantalla en construcción</strong>
        <p class="texto-suave">
          Este módulo se implementa en la rama <span class="mono">feature/pacientes</span>.
        </p>
      </div>
    </div>
  `,
})
export class PacientesComponent {}
