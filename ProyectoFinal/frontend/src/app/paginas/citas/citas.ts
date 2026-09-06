import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { CitaService } from '../../core/services/cita.service';
import { PacienteService } from '../../core/services/paciente.service';
import { UsuarioService } from '../../core/services/usuario.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import {
  BADGE_ESTADO_CITA,
  Cita,
  CitaRequest,
  ESTADOS_CITA,
  ETIQUETAS_ESTADO_CITA,
  ErrorApi,
  EstadoCita,
  PacienteResumen,
  Usuario,
} from '../../core/models';

/** Primera y ultima hora que dibuja la linea de tiempo de la agenda. */
const HORA_INICIO = 8;
const HORA_FIN = 20;
/** Alto en pixeles de cada franja de una hora. */
/* Alto de cada hora en la linea de tiempo. 72px deja que una cita
   de 30 minutos muestre hora, paciente y odontologo sin recortes. */
const ALTO_HORA = 72;

/** Bajo este alto solo caben dos lineas dentro del bloque. */
const ALTO_MINIMO_TRES_LINEAS = 46;

/** Cita ya posicionada dentro de la linea de tiempo. */
interface BloqueAgenda {
  cita: Cita;
  /** Desplazamiento vertical en pixeles. */
  top: number;
  /** Alto en pixeles. */
  alto: number;
  /** Posicion y ancho en porcentaje para repartir las citas que se cruzan. */
  izquierda: number;
  ancho: number;
  /** Verdadero cuando el bloque no tiene alto para mostrar tres lineas. */
  corto: boolean;
}

/** Valida que la combinacion de fecha y hora del formulario sea futura. */
function fechaFutura(grupo: AbstractControl): ValidationErrors | null {
  const fecha = grupo.get('fecha')?.value as string;
  const hora = grupo.get('hora')?.value as string;
  if (!fecha || !hora) {
    return null;
  }
  const valor = new Date(`${fecha}T${hora}:00`);
  if (Number.isNaN(valor.getTime())) {
    return { fechaInvalida: true };
  }
  return valor.getTime() > Date.now() ? null : { fechaPasada: true };
}

