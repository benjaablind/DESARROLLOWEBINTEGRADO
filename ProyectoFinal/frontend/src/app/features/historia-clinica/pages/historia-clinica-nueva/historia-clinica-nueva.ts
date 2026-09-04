import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HistoriaClinicaService } from '../../data/historia-clinica.service';

@Component({
  selector: 'app-historia-clinica-nueva',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './historia-clinica-nueva.html',
  styleUrl: './historia-clinica-nueva.scss',
})
export class HistoriaClinicaNueva {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(HistoriaClinicaService);
  private readonly router = inject(Router);

  protected readonly form = this.fb.nonNullable.group({
    pacienteNombre: ['', Validators.required],
    odontologo: ['', Validators.required],
    fecha: ['', Validators.required],
    motivoConsulta: ['', Validators.required],
    anamnesis: [''],
    examenClinico: [''],
    diagnostico: ['', Validators.required],
    procedimiento: [''],
    tratamiento: [''],
    medicamentos: [''],
    observaciones: [''],
  });

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const nuevo = this.service.crear(this.form.getRawValue());
    this.router.navigate(['/historia-clinica', nuevo.id]);
  }
}
