import { FormControl } from '@angular/forms';

import { dniValido, fechaPasada, sinSoloEspacios, telefonoValido } from './pacientes.validators';

describe('pacientes.validators', () => {
  describe('sinSoloEspacios', () => {
    const validar = sinSoloEspacios();

    it('deja el valor vacio a Validators.required', () => {
      expect(validar(new FormControl(''))).toBeNull();
      expect(validar(new FormControl(null))).toBeNull();
    });

    it('rechaza un texto que solo tiene espacios (@NotBlank)', () => {
      expect(validar(new FormControl('   '))).toEqual({ soloEspacios: true });
    });

    it('acepta un texto con contenido', () => {
      expect(validar(new FormControl('  Ana  '))).toBeNull();
    });
  });

  describe('dniValido', () => {
    const validar = dniValido();

    it('deja el valor vacio a Validators.required', () => {
      expect(validar(new FormControl(''))).toBeNull();
    });

    it('acepta exactamente 8 digitos', () => {
      expect(validar(new FormControl('45678912'))).toBeNull();
    });

    it('evalua el valor recortado, que es el que se envia', () => {
      expect(validar(new FormControl(' 45678912 '))).toBeNull();
    });

    it.each(['1234567', '123456789', '4567891A', '4567 8912', '   '])('rechaza "%s"', (valor) => {
      expect(validar(new FormControl(valor))).toEqual({ dniInvalido: true });
    });
  });

  describe('telefonoValido', () => {
    const validar = telefonoValido();

    it('deja el valor vacio a Validators.required', () => {
      expect(validar(new FormControl(''))).toBeNull();
    });

    it.each(['987654', '987654321', '123456789012345'])('acepta "%s"', (valor) => {
      expect(validar(new FormControl(valor))).toBeNull();
    });

    it.each(['98765', '1234567890123456', '987-654-321', '+51987654321', '   '])(
      'rechaza "%s"',
      (valor) => {
        expect(validar(new FormControl(valor))).toEqual({ telefonoInvalido: true });
      },
    );
  });

  describe('fechaPasada', () => {
    // Reloj fijo: las pruebas no dependen del dia en que se ejecutan.
    const hoy = () => new Date(2026, 8, 4, 15, 30);
    const validar = fechaPasada(hoy);

    it('deja el valor vacio a Validators.required', () => {
      expect(validar(new FormControl(''))).toBeNull();
    });

    it('acepta el dia anterior a hoy', () => {
      expect(validar(new FormControl('2026-09-03'))).toBeNull();
    });

    it('acepta una fecha antigua', () => {
      expect(validar(new FormControl('1950-01-31'))).toBeNull();
    });

    it('rechaza el dia de hoy, igual que @Past', () => {
      expect(validar(new FormControl('2026-09-04'))).toEqual({ fechaNoPasada: true });
    });

    it('rechaza una fecha futura', () => {
      expect(validar(new FormControl('2027-01-01'))).toEqual({ fechaNoPasada: true });
    });

    it('rechaza un texto que no tiene formato aaaa-mm-dd', () => {
      expect(validar(new FormControl('04/09/1990'))).toEqual({ fechaInvalida: true });
    });

    it('rechaza una fecha que no existe', () => {
      expect(validar(new FormControl('2023-02-30'))).toEqual({ fechaInvalida: true });
      expect(validar(new FormControl('2023-13-01'))).toEqual({ fechaInvalida: true });
    });

    it('consulta el reloj en cada validacion', () => {
      let ahora = new Date(2026, 8, 4);
      const validarConReloj = fechaPasada(() => ahora);
      const control = new FormControl('2026-09-04');

      expect(validarConReloj(control)).toEqual({ fechaNoPasada: true });

      ahora = new Date(2026, 8, 5);
      expect(validarConReloj(control)).toBeNull();
    });
  });
});
