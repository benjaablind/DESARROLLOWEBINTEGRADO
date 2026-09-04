import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';

import { NotificacionService } from '../../core/services/notificacion.service';
import { PacienteService } from '../../core/services/paciente.service';
import { ReporteService } from '../../core/services/reporte.service';
import {
  BADGE_ESTADO_CITA,
  BADGE_ESTADO_TRATAMIENTO,
  ETIQUETAS_ESTADO_CITA,
  ETIQUETAS_ESTADO_PACIENTE,
  ETIQUETAS_ESTADO_TRATAMIENTO,
  ETIQUETAS_METODO_PAGO,
  ETIQUETAS_TIPO_ARCHIVO,
  ErrorApi,
  EstadoCita,
  EstadoPaciente,
  EstadoTratamiento,
  FilaReporte,
  MetodoPago,
  PacienteResumen,
  RangoFechas,
  ReporteCitas,
  ReporteHistorialPaciente,
  ReporteIngresos,
  ReportePacientes,
  ReportePagosPendientes,
  ReporteTratamientos,
  TipoArchivo,
  aFilas,
} from '../../core/models';

/** Pestanas disponibles en la barra superior. */
export type PestanaReporte =
  'pacientes' | 'citas' | 'tratamientos' | 'ingresos' | 'pendientes' | 'historial';

interface OpcionPestana {
  clave: PestanaReporte;
  etiqueta: string;
}

const MESES_CORTOS = [
  'Ene',
  'Feb',
  'Mar',
  'Abr',
  'May',
  'Jun',
  'Jul',
  'Ago',
  'Set',
  'Oct',
  'Nov',
  'Dic',
];

/**
 * Pantalla de reportes: seis vistas con rango de fechas, graficos dibujados
 * con divs y exportacion a CSV generada en el navegador.
 */
