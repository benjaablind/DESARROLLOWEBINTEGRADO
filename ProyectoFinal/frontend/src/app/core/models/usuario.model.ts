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

/* ------------------------------ Autenticacion ---------------------------- */

export interface LoginRequest {
  usuario: string;
  password: string;
}

export interface LoginResponse {
  autenticado: boolean;
  usuario: Usuario;
  mensaje: string;
}
