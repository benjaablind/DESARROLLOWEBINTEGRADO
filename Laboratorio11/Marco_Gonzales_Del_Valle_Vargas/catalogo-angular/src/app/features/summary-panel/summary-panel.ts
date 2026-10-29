import { Component } from '@angular/core';

/**
 * Panel de resumen generado con: ng g c features/summary-panel --style=scss
 * Contenido estatico a proposito: recibir datos del padre con input() es
 * tema de la semana 12 (seccion 8 de la guia: "Por ahora no uses @Input").
 */
@Component({
  selector: 'app-summary-panel',
  standalone: true,
  templateUrl: './summary-panel.html',
  styleUrl: './summary-panel.scss',
})
export class SummaryPanel {}
