import { JsonPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import {
  dominioCorreoValidator,
  edadMinimaValidator,
  passwordsIgualesValidator
} from '../../validators/registro.validators';

/** Actividad 2: dominio institucional exigido en el correo. */
export const DOMINIO_INSTITUCIONAL = 'universidad.edu.pe';

@Component({
  selector: 'app-formulario-reactivo',
  imports: [ReactiveFormsModule, JsonPipe],
  templateUrl: './formulario-reactivo.html',
  styleUrl: './formulario-reactivo.css'
})
export class FormularioReactivoComponent {
  private readonly fb = inject(FormBuilder);

  readonly dominio = DOMINIO_INSTITUCIONAL;
  mensajeExito = '';

  readonly registroForm = this.fb.group(
    {
      nombre: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(60)
      ]),
      // Actividad 1: DNI peruano de 8 dígitos.
      dni: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.pattern(/^\d{8}$/)
      ]),
      correo: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.email,
        dominioCorreoValidator(DOMINIO_INSTITUCIONAL)
      ]),
      edad: this.fb.control<number | null>(null, [
        Validators.required,
        edadMinimaValidator(18)
      ]),
      telefono: this.fb.nonNullable.control('', [
        Validators.pattern(/^\d{9}$/)
      ]),
      password: this.fb.nonNullable.control('', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Z])(?=.*\d).+$/)
      ]),
      confirmarPassword: this.fb.nonNullable.control('', [
        Validators.required
      ]),
      aceptaTerminos: this.fb.nonNullable.control(false, [
        Validators.requiredTrue
      ]),
      habilidades: this.fb.array([
        this.crearControlHabilidad()
      ]),
      // Actividad 3: cada dirección es un FormGroup dentro de un FormArray.
      direcciones: this.fb.array([
        this.crearGrupoDireccion()
      ])
    },
    {
      validators: passwordsIgualesValidator
    }
  );

  get habilidades(): FormArray {
    return this.registroForm.controls.habilidades;
  }

  get direcciones() {
    return this.registroForm.controls.direcciones;
  }

  private crearControlHabilidad() {
    return this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(2)
    ]);
  }

  private crearGrupoDireccion() {
    return this.fb.nonNullable.group({
      departamento: ['', Validators.required],
      provincia: ['', Validators.required],
      direccion: ['', [Validators.required, Validators.minLength(5)]]
    });
  }

  agregarHabilidad(): void {
    this.habilidades.push(this.crearControlHabilidad());
  }

  eliminarHabilidad(indice: number): void {
    if (this.habilidades.length > 1) {
      this.habilidades.removeAt(indice);
    }
  }

  agregarDireccion(): void {
    this.direcciones.push(this.crearGrupoDireccion());
  }

  eliminarDireccion(indice: number): void {
    if (this.direcciones.length > 1) {
      this.direcciones.removeAt(indice);
    }
  }

  enviar(): void {
    this.mensajeExito = '';

    if (this.registroForm.invalid) {
      this.registroForm.markAllAsTouched();
      return;
    }

    const datos = this.registroForm.getRawValue();
    console.log('Datos del formulario reactivo:', datos);
    this.mensajeExito = `Registro preparado para ${datos.nombre}.`;
  }

  limpiar(): void {
    while (this.habilidades.length > 1) {
      this.habilidades.removeAt(this.habilidades.length - 1);
    }
    while (this.direcciones.length > 1) {
      this.direcciones.removeAt(this.direcciones.length - 1);
    }

    this.registroForm.reset({
      nombre: '',
      dni: '',
      correo: '',
      edad: null,
      telefono: '',
      password: '',
      confirmarPassword: '',
      aceptaTerminos: false,
      habilidades: [''],
      direcciones: [{ departamento: '', provincia: '', direccion: '' }]
    });

    this.mensajeExito = '';
  }
}
