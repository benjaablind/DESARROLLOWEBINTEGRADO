import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { NotificacionesComponent } from './layout/notificaciones';

/** Raiz de la aplicacion: solo enruta y monta la pila de notificaciones. */
@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, NotificacionesComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
