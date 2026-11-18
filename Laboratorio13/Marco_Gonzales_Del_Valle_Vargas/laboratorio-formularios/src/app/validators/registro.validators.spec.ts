import { FormControl, FormGroup } from '@angular/forms';
import {
  dominioCorreoValidator,
  edadMinimaValidator,
  passwordsIgualesValidator
} from './registro.validators';

describe('edadMinimaValidator', () => {
  const validar = edadMinimaValidator(18);

  it('deja el valor vacío a Validators.required', () => {
    expect(validar(new FormControl(null))).toBeNull();
    expect(validar(new FormControl(''))).toBeNull();
  });

  it('rechaza una edad menor al mínimo e informa ambos valores', () => {
    expect(validar(new FormControl(17))).toEqual({ edadMinima: { minima: 18, actual: 17 } });
  });

  it('acepta una edad igual o mayor al mínimo', () => {
    expect(validar(new FormControl(18))).toBeNull();
    expect(validar(new FormControl(40))).toBeNull();
  });
});

describe('passwordsIgualesValidator', () => {
  const grupo = (password: string, confirmarPassword: string) =>
    new FormGroup({
      password: new FormControl(password),
      confirmarPassword: new FormControl(confirmarPassword)
    });

  it('no opina mientras falte alguna de las dos contraseñas', () => {
    expect(passwordsIgualesValidator(grupo('Clave123', ''))).toBeNull();
  });

  it('marca el grupo cuando las contraseñas son distintas', () => {
    expect(passwordsIgualesValidator(grupo('Clave123', 'Clave124'))).toEqual({
      passwordsNoCoinciden: true
    });
  });

  it('acepta contraseñas iguales', () => {
    expect(passwordsIgualesValidator(grupo('Clave123', 'Clave123'))).toBeNull();
  });
});

describe('dominioCorreoValidator', () => {
  const validar = dominioCorreoValidator('universidad.edu.pe');

  it('acepta el dominio institucional sin importar mayúsculas', () => {
    expect(validar(new FormControl('ana@universidad.edu.pe'))).toBeNull();
    expect(validar(new FormControl('Ana@Universidad.EDU.pe'))).toBeNull();
  });

  it('rechaza otros dominios con dominioInvalido', () => {
    expect(validar(new FormControl('ana@gmail.com'))).toEqual({ dominioInvalido: true });
    expect(validar(new FormControl('ana@universidad.edu.pe.com'))).toEqual({ dominioInvalido: true });
  });

  it('deja el valor vacío a Validators.required', () => {
    expect(validar(new FormControl(''))).toBeNull();
  });
});
