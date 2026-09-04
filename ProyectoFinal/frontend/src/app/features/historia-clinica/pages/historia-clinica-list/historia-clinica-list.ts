import { Component, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HistoriaClinicaService } from '../../data/historia-clinica.service';

@Component({
  selector: 'app-historia-clinica-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './historia-clinica-list.html',
  styleUrl: './historia-clinica-list.scss',
})
export class HistoriaClinicaList {
  private readonly service = inject(HistoriaClinicaService);

  protected readonly filtro = signal('');

  protected readonly registros = computed(() =>
    this.service.buscarPorPaciente(this.filtro())
  );

  protected actualizarFiltro(valor: string): void {
    this.filtro.set(valor);
  }
}
