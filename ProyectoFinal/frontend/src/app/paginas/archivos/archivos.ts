import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { distinctUntilChanged } from 'rxjs/operators';

import { ArchivoService } from '../../core/services/archivo.service';
import { PacienteService } from '../../core/services/paciente.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { ErrorApi } from '../../core/models/comun.model';
import { PacienteResumen } from '../../core/models/paciente.model';
import {
  Archivo,
  ETIQUETAS_TIPO_ARCHIVO,
  TIPOS_ARCHIVO,
  TipoArchivo,
} from '../../core/models/archivo.model';

/**
 * El backend puede enviar campos derivados que el modelo base no declara
 * (nombre del paciente, del usuario y la URL ya construida).
 */
type ArchivoVista = Archivo & {
  pacienteNombre?: string | null;
  usuarioNombre?: string | null;
  urlDescarga?: string | null;
};

/** Extensiones que acepta el backend. */
const EXTENSIONES_PERMITIDAS = [
  'jpg',
  'jpeg',
  'png',
  'gif',
  'bmp',
  'webp',
  'pdf',
  'doc',
  'docx',
  'txt',
];

const EXTENSIONES_IMAGEN = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'];

/** Tope de 10 MB, el mismo que valida el backend. */
const TAMANO_MAXIMO = 10 * 1024 * 1024;

/** Geometria del icono que representa cada tipo de archivo. */
const ICONOS: Record<TipoArchivo, string> = {
  RADIOGRAFIA:
    'M4 3h16v18H4zM12 3v18M8 7v10M16 7v10',
  FOTOGRAFIA:
    'M3 7a2 2 0 0 1 2-2h2l1.5-2h7L17 5h2a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM12 16a4 4 0 1 0 0-8 4 4 0 0 0 0 8z',
  INFORME: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M9 13h6M9 17h6',
  CONSENTIMIENTO:
    'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M8 17c2-3 4 1 8-3',
  RECETA: 'M5 3h9l5 5v13H5zM14 3v5h5M8 12h8M8 16h5M9 8V6',
  DOCUMENTO: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6',
};

/** Clase de badge por tipo, para que la galeria se lea de un vistazo. */
const CLASES_TIPO: Record<TipoArchivo, string> = {
  RADIOGRAFIA: 'badge badge-info',
  FOTOGRAFIA: 'badge badge-primario',
  INFORME: 'badge badge-neutro',
  CONSENTIMIENTO: 'badge badge-exito',
  RECETA: 'badge badge-advertencia',
  DOCUMENTO: 'badge badge-neutro',
};

