import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
  untracked,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PacienteService } from '../../core/services/paciente.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { ErrorApi } from '../../core/models/comun.model';
import {
  Antecedentes,
  AntecedentesRequest,
  EstadoPaciente,
  Paciente,
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

/** Enlace rapido a los demas modulos, siempre con el paciente preseleccionado. */
export interface EnlacePaciente {
  ruta: string;
  etiqueta: string;
  descripcion: string;
  icono: string;
}

const ENLACES: EnlacePaciente[] = [
  {
    ruta: '/citas',
    etiqueta: 'Citas',
    descripcion: 'Agenda y asistencia',
    icono: 'M3 6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM16 2v4M8 2v4M3 10h18',
  },
  {
    ruta: '/historias',
    etiqueta: 'Historias clínicas',
    descripcion: 'Consultas y diagnósticos',
    icono: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8',
  },
  {
    ruta: '/odontograma',
    etiqueta: 'Odontograma',
    descripcion: 'Estado por pieza dental',
    icono:
      'M12 3c-2 0-2.5 1-4.5 1S4 3.6 4 6c0 3 1 4.5 1.6 7.5.5 2.4.6 5.5 2.4 5.5 1.7 0 1.4-4 4-4s2.3 4 4 4c1.8 0 1.9-3.1 2.4-5.5C19 10.5 20 9 20 6c0-2.4-1.5-2-3.5-2S14 3 12 3z',
  },
  {
    ruta: '/tratamientos',
    etiqueta: 'Tratamientos',
    descripcion: 'Planes y sesiones',
    icono:
      'M9 4H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-2M9 4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2M9 4h6M9 14l2 2 4-4',
  },
  {
    ruta: '/archivos',
    etiqueta: 'Archivos',
    descripcion: 'Radiografías y documentos',
    icono: 'M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z',
  },
];

/**
 * Ficha del paciente (ruta /pacientes/:id).
 * El parametro `id` llega por binding de ruta gracias a withComponentInputBinding().
 */
@Component({
  selector: 'app-paciente-detalle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './paciente-detalle.html',
  styleUrl: './paciente-detalle.scss',
})
export class PacienteDetalleComponent {
  readonly id = input<string>('');

  private readonly fb = inject(FormBuilder);
  private readonly pacientes = inject(PacienteService);
  private readonly notificaciones = inject(NotificacionService);

  protected readonly enlaces = ENLACES;

  protected readonly paciente = signal<Paciente | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly editando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly errorFormulario = signal<string | null>(null);

  /** Id numerico ya validado; null cuando la ruta trae basura. */
  protected readonly idNumero = computed<number | null>(() => {
    const bruto = (this.id() ?? '').toString().trim();
    const numero = Number(bruto);
    return bruto.length > 0 && Number.isInteger(numero) && numero > 0 ? numero : null;
  });

  /** Parametro de consulta que reciben las demas pantallas. */
  protected readonly parametroPaciente = computed(() => ({ pacienteId: this.idNumero() }));

  protected readonly formulario = this.fb.nonNullable.group({
    enfermedades: [''],
    alergias: [''],
    medicamentos: [''],
    habitos: [''],
    antecedentesOdontologicos: [''],
    observaciones: [''],
  });

  constructor() {
    effect(() => {
      const id = this.idNumero();
      untracked(() => this.cargar(id));
    });
  }

  /* ------------------------------ Consultas ----------------------------- */

  protected recargar(): void {
    this.cargar(this.idNumero());
  }

  private cargar(id: number | null): void {
    this.editando.set(false);
    this.errorFormulario.set(null);

    if (id === null) {
      this.paciente.set(null);
      this.error.set('El identificador del paciente no es válido.');
      this.cargando.set(false);
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.pacientes.buscarPorId(id).subscribe({
      next: (ficha) => {
        this.paciente.set(ficha);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.paciente.set(null);
        this.error.set(
          fallo?.estado === 404
            ? 'El paciente solicitado no existe o fue eliminado.'
            : (fallo?.mensaje ?? 'No se pudo cargar la ficha del paciente'),
        );
        this.cargando.set(false);
      },
    });
  }

  /* --------------------------- Antecedentes ----------------------------- */

  protected abrirEdicion(): void {
    const ficha = this.paciente();
    if (!ficha) {
      return;
    }

    this.formulario.setValue({
      enfermedades: deLista(ficha.antecedentes?.enfermedades),
      alergias: deLista(ficha.antecedentes?.alergias),
      medicamentos: deLista(ficha.antecedentes?.medicamentos),
      habitos: deLista(ficha.antecedentes?.habitos),
      antecedentesOdontologicos: ficha.antecedentes?.antecedentesOdontologicos ?? '',
      observaciones: ficha.antecedentes?.observaciones ?? '',
    });

    this.errorFormulario.set(null);
    this.editando.set(true);
  }

  protected cancelarEdicion(): void {
    this.editando.set(false);
    this.errorFormulario.set(null);
  }

  protected guardarAntecedentes(): void {
    const id = this.idNumero();
    if (id === null || this.guardando()) {
      return;
    }

    const v = this.formulario.getRawValue();
    const datos: AntecedentesRequest = {
      enfermedades: aLista(v.enfermedades),
      alergias: aLista(v.alergias),
      medicamentos: aLista(v.medicamentos),
      habitos: aLista(v.habitos),
      antecedentesOdontologicos: this.opcional(v.antecedentesOdontologicos),
      observaciones: this.opcional(v.observaciones),
    };

    this.guardando.set(true);
    this.errorFormulario.set(null);

    this.pacientes.actualizarAntecedentes(id, datos).subscribe({
      next: (ficha) => {
        this.paciente.set(ficha);
        this.guardando.set(false);
        this.editando.set(false);
        this.notificaciones.exito('Antecedentes actualizados correctamente');
      },
      error: (fallo: ErrorApi) => {
        this.guardando.set(false);
        const mensaje = fallo?.mensaje ?? 'No se pudieron actualizar los antecedentes';
        this.errorFormulario.set(mensaje);
        this.notificaciones.error(mensaje);
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
    return edad === null || edad === undefined ? 'Sin registro' : `${edad} años`;
  }

  protected textoSexo(sexo: string | null): string {
    if (sexo === 'M') {
      return 'Masculino';
    }
    if (sexo === 'F') {
      return 'Femenino';
    }
    return sexo && sexo.trim().length > 0 ? sexo : 'Sin registro';
  }

  /**
   * Lista de antecedentes lista para la plantilla.
   * Aunque el modelo declara `antecedentes` obligatorio, se protege el acceso
   * por si el backend responde sin ese bloque.
   */
  protected listaDe(
    ficha: Paciente,
    clave: 'enfermedades' | 'alergias' | 'medicamentos' | 'habitos',
  ): string[] {
    const antecedentes = ficha.antecedentes as Antecedentes | null | undefined;
    return antecedentes?.[clave] ?? [];
  }

  /** Texto libre de los antecedentes, con el mismo resguardo que listaDe(). */
  protected textoDe(
    ficha: Paciente,
    clave: 'antecedentesOdontologicos' | 'observaciones',
  ): string {
    const antecedentes = ficha.antecedentes as Antecedentes | null | undefined;
    return this.texto(antecedentes?.[clave] ?? null);
  }

  /** Muestra un valor de texto o el aviso de que no hay dato. */
  protected texto(valor: string | null | undefined): string {
    return valor && valor.trim().length > 0 ? valor : 'Sin registro';
  }

  /** Formatea una fecha ISO sin depender de la zona horaria del navegador. */
  protected fechaCorta(iso: string | null | undefined): string {
    if (!iso) {
      return 'Sin registro';
    }
    const dia = iso.split('T')[0];
    const partes = dia.split('-');
    return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : iso;
  }

  /** Igual que fechaCorta pero agregando la hora cuando el backend la envia. */
  protected fechaHora(iso: string | null | undefined): string {
    if (!iso) {
      return 'Sin registro';
    }
    const [dia, resto] = iso.split('T');
    const fecha = this.fechaCorta(dia);
    return resto ? `${fecha} ${resto.slice(0, 5)}` : fecha;
  }

  private opcional(valor: string): string | null {
    const limpio = valor.trim();
    return limpio.length > 0 ? limpio : null;
  }
}
