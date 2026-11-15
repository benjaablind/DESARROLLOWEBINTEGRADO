import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { CitaService } from '../../core/services/cita.service';
import { HistoriaClinicaService } from '../../core/services/historia-clinica.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { PacienteService } from '../../core/services/paciente.service';
import { UsuarioService } from '../../core/services/usuario.service';
import {
  Cita,
  ErrorApi,
  HistoriaClinica,
  HistoriaClinicaRequest,
  PacienteResumen,
  Odontologo,
} from '../../core/models';
import {
  LONGITUDES_HISTORIA,
  citaDelPaciente,
  sinSoloEspacios,
} from '../../core/validators/historia.validators';

/** Historia clinica de un paciente: linea de tiempo de sus atenciones. */
@Component({
  selector: 'app-historias',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './historias.html',
  styleUrl: './historias.scss',
})
export class HistoriasComponent {
  private readonly fb = inject(FormBuilder);
  private readonly historiaService = inject(HistoriaClinicaService);
  private readonly pacienteService = inject(PacienteService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly citaService = inject(CitaService);
  private readonly notificaciones = inject(NotificacionService);
  private readonly auth = inject(AuthService);
  private readonly ruta = inject(ActivatedRoute);

  /* ------------------------------- Catalogos ------------------------------ */

  protected readonly pacientes = signal<PacienteResumen[]>([]);
  protected readonly odontologos = signal<Odontologo[]>([]);
  protected readonly citasDelPaciente = signal<Cita[]>([]);

  /* --------------------------------- Estado ------------------------------- */

  protected readonly pacienteId = signal<string>('');
  protected readonly historias = signal<HistoriaClinica[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly expandidas = signal<number[]>([]);

  protected readonly formAbierto = signal(false);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  protected readonly historiaAAnular = signal<HistoriaClinica | null>(null);
  protected readonly anulando = signal(false);

  /* ------------------------------ Formularios ----------------------------- */

  /** Longitudes maximas, para validar y para los mensajes de error. */
  protected readonly maximos = LONGITUDES_HISTORIA;

  /**
   * Refleja HistoriaClinicaRequest: motivo y diagnostico son @NotBlank, y la
   * regla entre campos de la cita vive en el grupo (ver citaDelPaciente).
   */
  protected readonly formulario = this.fb.nonNullable.group(
    {
      pacienteId: ['', Validators.required],
      odontologoId: ['', Validators.required],
      citaId: [''],
      motivoConsulta: [
        '',
        [
          Validators.required,
          sinSoloEspacios(),
          Validators.maxLength(LONGITUDES_HISTORIA.motivoConsulta),
        ],
      ],
      diagnostico: [
        '',
        [
          Validators.required,
          sinSoloEspacios(),
          Validators.maxLength(LONGITUDES_HISTORIA.textoClinico),
        ],
      ],
      anamnesis: ['', Validators.maxLength(LONGITUDES_HISTORIA.textoClinico)],
      examenClinico: ['', Validators.maxLength(LONGITUDES_HISTORIA.textoClinico)],
      procedimiento: ['', Validators.maxLength(LONGITUDES_HISTORIA.textoClinico)],
      tratamiento: ['', Validators.maxLength(LONGITUDES_HISTORIA.textoClinico)],
      medicamentos: ['', Validators.maxLength(LONGITUDES_HISTORIA.textoClinico)],
      observaciones: ['', Validators.maxLength(LONGITUDES_HISTORIA.textoClinico)],
    },
    { validators: citaDelPaciente(() => this.citasDelPaciente()) },
  );

  /**
   * Refleja HistoriaAnularRequest: el motivo es @NotBlank. El minimo de 5
   * caracteres es propio del formulario (el backend no lo exige) y se conserva
   * para que el motivo diga algo.
   */
  protected readonly formAnular = this.fb.nonNullable.group({
    motivo: [
      '',
      [
        Validators.required,
        sinSoloEspacios(),
        Validators.minLength(5),
        Validators.maxLength(LONGITUDES_HISTORIA.motivoAnulacion),
      ],
    ],
  });

  constructor() {
    this.cargarCatalogos();

    const pacienteId = this.ruta.snapshot.queryParamMap.get('pacienteId');
    if (pacienteId) {
      this.pacienteId.set(pacienteId);
      this.cargarHistorias();
    }
  }

  /* ------------------------------- Derivados ------------------------------ */

  /** Consultas de la mas reciente a la mas antigua. */
  protected readonly consultas = computed<HistoriaClinica[]>(() =>
    [...this.historias()].sort((a, b) => (b.fecha ?? '').localeCompare(a.fecha ?? '')),
  );

  protected readonly totalAnuladas = computed<number>(
    () => this.historias().filter((h) => h.anulada).length,
  );

  protected readonly totalVigentes = computed<number>(
    () => this.historias().filter((h) => !h.anulada).length,
  );

  protected readonly nombrePaciente = computed<string>(() => {
    const id = Number(this.pacienteId());
    return this.pacientes().find((p) => p.id === id)?.nombreCompleto ?? '';
  });

  /** Fecha y hora en que se genera la vista de impresion. Se calcula una sola vez. */
  protected readonly impresoEl = (() => new Date().toLocaleString('es-PE'))();

  /* --------------------------------- Carga -------------------------------- */

  private cargarCatalogos(): void {
    this.pacienteService.listar().subscribe({
      next: (lista) => this.pacientes.set(lista),
      error: () => this.pacientes.set([]),
    });
    this.usuarioService.listarOdontologos().subscribe({
      next: (lista) => this.odontologos.set(lista),
      error: () => this.odontologos.set([]),
    });
  }

  /**
   * El <select> recibe su valor antes de que lleguen las opciones, asi que sin
   * esto el paciente preseleccionado por la URL no quedaria marcado.
   */
  protected esPacienteElegido(id: number): boolean {
    return this.pacienteId() === String(id);
  }

  protected cambiarPaciente(evento: Event): void {
    this.pacienteId.set((evento.target as HTMLSelectElement).value);
    this.expandidas.set([]);
    this.cargarHistorias();
  }

  protected cargarHistorias(): void {
    const id = Number(this.pacienteId());
    if (!id) {
      this.historias.set([]);
      this.error.set(null);
      return;
    }

    this.cargando.set(true);
    this.error.set(null);
    this.historiaService
      .listarPorPaciente(id)
      // finalize corre tanto si la peticion termina bien como si falla.
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (lista) => this.historias.set(lista),
        error: (fallo: ErrorApi) => {
          this.historias.set([]);
          this.error.set(fallo?.mensaje ?? 'No se pudo cargar la historia clinica');
        },
      });
  }