/** Archivos clinicos: subida, galeria, edicion de metadatos y baja. */
@Component({
  selector: 'app-archivos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './archivos.html',
  styleUrl: './archivos.scss',
})
export class ArchivosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly archivos = inject(ArchivoService);
  private readonly pacientesApi = inject(PacienteService);
  private readonly auth = inject(AuthService);
  private readonly notificaciones = inject(NotificacionService);
  private readonly ruta = inject(ActivatedRoute);

  private readonly entradaArchivo =
    viewChild<ElementRef<HTMLInputElement>>('entradaArchivo');

  protected readonly tipos = TIPOS_ARCHIVO;
  protected readonly etiquetasTipo = ETIQUETAS_TIPO_ARCHIVO;

  /* ------------------------------- Estado -------------------------------- */

  protected readonly lista = signal<ArchivoVista[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly pacientes = signal<PacienteResumen[]>([]);
  protected readonly cargandoPacientes = signal(false);
  protected readonly errorPacientes = signal<string | null>(null);

  protected readonly filtroPaciente = new FormControl<string>('', { nonNullable: true });
  protected readonly filtroTipo = new FormControl<'' | TipoArchivo>('', { nonNullable: true });

  protected readonly pacienteSeleccionado = signal<string>('');

  /** Usuario en sesion: es quien queda registrado como autor de la subida. */
  protected readonly nombreDelQueSube = computed(
    () => this.auth.usuarioActual()?.nombreCompleto ?? 'usuario sin sesión',
  );

  protected readonly nombrePacienteFiltro = computed(() => {
    const id = Number(this.pacienteSeleccionado());
    const encontrado = this.pacientes().find((p) => p.id === id);
    return encontrado ? encontrado.nombreCompleto : '';
  });

  /* ------------------------------- Subida -------------------------------- */

  protected readonly panelSubida = signal(false);
  protected readonly subiendo = signal(false);
  protected readonly errorSubida = signal<string | null>(null);
  protected readonly seleccionado = signal<File | null>(null);

  protected readonly formularioSubida = this.fb.nonNullable.group({
    pacienteId: ['', [Validators.required]],
    tipo: ['RADIOGRAFIA' as TipoArchivo, [Validators.required]],
    descripcion: ['', [Validators.maxLength(255)]],
  });

  /* ------------------------- Edicion y eliminacion ----------------------- */

  protected readonly editando = signal<ArchivoVista | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorEdicion = signal<string | null>(null);

  protected readonly formularioEdicion = this.fb.nonNullable.group({
    tipo: ['DOCUMENTO' as TipoArchivo, [Validators.required]],
    descripcion: ['', [Validators.maxLength(255)]],
  });

  protected readonly porEliminar = signal<ArchivoVista | null>(null);
  protected readonly eliminando = signal(false);

  constructor() {
    const desdeUrl = this.ruta.snapshot.queryParamMap.get('pacienteId');
    if (desdeUrl && Number(desdeUrl) > 0) {
      this.filtroPaciente.setValue(desdeUrl, { emitEvent: false });
      this.pacienteSeleccionado.set(desdeUrl);
      this.formularioSubida.controls.pacienteId.setValue(desdeUrl);
    }

    this.filtroPaciente.valueChanges
      .pipe(distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((valor) => {
        this.pacienteSeleccionado.set(valor);
        this.formularioSubida.controls.pacienteId.setValue(valor);
        this.cargar();
      });

    this.filtroTipo.valueChanges
      .pipe(distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => this.cargar());

    this.cargarPacientes();
    this.cargar();
  }

  /* ------------------------------ Consultas ------------------------------ */

  private cargarPacientes(): void {
    this.cargandoPacientes.set(true);
    this.errorPacientes.set(null);

    this.pacientesApi.listar().subscribe({
      next: (filas) => {
        this.pacientes.set(filas);
        this.cargandoPacientes.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.pacientes.set([]);
        this.cargandoPacientes.set(false);
        this.errorPacientes.set(fallo?.mensaje ?? 'No se pudo cargar la lista de pacientes');
      },
    });
  }

  protected cargar(): void {
    const pacienteId = Number(this.filtroPaciente.value);
    const tipo = this.filtroTipo.value;

    this.cargando.set(true);
    this.error.set(null);

    const peticion =
      pacienteId > 0
        ? this.archivos.listarPorPaciente(pacienteId, tipo || undefined)
        : this.archivos.listar({ tipo: tipo || undefined });

    peticion.subscribe({
      next: (filas) => {
        this.lista.set(filas as ArchivoVista[]);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.lista.set([]);
        this.error.set(fallo?.mensaje ?? 'No se pudieron cargar los archivos');
        this.cargando.set(false);
      },
    });
  }

  /* -------------------------------- Subida ------------------------------- */

  protected abrirSubida(): void {
    this.errorSubida.set(null);
    this.formularioSubida.controls.pacienteId.setValue(this.filtroPaciente.value);
    this.panelSubida.set(true);
  }

  protected cerrarSubida(): void {
    this.panelSubida.set(false);
    this.errorSubida.set(null);
    this.limpiarSeleccion();
  }

  protected alElegirArchivo(evento: Event): void {
    const entrada = evento.target as HTMLInputElement;
    const archivo = entrada.files && entrada.files.length > 0 ? entrada.files[0] : null;

    if (!archivo) {
      this.seleccionado.set(null);
      return;
    }

    const extension = this.extensionDe(archivo.name);

    if (!EXTENSIONES_PERMITIDAS.includes(extension)) {
      this.notificaciones.error(
        `La extensión .${extension || '?'} no está permitida. Use: ${EXTENSIONES_PERMITIDAS.join(', ')}.`,
      );
      this.limpiarSeleccion();
      return;
    }

    if (archivo.size > TAMANO_MAXIMO) {
      this.notificaciones.error(
        `El archivo pesa ${this.tamanoLegible(archivo.size)} y el máximo permitido es 10 MB.`,
      );
      this.limpiarSeleccion();
      return;
    }

    this.errorSubida.set(null);
    this.seleccionado.set(archivo);
  }

  protected subir(): void {
    if (this.subiendo()) {
      return;
    }

    this.errorSubida.set(null);

    const archivo = this.seleccionado();
    if (!archivo) {
      this.errorSubida.set('Seleccione el archivo que desea subir.');
      return;
    }

    if (this.formularioSubida.invalid) {
      this.formularioSubida.markAllAsTouched();
      this.errorSubida.set('Complete el paciente y el tipo de archivo.');
      return;
    }

    const usuario = this.auth.usuarioActual();
    if (!usuario) {
      this.errorSubida.set('Su sesión expiró. Vuelva a iniciar sesión para subir archivos.');
      return;
    }

    const v = this.formularioSubida.getRawValue();
    const pacienteId = Number(v.pacienteId);

    const datos = this.archivos.construirFormData({
      archivo,
      pacienteId,
      usuarioId: usuario.id,
      tipo: v.tipo,
      descripcion: v.descripcion.trim() || null,
    });

    this.subiendo.set(true);

    this.archivos.subir(datos).subscribe({
      next: (subido) => {
        this.subiendo.set(false);
        this.notificaciones.exito(`Archivo "${subido.nombreOriginal}" subido correctamente`);
        this.limpiarSeleccion();
        this.formularioSubida.controls.descripcion.setValue('');
        this.panelSubida.set(false);

        // El listado se posiciona en el paciente del archivo recien subido.
        if (this.filtroPaciente.value !== v.pacienteId) {
          this.filtroPaciente.setValue(v.pacienteId);
        } else {
          this.cargar();
        }
      },
      error: (fallo: ErrorApi) => {
        this.subiendo.set(false);
        const mensaje = fallo?.mensaje ?? 'No se pudo subir el archivo';
        this.errorSubida.set(mensaje);
        this.notificaciones.error(mensaje);
      },
    });
  }

  /* ------------------------- Edicion de metadatos ------------------------ */

  protected abrirEdicion(archivo: ArchivoVista): void {
    this.formularioEdicion.setValue({
      tipo: archivo.tipo,
      descripcion: archivo.descripcion ?? '',
    });
    this.errorEdicion.set(null);
    this.editando.set(archivo);
  }

  protected cerrarEdicion(): void {
    this.editando.set(null);
    this.errorEdicion.set(null);
  }

  protected guardarEdicion(): void {
    const archivo = this.editando();
    if (!archivo || this.guardando()) {
      return;
    }

    if (this.formularioEdicion.invalid) {
      this.formularioEdicion.markAllAsTouched();
      this.errorEdicion.set('Revise los datos del formulario.');
      return;
    }

    const v = this.formularioEdicion.getRawValue();
    this.guardando.set(true);
    this.errorEdicion.set(null);

    this.archivos
      .actualizar(archivo.id, {
        tipo: v.tipo,
        descripcion: v.descripcion.trim() || null,
      })
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.editando.set(null);
          this.notificaciones.exito('Datos del archivo actualizados');
          this.cargar();
        },
        error: (fallo: ErrorApi) => {
          this.guardando.set(false);
          const mensaje = fallo?.mensaje ?? 'No se pudo actualizar el archivo';
          this.errorEdicion.set(mensaje);
          this.notificaciones.error(mensaje);
        },
      });
  }

  /* ----------------------------- Eliminacion ----------------------------- */

  protected pedirEliminar(archivo: ArchivoVista): void {
    this.porEliminar.set(archivo);
  }

  protected cancelarEliminar(): void {
    this.porEliminar.set(null);
  }

  protected confirmarEliminar(): void {
    const archivo = this.porEliminar();
    if (!archivo || this.eliminando()) {
      return;
    }

    this.eliminando.set(true);

    this.archivos.eliminar(archivo.id).subscribe({
      next: () => {
        this.eliminando.set(false);
        this.porEliminar.set(null);
        this.notificaciones.exito(`Archivo "${archivo.nombreOriginal}" eliminado`);
        this.cargar();
      },
      error: (fallo: ErrorApi) => {
        this.eliminando.set(false);
        this.porEliminar.set(null);
        this.notificaciones.error(fallo?.mensaje ?? 'No se pudo eliminar el archivo');
      },
    });
  }

  /* -------------------------------- Apoyo -------------------------------- */

  protected urlDescarga(archivo: ArchivoVista): string {
    return archivo.urlDescarga && archivo.urlDescarga.length > 0
      ? archivo.urlDescarga
      : this.archivos.urlDescarga(archivo.id);
  }

  protected esImagen(archivo: ArchivoVista): boolean {
    return EXTENSIONES_IMAGEN.includes(this.extensionDe(archivo.extension || archivo.nombreOriginal));
  }

  protected icono(tipo: TipoArchivo): string {
    return ICONOS[tipo] ?? ICONOS.DOCUMENTO;
  }

  protected claseTipo(tipo: TipoArchivo): string {
    return CLASES_TIPO[tipo] ?? 'badge badge-neutro';
  }

  protected etiquetaTipo(tipo: TipoArchivo): string {
    return ETIQUETAS_TIPO_ARCHIVO[tipo] ?? tipo;
  }

  protected nombrePaciente(archivo: ArchivoVista): string {
    if (archivo.pacienteNombre) {
      return archivo.pacienteNombre;
    }
    const encontrado = this.pacientes().find((p) => p.id === archivo.pacienteId);
    return encontrado ? encontrado.nombreCompleto : `Paciente n.° ${archivo.pacienteId}`;
  }

  protected nombreUsuario(archivo: ArchivoVista): string {
    if (archivo.usuarioNombre) {
      return archivo.usuarioNombre;
    }
    const actual = this.auth.usuarioActual();
    return actual && actual.id === archivo.usuarioId
      ? actual.nombreCompleto
      : `Usuario n.° ${archivo.usuarioId}`;
  }

  protected pesoLegible(archivo: ArchivoVista): string {
    return archivo.tamanoLegible && archivo.tamanoLegible.length > 0
      ? archivo.tamanoLegible
      : this.tamanoLegible(archivo.tamanoBytes);
  }

  /** Convierte bytes a un texto corto: 950 KB, 1.4 MB, etc. */
  protected tamanoLegible(bytes: number): string {
    if (!bytes || bytes <= 0) {
      return '0 B';
    }
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  /** Fecha ISO a dd/mm/aaaa hh:mm sin depender de la zona horaria. */
  protected fechaHora(iso: string | null | undefined): string {
    if (!iso) {
      return '-';
    }
    const [dia, resto] = iso.split('T');
    const partes = dia.split('-');
    const fecha = partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : dia;
    return resto ? `${fecha} ${resto.slice(0, 5)}` : fecha;
  }

  private extensionDe(nombre: string): string {
    const limpio = (nombre ?? '').trim().toLowerCase();
    if (limpio.length === 0) {
      return '';
    }
    const punto = limpio.lastIndexOf('.');
    return punto >= 0 ? limpio.slice(punto + 1) : limpio;
  }

  private limpiarSeleccion(): void {
    this.seleccionado.set(null);
    const entrada = this.entradaArchivo();
    if (entrada) {
      entrada.nativeElement.value = '';
    }
  }
}
