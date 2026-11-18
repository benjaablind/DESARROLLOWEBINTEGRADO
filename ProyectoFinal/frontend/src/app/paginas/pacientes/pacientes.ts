import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormArray,
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, finalize } from 'rxjs/operators';

import { AuthService } from '../../core/services/auth.service';
import { PacienteService } from '../../core/services/paciente.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { ErrorApi } from '../../core/models/comun.model';
import {
  EstadoPaciente,
  Paciente,
  PacienteRequest,
  PacienteResumen,
} from '../../core/models/paciente.model';
import {
  LONGITUD_MAXIMA_PACIENTE as MAX,
  dniValido,
  fechaPasada,
  sinSoloEspacios,
  telefonoValido,
} from '../../core/validators/pacientes.validators';

/** Antecedentes que el backend recibe como List<String> (AntecedentesRequest). */
type CampoLista = 'enfermedades' | 'alergias' | 'medicamentos' | 'habitos';

const CAMPOS_LISTA: { clave: CampoLista; etiqueta: string; singular: string }[] = [
  { clave: 'enfermedades', etiqueta: 'Enfermedades', singular: 'enfermedad' },
  { clave: 'alergias', etiqueta: 'Alergias', singular: 'alergia' },
  { clave: 'medicamentos', etiqueta: 'Medicamentos', singular: 'medicamento' },
  { clave: 'habitos', etiqueta: 'Hábitos', singular: 'hábito' },
];

/** Recorta cada elemento y descarta los que quedaron vacios. */
function limpiarLista(valores: string[]): string[] {
  return valores.map((valor) => valor.trim()).filter((valor) => valor.length > 0);
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
  private readonly auth = inject(AuthService);

  /** El asistente solo consulta: no registra, edita ni elimina pacientes. */
  protected readonly puedeModificar = this.auth.puedeModificarPacientesYCitas;

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

  /** Reglas de PacienteRequest.java; las longitudes son las de las columnas. */
  protected readonly formulario = this.fb.nonNullable.group({
    dni: ['', [Validators.required, dniValido()]],
    nombres: ['', [Validators.required, sinSoloEspacios(), Validators.maxLength(MAX.nombres)]],
    apellidos: ['', [Validators.required, sinSoloEspacios(), Validators.maxLength(MAX.apellidos)]],
    fechaNacimiento: ['', [Validators.required, fechaPasada()]],
    sexo: ['', [Validators.required, Validators.pattern(/^[MF]$/)]],
    telefono: ['', [Validators.required, telefonoValido()]],
    email: ['', [Validators.email, Validators.maxLength(MAX.email)]],
    direccion: ['', [Validators.maxLength(MAX.direccion)]],
    distrito: ['', [Validators.maxLength(MAX.distrito)]],
    ciudad: ['', [Validators.maxLength(MAX.ciudad)]],
    enfermedades: this.fb.nonNullable.array<string>([]),
    alergias: this.fb.nonNullable.array<string>([]),
    medicamentos: this.fb.nonNullable.array<string>([]),
    habitos: this.fb.nonNullable.array<string>([]),
    antecedentesOdontologicos: ['', [Validators.maxLength(MAX.textoAntecedente)]],
    observaciones: ['', [Validators.maxLength(MAX.textoAntecedente)]],
  });

  protected readonly camposLista = CAMPOS_LISTA;

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

    this.pacientes
      .listar(texto || undefined, estado || undefined)
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (filas) => this.lista.set(filas),
        error: (fallo: ErrorApi) => {
          this.lista.set([]);
          this.error.set(fallo?.mensaje ?? 'No se pudo cargar el listado de pacientes');
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
    // reset() no cambia cuantos elementos tiene un FormArray: se vacian antes.
    for (const campo of CAMPOS_LISTA) {
      this.elementos(campo.clave).clear();
    }
    this.formulario.reset();
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

    this.pacientes
      .buscarPorId(fila.id)
      .pipe(finalize(() => this.cargandoFicha.set(false)))
      .subscribe({
        next: (paciente) => this.volcarEnFormulario(paciente),
        error: (fallo: ErrorApi) =>
          this.errorFormulario.set(fallo?.mensaje ?? 'No se pudo cargar la ficha del paciente'),
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

    peticion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: (paciente) => {
        this.notificaciones.exito(
          id === null
            ? `Paciente ${paciente.nombreCompleto} registrado correctamente`
            : `Paciente ${paciente.nombreCompleto} actualizado correctamente`,
        );
        this.cerrarFormulario();
        this.cargar();
      },
      error: (fallo: ErrorApi) => {
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

    this.pacientes
      .cambiarEstado(fila.id, nuevo)
      .pipe(finalize(() => this.accionEnCurso.set(null)))
      .subscribe({
        next: () => {
          this.notificaciones.exito(
            nuevo === 'ACTIVO'
              ? `${fila.nombreCompleto} fue activado`
              : `${fila.nombreCompleto} fue desactivado`,
          );
          this.cargar();
        },
        error: (fallo: ErrorApi) =>
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo cambiar el estado del paciente'),
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

    this.pacientes
      .eliminar(fila.id)
      .pipe(
        finalize(() => {
          this.accionEnCurso.set(null);
          this.confirmarBaja.set(null);
        }),
      )
      .subscribe({
        next: () => {
          this.notificaciones.exito(`Paciente ${fila.nombreCompleto} eliminado`);
          this.cargar();
        },
        error: (fallo: ErrorApi) =>
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo eliminar el paciente'),
      });
  }

  /* ---------------------- Listas de antecedentes ------------------------ */

  protected elementos(clave: CampoLista): FormArray<FormControl<string>> {
    return this.formulario.controls[clave];
  }

  protected agregarElemento(clave: CampoLista, valor = ''): void {
    this.elementos(clave).push(
      this.fb.nonNullable.control(valor, [Validators.maxLength(MAX.elementoAntecedente)]),
    );
  }

  protected quitarElemento(clave: CampoLista, indice: number): void {
    this.elementos(clave).removeAt(indice);
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
    // Un FormArray necesita un control por elemento antes de recibir valores.
    for (const campo of CAMPOS_LISTA) {
      this.elementos(campo.clave).clear();
      for (const valor of paciente.antecedentes?.[campo.clave] ?? []) {
        this.agregarElemento(campo.clave, valor);
      }
    }

    this.formulario.patchValue({
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
        enfermedades: limpiarLista(v.enfermedades),
        alergias: limpiarLista(v.alergias),
        medicamentos: limpiarLista(v.medicamentos),
        habitos: limpiarLista(v.habitos),
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
