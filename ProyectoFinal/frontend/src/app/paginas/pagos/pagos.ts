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
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { NotificacionService } from '../../core/services/notificacion.service';
import { PacienteService } from '../../core/services/paciente.service';
import { PagoService } from '../../core/services/pago.service';
import {
  ETIQUETAS_ESTADO_TRATAMIENTO,
  ETIQUETAS_METODO_PAGO,
  ErrorApi,
  EstadoCuenta,
  METODOS_PAGO,
  MetodoPago,
  PacienteResumen,
  Pago,
  PagoRequest,
  Tratamiento,
} from '../../core/models';
import {
  COMPROBANTE_MAXIMO_PAGO,
  OBSERVACIONES_MAXIMAS_PAGO,
  montoDentroDelSaldoValidator,
} from '../../core/validators/pagos.validators';
import { importeValidator } from '../../core/validators/tratamientos.validators';

/** Clase de badge sugerida para cada metodo de pago. */
const BADGE_METODO_PAGO: Record<MetodoPago, string> = {
  EFECTIVO: 'badge-exito',
  TARJETA: 'badge-info',
  TRANSFERENCIA: 'badge-primario',
  YAPE: 'badge-neutro',
  PLIN: 'badge-advertencia',
};

/**
 * Pantalla de pagos: estado de cuenta del paciente, registro de cobros contra
 * un tratamiento e historial con la opcion de anular.
 */