  /* ------------------------- Expandir / contraer -------------------------- */

  protected estaExpandida(id: number): boolean {
    return this.expandidas().includes(id);
  }

  protected alternar(id: number): void {
    this.expandidas.update((actuales) =>
      actuales.includes(id) ? actuales.filter((x) => x !== id) : [...actuales, id],
    );
  }

  protected expandirTodas(): void {
    this.expandidas.set(this.consultas().map((c) => c.id));
  }

  protected contraerTodas(): void {
    this.expandidas.set([]);
  }

  /* -------------------------------- Impresion ------------------------------ */

  /**
   * Prepara e imprime la historia clinica del paciente seleccionado.
   * Se expanden todas las consultas para que el detalle completo quede en el
   * papel, no solo el resumen visible en pantalla.
   */
  protected imprimir(): void {
    this.expandirTodas();
    setTimeout(() => window.print(), 100);
  }

  /* ------------------------------ Formulario ------------------------------ */

  protected abrirFormulario(): void {
    this.errorFormulario.set(null);
    const usuario = this.auth.usuarioActual();
    const odontologoPorDefecto = usuario && usuario.rol === 'ODONTOLOGO' ? String(usuario.id) : '';

    this.formulario.reset({
      pacienteId: this.pacienteId(),
      odontologoId: odontologoPorDefecto,
      citaId: '',
      motivoConsulta: '',
      diagnostico: '',
      anamnesis: '',
      examenClinico: '',
      procedimiento: '',
      tratamiento: '',
      medicamentos: '',
      observaciones: '',
    });

    this.formAbierto.set(true);
    this.cargarCitasDelPaciente();
  }

