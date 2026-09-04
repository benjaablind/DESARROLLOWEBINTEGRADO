import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { DashboardService } from '../../core/services/dashboard.service';
import {
  BADGE_ESTADO_CITA,
  BADGE_ESTADO_TRATAMIENTO,
  Dashboard,
  ErrorApi,
  ETIQUETAS_ESTADO_CITA,
  ETIQUETAS_ESTADO_TRATAMIENTO,
  EstadoCita,
  EstadoTratamiento,
} from '../../core/models';

/**
 * Pantalla principal.
 * Toda la informacion llega de una sola llamada a GET /api/dashboard, asi que
 * el componente solo gestiona los tres estados de siempre (cargando, error y
 * vacio) y se encarga del formato de fechas e importes, porque el proyecto no
 * registra los datos de la configuracion regional para los pipes de Angular.
 */
@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <div class="titulo-pagina">
      <div>
        <h1>{{ saludo() }}</h1>
        <span class="subtitulo">{{ fechaDeHoy() }}</span>
      </div>
      <button type="button" class="btn btn-secundario" (click)="recargar()" [disabled]="cargando()">
        @if (cargando()) {
          <span class="spinner" aria-hidden="true"></span>
        }
        Recargar
      </button>
    </div>

    @if (cargando() && !datos()) {
      <div class="card">
        <div class="estado-vacio">
          <span class="spinner spinner-grande" aria-hidden="true"></span>
          <span class="cargando">Cargando los indicadores de la clinica...</span>
        </div>
      </div>
    } @else if (error()) {
      <div class="alerta alerta-error" role="alert">{{ error() }}</div>
      <div class="card">
        <div class="estado-vacio">
          <strong>No se pudieron cargar los indicadores</strong>
          <p class="texto-suave">Revise la conexion con el servidor y vuelva a intentarlo.</p>
          <button type="button" class="btn btn-primario" (click)="recargar()">Reintentar</button>
        </div>
      </div>
    } @else if (datos(); as d) {
      <!-- ------------------------------ Metricas ------------------------------ -->
      <section class="grid" aria-label="Indicadores generales">
        <article class="card-metrica">
          <span class="valor">{{ d.pacientesRegistrados }}</span>
          <span class="etiqueta">Pacientes registrados</span>
        </article>
        <article class="card-metrica">
          <span class="valor">{{ d.citasHoy }}</span>
          <span class="etiqueta">Citas de hoy</span>
        </article>
        <article class="card-metrica">
          <span class="valor">{{ d.citasPendientes }}</span>
          <span class="etiqueta">Citas pendientes</span>
        </article>
        <article class="card-metrica">
          <span class="valor">{{ d.tratamientosActivos }}</span>
          <span class="etiqueta">Tratamientos activos</span>
        </article>
        <article class="card-metrica">
          <span class="valor">{{ moneda(d.pagosPendientes) }}</span>
          <span class="etiqueta">Pagos pendientes</span>
        </article>
        <article class="card-metrica">
          <span class="valor">{{ moneda(d.ingresosDelMes) }}</span>
          <span class="etiqueta">Ingresos del mes</span>
        </article>
      </section>

      <!-- ------------------------------- Alertas ------------------------------ -->
      <section class="bloque-alertas" aria-label="Alertas">
        @for (alerta of d.alertas; track $index) {
          <div class="alerta alerta-advertencia">{{ alerta }}</div>
        } @empty {
          <p class="texto-suave texto-pequeno sin-alertas">
            Sin alertas pendientes por el momento.
          </p>
        }
      </section>

      <div class="grid-2 bloque-tarjetas">
        <!-- --------------------------- Proximas citas ------------------------- -->
        <section class="card">
          <div class="card-encabezado">
            <h2>Proximas citas</h2>
            <a class="btn btn-fantasma btn-sm" routerLink="/citas">Ver citas</a>
          </div>

          @if (d.proximasCitas.length > 0) {
            <ul class="lista-panel">
              @for (cita of d.proximasCitas; track cita.id) {
                <li>
                  <div class="lista-panel-fila">
                    <span class="negrita">{{ cita.pacienteNombre }}</span>
                    <span class="badge" [class]="badgeCita(cita.estado)">
                      {{ etiquetaCita(cita.estado) }}
                    </span>
                  </div>
                  <p class="texto-pequeno texto-suave">
                    {{ fechaHora(cita.fechaHora) }} &middot; {{ cita.odontologoNombre }}
                  </p>
                  <p class="texto-pequeno texto-medio">{{ cita.motivo || 'Sin motivo' }}</p>
                </li>
              }
            </ul>
          } @else {
            <div class="estado-vacio">
              <strong>No hay citas proximas</strong>
              <p class="texto-suave">Cuando se agenden citas apareceran en esta lista.</p>
            </div>
          }
        </section>

        <!-- ------------------------- Tratamientos activos --------------------- -->
        <section class="card">
          <div class="card-encabezado">
            <h2>Tratamientos activos</h2>
            <a class="btn btn-fantasma btn-sm" routerLink="/tratamientos">Ver tratamientos</a>
          </div>

          @if (d.tratamientosActivosDetalle.length > 0) {
            <ul class="lista-panel">
              @for (tratamiento of d.tratamientosActivosDetalle; track tratamiento.id) {
                <li>
                  <div class="lista-panel-fila">
                    <span class="negrita">{{ tratamiento.nombre }}</span>
                    <span class="mono saldo">{{ moneda(tratamiento.saldoPendiente) }}</span>
                  </div>
                  <p class="texto-pequeno texto-suave">{{ tratamiento.pacienteNombre }}</p>
                  <span class="badge" [class]="badgeTratamiento(tratamiento.estado)">
                    {{ etiquetaTratamiento(tratamiento.estado) }}
                  </span>
                </li>
              }
            </ul>
          } @else {
            <div class="estado-vacio">
              <strong>No hay tratamientos activos</strong>
              <p class="texto-suave">Los tratamientos en curso se mostraran aqui.</p>
            </div>
          }
        </section>

        <!-- ------------------------- Pacientes recientes ---------------------- -->
        <section class="card">
          <div class="card-encabezado">
            <h2>Pacientes recientes</h2>
            <a class="btn btn-fantasma btn-sm" routerLink="/pacientes">Ver pacientes</a>
          </div>

          @if (d.pacientesRecientes.length > 0) {
            <ul class="lista-panel">
              @for (paciente of d.pacientesRecientes; track paciente.id) {
                <li>
                  <div class="lista-panel-fila">
                    <a class="enlace-ficha negrita" [routerLink]="['/pacientes', paciente.id]">
                      {{ paciente.nombreCompleto }}
                    </a>
                    <span class="mono texto-pequeno texto-suave">
                      {{ paciente.telefono || 'Sin telefono' }}
                    </span>
                  </div>
                </li>
              }
            </ul>
          } @else {
            <div class="estado-vacio">
              <strong>Aun no hay pacientes registrados</strong>
              <p class="texto-suave">Registre el primer paciente para verlo aqui.</p>
            </div>
          }
        </section>

        <!-- --------------------------- Actividad del mes ---------------------- -->
        <section class="card">
          <div class="card-encabezado">
            <h2>Actividad del mes</h2>
          </div>

          <dl class="lista-datos">
            <div>
              <dt>Citas atendidas</dt>
              <dd class="negrita">{{ d.resumenActividad.citasAtendidasMes }}</dd>
            </div>
            <div>
              <dt>Citas canceladas</dt>
              <dd class="negrita">{{ d.resumenActividad.citasCanceladasMes }}</dd>
            </div>
            <div>
              <dt>Inasistencias</dt>
              <dd class="negrita">{{ d.resumenActividad.citasNoAsistioMes }}</dd>
            </div>
            <div>
              <dt>Tratamientos completados</dt>
              <dd class="negrita">{{ d.resumenActividad.tratamientosCompletadosMes }}</dd>
            </div>
            <div>
              <dt>Nuevos pacientes</dt>
              <dd class="negrita">{{ d.resumenActividad.nuevosPacientesMes }}</dd>
            </div>
          </dl>
        </section>
      </div>
    }
  `,
  styles: `
    :host {
      display: block;
    }

    .bloque-alertas {
      margin-top: var(--e-5);
    }

    .bloque-alertas .alerta:last-child {
      margin-bottom: 0;
    }

    .sin-alertas {
      margin: 0;
    }

    .bloque-tarjetas {
      margin-top: var(--e-5);
      align-items: start;
    }

    /*
      Las tarjetas del panel son angostas, asi que en vez de tablas con muchas
      columnas se usan listas apiladas: se leen mejor y nunca cortan el texto.
    */
    .lista-panel {
      list-style: none;
      margin: 0;
      padding: 0;
    }

    .lista-panel li {
      padding: 0.7rem 0;
      border-bottom: 1px solid var(--c-borde);
    }

    .lista-panel li:first-child {
      padding-top: 0;
    }

    .lista-panel li:last-child {
      border-bottom: 0;
      padding-bottom: 0;
    }

    .lista-panel li p {
      margin: 0.15rem 0 0;
    }

    .lista-panel-fila {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: var(--e-2);
    }

    .lista-panel .badge {
      margin-top: 0.35rem;
    }

    .saldo {
      white-space: nowrap;
    }

    .enlace-ficha {
      color: var(--c-primario);
      text-decoration: none;
    }

    .enlace-ficha:hover,
    .enlace-ficha:focus-visible {
      text-decoration: underline;
    }

    .lista-datos dd {
      font-size: 1.15rem;
    }
  `,
})
export class DashboardComponent {
  private readonly servicio = inject(DashboardService);
  private readonly auth = inject(AuthService);

  protected readonly datos = signal<Dashboard | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  /** Saludo con el nombre del usuario en sesion. */
  protected readonly saludo = computed(() => {
    const usuario = this.auth.usuarioActual();
    const nombre = usuario?.nombres?.trim() || usuario?.nombreCompleto?.trim();
    return nombre ? `Hola, ${nombre}` : 'Panel de la clinica';
  });

  /** Fecha de hoy en formato largo, por ejemplo "jueves, 4 de septiembre de 2026". */
  protected readonly fechaDeHoy = computed(() => this.fechaLarga(new Date()));

  constructor() {
    this.recargar();
  }

  protected recargar(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.servicio.obtener().subscribe({
      next: (respuesta) => {
        this.datos.set(respuesta);
        this.cargando.set(false);
      },
      error: (fallo: ErrorApi) => {
        this.datos.set(null);
        this.error.set(fallo?.mensaje ?? 'No se pudieron cargar los indicadores');
        this.cargando.set(false);
      },
    });
  }

  /* ------------------------------- Formato ------------------------------- */

  /** Importe en soles con dos decimales: "S/ 1,250.00". */
  protected moneda(valor: number | null | undefined): string {
    const numero = typeof valor === 'number' && Number.isFinite(valor) ? valor : 0;
    return `S/ ${numero.toLocaleString('es-PE', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  }

  /** Fecha ISO con hora convertida a "04/09/2026 10:00". */
  protected fechaHora(iso: string | null | undefined): string {
    const fecha = this.aFecha(iso);
    if (!fecha) {
      return '-';
    }
    return fecha.toLocaleString('es-PE', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  }

  private fechaLarga(fecha: Date): string {
    return fecha.toLocaleDateString('es-PE', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  }

  private aFecha(iso: string | null | undefined): Date | null {
    if (!iso) {
      return null;
    }
    const fecha = new Date(iso);
    return Number.isNaN(fecha.getTime()) ? null : fecha;
  }

  /* -------------------------------- Badges -------------------------------- */

  protected badgeCita(estado: EstadoCita): string {
    return BADGE_ESTADO_CITA[estado] ?? 'badge-neutro';
  }

  protected etiquetaCita(estado: EstadoCita): string {
    return ETIQUETAS_ESTADO_CITA[estado] ?? estado;
  }

  protected badgeTratamiento(estado: EstadoTratamiento): string {
    return BADGE_ESTADO_TRATAMIENTO[estado] ?? 'badge-neutro';
  }

  protected etiquetaTratamiento(estado: EstadoTratamiento): string {
    return ETIQUETAS_ESTADO_TRATAMIENTO[estado] ?? estado;
  }
}
