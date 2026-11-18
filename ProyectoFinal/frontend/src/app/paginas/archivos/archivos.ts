import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { from, of } from 'rxjs';
import { catchError, concatMap, distinctUntilChanged, finalize, map, tap, toArray } from 'rxjs/operators';

import {
  DESCRIPCION_MAXIMA_ARCHIVO,
  EXTENSIONES_ARCHIVO,
  EXTENSIONES_IMAGEN,
  archivoPermitidoValidator,
  extensionDe,
} from '../../core/validators/archivos.validators';
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

/** Una fila del FormArray de subida: un archivo con sus propios metadatos. */
type FilaArchivo = FormGroup<{
  archivo: FormControl<File | null>;
  tipo: FormControl<TipoArchivo>;
  descripcion: FormControl<string>;
}>;

/** Resultado de subir una fila: el archivo creado o el error del backend. */
interface ResultadoSubida {
  fila: FilaArchivo;
  subido: Archivo | null;
  fallo: ErrorApi | null;
}

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
  private readonly destroyRef = inject(DestroyRef);

  private readonly entradaArchivo =
    viewChild<ElementRef<HTMLInputElement>>('entradaArchivo');

  protected readonly tipos = TIPOS_ARCHIVO;
  protected readonly etiquetasTipo = ETIQUETAS_TIPO_ARCHIVO;
  protected readonly extensionesPermitidas = EXTENSIONES_ARCHIVO;
  protected readonly descripcionMaxima = DESCRIPCION_MAXIMA_ARCHIVO;
  protected readonly aceptaArchivos = EXTENSIONES_ARCHIVO.map((e) => `.${e}`).join(',');

  /* ------------------------------- Estado -------------------------------- */

  protected readonly lista = signal<ArchivoVista[]>([]);

  /**
   * URLs blob: de las miniaturas, por id de archivo. La descarga esta protegida
   * con JWT y un <img src> no envia el token, asi que el binario se pide con
   * HttpClient (pasa por el interceptor) y se muestra desde memoria.
   */
  protected readonly vistasPrevias = signal<Record<number, string>>({});
  protected readonly descargandoId = signal<number | null>(null);
  /** Evita que una respuesta de una carga anterior pinte miniaturas viejas. */
  private generacionVistas = 0;
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
  /** Avance de una subida de varios archivos, que se envian uno por uno. */
  protected readonly progreso = signal<{ hechos: number; total: number } | null>(null);

  /**
   * Un paciente y una lista dinamica de archivos (FormArray, semana 13).
   * El backend recibe un archivo por peticion, asi que cada fila se sube aparte.
   */
  protected readonly formularioSubida = this.fb.group({
    pacienteId: this.fb.nonNullable.control('', [Validators.required]),
    archivos: this.fb.array<FilaArchivo>([], [Validators.required]),
  });

  /* ------------------------- Edicion y eliminacion ----------------------- */

  protected readonly editando = signal<ArchivoVista | null>(null);
  protected readonly guardando = signal(false);
  protected readonly errorEdicion = signal<string | null>(null);

  protected readonly formularioEdicion = this.fb.nonNullable.group({
    tipo: ['DOCUMENTO' as TipoArchivo, [Validators.required]],
    descripcion: ['', [Validators.maxLength(DESCRIPCION_MAXIMA_ARCHIVO)]],
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

    this.destroyRef.onDestroy(() => this.liberarVistasPrevias());
  }

  /* ------------------------------ Consultas ------------------------------ */

  private cargarPacientes(): void {
    this.cargandoPacientes.set(true);
    this.errorPacientes.set(null);

    this.pacientesApi
      .listar()
      .pipe(finalize(() => this.cargandoPacientes.set(false)))
      .subscribe({
        next: (filas) => this.pacientes.set(filas),
        error: (fallo: ErrorApi) => {
          this.pacientes.set([]);
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

    peticion.pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: (filas) => {
        this.lista.set(filas as ArchivoVista[]);
        this.cargarVistasPrevias(filas as ArchivoVista[]);
      },
      error: (fallo: ErrorApi) => {
        this.lista.set([]);
        this.liberarVistasPrevias();
        this.error.set(fallo?.mensaje ?? 'No se pudieron cargar los archivos');
      },
    });
  }

  private cargarVistasPrevias(filas: ArchivoVista[]): void {
    this.liberarVistasPrevias();
    const generacion = ++this.generacionVistas;

    for (const archivo of filas.filter((a) => this.esImagen(a))) {
      this.archivos.descargar(archivo.id).subscribe({
        next: (blob) => {
          if (generacion !== this.generacionVistas) {
            return;
          }
          const url = URL.createObjectURL(blob);
          this.vistasPrevias.update((actuales) => ({ ...actuales, [archivo.id]: url }));
        },
        // Sin miniatura se muestra el icono del tipo; no hace falta avisar.
        error: () => undefined,
      });
    }
  }

  private liberarVistasPrevias(): void {
    for (const url of Object.values(this.vistasPrevias())) {
      URL.revokeObjectURL(url);
    }
    this.vistasPrevias.set({});
  }

  /** Descarga con el token (via HttpClient) y entrega el archivo con su nombre original. */
  protected descargar(archivo: ArchivoVista): void {
    if (this.descargandoId() !== null) {
      return;
    }
    this.descargandoId.set(archivo.id);

    this.archivos
      .descargar(archivo.id)
      .pipe(finalize(() => this.descargandoId.set(null)))
      .subscribe({
        next: (blob) => {
          const url = URL.createObjectURL(blob);
          const enlace = document.createElement('a');
          enlace.href = url;
          enlace.download = archivo.nombreOriginal;
          enlace.click();
          setTimeout(() => URL.revokeObjectURL(url));
        },
        error: (fallo: ErrorApi) =>
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo descargar el archivo'),
      });
  }

  /* -------------------------------- Subida ------------------------------- */

  /** Filas del FormArray de subida. */
  protected get filas(): FormArray<FilaArchivo> {
    return this.formularioSubida.controls.archivos;
  }

  protected abrirSubida(): void {
    this.errorSubida.set(null);
    this.formularioSubida.controls.pacienteId.setValue(this.filtroPaciente.value);
    this.panelSubida.set(true);
  }

  protected cerrarSubida(): void {
    this.panelSubida.set(false);
    this.errorSubida.set(null);
    this.filas.clear();
    this.formularioSubida.markAsUntouched();
    this.limpiarEntrada();
  }

  /**
   * Cada archivo elegido se agrega como una fila. Los que no cumplen las
   * reglas tambien se agregan, marcados con su error, para que el usuario
   * vea por que no se pueden subir y los quite.
   */
  protected alElegirArchivos(evento: Event): void {
    const entrada = evento.target as HTMLInputElement;
    const elegidos = entrada.files ? Array.from(entrada.files) : [];

    for (const archivo of elegidos) {
      const fila = this.crearFila(archivo);
      fila.controls.archivo.markAsTouched();
      this.filas.push(fila);
    }

    this.errorSubida.set(null);
    // Se vacia el input para poder volver a elegir el mismo archivo.
    this.limpiarEntrada();
  }

  protected quitarFila(indice: number): void {
    this.filas.removeAt(indice);
  }

  private crearFila(archivo: File): FilaArchivo {
    const esImagen = EXTENSIONES_IMAGEN.includes(extensionDe(archivo.name));
    return this.fb.group({
      archivo: this.fb.control<File | null>(archivo, [
        Validators.required,
        archivoPermitidoValidator(),
      ]),
      tipo: this.fb.nonNullable.control<TipoArchivo>(esImagen ? 'RADIOGRAFIA' : 'DOCUMENTO', [
        Validators.required,
      ]),
      descripcion: this.fb.nonNullable.control('', [
        Validators.maxLength(DESCRIPCION_MAXIMA_ARCHIVO),
      ]),
    });
  }

  protected subir(): void {
    if (this.subiendo()) {
      return;
    }

    this.errorSubida.set(null);

    if (this.formularioSubida.invalid) {
      this.formularioSubida.markAllAsTouched();
      this.errorSubida.set(
        this.filas.length === 0
          ? 'Agregue al menos un archivo.'
          : 'Revise el paciente y los archivos marcados antes de subirlos.',
      );
      return;
    }

    if (!this.auth.sesionVigente()) {
      this.errorSubida.set('Su sesión expiró. Vuelva a iniciar sesión para subir archivos.');
      return;
    }

    const pacienteTexto = this.formularioSubida.controls.pacienteId.value;
    const pacienteId = Number(pacienteTexto);
    const filas = [...this.filas.controls];

    this.subiendo.set(true);
    this.progreso.set({ hechos: 0, total: filas.length });

    // Uno por uno (concatMap) y sin cortar la serie si uno falla (catchError por fila).
    // Quien sube el archivo lo toma el backend del token (no se envia usuarioId).
    from(filas)
      .pipe(
        concatMap((fila) => {
          const v = fila.getRawValue();
          const datos = this.archivos.construirFormData({
            archivo: v.archivo as File,
            pacienteId,
            tipo: v.tipo,
            descripcion: v.descripcion.trim() || null,
          });
          return this.archivos.subir(datos).pipe(
            map((subido): ResultadoSubida => ({ fila, subido, fallo: null })),
            catchError((fallo: ErrorApi) => of<ResultadoSubida>({ fila, subido: null, fallo })),
            tap(() => this.progreso.update((p) => (p ? { ...p, hechos: p.hechos + 1 } : p))),
          );
        }),
        toArray(),
        finalize(() => {
          this.subiendo.set(false);
          this.progreso.set(null);
        }),
      )
      .subscribe((resultados) => this.alTerminarSubida(resultados, pacienteTexto));
  }

  /**
   * Las filas subidas salen del formulario; las que fallaron se quedan para
   * reintentar, con el mensaje del backend.
   */
  private alTerminarSubida(resultados: ResultadoSubida[], pacienteTexto: string): void {
    const exitos = resultados.filter((r) => r.subido !== null);
    const fallos = resultados.filter((r) => r.fallo !== null);

    for (const { fila } of exitos) {
      const indice = this.filas.controls.indexOf(fila);
      if (indice >= 0) {
        this.filas.removeAt(indice);
      }
    }

    if (exitos.length > 0) {
      this.notificaciones.exito(
        exitos.length === 1
          ? `Archivo "${exitos[0].subido?.nombreOriginal}" subido correctamente`
          : `${exitos.length} archivos subidos correctamente`,
      );

      // El listado se posiciona en el paciente de los archivos recien subidos.
      if (this.filtroPaciente.value !== pacienteTexto) {
        this.filtroPaciente.setValue(pacienteTexto);
      } else {
        this.cargar();
      }
    }

    if (fallos.length === 0) {
      this.cerrarSubida();
      return;
    }

    const detalle = fallos
      .map((r) => `${r.fila.controls.archivo.value?.name}: ${r.fallo?.mensaje ?? 'error desconocido'}`)
      .join('; ');
    const mensaje =
      exitos.length > 0
        ? `Se subieron ${exitos.length} de ${resultados.length}. No se pudo subir ${detalle}.`
        : `No se pudo subir ${detalle}.`;
    this.errorSubida.set(mensaje);
    this.notificaciones.error(mensaje);
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
      .pipe(finalize(() => this.guardando.set(false)))
      .subscribe({
        next: () => {
          this.editando.set(null);
          this.notificaciones.exito('Datos del archivo actualizados');
          this.cargar();
        },
        error: (fallo: ErrorApi) => {
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

    this.archivos
      .eliminar(archivo.id)
      .pipe(
        finalize(() => {
          this.eliminando.set(false);
          this.porEliminar.set(null);
        }),
      )
      .subscribe({
        next: () => {
          this.notificaciones.exito(`Archivo "${archivo.nombreOriginal}" eliminado`);
          this.cargar();
        },
        error: (fallo: ErrorApi) => {
          this.notificaciones.error(fallo?.mensaje ?? 'No se pudo eliminar el archivo');
        },
      });
  }

  /* -------------------------------- Apoyo -------------------------------- */

  protected esImagen(archivo: ArchivoVista): boolean {
    return EXTENSIONES_IMAGEN.includes(
      (archivo.extension || extensionDe(archivo.nombreOriginal)).toLowerCase(),
    );
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

  private limpiarEntrada(): void {
    const entrada = this.entradaArchivo();
    if (entrada) {
      entrada.nativeElement.value = '';
    }
  }
}
