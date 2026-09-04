import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder-page',
  standalone: true,
  templateUrl: './placeholder-page.html',
  styleUrl: './placeholder-page.scss',
})
export class PlaceholderPage {
  private readonly route = inject(ActivatedRoute);

  protected readonly titulo = this.route.snapshot.data['titulo'] ?? 'Módulo';
  protected readonly integrante = this.route.snapshot.data['integrante'] ?? '';
}
