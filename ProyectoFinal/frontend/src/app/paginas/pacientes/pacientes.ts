import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { PacienteService } from '../../core/services/paciente.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { ErrorApi } from '../../core/models/comun.model';
import {
  EstadoPaciente,
  Paciente,
  PacienteRequest,
  PacienteResumen,
} from '../../core/models/paciente.model';

/** Convierte un texto separado por comas en un arreglo limpio. */
function aLista(texto: string): string[] {
  return texto
    .split(',')
    .map((parte) => parte.trim())
    .filter((parte) => parte.length > 0);
}

/** Convierte un arreglo del backend en el texto separado por comas del formulario. */
function deLista(valores: string[] | null | undefined): string {
  return (valores ?? []).join(', ');
}

/** La fecha de nacimiento tiene que ser anterior al dia de hoy. */
function fechaPasada(control: AbstractControl): ValidationErrors | null {
  const valor = (control.value ?? '') as string;
  if (!valor) {
    return null;
  }

  const partes = valor.split('-').map((p) => Number(p));
  if (partes.length !== 3 || partes.some((n) => Number.isNaN(n))) {
    return { fechaInvalida: true };
  }

  const fecha = new Date(partes[0], partes[1] - 1, partes[2]);
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);

  return fecha.getTime() < hoy.getTime() ? null : { fechaFutura: true };
}

/** Listado de pacientes con alta, edicion, cambio de estado y baja. */
@Component({
  selector: 'app-pacientes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './pacientes.html',
  styleUrl: './pacientes.scss',
})
export class PacientesComponent {
  private readonly fb = inject(FormBuilder);
  private readonly pacientes = inject(PacienteService);
  private readonly notificaciones = inject(NotificacionService);

  /* ------------------------------ Listado ------------------------------- */