@Component({
  selector: 'app-reportes',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './reportes.html',
  styleUrl: './reportes.scss',
})
export class ReportesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteService);
  private readonly pacienteService = inject(PacienteService);
  private readonly notificaciones = inject(NotificacionService);

  /* ------------------------------- Catalogos ----------------------------- */

  protected readonly pestanas: OpcionPestana[] = [
    { clave: 'pacientes', etiqueta: 'Pacientes' },
    { clave: 'citas', etiqueta: 'Citas' },
    { clave: 'tratamientos', etiqueta: 'Tratamientos' },
    { clave: 'ingresos', etiqueta: 'Ingresos' },
    { clave: 'pendientes', etiqueta: 'Pagos pendientes' },
    { clave: 'historial', etiqueta: 'Historial del paciente' },
  ];

  protected readonly etiquetasEstadoCita = ETIQUETAS_ESTADO_CITA;
  protected readonly badgesEstadoCita = BADGE_ESTADO_CITA;
  protected readonly etiquetasEstadoTratamiento = ETIQUETAS_ESTADO_TRATAMIENTO;
  protected readonly badgesEstadoTratamiento = BADGE_ESTADO_TRATAMIENTO;
  protected readonly etiquetasEstadoPaciente = ETIQUETAS_ESTADO_PACIENTE;
  protected readonly etiquetasTipoArchivo = ETIQUETAS_TIPO_ARCHIVO;

  protected readonly pacientes = signal<PacienteResumen[]>([]);

  /* --------------------------------- Estado ------------------------------ */

  protected readonly pestana = signal<PestanaReporte>('pacientes');
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  /** Rango de fechas del reporte. Ambos extremos son opcionales. */
  protected readonly rango = this.fb.nonNullable.group({
    desde: [''],
    hasta: [''],
  });

  protected readonly atajoActivo = signal<string>('todo');

  protected readonly repPacientes = signal<ReportePacientes | null>(null);
  protected readonly repCitas = signal<ReporteCitas | null>(null);
  protected readonly repTratamientos = signal<ReporteTratamientos | null>(null);
  protected readonly repIngresos = signal<ReporteIngresos | null>(null);
  protected readonly repPendientes = signal<ReportePagosPendientes | null>(null);
  protected readonly repHistorial = signal<ReporteHistorialPaciente | null>(null);

  protected readonly pacienteHistorial = signal<number | null>(null);

  /* -------------------------------- Graficos ----------------------------- */

  /** Citas agrupadas por estado, con la etiqueta legible. */
  protected readonly filasCitasPorEstado = computed<FilaReporte[]>(() =>
    aFilas(this.repCitas()?.porEstado).map((fila) => ({
      etiqueta: this.etiquetasEstadoCita[fila.etiqueta as EstadoCita] ?? fila.etiqueta,
      valor: fila.valor,
    })),
  );

  /** Ingresos agrupados por metodo de pago. */
  protected readonly filasPorMetodo = computed<FilaReporte[]>(() =>
    aFilas(this.repIngresos()?.porMetodo).map((fila) => ({
      etiqueta: ETIQUETAS_METODO_PAGO[fila.etiqueta as MetodoPago] ?? fila.etiqueta,
      valor: fila.valor,
    })),
  );

  /** Ingresos por mes, ordenados cronologicamente por la clave "YYYY-MM". */
  protected readonly filasPorMes = computed<FilaReporte[]>(() =>
    aFilas(this.repIngresos()?.porMes)
      .slice()
      .sort((a, b) => a.etiqueta.localeCompare(b.etiqueta))
      .map((fila) => ({ etiqueta: this.nombreMes(fila.etiqueta), valor: fila.valor })),
  );

  /* --------------------------------- Ciclo ------------------------------- */

  ngOnInit(): void {
    this.pacienteService.listar().subscribe({
      next: (lista) => this.pacientes.set(lista),
      error: () => this.pacientes.set([]),
    });

    this.cargar();
  }

  /* ------------------------------- Navegacion ---------------------------- */

  protected irA(clave: PestanaReporte): void {
    if (this.pestana() === clave) {
      return;
    }
    this.pestana.set(clave);
    this.error.set(null);
    this.cargar();
  }

  /** El rango no aplica a los pagos pendientes. */
  protected get usaRango(): boolean {
    return this.pestana() !== 'pendientes' && this.pestana() !== 'historial';
  }

  /* ---------------------------- Rango de fechas -------------------------- */

  /** El usuario tocó las fechas a mano: ningún atajo queda resaltado. */
  protected marcarRangoManual(): void {
    this.atajoActivo.set('');
  }

  protected aplicarAtajo(clave: 'mes' | 'trimestre' | 'anio' | 'todo'): void {
    const hoy = new Date();
    // Se construye la fecha local sin pasar por UTC para no perder un dia.
    const aIso = (fecha: Date) =>
      `${fecha.getFullYear()}-${String(fecha.getMonth() + 1).padStart(2, '0')}-${String(
        fecha.getDate(),
      ).padStart(2, '0')}`;

    switch (clave) {
      case 'mes':
        this.rango.setValue({
          desde: aIso(new Date(hoy.getFullYear(), hoy.getMonth(), 1)),
          hasta: aIso(hoy),
        });
        break;
      case 'trimestre':
        this.rango.setValue({
          desde: aIso(new Date(hoy.getFullYear(), hoy.getMonth() - 2, 1)),
          hasta: aIso(hoy),
        });
        break;
      case 'anio':
        this.rango.setValue({
          desde: aIso(new Date(hoy.getFullYear(), 0, 1)),
          hasta: aIso(hoy),
        });
        break;
      case 'todo':
        this.rango.setValue({ desde: '', hasta: '' });
        break;
    }

    this.atajoActivo.set(clave);
    this.cargar();
  }

  /* --------------------------------- Carga ------------------------------- */

  protected cargar(): void {
    const valores = this.rango.getRawValue();
    const rango: RangoFechas = {
      desde: valores.desde || undefined,
      hasta: valores.hasta || undefined,
    };

    this.cargando.set(true);
    this.error.set(null);

    switch (this.pestana()) {
      case 'pacientes':
        this.reporteService.pacientes(rango).subscribe({
          next: (r) => {
            this.repPacientes.set(r);
            this.cargando.set(false);
          },
          error: (fallo: ErrorApi) => {
            this.repPacientes.set(null);
            this.marcarError(fallo);
          },
        });
        break;

      case 'citas':
        this.reporteService.citas(rango).subscribe({
          next: (r) => {
            this.repCitas.set(r);
            this.cargando.set(false);
          },
          error: (fallo: ErrorApi) => {
            this.repCitas.set(null);
            this.marcarError(fallo);
          },
        });
        break;

      case 'tratamientos':
        this.reporteService.tratamientos(rango).subscribe({
          next: (r) => {
            this.repTratamientos.set(r);
            this.cargando.set(false);
          },
          error: (fallo: ErrorApi) => {
            this.repTratamientos.set(null);
            this.marcarError(fallo);
          },
        });
        break;

      case 'ingresos':
        this.reporteService.ingresos(rango).subscribe({
          next: (r) => {
            this.repIngresos.set(r);
            this.cargando.set(false);
          },
          error: (fallo: ErrorApi) => {
            this.repIngresos.set(null);
            this.marcarError(fallo);
          },
        });
        break;

      case 'pendientes':
        this.reporteService.pagosPendientes().subscribe({
          next: (r) => {
            this.repPendientes.set(r);
            this.cargando.set(false);
          },
          error: (fallo: ErrorApi) => {
            this.repPendientes.set(null);
            this.marcarError(fallo);
          },
        });
        break;

      case 'historial':
        this.cargarHistorial();
        break;
    }
  }

  protected elegirPacienteHistorial(valor: string): void {
    this.pacienteHistorial.set(valor ? Number(valor) : null);
    this.repHistorial.set(null);
    this.error.set(null);
    this.cargarHistorial();
  }

  private cargarHistorial(): void {
    const id = this.pacienteHistorial();
    if (!id) {
      this.cargando.set(false);
      this.repHistorial.set(null);
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.reporteService.historialDePaciente(id).subscribe({
      next: (r) => {
        this.repHistorial.set(r);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.repHistorial.set(null);
        this.marcarError(fallo);
      },
    });
  }

  /** El backend responde 400 si la fecha inicial es posterior a la final. */
  private marcarError(fallo: ErrorApi): void {
    this.error.set(fallo?.mensaje ?? 'No se pudo generar el reporte');
    this.cargando.set(false);
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

  protected fecha(iso: string | null | undefined): string {
    if (!iso) {
      return '—';
    }
    const partes = iso.slice(0, 10).split('-');
    return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : iso;
  }

  protected fechaHora(iso: string | null | undefined): string {
    if (!iso) {
      return '—';
    }
    const hora = iso.length >= 16 ? iso.slice(11, 16) : '';
    return hora ? `${this.fecha(iso)} ${hora}` : this.fecha(iso);
  }

  /** "2026-01" se muestra como "Ene 2026". */
  protected nombreMes(clave: string): string {
    const partes = clave.split('-');
    if (partes.length < 2) {
      return clave;
    }
    const indice = Number(partes[1]) - 1;
    const mes = MESES_CORTOS[indice] ?? partes[1];
    return `${mes} ${partes[0]}`;
  }

  /** Ancho porcentual de una barra respecto al valor maximo de su serie. */
  protected anchoBarra(fila: FilaReporte, filas: FilaReporte[]): number {
    const maximo = filas.reduce((max, f) => Math.max(max, f.valor), 0);
    if (maximo <= 0) {
      return 0;
    }
    return Math.max(2, Math.round((fila.valor / maximo) * 100));
  }

  protected totalFilas(filas: FilaReporte[]): number {
    return filas.reduce((suma, f) => suma + f.valor, 0);
  }

  protected etiquetaPaciente(estado: EstadoPaciente): string {
    return this.etiquetasEstadoPaciente[estado] ?? estado;
  }

  protected etiquetaTratamiento(estado: EstadoTratamiento): string {
    return this.etiquetasEstadoTratamiento[estado] ?? estado;
  }

  protected etiquetaArchivo(tipo: TipoArchivo): string {
    return this.etiquetasTipoArchivo[tipo] ?? tipo;
  }

  /* ------------------------------ Exportacion ---------------------------- */

  protected exportarPacientes(): void {
    const reporte = this.repPacientes();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'reporte-pacientes',
      ['DNI', 'Nombre completo', 'Edad', 'Telefono', 'Estado', 'Fecha de registro'],
      reporte.pacientes.map((p) => [
        p.dni,
        p.nombreCompleto,
        p.edad ?? '',
        p.telefono,
        this.etiquetaPaciente(p.estado),
        this.fecha(p.fechaRegistro),
      ]),
    );
  }

  protected exportarCitas(): void {
    const reporte = this.repCitas();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'reporte-citas',
      ['Fecha y hora', 'Paciente', 'Odontologo', 'Motivo', 'Estado'],
      reporte.citas.map((c) => [
        this.fechaHora(c.fechaHora),
        c.pacienteNombre,
        c.odontologoNombre,
        c.motivo,
        this.etiquetasEstadoCita[c.estado] ?? c.estado,
      ]),
    );
  }

  protected exportarTratamientos(): void {
    const reporte = this.repTratamientos();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'reporte-tratamientos',
      [
        'Paciente',
        'Tratamiento',
        'Estado',
        'Precio',
        'Pagado',
        'Saldo',
        'Fecha de inicio',
        'Fecha de fin',
      ],
      reporte.tratamientos.map((t) => [
        t.pacienteNombre,
        t.nombre,
        this.etiquetaTratamiento(t.estado),
        this.numeroCsv(t.precio),
        this.numeroCsv(t.totalPagado),
        this.numeroCsv(t.saldoPendiente),
        this.fecha(t.fechaInicio),
        this.fecha(t.fechaFin),
      ]),
    );
  }

  protected exportarIngresos(): void {
    const reporte = this.repIngresos();
    if (!reporte) {
      return;
    }
    const filas: (string | number)[][] = [
      ['Total de ingresos', this.numeroCsv(reporte.totalIngresos)],
      ['Cantidad de pagos', reporte.cantidadPagos],
    ];
    for (const fila of this.filasPorMetodo()) {
      filas.push([`Metodo: ${fila.etiqueta}`, this.numeroCsv(fila.valor)]);
    }
    for (const fila of this.filasPorMes()) {
      filas.push([`Mes: ${fila.etiqueta}`, this.numeroCsv(fila.valor)]);
    }
    this.descargarCsv('reporte-ingresos', ['Concepto', 'Importe'], filas);
  }

  protected exportarPendientes(): void {
    const reporte = this.repPendientes();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'reporte-pagos-pendientes',
      ['Paciente', 'Tratamiento', 'Estado', 'Precio', 'Pagado', 'Saldo', 'Fecha de inicio'],
      reporte.items.map((t) => [
        t.pacienteNombre,
        t.nombre,
        this.etiquetaTratamiento(t.estado),
        this.numeroCsv(t.precio),
        this.numeroCsv(t.totalPagado),
        this.numeroCsv(t.saldoPendiente),
        this.fecha(t.fechaInicio),
      ]),
    );
  }

  protected exportarHistorialConsultas(): void {
    const reporte = this.repHistorial();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'historial-consultas',
      ['Fecha', 'Motivo de consulta', 'Diagnostico', 'Anulada'],
      reporte.consultas.map((c) => [
        this.fecha(c.fecha),
        c.motivoConsulta,
        c.diagnostico,
        c.anulada ? 'Si' : 'No',
      ]),
    );
  }

  protected exportarHistorialCitas(): void {
    const reporte = this.repHistorial();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'historial-citas',
      ['Fecha y hora', 'Odontologo', 'Motivo', 'Estado'],
      reporte.citas.map((c) => [
        this.fechaHora(c.fechaHora),
        c.odontologoNombre,
        c.motivo,
        this.etiquetasEstadoCita[c.estado] ?? c.estado,
      ]),
    );
  }

  protected exportarHistorialTratamientos(): void {
    const reporte = this.repHistorial();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'historial-tratamientos',
      ['Tratamiento', 'Estado', 'Precio', 'Pagado', 'Saldo'],
      reporte.tratamientos.map((t) => [
        t.nombre,
        this.etiquetaTratamiento(t.estado),
        this.numeroCsv(t.precio),
        this.numeroCsv(t.totalPagado),
        this.numeroCsv(t.saldoPendiente),
      ]),
    );
  }

  protected exportarHistorialArchivos(): void {
    const reporte = this.repHistorial();
    if (!reporte) {
      return;
    }
    this.descargarCsv(
      'historial-archivos',
      ['Nombre', 'Tipo', 'Tamano', 'Descripcion', 'Fecha de subida'],
      reporte.archivos.map((a) => [
        a.nombreOriginal,
        this.etiquetaArchivo(a.tipo),
        a.tamanoLegible,
        a.descripcion,
        this.fechaHora(a.fechaSubida),
      ]),
    );
  }

  /** Numero con coma decimal para que Excel en espanol lo interprete bien. */
  private numeroCsv(valor: number | null | undefined): string {
    const numero = typeof valor === 'number' && Number.isFinite(valor) ? valor : 0;
    return numero.toFixed(2).replace('.', ',');
  }

  /** Escapa un valor y lo envuelve en comillas dobles. */
  private celdaCsv(valor: string | number | null | undefined): string {
    const texto = valor === null || valor === undefined ? '' : String(valor);
    return `"${texto.replace(/"/g, '""')}"`;
  }

  /**
   * Arma el CSV en memoria y lo descarga con un enlace temporal.
   * Separador punto y coma y BOM UTF-8 para que Excel lo abra correctamente.
   */
  private descargarCsv(
    nombre: string,
    encabezados: string[],
    filas: (string | number | null | undefined)[][],
  ): void {
    if (filas.length === 0) {
      this.notificaciones.info('No hay datos que exportar en este reporte');
      return;
    }

    const lineas = [
      encabezados.map((e) => this.celdaCsv(e)).join(';'),
      ...filas.map((fila) => fila.map((celda) => this.celdaCsv(celda)).join(';')),
    ];

    // El BOM hace que Excel reconozca el UTF-8 y respete las tildes.
    const contenido = '\ufeff' + lineas.join('\r\n');
    const blob = new Blob([contenido], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);

    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = `${nombre}-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(enlace);
    enlace.click();
    document.body.removeChild(enlace);
    URL.revokeObjectURL(url);

    this.notificaciones.exito('Archivo CSV generado correctamente');
  }
}
