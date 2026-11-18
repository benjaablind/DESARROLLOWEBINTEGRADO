import { FormControl } from '@angular/forms';

import { PIEZAS_FDI } from '../models/odontograma.model';
import { esPiezaFdi, estadoPiezaValido, piezaFdi } from './odontograma.validators';

describe('Validadores del odontograma', () => {
  describe('esPiezaFdi (OdontogramaService.validarNumeroPieza)', () => {
    it('acepta las 32 piezas permanentes', () => {
      const permanentes = Object.values(PIEZAS_FDI).flat();
      expect(permanentes.length).toBe(32);
      expect(permanentes.every((numero) => esPiezaFdi(numero))).toBe(true);
    });

    it('acepta las piezas deciduas de cada cuadrante (x1 a x5)', () => {
      for (const numero of [51, 55, 61, 65, 71, 75, 81, 85]) {
        expect(esPiezaFdi(numero)).toBe(true);
      }
    });

    it('rechaza numeros que no son piezas reales', () => {
      // 19 y 29: no hay novena pieza. 56 y 86: la decidua llega a 5. 10, 50 y 90: pieza 0.
      for (const numero of [0, 9, 10, 19, 29, 49, 50, 56, 86, 90, 99, 100, -11]) {
        expect(esPiezaFdi(numero)).toBe(false);
      }
    });

    it('rechaza numeros con decimales', () => {
      expect(esPiezaFdi(11.5)).toBe(false);
      expect(esPiezaFdi(Number.NaN)).toBe(false);
    });
  });

  describe('piezaFdi (ValidatorFn)', () => {
    const validar = piezaFdi();

    it('deja el valor vacio a Validators.required', () => {
      expect(validar(new FormControl(null))).toBeNull();
      expect(validar(new FormControl(''))).toBeNull();
    });

    it('acepta una pieza FDI, tambien escrita como texto', () => {
      expect(validar(new FormControl(46))).toBeNull();
      expect(validar(new FormControl('54'))).toBeNull();
    });

    it('rechaza una pieza que no existe e informa el valor', () => {
      expect(validar(new FormControl(19))).toEqual({ piezaFdi: { valor: 19 } });
      expect(validar(new FormControl('abc'))).toEqual({ piezaFdi: { valor: 'abc' } });
    });
  });

  describe('estadoPiezaValido (enum EstadoPieza)', () => {
    const validar = estadoPiezaValido();

    it('deja el valor vacio a Validators.required', () => {
      expect(validar(new FormControl(''))).toBeNull();
    });

    it('acepta los estados del enum', () => {
      expect(validar(new FormControl('CARIES'))).toBeNull();
      expect(validar(new FormControl('EXTRACCION_INDICADA'))).toBeNull();
    });

    it('rechaza un estado que el backend no conoce', () => {
      expect(validar(new FormControl('ROTO'))).toEqual({ estadoPieza: { valor: 'ROTO' } });
      // El enum de Java distingue mayusculas.
      expect(validar(new FormControl('caries'))).toEqual({ estadoPieza: { valor: 'caries' } });
    });
  });
});
