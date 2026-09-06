import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { NotificacionService } from '../../core/services/notificacion.service';
import { UsuarioService } from '../../core/services/usuario.service';
import {
  ActualizarUsuarioRequest,
  ErrorApi,
  ETIQUETAS_ROL,
  ROLES,
  Rol,
  Usuario,
  UsuarioRequest,
} from '../../core/models';

/** Panel lateral abierto en cada momento. */
type PanelAbierto = 'formulario' | 'password' | 'permisos' | null;

/** Filtro de estado de la cuenta. */
type FiltroEstado = 'todos' | 'activos' | 'inactivos';

/**
 * Administracion de las cuentas del personal.
 * El listado vive en la tabla y todas las acciones (alta, edicion, cambio de
 * contrasena, permisos y baja) se resuelven en paneles propios del componente,
 * sin librerias externas ni dialogos del navegador.
 */
@Component({
  selector: 'app-usuarios',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  template: `
    <div class="titulo-pagina">
      <div>
        <h1>Usuarios</h1>
        <span class="subtitulo">Cuentas del personal y roles</span>
      </div>
      <button type="button" class="btn btn-primario" (click)="abrirCreacion()">Nuevo usuario</button>
    </div>

    <!-- -------------------------------- Filtros ------------------------------- -->
    <div class="card card-compacta">
      <div class="filtros">
        <div class="form-campo">
          <label for="filtroRol">Rol</label>
          <select id="filtroRol" [value]="filtroRol()" (change)="cambiarRol($event)">
            <option value="">Todos los roles</option>
            @for (rol of roles; track rol) {
              <option [value]="rol">{{ etiquetaRol(rol) }}</option>
            }
          </select>
        </div>

        <div class="form-campo">
          <label for="filtroEstado">Estado</label>
          <select id="filtroEstado" [value]="filtroEstado()" (change)="cambiarEstado($event)">
            <option value="todos">Todos</option>
            <option value="activos">Activos</option>
            <option value="inactivos">Inactivos</option>
          </select>
        </div>

        <div class="espaciador"></div>

        <button
          type="button"
          class="btn btn-secundario"
          (click)="cargarUsuarios()"
          [disabled]="cargando()"
        >
          Recargar
        </button>
      </div>

      <!-- -------------------------------- Listado ----------------------------- -->
      @if (cargando()) {
        <div class="estado-vacio">
          <span class="spinner spinner-grande" aria-hidden="true"></span>
          <span class="cargando">Cargando usuarios...</span>
        </div>
      } @else if (error()) {
        <div class="alerta alerta-error" role="alert">{{ error() }}</div>
        <div class="estado-vacio">
          <button type="button" class="btn btn-primario" (click)="cargarUsuarios()">
            Reintentar
          </button>
        </div>
      } @else if (usuarios().length === 0) {
        <div class="estado-vacio">
          <strong>No hay usuarios que coincidan</strong>
          <p class="texto-suave">Ajuste los filtros o registre una cuenta nueva.</p>
          <button type="button" class="btn btn-primario" (click)="abrirCreacion()">
            Nuevo usuario
          </button>
        </div>
      } @else {
        <div class="tabla-scroll">
          <table class="tabla">
            <thead>
              <tr>
                <th scope="col">Nombre completo</th>
                <th scope="col">Usuario</th>
                <th scope="col">Correo</th>
                <th scope="col">Rol</th>
                <th scope="col">Estado</th>
                <th scope="col" class="celda-acciones">Acciones</th>
              </tr>
            </thead>
            <tbody>
              @for (usuario of usuarios(); track usuario.id) {
                <tr>
                  <td class="negrita">{{ usuario.nombreCompleto }}</td>
                  <td class="mono">{{ usuario.usuario }}</td>
                  <td class="texto-medio">{{ usuario.email }}</td>
                  <td>
                    <span class="badge" [class]="badgeRol(usuario.rol)">
                      {{ etiquetaRol(usuario.rol) }}
                    </span>
                  </td>
                  <td>
                    <span class="badge" [class]="usuario.activo ? 'badge-exito' : 'badge-peligro'">
                      {{ usuario.activo ? 'Activo' : 'Inactivo' }}
                    </span>
                  </td>
                  <td class="celda-acciones">
                    <div class="fila acciones-fila">
                      <button
                        type="button"
                        class="btn btn-secundario btn-sm"
                        (click)="abrirEdicion(usuario)"
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        class="btn btn-secundario btn-sm"
                        (click)="abrirCambioPassword(usuario)"
                      >
                        Contrasena
                      </button>
                      @if (usuario.activo) {
                        <button
                          type="button"
                          class="btn btn-secundario btn-sm"
                          (click)="desactivar(usuario)"
                          [disabled]="procesandoId() === usuario.id"
                        >
                          Desactivar
                        </button>
                      } @else {
                        <button
                          type="button"
                          class="btn btn-secundario btn-sm"
                          (click)="activar(usuario)"
                          [disabled]="procesandoId() === usuario.id"
                        >
                          Activar
                        </button>
                      }
                      <button
                        type="button"
                        class="btn btn-fantasma btn-sm"
                        (click)="abrirPermisos(usuario)"
                      >
                        Permisos
                      </button>
                      <button
                        type="button"
                        class="btn btn-peligro btn-sm"
                        (click)="pedirConfirmacion(usuario)"
                      >
                        Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>

    <!-- ------------------------------ Panel lateral --------------------------- -->
    @if (panel() !== null) {
      <div class="capa" (click)="cerrarPanel()"></div>

      <aside class="panel" role="dialog" aria-modal="true" [attr.aria-label]="tituloPanel()">
        <header class="panel-encabezado">
          <h2>{{ tituloPanel() }}</h2>
          <button
            type="button"
            class="btn btn-fantasma btn-sm"
            (click)="cerrarPanel()"
            aria-label="Cerrar"
          >
            Cerrar
          </button>
        </header>

        <div class="panel-cuerpo">
          <!-- ---------------------- Alta y edicion de usuario ------------------- -->
          @if (panel() === 'formulario') {
            @if (errorPanel()) {
              <div class="alerta alerta-error" role="alert">{{ errorPanel() }}</div>
            }

            <form [formGroup]="formulario" (ngSubmit)="guardar()" novalidate>
              <div class="form-campo">
                <label for="nombres">Nombres</label>
                <input id="nombres" type="text" formControlName="nombres" autocomplete="off" />
                @if (nombres.touched && nombres.invalid) {
                  <span class="form-error">Los nombres son obligatorios.</span>
                }
              </div>

              <div class="form-campo">
                <label for="apellidos">Apellidos</label>
                <input id="apellidos" type="text" formControlName="apellidos" autocomplete="off" />
                @if (apellidos.touched && apellidos.invalid) {
                  <span class="form-error">Los apellidos son obligatorios.</span>
                }
              </div>

              <div class="form-campo">
                <label for="email">Correo electronico</label>
                <input id="email" type="email" formControlName="email" autocomplete="off" />
                @if (email.touched && email.invalid) {
                  <span class="form-error">
                    @if (email.hasError('required')) {
                      El correo es obligatorio.
                    } @else {
                      Escriba un correo con formato valido.
                    }
                  </span>
                }
              </div>

              @if (modoFormulario() === 'crear') {
                <div class="form-campo">
                  <label for="usuario">Usuario</label>
                  <input id="usuario" type="text" formControlName="usuario" autocomplete="off" />
                  <span class="form-ayuda">Entre 4 y 30 caracteres.</span>
                  @if (usuarioCtrl.touched && usuarioCtrl.invalid) {
                    <span class="form-error">
                      @if (usuarioCtrl.hasError('required')) {
                        El usuario es obligatorio.
                      } @else {
                        El usuario debe tener entre 4 y 30 caracteres.
                      }
                    </span>
                  }
                </div>

                <div class="form-campo">
                  <label for="password">Contrasena</label>
                  <input
                    id="password"
                    type="password"
                    formControlName="password"
                    autocomplete="new-password"
                  />
                  <span class="form-ayuda">Minimo 6 caracteres.</span>
                  @if (password.touched && password.invalid) {
                    <span class="form-error">
                      @if (password.hasError('required')) {
                        La contrasena es obligatoria.
                      } @else {
                        La contrasena debe tener al menos 6 caracteres.
                      }
                    </span>
                  }
                </div>
              }

              <div class="form-campo">
                <label for="rol">Rol</label>
                <select id="rol" formControlName="rol">
                  @for (rol of roles; track rol) {
                    <option [value]="rol">{{ etiquetaRol(rol) }}</option>
                  }
                </select>
                @if (rolCtrl.touched && rolCtrl.invalid) {
                  <span class="form-error">Seleccione un rol.</span>
                }
              </div>

              <div class="form-acciones">
                <button type="button" class="btn btn-secundario" (click)="cerrarPanel()">
                  Cancelar
                </button>
                <button type="submit" class="btn btn-primario" [disabled]="guardando()">
                  @if (guardando()) {
                    <span class="spinner" aria-hidden="true"></span>
                  }
                  {{ modoFormulario() === 'crear' ? 'Crear usuario' : 'Guardar cambios' }}
                </button>
              </div>
            </form>
          }

          <!-- ------------------------ Cambio de contrasena ---------------------- -->
          @if (panel() === 'password') {
            <p class="texto-suave texto-pequeno">
              Cuenta: <span class="negrita">{{ seleccionado()?.nombreCompleto }}</span>
            </p>

            @if (errorPanel()) {
              <div class="alerta alerta-error" role="alert">{{ errorPanel() }}</div>
            }

            <form [formGroup]="formularioPassword" (ngSubmit)="guardarPassword()" novalidate>
              <div class="form-campo">
                <label for="passwordActual">Contrasena actual</label>
                <input
                  id="passwordActual"
                  type="password"
                  formControlName="passwordActual"
                  autocomplete="current-password"
                />
                @if (passwordActual.touched && passwordActual.invalid) {
                  <span class="form-error">
                    @if (passwordActual.hasError('required')) {
                      La contrasena actual es obligatoria.
                    } @else {
                      La contrasena debe tener al menos 6 caracteres.
                    }
                  </span>
                }
              </div>

              <div class="form-campo">
                <label for="passwordNuevo">Contrasena nueva</label>
                <input
                  id="passwordNuevo"
                  type="password"
                  formControlName="passwordNuevo"
                  autocomplete="new-password"
                />
                <span class="form-ayuda">Minimo 6 caracteres.</span>
                @if (passwordNuevo.touched && passwordNuevo.invalid) {
                  <span class="form-error">
                    @if (passwordNuevo.hasError('required')) {
                      La contrasena nueva es obligatoria.
                    } @else {
                      La contrasena debe tener al menos 6 caracteres.
                    }
                  </span>
                }
              </div>

              <div class="form-acciones">
                <button type="button" class="btn btn-secundario" (click)="cerrarPanel()">
                  Cancelar
                </button>
                <button type="submit" class="btn btn-primario" [disabled]="guardando()">
                  @if (guardando()) {
                    <span class="spinner" aria-hidden="true"></span>
                  }
                  Cambiar contrasena
                </button>
              </div>
            </form>
          }

          <!-- ------------------------------ Permisos ---------------------------- -->
          @if (panel() === 'permisos') {
            <p class="texto-suave texto-pequeno">
              Cuenta: <span class="negrita">{{ seleccionado()?.nombreCompleto }}</span>
            </p>

            @if (cargandoPermisos()) {
              <div class="estado-vacio">
                <span class="spinner spinner-grande" aria-hidden="true"></span>
                <span class="cargando">Cargando permisos...</span>
              </div>
            } @else if (errorPanel()) {
              <div class="alerta alerta-error" role="alert">{{ errorPanel() }}</div>
            } @else {
              <p class="texto-medio texto-pequeno">
                Rol asignado:
                <span class="badge" [class]="badgeRol(rolPermisos() ?? 'ASISTENTE')">
                  {{ etiquetaRol(rolPermisos() ?? 'ASISTENTE') }}
                </span>
              </p>

              <ul class="lista-permisos">
                @for (permiso of permisos(); track permiso) {
                  <li class="mono texto-pequeno">{{ permiso }}</li>
                } @empty {
                  <li class="texto-suave">Este rol no tiene permisos declarados.</li>
                }
              </ul>
            }

            <div class="form-acciones">
              <button type="button" class="btn btn-secundario" (click)="cerrarPanel()">
                Cerrar
              </button>
            </div>
          }
        </div>
      </aside>
    }

    <!-- --------------------- Confirmacion de eliminacion ---------------------- -->
    @if (porEliminar(); as candidato) {
      <div class="capa" (click)="cancelarEliminacion()"></div>

      <div class="dialogo" role="alertdialog" aria-modal="true" aria-label="Confirmar eliminacion">
        <h2>Eliminar usuario</h2>
        <p class="texto-medio">
          Se eliminara la cuenta de
          <span class="negrita">{{ candidato.nombreCompleto }}</span>
          ({{ candidato.usuario }}). Esta accion no se puede deshacer.
        </p>

        <div class="form-acciones">
          <button type="button" class="btn btn-secundario" (click)="cancelarEliminacion()">
            Cancelar
          </button>
          <button
            type="button"
            class="btn btn-peligro"
            (click)="confirmarEliminacion(candidato)"
            [disabled]="guardando()"
          >
            @if (guardando()) {
              <span class="spinner" aria-hidden="true"></span>
            }
            Eliminar
          </button>
        </div>
      </div>
    }
  `,
  styles: `
    :host {
      display: block;
    }

    .acciones-fila {
      justify-content: flex-end;
      flex-wrap: nowrap;
    }

    .capa {
      position: fixed;
      inset: 0;
      background: rgba(16, 42, 48, 0.45);
      z-index: 40;
    }

    .panel {
      position: fixed;
      top: 0;
      right: 0;
      bottom: 0;
      width: min(440px, 100%);
      display: flex;
      flex-direction: column;
      background: var(--c-superficie);
      border-left: 1px solid var(--c-borde);
      box-shadow: var(--s-4);
      z-index: 50;
    }

    .panel-encabezado {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--e-3);
      padding: var(--e-3) var(--e-5);
      border-bottom: 1px solid var(--c-borde);
      background: var(--c-superficie-2);
    }

    .panel-encabezado h2 {
      margin: 0;
      font-size: 1rem;
    }

    .panel-cuerpo {
      flex: 1 1 auto;
      overflow-y: auto;
      padding: var(--e-5);
    }

    .lista-permisos {
      list-style: none;
      margin: var(--e-3) 0 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: var(--e-1);
    }

    .lista-permisos li {
      padding: var(--e-2) var(--e-3);
      border: 1px solid var(--c-borde);
      border-radius: var(--r-sm);
      background: var(--c-superficie-2);
    }

    .dialogo {
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: min(430px, calc(100% - 2rem));
      padding: var(--e-5);
      background: var(--c-superficie);
      border: 1px solid var(--c-borde);
      border-radius: var(--r-lg);
      box-shadow: var(--s-4);
      z-index: 50;
    }

    .dialogo h2 {
      font-size: 1.1rem;
    }
  `,
})
export class UsuariosComponent {
  private readonly servicio = inject(UsuarioService);
  private readonly notificaciones = inject(NotificacionService);
  private readonly fb = inject(FormBuilder);

