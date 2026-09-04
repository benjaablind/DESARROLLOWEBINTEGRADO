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
  protected readonly excesoConfirmado = signal(false);
  protected readonly porAnular = signal<Pago | null>(null);

  /** Tratamiento elegido en el formulario, para mostrar su saldo. */
  protected readonly tratamientoElegido = signal<Tratamiento | null>(null);
  /** Monto tecleado, para avisar en vivo cuando supera el saldo. */
  protected readonly montoTecleado = signal(0);

  protected readonly saldoElegido = computed(() => this.tratamientoElegido()?.saldoPendiente ?? 0);

  protected readonly excedeSaldo = computed(() => {
    const tratamiento = this.tratamientoElegido();
    return !!tratamiento && this.montoTecleado() > tratamiento.saldoPendiente;
  });

  /* ------------------------------ Formulario ----------------------------- */

  protected readonly formulario = this.fb.nonNullable.group({
    tratamientoId: ['', Validators.required],
    monto: [0, [Validators.required, Validators.min(0.01)]],
    metodo: ['EFECTIVO' as MetodoPago, Validators.required],
    comprobante: [''],
    observaciones: [''],
  });

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
    }).subscribe({
      next: ({ cuenta, historial }) => {
        this.estadoCuenta.set(cuenta);
        this.pagos.set(historial);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.estadoCuenta.set(null);
        this.pagos.set([]);
        this.error.set(fallo?.mensaje ?? 'No se pudo cargar el estado de cuenta');
        this.cargando.set(false);
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
    this.excesoConfirmado.set(false);
    this.tratamientoElegido.set(elegido);
    this.montoTecleado.set(elegido?.saldoPendiente ?? 0);

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
    this.excesoConfirmado.set(false);
    this.tratamientoElegido.set(null);
    this.montoTecleado.set(0);
  }

  protected cambiarTratamiento(valor: string): void {
    const id = valor ? Number(valor) : null;
    const tratamiento = this.tratamientos().find((t) => t.id === id) ?? null;
    this.tratamientoElegido.set(tratamiento);
    this.excesoConfirmado.set(false);
  }

  protected cambiarMonto(valor: string): void {
    const numero = Number(valor);
    this.montoTecleado.set(Number.isFinite(numero) ? numero : 0);
    this.excesoConfirmado.set(false);
  }

  protected registrar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    // Aviso previo: obliga a confirmar si el monto supera el saldo pendiente.
    if (this.excedeSaldo() && !this.excesoConfirmado()) {
      this.excesoConfirmado.set(true);
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

    this.pagoService.registrar(datos).subscribe({
      next: (pago) => {
        this.guardando.set(false);
        this.cerrarFormulario();
        this.notificaciones.exito(`Pago de ${this.soles(pago.monto)} registrado correctamente`);
        this.cargar();
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        // El backend detalla el saldo actual cuando el monto lo excede.
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
    this.pagoService.anular(objetivo.id).subscribe({
      next: () => {
        this.guardando.set(false);
        this.porAnular.set(null);
        this.notificaciones.exito('Pago anulado correctamente');
        this.cargar();
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        this.porAnular.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo anular el pago');
      },
    });
  }
}
