import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { OdontogramaService } from '../../core/services/odontograma.service';
import { PacienteService } from '../../core/services/paciente.service';
import { UsuarioService } from '../../core/services/usuario.service';
import {
  COLORES_ESTADO_PIEZA,
  ESTADOS_PIEZA,
  ETIQUETAS_ESTADO_PIEZA,
  ErrorApi,
  EstadoPieza,
  Odontograma,
  OdontogramaPieza,
  OdontogramaRegistroRequest,
  PIEZAS_FDI,
  PacienteResumen,
  ResumenOdontograma,
  Usuario,
} from '../../core/models';

/** Numeracion FDI de la denticion decidua, agrupada por cuadrante. */
const PIEZAS_FDI_DECIDUAS = {
  superiorDerecho: [55, 54, 53, 52, 51],
  superiorIzquierdo: [61, 62, 63, 64, 65],
  inferiorIzquierdo: [71, 72, 73, 74, 75],
  inferiorDerecho: [85, 84, 83, 82, 81],
} as const;

type Denticion = 'PERMANENTE' | 'DECIDUA';

/** Cuadrantes ya ordenados como se ven en la boca. */
interface Cuadrantes {
  supDerecho: readonly number[];
  supIzquierdo: readonly number[];
  infDerecho: readonly number[];
  infIzquierdo: readonly number[];
}

/** Fila del resumen por estado. */
interface FilaResumen {
  estado: EstadoPieza;
  etiqueta: string;
  color: string;
  cantidad: number;
}