/** Agenda del dia y listado de citas con su control de estado. */
@Component({
  selector: 'app-citas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, NgTemplateOutlet],
  templateUrl: './citas.html',
  styleUrl: './citas.scss',
})
export class CitasComponent {
  private readonly fb = inject(FormBuilder);
  private readonly citaService = inject(CitaService);
  private readonly pacienteService = inject(PacienteService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly notificaciones = inject(NotificacionService);
  private readonly ruta = inject(ActivatedRoute);

  /* ------------------------------- Catalogos ------------------------------ */

  protected readonly estadosCita = ESTADOS_CITA;
  protected readonly pacientes = signal<PacienteResumen[]>([]);
  protected readonly odontologos = signal<Usuario[]>([]);

  /* --------------------------------- Estado ------------------------------- */

  protected readonly vista = signal<'agenda' | 'lista'>('agenda');
  protected readonly dia = signal<string>(CitasComponent.hoy());
  protected readonly citas = signal<Cita[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  /** Cita cuyo formulario esta abierto. null = alta nueva. */
  protected readonly citaEnEdicion = signal<Cita | null>(null);
  protected readonly formAbierto = signal(false);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);
  /** Horas ya ocupadas por el odontologo elegido en el dia elegido. */
  protected readonly ocupadas = signal<string[]>([]);
  protected readonly consultandoAgenda = signal(false);

  protected readonly citaAReprogramar = signal<Cita | null>(null);
  protected readonly citaACancelar = signal<Cita | null>(null);
  protected readonly citaAEliminar = signal<Cita | null>(null);
  protected readonly procesando = signal<number | null>(null);

  /* ------------------------------ Formularios ----------------------------- */

  protected readonly filtros = this.fb.nonNullable.group({
    dia: [''],
    pacienteId: [''],
    odontologoId: [''],
    estado: [''],
  });

  protected readonly formulario = this.fb.nonNullable.group(
    {
      pacienteId: ['', Validators.required],
      odontologoId: ['', Validators.required],
      fecha: ['', Validators.required],
      hora: ['', Validators.required],
      duracionMinutos: [30, [Validators.required, Validators.min(10), Validators.max(240)]],
      motivo: [''],
      observaciones: [''],
    },
    { validators: fechaFutura },
  );

  protected readonly formReprogramar = this.fb.nonNullable.group(
    {
      fecha: ['', Validators.required],
      hora: ['', Validators.required],
      motivo: [''],
    },
    { validators: fechaFutura },
  );

  protected readonly formCancelar = this.fb.nonNullable.group({ motivo: [''] });

  constructor() {
    const pacienteId = this.ruta.snapshot.queryParamMap.get('pacienteId');
    if (pacienteId) {
      this.filtros.controls.pacienteId.setValue(pacienteId);
      this.vista.set('lista');
    }

    this.cargarCatalogos();
    this.cargarCitas();
  }

  /* ------------------------------- Derivados ------------------------------ */

  /** Franjas horarias que dibuja la agenda: 08:00 ... 19:00. */
  protected readonly franjas = computed<string[]>(() => {
    const horas: string[] = [];
    for (let h = HORA_INICIO; h < HORA_FIN; h++) {
      horas.push(`${String(h).padStart(2, '0')}:00`);
    }
    return horas;
  });

  protected readonly altoLienzo = (HORA_FIN - HORA_INICIO) * ALTO_HORA;
  protected readonly altoHora = ALTO_HORA;

  /** Citas del dia que caen dentro del horario dibujado, ya posicionadas. */
  protected readonly bloques = computed<BloqueAgenda[]>(() =>
    this.calcularBloques(this.citas().filter((c) => this.enHorario(c))),
  );

  /** Citas del dia que quedan fuera de la franja 08:00 - 20:00. */
  protected readonly fueraDeHorario = computed<Cita[]>(() =>
    this.citas()
      .filter((c) => !this.enHorario(c))
      .sort((a, b) => a.fechaHora.localeCompare(b.fechaHora)),
  );

  protected readonly citasOrdenadas = computed<Cita[]>(() =>
    [...this.citas()].sort((a, b) => a.fechaHora.localeCompare(b.fechaHora)),
  );

  protected readonly diaLegible = computed<string>(() => this.formatearDiaLargo(this.dia()));

  protected readonly tituloFormulario = computed<string>(() =>
    this.citaEnEdicion() ? 'Editar cita' : 'Nueva cita',
  );

  /* -------------------------------- Carga --------------------------------- */

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

  protected cargarCitas(): void {
    this.cargando.set(true);
    this.error.set(null);

    const peticion =
      this.vista() === 'agenda'
        ? this.citaService.agenda(this.dia())
        : this.citaService.listar({
            dia: this.filtros.controls.dia.value || undefined,
            pacienteId: this.numeroOpcional(this.filtros.controls.pacienteId.value),
            odontologoId: this.numeroOpcional(this.filtros.controls.odontologoId.value),
            estado: (this.filtros.controls.estado.value || undefined) as EstadoCita | undefined,
          });

    peticion.subscribe({
      next: (lista) => {
        this.citas.set(lista);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.citas.set([]);
        this.cargando.set(false);
        this.error.set(fallo?.mensaje ?? 'No se pudieron cargar las citas');
      },
    });
  }

  protected cambiarVista(vista: 'agenda' | 'lista'): void {
    if (this.vista() === vista) {
      return;
    }
    this.vista.set(vista);
    this.cargarCitas();
  }

  protected limpiarFiltros(): void {
    this.filtros.reset({ dia: '', pacienteId: '', odontologoId: '', estado: '' });
    this.cargarCitas();
  }

  /* ---------------------------- Navegacion del dia ------------------------ */

  protected irADia(valor: string): void {
    if (!valor) {
      return;
    }
    this.dia.set(valor);
    this.cargarCitas();
  }

  protected desplazarDia(dias: number): void {
    const base = new Date(`${this.dia()}T00:00:00`);
    base.setDate(base.getDate() + dias);
    this.irADia(CitasComponent.aIso(base));
  }

  protected irAHoy(): void {
    this.irADia(CitasComponent.hoy());
  }

  protected cambiarDiaDesdeInput(evento: Event): void {
    this.irADia((evento.target as HTMLInputElement).value);
  }

  /* ------------------------------ Formulario ------------------------------ */

  protected abrirNueva(): void {
    this.citaEnEdicion.set(null);
    this.errorFormulario.set(null);
    this.ocupadas.set([]);
    this.formulario.reset({
      pacienteId: this.filtros.controls.pacienteId.value,
      odontologoId: '',
      fecha: this.vista() === 'agenda' ? this.dia() : CitasComponent.hoy(),
      hora: '',
      duracionMinutos: 30,
      motivo: '',
      observaciones: '',
    });
    this.formAbierto.set(true);
  }

  protected abrirEdicion(cita: Cita): void {
    this.citaEnEdicion.set(cita);
    this.errorFormulario.set(null);
    this.ocupadas.set([]);
    this.formulario.reset({
      pacienteId: String(cita.pacienteId),
      odontologoId: String(cita.odontologoId),
      fecha: this.parteFecha(cita.fechaHora),
      hora: this.parteHora(cita.fechaHora),
      duracionMinutos: cita.duracionMinutos ?? 30,
      motivo: cita.motivo ?? '',
      observaciones: cita.observaciones ?? '',
    });
    this.formAbierto.set(true);
    this.consultarDisponibilidad();
  }

  protected cerrarFormulario(): void {
    this.formAbierto.set(false);
    this.citaEnEdicion.set(null);
    this.errorFormulario.set(null);
    this.ocupadas.set([]);
  }

  /** Consulta las horas ya ocupadas del odontologo para avisar de los cruces. */
  protected consultarDisponibilidad(): void {
    const odontologoId = this.numeroOpcional(this.formulario.controls.odontologoId.value);
    const fecha = this.formulario.controls.fecha.value;
    if (!odontologoId || !fecha) {
      this.ocupadas.set([]);
      return;
    }

    this.consultandoAgenda.set(true);
    this.citaService.disponibilidad(odontologoId, fecha).subscribe({
      next: (horas) => {
        this.ocupadas.set(horas ?? []);
        this.consultandoAgenda.set(false);
      },
      error: () => {
        this.ocupadas.set([]);
        this.consultandoAgenda.set(false);
      },
    });
  }

  protected guardar(): void {
    this.errorFormulario.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const valores = this.formulario.getRawValue();
    const datos: CitaRequest = {
      pacienteId: Number(valores.pacienteId),
      odontologoId: Number(valores.odontologoId),
      fechaHora: `${valores.fecha}T${valores.hora}:00`,
      duracionMinutos: Number(valores.duracionMinutos),
      motivo: valores.motivo.trim() || null,
      observaciones: valores.observaciones.trim() || null,
    };

    const enEdicion = this.citaEnEdicion();
    const peticion = enEdicion
      ? this.citaService.actualizar(enEdicion.id, datos)
      : this.citaService.crear(datos);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.notificaciones.exito(enEdicion ? 'Cita actualizada' : 'Cita registrada');
        this.cerrarFormulario();
        this.cargarCitas();
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        this.errorFormulario.set(fallo?.mensaje ?? 'No se pudo guardar la cita');
      },
    });
  }

  /* -------------------------------- Acciones ------------------------------ */

  protected confirmar(cita: Cita): void {
    this.procesando.set(cita.id);
    this.citaService.confirmar(cita.id).subscribe({
      next: () => {
        this.procesando.set(null);
        this.notificaciones.exito('Cita confirmada');
        this.cargarCitas();
      },
      error: (fallo: ErrorApi) => {
        this.procesando.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo confirmar la cita');
      },
    });
  }

  protected registrarAsistencia(cita: Cita, asistio: boolean): void {
    this.procesando.set(cita.id);
    this.citaService.registrarAsistencia(cita.id, asistio).subscribe({
      next: () => {
        this.procesando.set(null);
        this.notificaciones.exito(asistio ? 'Asistencia registrada' : 'Se registro la inasistencia');
        this.cargarCitas();
      },
      error: (fallo: ErrorApi) => {
        this.procesando.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo registrar la asistencia');
      },
    });
  }

  /* ------------------------------ Reprogramar ----------------------------- */

  protected abrirReprogramar(cita: Cita): void {
    this.citaAReprogramar.set(cita);
    this.formReprogramar.reset({
      fecha: this.parteFecha(cita.fechaHora),
      hora: this.parteHora(cita.fechaHora),
      motivo: '',
    });
  }

  protected cerrarReprogramar(): void {
    this.citaAReprogramar.set(null);
  }

  protected confirmarReprogramar(): void {
    const cita = this.citaAReprogramar();
    if (!cita) {
      return;
    }
    if (this.formReprogramar.invalid) {
      this.formReprogramar.markAllAsTouched();
      return;
    }

    const { fecha, hora, motivo } = this.formReprogramar.getRawValue();
    this.procesando.set(cita.id);
    this.citaService
      .reprogramar(cita.id, {
        nuevaFechaHora: `${fecha}T${hora}:00`,
        motivo: motivo.trim() || null,
      })
      .subscribe({
        next: () => {
          this.procesando.set(null);
          this.cerrarReprogramar();
          this.notificaciones.exito(
            'Se creo una cita nueva. La anterior quedo marcada como reprogramada.',
          );
          this.cargarCitas();
        },
        error: (fallo: ErrorApi) => {
          this.procesando.set(null);
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo reprogramar la cita');
        },
      });
  }

  /* -------------------------------- Cancelar ------------------------------ */

  protected abrirCancelar(cita: Cita): void {
    this.citaACancelar.set(cita);
    this.formCancelar.reset({ motivo: '' });
  }

  protected cerrarCancelar(): void {
    this.citaACancelar.set(null);
  }

  protected confirmarCancelar(): void {
    const cita = this.citaACancelar();
    if (!cita) {
      return;
    }
    const motivo = this.formCancelar.controls.motivo.value.trim();
    this.procesando.set(cita.id);
    this.citaService.cancelar(cita.id, motivo || undefined).subscribe({
      next: () => {
        this.procesando.set(null);
        this.cerrarCancelar();
        this.notificaciones.exito('Cita cancelada');
        this.cargarCitas();
      },
      error: (fallo: ErrorApi) => {
        this.procesando.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo cancelar la cita');
      },
    });
  }

  /* -------------------------------- Eliminar ------------------------------ */

  protected abrirEliminar(cita: Cita): void {
    this.citaAEliminar.set(cita);
  }

  protected cerrarEliminar(): void {
    this.citaAEliminar.set(null);
  }

  protected confirmarEliminar(): void {
    const cita = this.citaAEliminar();
    if (!cita) {
      return;
    }
    this.procesando.set(cita.id);
    this.citaService.eliminar(cita.id).subscribe({
      next: () => {
        this.procesando.set(null);
        this.cerrarEliminar();
        this.notificaciones.exito('Cita eliminada');
        this.cargarCitas();
      },
      error: (fallo: ErrorApi) => {
        this.procesando.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo eliminar la cita');
      },
    });
  }

  /* ------------------------- Reglas segun el estado ----------------------- */

  /** Desde CANCELADA o ATENDIDA la cita ya no admite cambios de estado. */
  protected esFinal(cita: Cita): boolean {
    return cita.estado === 'CANCELADA' || cita.estado === 'ATENDIDA';
  }

  protected puedeConfirmar(cita: Cita): boolean {
    return cita.estado === 'PENDIENTE';
  }

  protected puedeRegistrarAsistencia(cita: Cita): boolean {
    return cita.estado === 'PENDIENTE' || cita.estado === 'CONFIRMADA';
  }

  protected puedeReprogramar(cita: Cita): boolean {
    return !this.esFinal(cita) && cita.estado !== 'REPROGRAMADA';
  }

  protected puedeCancelar(cita: Cita): boolean {
    return !this.esFinal(cita) && cita.estado !== 'REPROGRAMADA';
  }

  protected puedeEditar(cita: Cita): boolean {
    return !this.esFinal(cita);
  }

  /* --------------------------------- Ayudas ------------------------------- */

  protected etiquetaEstado(estado: EstadoCita): string {
    return ETIQUETAS_ESTADO_CITA[estado];
  }

  protected badgeEstado(estado: EstadoCita): string {
    return BADGE_ESTADO_CITA[estado];
  }

  protected parteFecha(iso: string): string {
    return (iso ?? '').split('T')[0] ?? '';
  }

  protected parteHora(iso: string): string {
    const hora = (iso ?? '').split('T')[1] ?? '';
    return hora.slice(0, 5);
  }

  /** "2026-09-04T10:00:00" -> "04/09/2026". */
  protected fechaCorta(iso: string): string {
    const [anio, mes, dia] = this.parteFecha(iso).split('-');
    return anio ? `${dia}/${mes}/${anio}` : '';
  }

  protected rangoHorario(cita: Cita): string {
    const inicio = this.parteHora(cita.fechaHora);
    const fin = cita.fechaHoraFin
      ? this.parteHora(cita.fechaHoraFin)
      : this.sumarMinutos(inicio, cita.duracionMinutos ?? 30);
    return `${inicio} - ${fin}`;
  }

  private sumarMinutos(hora: string, minutos: number): string {
    const total = this.aMinutos(hora) + minutos;
    const h = Math.floor(total / 60) % 24;
    const m = total % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
  }

  private aMinutos(hora: string): number {
    const [h, m] = hora.split(':');
    return Number(h) * 60 + Number(m ?? 0);
  }

  private enHorario(cita: Cita): boolean {
    const inicio = this.aMinutos(this.parteHora(cita.fechaHora));
    return inicio >= HORA_INICIO * 60 && inicio < HORA_FIN * 60;
  }

  private formatearDiaLargo(dia: string): string {
    const fecha = new Date(`${dia}T00:00:00`);
    if (Number.isNaN(fecha.getTime())) {
      return dia;
    }
    return new Intl.DateTimeFormat('es-PE', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(fecha);
  }

  private numeroOpcional(valor: string): number | undefined {
    const numero = Number(valor);
    return valor && !Number.isNaN(numero) ? numero : undefined;
  }

  /**
   * Reparte las citas en carriles para que las que se cruzan queden lado a lado
   * en lugar de superponerse.
   */
  private calcularBloques(citas: Cita[]): BloqueAgenda[] {
    const items = citas
      .map((cita) => {
        const inicio = this.aMinutos(this.parteHora(cita.fechaHora));
        const duracion = Math.max(cita.duracionMinutos ?? 30, 10);
        return { cita, inicio, fin: inicio + duracion };
      })
      .sort((a, b) => a.inicio - b.inicio || a.fin - b.fin);

    const bloques: BloqueAgenda[] = [];
    let grupo: typeof items = [];
    let finMaximo = -1;

    const cerrarGrupo = () => {
      if (grupo.length === 0) {
        return;
      }
      const finDeCarril: number[] = [];
      const carrilDe: number[] = [];

      for (const item of grupo) {
        let carril = finDeCarril.findIndex((fin) => fin <= item.inicio);
        if (carril === -1) {
          finDeCarril.push(item.fin);
          carril = finDeCarril.length - 1;
        } else {
          finDeCarril[carril] = item.fin;
        }
        carrilDe.push(carril);
      }

      const total = finDeCarril.length;
      grupo.forEach((item, indice) => {
        const minutosDesdeInicio = item.inicio - HORA_INICIO * 60;
        const duracion = item.fin - item.inicio;
        const topeInferior = (HORA_FIN - HORA_INICIO) * 60 - minutosDesdeInicio;
        const alto = Math.max((Math.min(duracion, topeInferior) / 60) * ALTO_HORA, 24);
        bloques.push({
          cita: item.cita,
          top: (minutosDesdeInicio / 60) * ALTO_HORA,
          alto,
          izquierda: (carrilDe[indice] * 100) / total,
          ancho: 100 / total,
          corto: alto < ALTO_MINIMO_TRES_LINEAS,
        });
      });
    };

    for (const item of items) {
      if (grupo.length > 0 && item.inicio >= finMaximo) {
        cerrarGrupo();
        grupo = [];
        finMaximo = -1;
      }
      grupo.push(item);
      finMaximo = Math.max(finMaximo, item.fin);
    }
    cerrarGrupo();

    return bloques;
  }

  private static hoy(): string {
    return CitasComponent.aIso(new Date());
  }

  private static aIso(fecha: Date): string {
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${fecha.getFullYear()}-${mes}-${dia}`;
  }
}
