import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../core/services/auth.service';
import { NotificacionService } from '../core/services/notificacion.service';

/** Entrada del menu lateral. El icono es la geometria de un SVG de 24x24. */
export interface ItemMenu {
  ruta: string;
  etiqueta: string;
  /** Clave del modulo usada por AuthService.tienePermiso(). */
  modulo: string;
  icono: string;
}

const MENU: ItemMenu[] = [
  {
    ruta: '/dashboard',
    etiqueta: 'Dashboard',
    modulo: 'dashboard',
    icono: 'M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z',
  },
  {
    ruta: '/pacientes',
    etiqueta: 'Pacientes',
    modulo: 'pacientes',
    icono:
      'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
  },
  {
    ruta: '/citas',
    etiqueta: 'Citas',
    modulo: 'citas',
    icono: 'M3 6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2zM16 2v4M8 2v4M3 10h18',
  },
  {
    ruta: '/historias',
    etiqueta: 'Historias',
    modulo: 'historias',
    icono: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8',
  },
  {
    ruta: '/odontograma',
    etiqueta: 'Odontograma',
    modulo: 'odontograma',
    icono:
      'M12 3c-2 0-2.5 1-4.5 1S4 3.6 4 6c0 3 1 4.5 1.6 7.5.5 2.4.6 5.5 2.4 5.5 1.7 0 1.4-4 4-4s2.3 4 4 4c1.8 0 1.9-3.1 2.4-5.5C19 10.5 20 9 20 6c0-2.4-1.5-2-3.5-2S14 3 12 3z',
  },
  {
    ruta: '/tratamientos',
    etiqueta: 'Tratamientos',
    modulo: 'tratamientos',
    icono:
      'M9 4H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-2M9 4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2M9 4h6M9 14l2 2 4-4',
  },
  {
    ruta: '/pagos',
    etiqueta: 'Pagos',
    modulo: 'pagos',
    icono: 'M2 7a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2zM2 10h20M6 15h4',
  },
  {
    ruta: '/archivos',
    etiqueta: 'Archivos',
    modulo: 'archivos',
    icono: 'M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z',
  },
  {
    ruta: '/reportes',
    etiqueta: 'Reportes',
    modulo: 'reportes',
    icono: 'M18 20V10M12 20V4M6 20v-6M3 22h18',
  },
  {
    ruta: '/usuarios',
    etiqueta: 'Usuarios',
    modulo: 'usuarios',
    icono:
      'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 3a4 4 0 1 1 0 8 4 4 0 0 1 0-8M19 8v4M17 10h4',
  },
];

/** Cascaron de la aplicacion: barra lateral, barra superior y contenido. */
@Component({
  selector: 'app-layout',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class LayoutComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notificaciones = inject(NotificacionService);

  /** Controla la barra lateral cuando la pantalla es angosta. */
  protected readonly barraAbierta = signal(false);

  protected readonly usuario = this.auth.usuarioActual;
  protected readonly rol = this.auth.rol;

  /** Solo los modulos permitidos para el rol en sesion. */
  protected readonly menu = computed<ItemMenu[]>(() => {
    this.auth.rol();
    return MENU.filter((item) => this.auth.tienePermiso(item.modulo));
  });

  protected alternarBarra(): void {
    this.barraAbierta.update((v) => !v);
  }

  protected cerrarBarra(): void {
    this.barraAbierta.set(false);
  }

  protected cerrarSesion(): void {
    this.auth.logout();
    this.notificaciones.info('Sesion cerrada');
    this.router.navigate(['/login']);
  }
}
