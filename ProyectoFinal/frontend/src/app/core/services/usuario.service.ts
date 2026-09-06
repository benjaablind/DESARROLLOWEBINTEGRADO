import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import {
  ActualizarUsuarioRequest,
  CambiarPasswordRequest,
  PermisosResponse,
  Rol,
  Usuario,
  UsuarioRequest,
} from '../models/usuario.model';

/** Consume /api/usuarios. */
@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/usuarios';

  /** GET /api/usuarios?rol=&activo= */
  listar(rol?: Rol, activo?: boolean): Observable<Usuario[]> {
    return this.api.get<Usuario[]>(this.ruta, { rol, activo });
  }

  /** Atajo util para poblar selects de odontologos. */
  listarOdontologos(): Observable<Usuario[]> {
    return this.listar('ODONTOLOGO', true);
  }

  buscarPorId(id: number): Observable<Usuario> {
    return this.api.get<Usuario>(`${this.ruta}/${id}`);
  }

  crear(datos: UsuarioRequest): Observable<Usuario> {
    return this.api.post<Usuario>(this.ruta, datos);
  }

  actualizar(id: number, datos: ActualizarUsuarioRequest): Observable<Usuario> {
    return this.api.put<Usuario>(`${this.ruta}/${id}`, datos);
  }

  cambiarPassword(id: number, datos: CambiarPasswordRequest): Observable<Usuario> {
    return this.api.patch<Usuario>(`${this.ruta}/${id}/password`, datos);
  }

  activar(id: number): Observable<Usuario> {
    return this.api.patch<Usuario>(`${this.ruta}/${id}/activar`);
  }

  desactivar(id: number): Observable<Usuario> {
    return this.api.patch<Usuario>(`${this.ruta}/${id}/desactivar`);
  }

  eliminar(id: number): Observable<void> {
    return this.api.delete<void>(`${this.ruta}/${id}`);
  }

  permisos(id: number): Observable<PermisosResponse> {
    return this.api.get<PermisosResponse>(`${this.ruta}/${id}/permisos`);
  }
}
