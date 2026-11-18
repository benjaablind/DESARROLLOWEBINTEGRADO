import { Component, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { Usuario } from '../usuario.model';
import { UsuarioService } from '../usuario.service';

/** Actividad complementaria: ruta /usuarios/:id que usa obtenerPorId(). */
@Component({
  selector: 'app-usuario-detalle',
  imports: [RouterLink],
  templateUrl: './usuario-detalle.html',
  styleUrl: './usuario-detalle.css'
})
export class UsuarioDetalle {
  private readonly usuarioService = inject(UsuarioService);

  /** Llega desde el parámetro :id gracias a withComponentInputBinding(). */
  readonly id = input.required<string>();

  protected readonly usuario = signal<Usuario | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal('');

  constructor() {
    effect(() => this.cargar(Number(this.id())));
  }

  private cargar(id: number): void {
    this.usuario.set(null);
    this.error.set('');

    if (!Number.isInteger(id) || id <= 0) {
      this.error.set('El identificador no es válido.');
      return;
    }

    this.cargando.set(true);
    this.usuarioService.obtenerPorId(id)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: usuario => this.usuario.set(usuario),
        error: error => {
          console.error('Error al obtener el usuario:', error);
          this.error.set(
            error.status === 404
              ? `No existe un usuario con ID ${id}.`
              : 'No se pudo obtener el usuario.'
          );
        }
      });
  }
}
