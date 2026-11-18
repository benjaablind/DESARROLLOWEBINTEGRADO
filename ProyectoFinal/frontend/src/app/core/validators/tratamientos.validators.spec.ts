import { FormControl, Validators } from '@angular/forms';

import { importeValidator, textoNoVacioValidator } from './tratamientos.validators';

describe('importeValidator', () => {
  const validar = importeValidator();

  it('deja el valor vacio a Validators.required', () => {
    expect(validar(new FormControl(null))).toBeNull();
    expect(validar(new FormControl(''))).toBeNull();
    expect(validar(new FormControl('   '))).toBeNull();
  });

  it('acepta importes positivos con hasta 2 decimales', () => {
    for (const valor of [0.01, 1, 150, 1500.5, 99.99, '250.00', '1.50', 9999999999.99]) {
      expect(validar(new FormControl(valor))).toBeNull();
    }
  });

  it('rechaza cero y negativos, como @Positive', () => {
    expect(validar(new FormControl(0))).toEqual({ importeNoPositivo: true });
    expect(validar(new FormControl(-20))).toEqual({ importeNoPositivo: true });
  });

  it('rechaza mas de 2 decimales, como @Digits(fraction = 2)', () => {
    expect(validar(new FormControl(10.555))).toEqual({
      importeDecimales: { maximo: 2, actual: 3 },
    });
    expect(validar(new FormControl(0.001))?.['importeDecimales']).toBeTruthy();
    expect(validar(new FormControl(1e-7))?.['importeDecimales']).toBeTruthy();
  });

  it('no cuenta los ceros finales como decimales', () => {
    expect(validar(new FormControl('12.5000'))).toBeNull();
  });

  it('rechaza mas de 10 digitos enteros, como @Digits(integer = 10)', () => {
    expect(validar(new FormControl(12345678901))).toEqual({
      importeEnteros: { maximo: 10, actual: 11 },
    });
    expect(validar(new FormControl(1e21))?.['importeEnteros']).toBeTruthy();
    expect(validar(new FormControl(1234567890))).toBeNull();
  });

  it('rechaza texto que no es un numero', () => {
    expect(validar(new FormControl('abc'))).toEqual({ importeInvalido: true });
  });

  it('convive con Validators.required: cada uno reporta su error', () => {
    const control = new FormControl<number | null>(null, [Validators.required, validar]);
    expect(control.errors).toEqual({ required: true });

    control.setValue(0.123);
    expect(control.hasError('importeDecimales')).toBe(true);
    expect(control.hasError('required')).toBe(false);
  });
});

describe('textoNoVacioValidator', () => {
  it('deja la cadena vacia y los valores no textuales a Validators.required', () => {
    expect(textoNoVacioValidator(new FormControl(''))).toBeNull();
    expect(textoNoVacioValidator(new FormControl(null))).toBeNull();
  });

  it('rechaza el texto compuesto solo de espacios, como @NotBlank', () => {
    expect(textoNoVacioValidator(new FormControl('   '))).toEqual({ soloEspacios: true });
    expect(textoNoVacioValidator(new FormControl('\t\n'))).toEqual({ soloEspacios: true });
  });

  it('acepta texto con contenido aunque tenga espacios alrededor', () => {
    expect(textoNoVacioValidator(new FormControl('  Ortodoncia  '))).toBeNull();
  });
});
