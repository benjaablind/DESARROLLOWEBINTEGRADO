import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { NotificacionService } from '../../core/services/notificacion.service';
import { PacienteService } from '../../core/services/paciente.service';
import { TratamientoService } from '../../core/services/tratamiento.service';
import { UsuarioService } from '../../core/services/usuario.service';
import {
  BADGE_ESTADO_TRATAMIENTO,
  ESTADOS_TRATAMIENTO,
  ETIQUETAS_ESTADO_TRATAMIENTO,
  ErrorApi,
  EstadoTratamiento,
  PacienteResumen,
  SesionTratamiento,
  Tratamiento,
  TratamientoRequest,
  Usuario,
} from '../../core/models';

/** Estados en los que el tratamiento ya no admite cambios. */
const ESTADOS_FINALES: EstadoTratamiento[] = ['COMPLETADO', 'CANCELADO'];

/** Confirmacion pendiente de borrado de una sesion. */
interface BorradoSesion {
  tratamientoId: number;
  sesion: SesionTratamiento;
}

/**
 * Pantalla de tratamientos: filtros, tarjetas expandibles con el plan de
 * sesiones, cambio de estado y alta/edicion en un dialogo propio.
 */
@Component({
  selector: 'app-tratamientos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './tratamientos.html',
  styleUrl: './tratamientos.scss',
})
export class TratamientosComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly tratamientoService = inject(TratamientoService);
  private readonly pacienteService = inject(PacienteService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly notificaciones = inject(NotificacionService);
  private readonly ruta = inject(ActivatedRoute);

  /* ------------------------------- Catalogos ----------------------------- */

  protected readonly estados = ESTADOS_TRATAMIENTO;
  protected readonly etiquetasEstado = ETIQUETAS_ESTADO_TRATAMIENTO;
  protected readonly badgesEstado = BADGE_ESTADO_TRATAMIENTO;

  protected readonly pacientes = signal<PacienteResumen[]>([]);
  protected readonly odontologos = signal<Usuario[]>([]);

  /* --------------------------------- Estado ------------------------------ */

  protected readonly tratamientos = signal<Tratamiento[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Aviso puntual de una accion fallida, sin tapar el listado. */
  protected readonly aviso = signal<string | null>(null);
  protected readonly guardando = signal(false);
  protected readonly expandido = signal<number | null>(null);

  /* --------------------------------- Filtros ----------------------------- */

  protected readonly filtroPaciente = signal<number | null>(null);
  protected readonly filtroEstado = signal<EstadoTratamiento | ''>('');
  protected readonly soloActivos = signal(false);

  /** El filtro "solo activos" se resuelve en el cliente: la API no lo expone. */
  protected readonly visibles = computed(() => {
    const lista = this.tratamientos();
    return this.soloActivos() ? lista.filter((t) => !this.esFinal(t.estado)) : lista;
  });

  /* -------------------------------- Dialogos ----------------------------- */

  protected readonly formularioAbierto = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly sesionAbiertaEn = signal<number | null>(null);
  protected readonly errorSesion = signal<string | null>(null);

  protected readonly porEliminar = signal<Tratamiento | null>(null);
  protected readonly sesionPorEliminar = signal<BorradoSesion | null>(null);

  /* ------------------------------ Formularios ---------------------------- */

  protected readonly formulario = this.fb.nonNullable.group({
    pacienteId: ['', Validators.required],
    odontologoId: ['', Validators.required],
    nombre: ['', [Validators.required, Validators.minLength(3)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0.01)]],
    fechaInicio: [''],
    observaciones: [''],
  });

  protected readonly formularioSesion = this.fb.nonNullable.group({
    fecha: [''],
    descripcion: ['', [Validators.required, Validators.minLength(3)]],
    observaciones: [''],
  });

  protected get campos() {
    return this.formulario.controls;
  }

  protected get camposSesion() {
    return this.formularioSesion.controls;
  }

  /* --------------------------------- Ciclo ------------------------------- */

  ngOnInit(): void {
    const pacienteId = Number(this.ruta.snapshot.queryParamMap.get('pacienteId'));
    if (Number.isFinite(pacienteId) && pacienteId > 0) {
      this.filtroPaciente.set(pacienteId);
    }

    this.pacienteService.listar().subscribe({
      next: (lista) => this.pacientes.set(lista),
      error: () => this.pacientes.set([]),
    });

    this.usuarioService.listarOdontologos().subscribe({
      next: (lista) => this.odontologos.set(lista),
      error: () => this.odontologos.set([]),
    });

    this.cargar();
  }

  /* --------------------------------- Datos ------------------------------- */

  protected cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.aviso.set(null);

    this.tratamientoService
      .listar({
        pacienteId: this.filtroPaciente() ?? undefined,
        estado: this.filtroEstado() || undefined,
      })
      .subscribe({
        next: (lista) => {
          this.tratamientos.set(lista);
          this.cargando.set(false);
        },
        error: (fallo: ErrorApi) => {
          this.tratamientos.set([]);
          this.error.set(fallo?.mensaje ?? 'No se pudieron cargar los tratamientos');
          this.cargando.set(false);
        },
      });
  }

  protected cambiarFiltroPaciente(valor: string): void {
    this.filtroPaciente.set(valor ? Number(valor) : null);
    this.cargar();
  }

  protected cambiarFiltroEstado(valor: string): void {
    this.filtroEstado.set((valor as EstadoTratamiento) || '');
    this.cargar();
  }

  protected cambiarSoloActivos(marcado: boolean): void {
    this.soloActivos.set(marcado);
  }

  protected cerrarAviso(): void {
    this.aviso.set(null);
  }

  protected limpiarFiltros(): void {
    this.filtroPaciente.set(null);
    this.filtroEstado.set('');
    this.soloActivos.set(false);
    this.cargar();
  }

  /* -------------------------------- Ayudas ------------------------------- */

  /** Importe en soles con dos decimales. */
  protected soles(valor: number | null | undefined): string {
    const numero = typeof valor === 'number' && Number.isFinite(valor) ? valor : 0;
    return `S/ ${numero.toLocaleString('es-PE', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  /** Fecha ISO "YYYY-MM-DD" mostrada como "DD/MM/YYYY". */
  protected fecha(iso: string | null | undefined): string {
    if (!iso) {
      return '—';
    }
    const partes = iso.slice(0, 10).split('-');
    return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : iso;
  }

  protected esFinal(estado: EstadoTratamiento): boolean {
    return ESTADOS_FINALES.includes(estado);
  }

  /** Porcentaje pagado, acotado entre 0 y 100, para la barra de progreso. */
  protected porcentajePagado(t: Tratamiento): number {
    if (!t.precio || t.precio <= 0) {
      return 0;
    }
    const porcentaje = Math.round((t.totalPagado / t.precio) * 100);
    return Math.max(0, Math.min(100, porcentaje));
  }

  protected sesionesRealizadas(t: Tratamiento): number {
    return t.sesiones.filter((s) => s.realizada).length;
  }

  protected alternar(id: number): void {
    this.expandido.update((actual) => (actual === id ? null : id));
    this.sesionAbiertaEn.set(null);
  }

  private reemplazar(actualizado: Tratamiento): void {
    this.tratamientos.update((lista) =>
      lista.map((t) => (t.id === actualizado.id ? actualizado : t)),
    );
  }

  /* ------------------------- Alta y edicion ------------------------------ */

  protected abrirCrear(): void {
    this.editandoId.set(null);
    this.errorFormulario.set(null);
    this.formulario.reset({
      pacienteId: this.filtroPaciente() ? String(this.filtroPaciente()) : '',
      odontologoId: '',
      nombre: '',
      descripcion: '',
      precio: 0,
      fechaInicio: '',
      observaciones: '',
    });
    this.formularioAbierto.set(true);
  }

  protected abrirEditar(t: Tratamiento): void {
    if (this.esFinal(t.estado)) {
      return;
    }
    this.editandoId.set(t.id);
    this.errorFormulario.set(null);
    this.formulario.reset({
      pacienteId: String(t.pacienteId),
      odontologoId: String(t.odontologoId),
      nombre: t.nombre,
      descripcion: t.descripcion ?? '',
      precio: t.precio,
      fechaInicio: t.fechaInicio ? t.fechaInicio.slice(0, 10) : '',
      observaciones: t.observaciones ?? '',
    });
    this.formularioAbierto.set(true);
  }

  protected cerrarFormulario(): void {
    this.formularioAbierto.set(false);
    this.editandoId.set(null);
    this.errorFormulario.set(null);
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const datos: TratamientoRequest = {
      pacienteId: Number(valores.pacienteId),
      odontologoId: Number(valores.odontologoId),
      nombre: valores.nombre.trim(),
      descripcion: valores.descripcion.trim() || null,
      precio: Number(valores.precio),
      fechaInicio: valores.fechaInicio || null,
      observaciones: valores.observaciones.trim() || null,
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    const id = this.editandoId();
    const peticion = id
      ? this.tratamientoService.actualizar(id, datos)
      : this.tratamientoService.crear(datos);

    peticion.subscribe({
      next: (guardado) => {
        this.guardando.set(false);
        this.cerrarFormulario();
        this.notificaciones.exito(
          id ? 'Tratamiento actualizado correctamente' : 'Tratamiento registrado correctamente',
        );
        if (id) {
          this.reemplazar(guardado);
        } else {
          this.cargar();
          this.expandido.set(guardado.id);
        }
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        this.errorFormulario.set(fallo?.mensaje ?? 'No se pudo guardar el tratamiento');
      },
    });
  }

  /* ----------------------------- Cambio de estado ------------------------ */

  protected cambiarEstado(t: Tratamiento, evento: Event): void {
    const control = evento.target as HTMLSelectElement;
    const nuevo = control.value as EstadoTratamiento;
    if (!nuevo || nuevo === t.estado || this.esFinal(t.estado)) {
      return;
    }

    this.tratamientoService.cambiarEstado(t.id, nuevo).subscribe({
      next: (actualizado) => {
        this.reemplazar(actualizado);
        this.notificaciones.exito(
          `El tratamiento pasó a «${this.etiquetasEstado[actualizado.estado]}»`,
        );
      },
      error: (fallo: ErrorApi) => {
        // Se devuelve el select al estado real del tratamiento.
        control.value = t.estado;
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo cambiar el estado');
      },
    });
  }

  /* -------------------------------- Sesiones ----------------------------- */

  protected abrirSesion(t: Tratamiento): void {
    if (this.esFinal(t.estado)) {
      return;
    }
    this.errorSesion.set(null);
    this.formularioSesion.reset({ fecha: '', descripcion: '', observaciones: '' });
    this.sesionAbiertaEn.set(t.id);
  }

  protected cerrarSesion(): void {
    this.sesionAbiertaEn.set(null);
    this.errorSesion.set(null);
  }

  protected guardarSesion(): void {
    const tratamientoId = this.sesionAbiertaEn();
    if (!tratamientoId) {
      return;
    }
    if (this.formularioSesion.invalid) {
      this.formularioSesion.markAllAsTouched();
      return;
    }

    const valores = this.formularioSesion.getRawValue();
    this.guardando.set(true);
    this.errorSesion.set(null);

    this.tratamientoService
      .agregarSesion(tratamientoId, {
        fecha: valores.fecha || null,
        descripcion: valores.descripcion.trim(),
        observaciones: valores.observaciones.trim() || null,
      })
      .subscribe({
        next: (actualizado) => {
          this.guardando.set(false);
          this.reemplazar(actualizado);
          this.cerrarSesion();
          this.notificaciones.exito('Sesión agregada al plan');
        },
        error: (fallo: ErrorApi) => {
          this.guardando.set(false);
          this.errorSesion.set(fallo?.mensaje ?? 'No se pudo agregar la sesión');
        },
      });
  }

  protected alternarSesion(t: Tratamiento, sesion: SesionTratamiento, evento: Event): void {
    const casilla = evento.target as HTMLInputElement;
    const realizada = casilla.checked;

    if (this.esFinal(t.estado)) {
      casilla.checked = sesion.realizada;
      return;
    }

    this.tratamientoService
      .cambiarEstadoSesion(t.id, sesion.id, {
        realizada,
        observaciones: sesion.observaciones,
      })
      .subscribe({
        next: (actualizado) => {
          this.reemplazar(actualizado);
          this.notificaciones.exito(
            realizada ? 'Sesión marcada como realizada' : 'Sesión marcada como pendiente',
          );
        },
        error: (fallo: ErrorApi) => {
          // Se devuelve la casilla al valor real de la sesion.
          casilla.checked = sesion.realizada;
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo actualizar la sesión');
        },
      });
  }

  protected pedirEliminarSesion(t: Tratamiento, sesion: SesionTratamiento): void {
    if (this.esFinal(t.estado)) {
      return;
    }
    this.sesionPorEliminar.set({ tratamientoId: t.id, sesion });
  }

  protected cancelarEliminarSesion(): void {
    this.sesionPorEliminar.set(null);
  }

  protected confirmarEliminarSesion(): void {
    const objetivo = this.sesionPorEliminar();
    if (!objetivo) {
      return;
    }

    this.guardando.set(true);
    this.tratamientoService.eliminarSesion(objetivo.tratamientoId, objetivo.sesion.id).subscribe({
      next: (actualizado) => {
        this.guardando.set(false);
        this.sesionPorEliminar.set(null);
        if (actualizado) {
          this.reemplazar(actualizado);
        } else {
          this.cargar();
        }
        this.notificaciones.exito('Sesión eliminada');
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        this.sesionPorEliminar.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo eliminar la sesión');
      },
    });
  }

  /* -------------------------------- Eliminar ----------------------------- */

  protected pedirEliminar(t: Tratamiento): void {
    this.porEliminar.set(t);
  }

  protected cancelarEliminar(): void {
    this.porEliminar.set(null);
  }

  protected confirmarEliminar(): void {
    const objetivo = this.porEliminar();
    if (!objetivo) {
      return;
    }

    this.guardando.set(true);
    this.tratamientoService.eliminar(objetivo.id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.porEliminar.set(null);
        this.tratamientos.update((lista) => lista.filter((t) => t.id !== objetivo.id));
        this.notificaciones.exito('Tratamiento eliminado');
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        this.porEliminar.set(null);
        // El backend responde 400 cuando el tratamiento ya tiene pagos registrados.
        const mensaje = fallo?.mensaje ?? 'No se pudo eliminar el tratamiento';
        this.notificaciones.error(mensaje);
        this.aviso.set(mensaje);
      },
    });
  }
}
