import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Marcador de posición del módulo de Usuarios, roles y autenticacion.
 *
 * El Integrante 1 reemplaza este componente por la pantalla real en la rama
 * feature/usuarios. Existe aquí para que el ruteo y la compilación
 * funcionen antes de que los módulos se integren.
 */
@Component({
  selector: 'app-usuarios',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h1 class="titulo-pagina">Usuarios</h1>
    <p class="subtitulo">Responsable: Integrante 1</p>

    <div class="card">
      <div class="estado-vacio">
        <strong>Pantalla en construcción</strong>
        <p class="texto-suave">
          Este módulo se implementa en la rama <span class="mono">feature/usuarios</span>.
        </p>
      </div>
    </div>
  `,
})
export class UsuariosComponent {}
