import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { ApiService } from './api.service';
import { LoginResponse, Rol, Usuario } from '../models/usuario.model';

const CLAVE_ALMACEN = 'odontologia.sesion';

/** Sesion guardada: el JWT, cuando vence y los datos del usuario. */
interface Sesion {
  token: string;
  /** Instante de vencimiento en milisegundos (Date.now()). */
  expiraEn: number;
  usuario: Usuario;
}

/**
 * Modulos que puede abrir cada rol. Es la misma matriz que aplica el backend
 * en SecurityConfig: aqui solo decide que se muestra, el servidor decide que
 * se permite (y responde 403 si alguien fuerza la URL).
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
  RECEPCIONISTA: ['dashboard', 'pacientes', 'citas', 'pagos', 'reportes'],
  ASISTENTE: ['dashboard', 'pacientes', 'citas'],
};

/**
 * Reportes por rol. El historial consolidado incluye consultas clinicas
 * (administrador y odontologo); el resto son reportes de gestion
 * (administrador y recepcion).
 */
const REPORTES_POR_ROL: Record<Rol, string[]> = {
  ADMINISTRADOR: ['pacientes', 'citas', 'tratamientos', 'ingresos', 'pendientes', 'historial'],
  ODONTOLOGO: ['historial'],
  RECEPCIONISTA: ['pacientes', 'citas', 'tratamientos', 'ingresos', 'pendientes'],
  ASISTENTE: [],
};

/**
 * Sesion del usuario autenticado con JWT (semana 9).
 * El token vive en un signal respaldado en localStorage; el interceptor lo
 * agrega como "Authorization: Bearer" a cada peticion a la API.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly sesion = signal<Sesion | null>(this.leerDelAlmacen());

  /** Usuario autenticado o null. */
  readonly usuarioActual = computed(() => this.sesion()?.usuario ?? null);
  readonly estaAutenticado = computed(() => this.sesion() !== null);
  readonly rol = computed<Rol | null>(() => this.sesion()?.usuario.rol ?? null);
  readonly nombreUsuario = computed(() => this.sesion()?.usuario.nombreCompleto ?? '');

  /** Administrador, odontologo y recepcion registran y editan pacientes y citas; el asistente solo consulta. */
  readonly puedeModificarPacientesYCitas = computed(() =>
    this.tieneRol('ADMINISTRADOR', 'ODONTOLOGO', 'RECEPCIONISTA'),
  );

  /** POST /api/auth/login. Si las credenciales son validas guarda el token. */
  login(usuario: string, password: string): Observable<LoginResponse> {
    return this.api.post<LoginResponse>('/auth/login', { usuario, password }).pipe(
      tap((respuesta) =>
        this.guardarSesion({
          token: respuesta.accessToken,
          expiraEn: Date.now() + respuesta.expiresInSeconds * 1000,
          usuario: respuesta.usuario,
        }),
      ),
    );
  }

  /**
   * POST /api/auth/logout y limpieza local. El servidor no guarda tokens:
   * cerrar sesion es descartar el token del navegador. La peticion se envia
   * antes de limpiar para que el interceptor todavia adjunte el token.
   */
  logout(): void {
    if (this.token()) {
      this.api.post('/auth/logout').subscribe({ next: () => {}, error: () => {} });
    }
    this.cerrarSesionLocal();
  }

  /** GET /api/auth/perfil: el backend identifica al usuario por el token. */
  perfil(): Observable<Usuario> {
    return this.api.get<Usuario>('/auth/perfil').pipe(
      tap((usuario) => {
        const actual = this.sesion();
        if (actual) {
          this.guardarSesion({ ...actual, usuario });
        }
      }),
    );
  }

  /** Token vigente, o null si no hay sesion o ya vencio. */
  token(): string | null {
    return this.sesionVigente() ? (this.sesion()?.token ?? null) : null;
  }

  /** true si hay sesion y el token no vencio. Si vencio, la descarta. */
  sesionVigente(): boolean {
    const actual = this.sesion();
    if (!actual) {
      return false;
    }
    if (Date.now() >= actual.expiraEn) {
      this.cerrarSesionLocal();
      return false;
    }
    return true;
  }

  /** Borra la sesion del navegador sin llamar al backend (token vencido o rechazado). */
  cerrarSesionLocal(): void {
    this.sesion.set(null);
    try {
      localStorage.removeItem(CLAVE_ALMACEN);
    } catch {
      // Sin localStorage no hay nada que limpiar.
    }
  }

  /** Indica si el rol actual puede entrar a un modulo ('pacientes', 'pagos', ...). */
  tienePermiso(modulo: string): boolean {
    const rol = this.rol();
    return rol !== null && PERMISOS_POR_ROL[rol].includes(modulo);
  }

  /** Indica si el rol actual puede ver un reporte ('ingresos', 'historial', ...). */
  puedeVerReporte(reporte: string): boolean {
    const rol = this.rol();
    return rol !== null && REPORTES_POR_ROL[rol].includes(reporte);
  }

  /** true si el usuario en sesion tiene alguno de los roles indicados. */
  tieneRol(...roles: Rol[]): boolean {
    const rol = this.rol();
    return rol !== null && roles.includes(rol);
  }

  private guardarSesion(sesion: Sesion): void {
    this.sesion.set(sesion);
    try {
      localStorage.setItem(CLAVE_ALMACEN, JSON.stringify(sesion));
    } catch {
      // Sin almacenamiento la sesion solo dura lo que dure la pestana.
    }
  }

  private leerDelAlmacen(): Sesion | null {
    try {
      // La sesion del Avance 1 (sin token) ya no sirve: se descarta.
      localStorage.removeItem('odontologia.usuario');
      const bruto = localStorage.getItem(CLAVE_ALMACEN);
      if (!bruto) {
        return null;
      }
      const sesion = JSON.parse(bruto) as Sesion;
      return sesion.token && Date.now() < sesion.expiraEn ? sesion : null;
    } catch {
      return null;
    }
  }
}