  protected readonly lista = signal<PacienteResumen[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  /** Id del paciente sobre el que corre una accion (estado o baja). */
  protected readonly accionEnCurso = signal<number | null>(null);

  protected readonly filtroTexto = new FormControl('', { nonNullable: true });
  protected readonly filtroEstado = new FormControl<'' | EstadoPaciente>('', {
    nonNullable: true,
  });

  protected readonly hayFiltros = computed(
    () => this.filtroTexto.value.trim().length > 0 || this.filtroEstado.value !== '',
  );

  /* ---------------------------- Formulario ------------------------------ */

  /** null = panel cerrado; 0 = alta; id > 0 = edicion. */
  protected readonly formularioAbierto = signal(false);
  protected readonly editandoId = signal<number | null>(null);
  protected readonly guardando = signal(false);
  protected readonly cargandoFicha = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly confirmarBaja = signal<PacienteResumen | null>(null);

  protected readonly formulario = this.fb.nonNullable.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.maxLength(100)]],
    fechaNacimiento: ['', [Validators.required, fechaPasada]],
    sexo: ['', [Validators.required]],
    telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{6,15}$/)]],
    email: ['', [Validators.email]],
    direccion: ['', [Validators.maxLength(180)]],
    distrito: ['', [Validators.maxLength(80)]],
    ciudad: ['', [Validators.maxLength(80)]],
    enfermedades: [''],
    alergias: [''],
    medicamentos: [''],
    habitos: [''],
    antecedentesOdontologicos: [''],
    observaciones: [''],
  });

  protected get c() {
    return this.formulario.controls;
  }

  constructor() {
    this.filtroTexto.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => this.cargar());

    this.filtroEstado.valueChanges
      .pipe(distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => this.cargar());

    this.cargar();
  }

  /* ------------------------------ Consultas ----------------------------- */

  protected cargar(): void {
    const texto = this.filtroTexto.value.trim();
    const estado = this.filtroEstado.value;

    this.cargando.set(true);
    this.error.set(null);

    this.pacientes.listar(texto || undefined, estado || undefined).subscribe({
      next: (filas) => {
        this.lista.set(filas);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.lista.set([]);
        this.error.set(fallo?.mensaje ?? 'No se pudo cargar el listado de pacientes');
        this.cargando.set(false);
      },
    });
  }

  protected limpiarFiltros(): void {
    this.filtroTexto.setValue('', { emitEvent: false });
    this.filtroEstado.setValue('', { emitEvent: false });
    this.cargar();
  }

  /* ---------------------------- Alta y edicion -------------------------- */

  protected abrirCreacion(): void {
    this.formulario.reset({
      dni: '',
      nombres: '',
      apellidos: '',
      fechaNacimiento: '',
      sexo: '',
      telefono: '',
      email: '',
      direccion: '',
      distrito: '',
      ciudad: '',
      enfermedades: '',
      alergias: '',
      medicamentos: '',
      habitos: '',
      antecedentesOdontologicos: '',
      observaciones: '',
    });
    this.editandoId.set(null);
    this.errorFormulario.set(null);
    this.cargandoFicha.set(false);
    this.formularioAbierto.set(true);
  }

  protected abrirEdicion(fila: PacienteResumen): void {
    this.editandoId.set(fila.id);
    this.errorFormulario.set(null);
    this.formularioAbierto.set(true);
    this.cargandoFicha.set(true);

    this.pacientes.buscarPorId(fila.id).subscribe({
      next: (paciente) => {
        this.volcarEnFormulario(paciente);
        this.cargandoFicha.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.cargandoFicha.set(false);
        this.errorFormulario.set(fallo?.mensaje ?? 'No se pudo cargar la ficha del paciente');
      },
    });
  }

  protected cerrarFormulario(): void {
    this.formularioAbierto.set(false);
    this.editandoId.set(null);
    this.errorFormulario.set(null);
  }

  protected guardar(): void {
    if (this.cargandoFicha() || this.guardando()) {
      return;
    }

    this.errorFormulario.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.errorFormulario.set('Revise los campos marcados en rojo.');
      return;
    }

    const datos = this.armarRequest();
    const id = this.editandoId();
    this.guardando.set(true);

    const peticion = id === null ? this.pacientes.crear(datos) : this.pacientes.actualizar(id, datos);

    peticion.subscribe({
      next: (paciente) => {
        this.guardando.set(false);
        this.notificaciones.exito(
          id === null
            ? `Paciente ${paciente.nombreCompleto} registrado correctamente`
            : `Paciente ${paciente.nombreCompleto} actualizado correctamente`,
        );
        this.cerrarFormulario();
        this.cargar();
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        const mensaje = fallo?.mensaje ?? 'No se pudo guardar el paciente';
        this.errorFormulario.set(mensaje);
        this.notificaciones.error(mensaje);
      },
    });
  }

  /* --------------------------- Estado y baja ---------------------------- */

  protected alternarEstado(fila: PacienteResumen): void {
    if (this.accionEnCurso() !== null) {
      return;
    }

    const nuevo: EstadoPaciente = fila.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    this.accionEnCurso.set(fila.id);

    this.pacientes.cambiarEstado(fila.id, nuevo).subscribe({
      next: () => {
        this.accionEnCurso.set(null);
        this.notificaciones.exito(
          nuevo === 'ACTIVO'
            ? `${fila.nombreCompleto} fue activado`
            : `${fila.nombreCompleto} fue desactivado`,
        );
        this.cargar();
      },
      error: (fallo: ErrorApi) => {
        this.accionEnCurso.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo cambiar el estado del paciente');
      },
    });
  }

  protected pedirBaja(fila: PacienteResumen): void {
    this.confirmarBaja.set(fila);
  }

  protected cancelarBaja(): void {
    this.confirmarBaja.set(null);
  }

  protected confirmarEliminacion(): void {
    const fila = this.confirmarBaja();
    if (!fila) {
      return;
    }

    this.accionEnCurso.set(fila.id);

    this.pacientes.eliminar(fila.id).subscribe({
      next: () => {
        this.accionEnCurso.set(null);
        this.confirmarBaja.set(null);
        this.notificaciones.exito(`Paciente ${fila.nombreCompleto} eliminado`);
        this.cargar();
      },
      error: (fallo: ErrorApi) => {
        this.accionEnCurso.set(null);
        this.confirmarBaja.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo eliminar el paciente');
      },
    });
  }

  /* ------------------------------- Apoyo -------------------------------- */

  protected claseEstado(estado: EstadoPaciente): string {
    return estado === 'ACTIVO' ? 'badge badge-exito' : 'badge badge-neutro';
  }

  protected etiquetaEstado(estado: EstadoPaciente): string {
    return estado === 'ACTIVO' ? 'Activo' : 'Inactivo';
  }

  protected textoEdad(edad: number | null): string {
    return edad === null || edad === undefined ? '-' : `${edad} años`;
  }

  private volcarEnFormulario(paciente: Paciente): void {
    this.formulario.setValue({
      dni: paciente.dni ?? '',
      nombres: paciente.nombres ?? '',
      apellidos: paciente.apellidos ?? '',
      fechaNacimiento: (paciente.fechaNacimiento ?? '').slice(0, 10),
      sexo: paciente.sexo ?? '',
      telefono: paciente.telefono ?? '',
      email: paciente.email ?? '',
      direccion: paciente.direccion ?? '',
      distrito: paciente.distrito ?? '',
      ciudad: paciente.ciudad ?? '',
      enfermedades: deLista(paciente.antecedentes?.enfermedades),
      alergias: deLista(paciente.antecedentes?.alergias),
      medicamentos: deLista(paciente.antecedentes?.medicamentos),
      habitos: deLista(paciente.antecedentes?.habitos),
      antecedentesOdontologicos: paciente.antecedentes?.antecedentesOdontologicos ?? '',
      observaciones: paciente.antecedentes?.observaciones ?? '',
    });
  }

  private armarRequest(): PacienteRequest {
    const v = this.formulario.getRawValue();
    return {
      dni: v.dni.trim(),
      nombres: v.nombres.trim(),
      apellidos: v.apellidos.trim(),
      fechaNacimiento: v.fechaNacimiento || null,
      sexo: v.sexo || null,
      telefono: this.opcional(v.telefono),
      email: this.opcional(v.email),
      direccion: this.opcional(v.direccion),
      distrito: this.opcional(v.distrito),
      ciudad: this.opcional(v.ciudad),
      antecedentes: {
        enfermedades: aLista(v.enfermedades),
        alergias: aLista(v.alergias),
        medicamentos: aLista(v.medicamentos),
        habitos: aLista(v.habitos),
        antecedentesOdontologicos: this.opcional(v.antecedentesOdontologicos),
        observaciones: this.opcional(v.observaciones),
      },
    };
  }

  private opcional(valor: string): string | null {
    const limpio = valor.trim();
    return limpio.length > 0 ? limpio : null;
  }
}
