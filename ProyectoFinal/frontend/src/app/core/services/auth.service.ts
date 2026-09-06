import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { ApiService } from './api.service';
import { LoginResponse, Rol, Usuario } from '../models/usuario.model';

const CLAVE_ALMACEN = 'odontologia.usuario';

/**
 * Modulos que puede abrir cada rol.
 * El backend valida de verdad; esto solo controla que se muestra en el menu.
 */
const PERMISOS_POR_ROL: Record<Rol, string[]> = {
  ADMINISTRADOR: [
    'dashboard',
    'pacientes',
    'citas',
    'historias',
    'odontograma',
    'tratamientos',
    'pagos',
    'archivos',
    'reportes',
    'usuarios',
  ],
  ODONTOLOGO: [
    'dashboard',
    'pacientes',
    'citas',
    'historias',
    'odontograma',
    'tratamientos',
    'archivos',
    'reportes',
  ],
  RECEPCIONISTA: ['dashboard', 'pacientes', 'citas', 'pagos', 'archivos', 'reportes'],
  ASISTENTE: ['dashboard', 'pacientes', 'citas', 'historias', 'odontograma', 'archivos'],
};

/** Sesion del usuario. El estado vive en signals y se respalda en localStorage. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly usuario = signal<Usuario | null>(this.leerDelAlmacen());

  /** Usuario autenticado o null. */
  readonly usuarioActual = computed(() => this.usuario());
  readonly estaAutenticado = computed(() => this.usuario() !== null);
  readonly rol = computed<Rol | null>(() => this.usuario()?.rol ?? null);
  readonly nombreUsuario = computed(() => this.usuario()?.nombreCompleto ?? '');

  /** POST /api/auth/login. Guarda la sesion si el backend la acepta. */
  login(usuario: string, password: string): Observable<LoginResponse> {
    return this.api
      .post<LoginResponse>('/auth/login', { usuario, password })
      .pipe(
        tap((respuesta) => {
          if (respuesta.autenticado && respuesta.usuario) {
            this.establecerSesion(respuesta.usuario);
          }
        }),
      );
  }

  /** POST /api/auth/logout. La sesion local se limpia pase lo que pase. */
  logout(): void {
    const id = this.usuario()?.id;
    this.limpiarSesion();
    if (id !== undefined) {
      this.api.post('/auth/logout', { usuarioId: id }).subscribe({
        next: () => {},
        error: () => {},
      });
    }
  }

  /** GET /api/auth/perfil?usuarioId= : refresca los datos del usuario en sesion. */
  perfil(usuarioId: number): Observable<Usuario> {
    return this.api
      .get<Usuario>('/auth/perfil', { usuarioId })
      .pipe(tap((u) => this.establecerSesion(u)));
  }

  /** Indica si el rol actual puede entrar a un modulo ('pacientes', 'pagos', ...). */
  tienePermiso(modulo: string): boolean {
    const rol = this.rol();
    if (!rol) {
      return false;
    }
    return PERMISOS_POR_ROL[rol].includes(modulo);
  }

  /** true si el usuario en sesion tiene alguno de los roles indicados. */
  tieneRol(...roles: Rol[]): boolean {
    const rol = this.rol();
    return rol !== null && roles.includes(rol);
  }

  establecerSesion(usuario: Usuario): void {
    this.usuario.set(usuario);
    this.guardarEnAlmacen(usuario);
  }

  private limpiarSesion(): void {
    this.usuario.set(null);
    try {
      localStorage.removeItem(CLAVE_ALMACEN);
    } catch {
      // En SSR no existe localStorage; no hay nada que limpiar.
    }
  }

  private guardarEnAlmacen(usuario: Usuario): void {
    try {
      localStorage.setItem(CLAVE_ALMACEN, JSON.stringify(usuario));
    } catch {
      // Sin almacenamiento la sesion solo dura lo que dure la pestana.
    }
  }

  private leerDelAlmacen(): Usuario | null {
    try {
      const bruto = localStorage.getItem(CLAVE_ALMACEN);
      return bruto ? (JSON.parse(bruto) as Usuario) : null;
    } catch {
      return null;
    }
  }
}