  protected cerrarFormulario(): void {
    this.formAbierto.set(false);
    this.errorFormulario.set(null);
    this.citasDelPaciente.set([]);
  }

  /** Las citas del select deben pertenecer al mismo paciente de la consulta. */
  protected cargarCitasDelPaciente(): void {
    const id = Number(this.formulario.controls.pacienteId.value);
    this.formulario.controls.citaId.setValue('');
    if (!id) {
      this.citasDelPaciente.set([]);
      return;
    }
    this.citaService.listar({ pacienteId: id }).subscribe({
      next: (lista) => this.citasDelPaciente.set(lista),
      error: () => this.citasDelPaciente.set([]),
    });
  }

  protected guardar(): void {
    this.errorFormulario.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const v = this.formulario.getRawValue();
    const datos: HistoriaClinicaRequest = {
      pacienteId: Number(v.pacienteId),
      odontologoId: Number(v.odontologoId),
      citaId: v.citaId ? Number(v.citaId) : null,
      motivoConsulta: v.motivoConsulta.trim(),
      diagnostico: v.diagnostico.trim(),
      anamnesis: v.anamnesis.trim() || null,
      examenClinico: v.examenClinico.trim() || null,
      procedimiento: v.procedimiento.trim() || null,
      tratamiento: v.tratamiento.trim() || null,
      medicamentos: v.medicamentos.trim() || null,
      observaciones: v.observaciones.trim() || null,
    };

    this.guardando.set(true);
    this.historiaService
      .crear(datos)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe({
        next: (creada) => {
          this.notificaciones.exito('Consulta registrada en la historia clinica');
          this.cerrarFormulario();
          // Si la consulta es de otro paciente, se cambia el paciente mostrado.
          this.pacienteId.set(String(creada.pacienteId));
          this.cargarHistorias();
        },
        error: (fallo: ErrorApi) => {
          this.errorFormulario.set(fallo?.mensaje ?? 'No se pudo registrar la consulta');
        },
      });
  }

  /* -------------------------------- Anular -------------------------------- */

  protected abrirAnular(historia: HistoriaClinica): void {
    this.historiaAAnular.set(historia);
    this.formAnular.reset({ motivo: '' });
  }

  protected cerrarAnular(): void {
    this.historiaAAnular.set(null);
  }

  protected confirmarAnular(): void {
    const historia = this.historiaAAnular();
    if (!historia) {
      return;
    }
    if (this.formAnular.invalid) {
      this.formAnular.markAllAsTouched();
      return;
    }

    this.anulando.set(true);
    this.historiaService
      .anular(historia.id, this.formAnular.controls.motivo.value.trim())
      .pipe(finalize(() => this.anulando.set(false)))
      .subscribe({
        next: () => {
          this.cerrarAnular();
          this.notificaciones.exito('La consulta quedo anulada y se conserva en el historial');
          this.cargarHistorias();
        },
        error: (fallo: ErrorApi) => {
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo anular la consulta');
        },
      });
  }

  /* --------------------------------- Ayudas ------------------------------- */

  /** "2026-09-04T10:00:00" -> "04/09/2026". */
  protected fechaCorta(iso: string | null): string {
    const [anio, mes, dia] = (iso ?? '').split('T')[0]?.split('-') ?? [];
    return anio ? `${dia}/${mes}/${anio}` : '-';
  }

  protected horaCorta(iso: string | null): string {
    const hora = (iso ?? '').split('T')[1] ?? '';
    return hora.slice(0, 5);
  }

  protected etiquetaCita(cita: Cita): string {
    return `${this.fechaCorta(cita.fechaHora)} ${this.horaCorta(cita.fechaHora)} - ${
      cita.motivo || 'Sin motivo'
    }`;
  }
}