  protected readonly roles = ROLES;

  /* ------------------------------- Estado -------------------------------- */

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly filtroRol = signal<Rol | ''>('');
  protected readonly filtroEstado = signal<FiltroEstado>('todos');

  protected readonly panel = signal<PanelAbierto>(null);
  protected readonly modoFormulario = signal<'crear' | 'editar'>('crear');
  protected readonly seleccionado = signal<Usuario | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorPanel = signal<string | null>(null);
  protected readonly procesandoId = signal<number | null>(null);

  protected readonly permisos = signal<string[]>([]);
  protected readonly rolPermisos = signal<Rol | null>(null);
  protected readonly cargandoPermisos = signal(false);

  protected readonly porEliminar = signal<Usuario | null>(null);

  protected readonly tituloPanel = computed(() => {
    switch (this.panel()) {
      case 'formulario':
        return this.modoFormulario() === 'crear' ? 'Nuevo usuario' : 'Editar usuario';
      case 'password':
        return 'Cambiar contrasena';
      case 'permisos':
        return 'Permisos del usuario';
      default:
        return '';
    }
  });

  /* ----------------------------- Formularios ------------------------------ */

  protected readonly formulario = this.fb.nonNullable.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email]],
    usuario: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(30)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    rol: ['RECEPCIONISTA' as Rol, [Validators.required]],
  });

  protected readonly formularioPassword = this.fb.nonNullable.group({
    passwordActual: ['', [Validators.required, Validators.minLength(6)]],
    passwordNuevo: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected get nombres() {
    return this.formulario.controls.nombres;
  }
  protected get apellidos() {
    return this.formulario.controls.apellidos;
  }
  protected get email() {
    return this.formulario.controls.email;
  }
  protected get usuarioCtrl() {
    return this.formulario.controls.usuario;
  }
  protected get password() {
    return this.formulario.controls.password;
  }
  protected get rolCtrl() {
    return this.formulario.controls.rol;
  }
  protected get passwordActual() {
    return this.formularioPassword.controls.passwordActual;
  }
  protected get passwordNuevo() {
    return this.formularioPassword.controls.passwordNuevo;
  }

  constructor() {
    this.cargarUsuarios();
  }

  /* ------------------------------- Listado -------------------------------- */

  protected cargarUsuarios(): void {
    this.cargando.set(true);
    this.error.set(null);

    const rol = this.filtroRol() === '' ? undefined : (this.filtroRol() as Rol);
    const estado = this.filtroEstado();
    const activo = estado === 'todos' ? undefined : estado === 'activos';

    this.servicio.listar(rol, activo).subscribe({
      next: (lista) => {
        this.usuarios.set(lista ?? []);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.usuarios.set([]);
        this.error.set(fallo?.mensaje ?? 'No se pudo cargar la lista de usuarios');
        this.cargando.set(false);
      },
    });
  }

  protected cambiarRol(evento: Event): void {
    const valor = (evento.target as HTMLSelectElement).value;
    this.filtroRol.set(valor === '' ? '' : (valor as Rol));
    this.cargarUsuarios();
  }

  protected cambiarEstado(evento: Event): void {
    this.filtroEstado.set((evento.target as HTMLSelectElement).value as FiltroEstado);
    this.cargarUsuarios();
  }

  /* ------------------------------- Paneles -------------------------------- */

  protected abrirCreacion(): void {
    this.modoFormulario.set('crear');
    this.seleccionado.set(null);
    this.errorPanel.set(null);
    this.formulario.reset({
      nombres: '',
      apellidos: '',
      email: '',
      usuario: '',
      password: '',
      rol: 'RECEPCIONISTA',
    });
    this.usuarioCtrl.enable();
    this.password.enable();
    this.panel.set('formulario');
  }

  protected abrirEdicion(usuario: Usuario): void {
    this.modoFormulario.set('editar');
    this.seleccionado.set(usuario);
    this.errorPanel.set(null);
    this.formulario.reset({
      nombres: usuario.nombres,
      apellidos: usuario.apellidos,
      email: usuario.email,
      usuario: usuario.usuario,
      password: '',
      rol: usuario.rol,
    });
    // El backend actualiza sin credenciales: se desactivan para que no validen.
    this.usuarioCtrl.disable();
    this.password.disable();
    this.panel.set('formulario');
  }

  protected abrirCambioPassword(usuario: Usuario): void {
    this.seleccionado.set(usuario);
    this.errorPanel.set(null);
    this.formularioPassword.reset({ passwordActual: '', passwordNuevo: '' });
    this.panel.set('password');
  }

  protected abrirPermisos(usuario: Usuario): void {
    this.seleccionado.set(usuario);
    this.errorPanel.set(null);
    this.permisos.set([]);
    this.rolPermisos.set(null);
    this.cargandoPermisos.set(true);
    this.panel.set('permisos');

    this.servicio.permisos(usuario.id).subscribe({
      next: (respuesta) => {
        this.permisos.set(respuesta?.permisos ?? []);
        this.rolPermisos.set(respuesta?.rol ?? usuario.rol);
        this.cargandoPermisos.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.errorPanel.set(fallo?.mensaje ?? 'No se pudieron cargar los permisos');
        this.cargandoPermisos.set(false);
      },
    });
  }

  protected cerrarPanel(): void {
    this.panel.set(null);
    this.errorPanel.set(null);
    this.seleccionado.set(null);
    this.guardando.set(false);
  }

  /* ------------------------------- Acciones ------------------------------- */

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.errorPanel.set(null);
    this.guardando.set(true);
    const datos = this.formulario.getRawValue();

    if (this.modoFormulario() === 'crear') {
      const cuerpo: UsuarioRequest = {
        nombres: datos.nombres.trim(),
        apellidos: datos.apellidos.trim(),
        email: datos.email.trim(),
        usuario: datos.usuario.trim(),
        password: datos.password,
        rol: datos.rol,
      };

      this.servicio.crear(cuerpo).subscribe({
        next: (creado) => {
          this.guardando.set(false);
          this.notificaciones.exito(`Usuario "${creado.usuario}" creado correctamente`);
          this.cerrarPanel();
          this.cargarUsuarios();
        },
        error: (fallo: ErrorApi) => this.fallarPanel(fallo, 'No se pudo crear el usuario'),
      });
      return;
    }

    const actual = this.seleccionado();
    if (!actual) {
      this.guardando.set(false);
      return;
    }

    const cuerpo: ActualizarUsuarioRequest = {
      nombres: datos.nombres.trim(),
      apellidos: datos.apellidos.trim(),
      email: datos.email.trim(),
      rol: datos.rol,
    };

    this.servicio.actualizar(actual.id, cuerpo).subscribe({
      next: (actualizado) => {
        this.guardando.set(false);
        this.notificaciones.exito(`Usuario "${actualizado.usuario}" actualizado`);
        this.cerrarPanel();
        this.cargarUsuarios();
      },
      error: (fallo: ErrorApi) => this.fallarPanel(fallo, 'No se pudo actualizar el usuario'),
    });
  }

  protected guardarPassword(): void {
    const actual = this.seleccionado();
    if (!actual) {
      return;
    }

    if (this.formularioPassword.invalid) {
      this.formularioPassword.markAllAsTouched();
      return;
    }

    this.errorPanel.set(null);
    this.guardando.set(true);
    const datos = this.formularioPassword.getRawValue();

    this.servicio
      .cambiarPassword(actual.id, {
        passwordActual: datos.passwordActual,
        passwordNuevo: datos.passwordNuevo,
      })
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.notificaciones.exito(`Contrasena de "${actual.usuario}" actualizada`);
          this.cerrarPanel();
        },
        // El backend responde 400 con el detalle exacto: se muestra tal cual.
        error: (fallo: ErrorApi) => this.fallarPanel(fallo, 'No se pudo cambiar la contrasena'),
      });
  }

  protected activar(usuario: Usuario): void {
    this.procesandoId.set(usuario.id);

    this.servicio.activar(usuario.id).subscribe({
      next: () => {
        this.procesandoId.set(null);
        this.notificaciones.exito(`Usuario "${usuario.usuario}" activado`);
        this.cargarUsuarios();
      },
      error: (fallo: ErrorApi) => {
        this.procesandoId.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo activar el usuario');
      },
    });
  }

  protected desactivar(usuario: Usuario): void {
    this.procesandoId.set(usuario.id);

    this.servicio.desactivar(usuario.id).subscribe({
      next: () => {
        this.procesandoId.set(null);
        this.notificaciones.exito(`Usuario "${usuario.usuario}" desactivado`);
        this.cargarUsuarios();
      },
      error: (fallo: ErrorApi) => {
        this.procesandoId.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo desactivar el usuario');
      },
    });
  }

  protected pedirConfirmacion(usuario: Usuario): void {
    this.porEliminar.set(usuario);
  }

  protected cancelarEliminacion(): void {
    this.porEliminar.set(null);
  }

  protected confirmarEliminacion(usuario: Usuario): void {
    this.guardando.set(true);

    this.servicio.eliminar(usuario.id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.porEliminar.set(null);
        this.notificaciones.exito(`Usuario "${usuario.usuario}" eliminado`);
        this.cargarUsuarios();
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        this.porEliminar.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo eliminar el usuario');
      },
    });
  }

  /* ------------------------------- Auxiliares ----------------------------- */

  private fallarPanel(fallo: ErrorApi, respaldo: string): void {
    this.guardando.set(false);
    const mensaje = fallo?.mensaje ?? respaldo;
    this.errorPanel.set(mensaje);
    this.notificaciones.error(mensaje);
  }

  protected etiquetaRol(rol: Rol): string {
    return ETIQUETAS_ROL[rol] ?? rol;
  }

  protected badgeRol(rol: Rol): string {
    switch (rol) {
      case 'ADMINISTRADOR':
        return 'badge-primario';
      case 'ODONTOLOGO':
        return 'badge-info';
      case 'RECEPCIONISTA':
        return 'badge-neutro';
      case 'ASISTENTE':
        return 'badge-advertencia';
      default:
        return 'badge-neutro';
    }
  }
}
