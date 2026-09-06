import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { NotificacionService } from '../../core/services/notificacion.service';
import { ErrorApi } from '../../core/models/comun.model';

/** Pantalla de inicio de sesion. Es la unica ruta fuera del layout. */
@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);
  private readonly notificaciones = inject(NotificacionService);

  protected readonly cargando = signal(false);
  protected readonly errorApi = signal<string | null>(null);
  protected readonly verPassword = signal(false);

  protected readonly formulario = this.fb.nonNullable.group({
    usuario: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(4)]],
  });

  protected get usuario() {
    return this.formulario.controls.usuario;
  }

  protected get password() {
    return this.formulario.controls.password;
  }

  protected alternarPassword(): void {
    this.verPassword.update((v) => !v);
  }

  /** Rellena el formulario con las credenciales de demostracion. */
  protected usarDemo(): void {
    this.formulario.setValue({ usuario: 'admin', password: 'admin123' });
    this.errorApi.set(null);
  }

  protected enviar(): void {
    this.errorApi.set(null);

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const { usuario, password } = this.formulario.getRawValue();
    this.cargando.set(true);
    this.formulario.disable();

    this.auth.login(usuario, password).subscribe({
      next: (respuesta) => {
        this.cargando.set(false);
        this.formulario.enable();

        if (!respuesta.autenticado) {
          this.errorApi.set(respuesta.mensaje || 'Usuario o contrasena incorrectos');
          return;
        }

        this.notificaciones.exito(`Bienvenido, ${respuesta.usuario.nombres}`);
        const retorno = this.ruta.snapshot.queryParamMap.get('retorno');
        this.router.navigateByUrl(retorno && retorno !== '/login' ? retorno : '/dashboard');
      },
      error: (fallo: ErrorApi) => {
        this.cargando.set(false);
        this.formulario.enable();
        this.errorApi.set(fallo?.mensaje ?? 'No se pudo iniciar sesion');
      },
    });
  }
}