/** Mapa dental del paciente con el historial de cada pieza. */
@Component({
  selector: 'app-odontograma',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './odontograma.html',
  styleUrl: './odontograma.scss',
})
export class OdontogramaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly odontogramaService = inject(OdontogramaService);
  private readonly pacienteService = inject(PacienteService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly notificaciones = inject(NotificacionService);
  private readonly auth = inject(AuthService);
  private readonly ruta = inject(ActivatedRoute);

  /* ------------------------------- Catalogos ------------------------------ */

  protected readonly estadosPieza = ESTADOS_PIEZA;
  protected readonly pacientes = signal<PacienteResumen[]>([]);
  protected readonly odontologos = signal<Usuario[]>([]);

  /* --------------------------------- Estado ------------------------------- */

  protected readonly pacienteId = signal<string>('');
  protected readonly denticion = signal<Denticion>('PERMANENTE');
  protected readonly odontograma = signal<Odontograma | null>(null);
  protected readonly resumen = signal<ResumenOdontograma>({});
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly piezaSeleccionada = signal<number | null>(null);
  protected readonly historial = signal<OdontogramaPieza[]>([]);
  protected readonly cargandoHistorial = signal(false);
  protected readonly errorHistorial = signal<string | null>(null);

  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /* ------------------------------ Formulario ------------------------------ */

  protected readonly formulario = this.fb.nonNullable.group({
    odontologoId: ['', Validators.required],
    estado: ['' as EstadoPieza | '', Validators.required],
    superficie: [''],
    diagnostico: [''],
    tratamiento: [''],
    observaciones: [''],
  });

  constructor() {
    this.cargarCatalogos();

    const pacienteId = this.ruta.snapshot.queryParamMap.get('pacienteId');
    if (pacienteId) {
      this.pacienteId.set(pacienteId);
      this.cargarOdontograma();
    }
  }

  /* ------------------------------- Derivados ------------------------------ */

  protected readonly cuadrantes = computed<Cuadrantes>(() => {
    const fuente = this.denticion() === 'PERMANENTE' ? PIEZAS_FDI : PIEZAS_FDI_DECIDUAS;
    return {
      supDerecho: fuente.superiorDerecho,
      supIzquierdo: fuente.superiorIzquierdo,
      // El cuadrante inferior derecho se dibuja a la izquierda, como en la boca.
      infDerecho: fuente.inferiorDerecho,
      infIzquierdo: fuente.inferiorIzquierdo,
    };
  });

  /** Estado vigente de cada pieza indexado por su numero FDI. */
  protected readonly piezasPorNumero = computed<Map<number, OdontogramaPieza>>(() => {
    const mapa = new Map<number, OdontogramaPieza>();
    for (const pieza of this.odontograma()?.piezas ?? []) {
      mapa.set(pieza.numeroPieza, pieza);
    }
    return mapa;
  });

  protected readonly filasResumen = computed<FilaResumen[]>(() => {
    const conteo = this.resumen();
    return ESTADOS_PIEZA.map((estado) => ({
      estado,
      etiqueta: ETIQUETAS_ESTADO_PIEZA[estado],
      color: COLORES_ESTADO_PIEZA[estado],
      cantidad: conteo[estado] ?? 0,
    })).filter((fila) => fila.cantidad > 0);
  });

  protected readonly totalResumen = computed<number>(() =>
    this.filasResumen().reduce((suma, fila) => suma + fila.cantidad, 0),
  );

  protected readonly piezaActual = computed<OdontogramaPieza | null>(() => {
    const numero = this.piezaSeleccionada();
    return numero === null ? null : (this.piezasPorNumero().get(numero) ?? null);
  });

  protected readonly nombrePaciente = computed<string>(() => {
    const id = Number(this.pacienteId());
    return (
      this.odontograma()?.pacienteNombre ??
      this.pacientes().find((p) => p.id === id)?.nombreCompleto ??
      ''
    );
  });

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
    this.cerrarPanel();
    this.cargarOdontograma();
  }

  protected cambiarDenticion(valor: Denticion): void {
    if (this.denticion() === valor) {
      return;
    }
    this.denticion.set(valor);
    this.cerrarPanel();
  }

  protected cargarOdontograma(): void {
    const id = Number(this.pacienteId());
    if (!id) {
      this.odontograma.set(null);
      this.resumen.set({});
      this.error.set(null);
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.odontogramaService.obtenerActual(id).subscribe({
      next: (datos) => {
        this.odontograma.set(datos);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.odontograma.set(null);
        this.cargando.set(false);
        this.error.set(fallo?.mensaje ?? 'No se pudo cargar el odontograma');
      },
    });

    this.odontogramaService.resumenEstados(id).subscribe({
      next: (conteo) => this.resumen.set(conteo ?? {}),
      error: () => this.resumen.set({}),
    });
  }

  /* ---------------------------- Panel de la pieza ------------------------- */

  protected seleccionarPieza(numero: number): void {
    if (!this.pacienteId()) {
      this.notificaciones.info('Primero elija un paciente');
      return;
    }

    this.piezaSeleccionada.set(numero);
    this.errorFormulario.set(null);

    const usuario = this.auth.usuarioActual();
    const actual = this.piezasPorNumero().get(numero);
    this.formulario.reset({
      odontologoId: usuario && usuario.rol === 'ODONTOLOGO' ? String(usuario.id) : '',
      estado: actual?.estado ?? '',
      superficie: '',
      diagnostico: '',
      tratamiento: '',
      observaciones: '',
    });

    this.cargarHistorial(numero);
  }

  protected cerrarPanel(): void {
    this.piezaSeleccionada.set(null);
    this.historial.set([]);
    this.errorHistorial.set(null);
    this.errorFormulario.set(null);
  }

  protected cargarHistorial(numero: number): void {
    const id = Number(this.pacienteId());
    if (!id) {
      return;
    }

    this.cargandoHistorial.set(true);
    this.errorHistorial.set(null);
    this.odontogramaService.historialDePieza(id, numero).subscribe({
      next: (datos) => {
        const registros = [...(datos?.registros ?? [])].sort((a, b) =>
          (b.fecha ?? '').localeCompare(a.fecha ?? ''),
        );
        this.historial.set(registros);
        this.cargandoHistorial.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.historial.set([]);
        this.cargandoHistorial.set(false);
        this.errorHistorial.set(fallo?.mensaje ?? 'No se pudo cargar el historial de la pieza');
      },
    });
  }

  protected recargarHistorial(): void {
    const numero = this.piezaSeleccionada();
    if (numero !== null) {
      this.cargarHistorial(numero);
    }
  }

  /* ------------------------------- Registrar ------------------------------ */

  protected registrar(): void {
    const numero = this.piezaSeleccionada();
    const pacienteId = Number(this.pacienteId());
    if (numero === null || !pacienteId) {
      return;
    }

    this.errorFormulario.set(null);
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const v = this.formulario.getRawValue();
    const datos: OdontogramaRegistroRequest = {
      pacienteId,
      odontologoId: Number(v.odontologoId),
      numeroPieza: numero,
      estado: v.estado as EstadoPieza,
      superficie: v.superficie.trim() || null,
      diagnostico: v.diagnostico.trim() || null,
      tratamiento: v.tratamiento.trim() || null,
      observaciones: v.observaciones.trim() || null,
    };

    this.guardando.set(true);
    this.odontogramaService.registrar(datos).subscribe({
      next: () => {
        this.guardando.set(false);
        this.notificaciones.exito(
          `Se agrego un registro nuevo a la pieza ${numero}. El anterior se conserva en el historial.`,
        );
        this.formulario.patchValue({
          superficie: '',
          diagnostico: '',
          tratamiento: '',
          observaciones: '',
        });
        this.formulario.markAsUntouched();
        this.cargarOdontograma();
        this.cargarHistorial(numero);
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        this.errorFormulario.set(fallo?.mensaje ?? 'No se pudo registrar el estado de la pieza');
      },
    });
  }

  /* --------------------------------- Ayudas ------------------------------- */

  protected estadoDePieza(numero: number): EstadoPieza | null {
    return this.piezasPorNumero().get(numero)?.estado ?? null;
  }

  protected colorDePieza(numero: number): string {
    const estado = this.estadoDePieza(numero);
    return estado ? COLORES_ESTADO_PIEZA[estado] : 'var(--c-superficie-2)';
  }

  /** Texto blanco o oscuro segun lo claro que sea el color del estado. */
  protected colorTextoPieza(numero: number): string {
    const estado = this.estadoDePieza(numero);
    if (!estado) {
      return 'var(--c-texto)';
    }
    return this.luminancia(COLORES_ESTADO_PIEZA[estado]) > 0.62 ? '#14262b' : '#ffffff';
  }

  protected colorTextoEstado(estado: EstadoPieza): string {
    return this.luminancia(COLORES_ESTADO_PIEZA[estado]) > 0.62 ? '#14262b' : '#ffffff';
  }

  protected colorEstado(estado: EstadoPieza): string {
    return COLORES_ESTADO_PIEZA[estado];
  }

  protected etiquetaEstado(estado: EstadoPieza | null): string {
    return estado ? ETIQUETAS_ESTADO_PIEZA[estado] : 'Sin registro';
  }

  protected tituloPieza(numero: number): string {
    return `Pieza ${numero} - ${this.etiquetaEstado(this.estadoDePieza(numero))}`;
  }

  protected fechaCorta(iso: string | null): string {
    const partes = (iso ?? '').split('T')[0].split('-');
    return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : '-';
  }

  protected horaCorta(iso: string | null): string {
    return ((iso ?? '').split('T')[1] ?? '').slice(0, 5);
  }

  private luminancia(hex: string): number {
    const limpio = hex.replace('#', '');
    const r = parseInt(limpio.slice(0, 2), 16);
    const g = parseInt(limpio.slice(2, 4), 16);
    const b = parseInt(limpio.slice(4, 6), 16);
    return (0.299 * r + 0.587 * g + 0.114 * b) / 255;
  }
}