@Component({
  selector: 'app-pagos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './pagos.html',
  styleUrl: './pagos.scss',
})
export class PagosComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly pagoService = inject(PagoService);
  private readonly pacienteService = inject(PacienteService);
  private readonly notificaciones = inject(NotificacionService);
  private readonly ruta = inject(ActivatedRoute);

  /* ------------------------------- Catalogos ----------------------------- */

  protected readonly metodos = METODOS_PAGO;
  protected readonly etiquetasMetodo = ETIQUETAS_METODO_PAGO;
  protected readonly badgesMetodo = BADGE_METODO_PAGO;
  protected readonly etiquetasEstadoTratamiento = ETIQUETAS_ESTADO_TRATAMIENTO;

  protected readonly pacientes = signal<PacienteResumen[]>([]);

  /* --------------------------------- Estado ------------------------------ */

  protected readonly pacienteId = signal<number | null>(null);
  protected readonly estadoCuenta = signal<EstadoCuenta | null>(null);
  protected readonly pagos = signal<Pago[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly guardando = signal(false);

  /** Tratamientos del paciente segun el estado de cuenta. */
  protected readonly tratamientos = computed(() => this.estadoCuenta()?.tratamientos ?? []);

  /** Solo los tratamientos que todavia admiten un cobro. */
  protected readonly tratamientosCobrables = computed(() =>
    this.tratamientos().filter((t) => t.estado !== 'CANCELADO' && t.saldoPendiente > 0),
  );

  protected readonly sinDatos = computed(
    () => this.tratamientos().length === 0 && this.pagos().length === 0,
  );

  /* -------------------------------- Dialogos ----------------------------- */

  protected readonly formularioAbierto = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly porAnular = signal<Pago | null>(null);

  /** Tratamiento elegido en el formulario, para mostrar su saldo. */
  protected readonly tratamientoElegido = signal<Tratamiento | null>(null);

  protected readonly saldoElegido = computed(() => this.tratamientoElegido()?.saldoPendiente ?? 0);

  /* ------------------------------ Formulario ----------------------------- */

  protected readonly comprobanteMaximo = COMPROBANTE_MAXIMO_PAGO;
  protected readonly observacionesMaximas = OBSERVACIONES_MAXIMAS_PAGO;

  /**
   * Mismas reglas que PagoRequest y PagoService: monto positivo con hasta
   * 2 decimales y, a nivel de grupo, nunca mayor al saldo del tratamiento.
   */
  protected readonly formulario = this.fb.nonNullable.group(
    {
      tratamientoId: ['', Validators.required],
      monto: [0, [Validators.required, importeValidator()]],
      metodo: ['EFECTIVO' as MetodoPago, Validators.required],
      comprobante: ['', Validators.maxLength(COMPROBANTE_MAXIMO_PAGO)],
      observaciones: ['', Validators.maxLength(OBSERVACIONES_MAXIMAS_PAGO)],
    },
    {
      validators: montoDentroDelSaldoValidator(
        (id) => this.tratamientos().find((t) => t.id === id)?.saldoPendiente ?? null,
      ),
    },
  );

  protected get campos() {
    return this.formulario.controls;
  }

  /* --------------------------------- Ciclo ------------------------------- */

  ngOnInit(): void {
    this.pacienteService.listar().subscribe({
      next: (lista) => this.pacientes.set(lista),
      error: () => this.pacientes.set([]),
    });

    const desdeUrl = Number(this.ruta.snapshot.queryParamMap.get('pacienteId'));
    if (Number.isFinite(desdeUrl) && desdeUrl > 0) {
      this.pacienteId.set(desdeUrl);
      this.cargar();
    }
  }

  /* --------------------------------- Datos ------------------------------- */

  protected elegirPaciente(valor: string): void {
    const id = valor ? Number(valor) : null;
    this.pacienteId.set(id);
    this.estadoCuenta.set(null);
    this.pagos.set([]);
    this.error.set(null);
    this.cerrarFormulario();

    if (id) {
      this.cargar();
    }
  }

  protected cargar(): void {
    const id = this.pacienteId();
    if (!id) {
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    forkJoin({
      cuenta: this.pagoService.estadoCuenta(id),
      historial: this.pagoService.listarPorPaciente(id),
    })
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: ({ cuenta, historial }) => {
          this.estadoCuenta.set(cuenta);
          this.pagos.set(historial);
        },
        error: (fallo: ErrorApi) => {
          this.estadoCuenta.set(null);
          this.pagos.set([]);
          this.error.set(fallo?.mensaje ?? 'No se pudo cargar el estado de cuenta');
        },
      });
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

  /** Fecha ISO mostrada como "DD/MM/YYYY hh:mm" cuando trae hora. */
  protected fechaHora(iso: string | null | undefined): string {
    if (!iso) {
      return '—';
    }
    const partes = iso.slice(0, 10).split('-');
    const dia = partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : iso;
    const hora = iso.length >= 16 ? iso.slice(11, 16) : '';
    return hora ? `${dia} ${hora}` : dia;
  }

  protected porcentajePagado(t: Tratamiento): number {
    if (!t.precio || t.precio <= 0) {
      return 0;
    }
    return Math.max(0, Math.min(100, Math.round((t.totalPagado / t.precio) * 100)));
  }

  /* ---------------------------- Registro de pago ------------------------- */

  protected abrirFormulario(tratamiento?: Tratamiento): void {
    if (this.tratamientosCobrables().length === 0 && !tratamiento) {
      this.notificaciones.info('El paciente no tiene tratamientos con saldo pendiente');
      return;
    }

    const elegido = tratamiento ?? this.tratamientosCobrables()[0] ?? null;
    this.errorFormulario.set(null);
    this.tratamientoElegido.set(elegido);

    this.formulario.reset({
      tratamientoId: elegido ? String(elegido.id) : '',
      monto: elegido?.saldoPendiente ?? 0,
      metodo: 'EFECTIVO',
      comprobante: '',
      observaciones: '',
    });

    this.formularioAbierto.set(true);
  }

  protected cerrarFormulario(): void {
    this.formularioAbierto.set(false);
    this.errorFormulario.set(null);
    this.tratamientoElegido.set(null);
  }

  protected cambiarTratamiento(valor: string): void {
    const id = valor ? Number(valor) : null;
    const tratamiento = this.tratamientos().find((t) => t.id === id) ?? null;
    this.tratamientoElegido.set(tratamiento);
  }

  protected registrar(): void {
    // Incluye la regla de grupo: un monto mayor al saldo ya no llega al servidor.
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const datos: PagoRequest = {
      tratamientoId: Number(valores.tratamientoId),
      monto: Number(valores.monto),
      metodo: valores.metodo,
      comprobante: valores.comprobante.trim() || null,
      observaciones: valores.observaciones.trim() || null,
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    this.pagoService
      .registrar(datos)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe({
        next: (pago) => {
          this.cerrarFormulario();
          this.notificaciones.exito(`Pago de ${this.soles(pago.monto)} registrado correctamente`);
          this.cargar();
        },
        error: (fallo: ErrorApi) => {
          // Si el saldo cambio desde otro equipo, el backend detalla el saldo actual.
          this.errorFormulario.set(fallo?.mensaje ?? 'No se pudo registrar el pago');
        },
      });
  }

  /* --------------------------------- Anular ------------------------------ */

  protected pedirAnular(pago: Pago): void {
    this.porAnular.set(pago);
  }

  protected cancelarAnular(): void {
    this.porAnular.set(null);
  }

  protected confirmarAnular(): void {
    const objetivo = this.porAnular();
    if (!objetivo) {
      return;
    }

    this.guardando.set(true);
    this.pagoService
      .anular(objetivo.id)
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe({
        next: () => {
          this.porAnular.set(null);
          this.notificaciones.exito('Pago anulado correctamente');
          this.cargar();
        },
        error: (fallo: ErrorApi) => {
          this.porAnular.set(null);
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo anular el pago');
        },
      });
  }
}
