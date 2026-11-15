import { FormControl, FormGroup } from '@angular/forms';

import {
  longitudRecortadaValidator,
  noEnBlancoValidator,
  passwordDistintaValidator,
  passwordsIgualesValidator,
} from './usuarios.validators';

describe('noEnBlancoValidator (@NotBlank)', () => {
  it('deja el valor vacio a Validators.required', () => {
    expect(noEnBlancoValidator(new FormControl(null))).toBeNull();
    expect(noEnBlancoValidator(new FormControl(''))).toBeNull();
  });

  it('rechaza el texto que solo tiene espacios', () => {
    expect(noEnBlancoValidator(new FormControl('   '))).toEqual({ enBlanco: true });
    expect(noEnBlancoValidator(new FormControl('\t \n'))).toEqual({ enBlanco: true });
  });

  it('acepta texto con contenido aunque tenga espacios alrededor', () => {
    expect(noEnBlancoValidator(new FormControl('admin'))).toBeNull();
    expect(noEnBlancoValidator(new FormControl('  Ana  '))).toBeNull();
  });
});

describe('longitudRecortadaValidator (@Size del usuario)', () => {
  const validar = longitudRecortadaValidator(4, 30);

  it('deja el vacio y los espacios a los otros validadores', () => {
    expect(validar(new FormControl(''))).toBeNull();
    expect(validar(new FormControl('    '))).toBeNull();
  });

  it('mide el valor recortado, que es el que se envia al backend', () => {
    expect(validar(new FormControl(' abc '))).toEqual({
      longitudRecortada: { minima: 4, maxima: 30, actual: 3 },
    });
  });

  it('rechaza un valor mas largo que el maximo', () => {
    expect(validar(new FormControl('a'.repeat(31)))).toEqual({
      longitudRecortada: { minima: 4, maxima: 30, actual: 31 },
    });
  });

  it('acepta los limites exactos', () => {
    expect(validar(new FormControl('abcd'))).toBeNull();
    expect(validar(new FormControl('a'.repeat(30)))).toBeNull();
    expect(validar(new FormControl('  lramos  '))).toBeNull();
  });
});

describe('passwordsIgualesValidator (grupo)', () => {
  const validar = passwordsIgualesValidator('password', 'confirmarPassword');
  const grupo = (password: string, confirmarPassword: string) =>
    new FormGroup({
      password: new FormControl(password),
      confirmarPassword: new FormControl(confirmarPassword),
    });

  it('no opina mientras falte alguna de las dos contrasenas', () => {
    expect(validar(grupo('clave123', ''))).toBeNull();
    expect(validar(grupo('', 'clave123'))).toBeNull();
  });

  it('marca el grupo cuando las contrasenas son distintas', () => {
    expect(validar(grupo('clave123', 'clave124'))).toEqual({ passwordsNoCoinciden: true });
  });

  it('acepta contrasenas iguales', () => {
    expect(validar(grupo('clave123', 'clave123'))).toBeNull();
  });

  it('compara exactamente, sin recortar ni ignorar mayusculas', () => {
    expect(validar(grupo('Clave123', 'clave123'))).toEqual({ passwordsNoCoinciden: true });
    expect(validar(grupo('clave123', 'clave123 '))).toEqual({ passwordsNoCoinciden: true });
  });

  it('usa los nombres de control recibidos', () => {
    const otro = new FormGroup({
      passwordNuevo: new FormControl('nueva123'),
      confirmarPassword: new FormControl('nueva999'),
    });
    expect(passwordsIgualesValidator('passwordNuevo', 'confirmarPassword')(otro)).toEqual({
      passwordsNoCoinciden: true,
    });
  });
});

describe('passwordDistintaValidator (grupo)', () => {
  const validar = passwordDistintaValidator('passwordActual', 'passwordNuevo');
  const grupo = (passwordActual: string, passwordNuevo: string) =>
    new FormGroup({
      passwordActual: new FormControl(passwordActual),
      passwordNuevo: new FormControl(passwordNuevo),
    });

  it('no opina mientras falte alguna de las dos', () => {
    expect(validar(grupo('', 'nueva123'))).toBeNull();
    expect(validar(grupo('actual123', ''))).toBeNull();
  });

  it('rechaza una contrasena nueva igual a la actual', () => {
    expect(validar(grupo('admin123', 'admin123'))).toEqual({ passwordSinCambio: true });
  });

  it('acepta una contrasena nueva distinta', () => {
    expect(validar(grupo('admin123', 'admin456'))).toBeNull();
  });
});
