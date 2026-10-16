export type Rol = 'ADMINISTRADOR' | 'ODONTOLOGO' | 'RECEPCIONISTA' | 'ASISTENTE';

export const ROLES: Rol[] = ['ADMINISTRADOR', 'ODONTOLOGO', 'RECEPCIONISTA', 'ASISTENTE'];

export const ETIQUETAS_ROL: Record<Rol, string> = {
  ADMINISTRADOR: 'Administrador',
  ODONTOLOGO: 'Odontologo',
  RECEPCIONISTA: 'Recepcionista',
  ASISTENTE: 'Asistente',
};

/** Usuario del sistema. La contrasena nunca viaja en las respuestas. */
export interface Usuario {
  id: number;
  nombres: string;
  apellidos: string;
  nombreCompleto: string;
  email: string;
  usuario: string;
  rol: Rol;
  activo: boolean;
  /** Fecha ISO. */
  fechaRegistro: string;
}

/** Cuerpo para crear un usuario. */
export interface UsuarioRequest {
  nombres: string;
  apellidos: string;
  email: string;
  usuario: string;
  password: string;
  rol: Rol;
}

/** Cuerpo para actualizar los datos de un usuario (sin credenciales). */
export interface ActualizarUsuarioRequest {
  nombres: string;
  apellidos: string;
  email: string;
  rol: Rol;
}

export interface CambiarPasswordRequest {
  passwordActual: string;
  passwordNuevo: string;
}

export interface PermisosResponse {
  rol: Rol;
  permisos: string[];
}

/**
 * Odontologo para los selectores de citas, tratamientos, historias y odontograma.
 * GET /api/usuarios/odontologos: cualquier usuario autenticado puede pedirlo.
 */
export interface Odontologo {
  id: number;
  nombreCompleto: string;
}

/* ------------------------------ Autenticacion ---------------------------- */

export interface LoginRequest {
  usuario: string;
  password: string;
}

/** Respuesta de POST /api/auth/login: un JWT que se envia en cada peticion. */
export interface LoginResponse {
  /** Siempre "Bearer". */
  tokenType: string;
  accessToken: string;
  /** Segundos de vida del token (60 minutos). */
  expiresInSeconds: number;
  usuario: Usuario;
}
