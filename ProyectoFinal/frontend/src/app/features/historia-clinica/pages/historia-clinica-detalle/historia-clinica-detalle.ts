import { Component, inject, computed } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HistoriaClinicaService } from '../../data/historia-clinica.service';

@Component({
  selector: 'app-historia-clinica-detalle',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './historia-clinica-detalle.html',
  styleUrl: './historia-clinica-detalle.scss',
})
export class HistoriaClinicaDetalle {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(HistoriaClinicaService);

  private readonly id = Number(this.route.snapshot.paramMap.get('id'));

  protected readonly registro = computed(() => this.service.buscarPorId(this.id));
}
