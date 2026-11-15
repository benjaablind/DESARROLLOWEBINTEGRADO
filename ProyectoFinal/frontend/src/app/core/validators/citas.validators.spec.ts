import { FormControl, FormGroup, Validators } from '@angular/forms';

import {
  DURACION_MAXIMA,
  DURACION_MINIMA,
  MAX_MOTIVO_REPROGRAMACION,
  MAX_OBSERVACIONES,
  PATRON_FECHA,
  PATRON_HORA,
  Reloj,
  aFechaLocal,
  fechaHoraDistinta,
  fechaHoraFutura,
  numeroEntero,
  textoNoVacio,
} from './citas.validators';

/** Reloj fijo: sabado 14 de noviembre de 2026, 10:00 en hora local. */
const ahora: Reloj = () => new Date(2026, 10, 14, 10, 0, 0, 0);

function grupoFecha(fecha: string, hora: string): FormGroup {
  return new FormGroup({
    fecha: new FormControl(fecha, { nonNullable: true }),
    hora: new FormControl(hora, { nonNullable: true }),
  });
}

describe('citas.validators', () => {
  describe('textoNoVacio (@NotBlank)', () => {
    it('deja la cadena vacia a Validators.required', () => {
      expect(textoNoVacio(new FormControl(''))).toBeNull();
      expect(textoNoVacio(new FormControl(null))).toBeNull();
    });

    it('rechaza un texto que solo tiene espacios', () => {
      expect(textoNoVacio(new FormControl('   '))).toEqual({ soloEspacios: true });
      expect(textoNoVacio(new FormControl('\t \n'))).toEqual({ soloEspacios: true });
    });

    it('acepta un texto con contenido aunque tenga espacios alrededor', () => {
      expect(textoNoVacio(new FormControl('  Control  '))).toBeNull();
    });

    it('junto con required cubre los dos casos que rechaza @NotBlank', () => {
      const control = new FormControl('', [Validators.required, textoNoVacio]);
      expect(control.errors).toEqual({ required: true });
      control.setValue('  ');
      expect(control.errors).toEqual({ soloEspacios: true });
      control.setValue('Limpieza');
      expect(control.errors).toBeNull();
    });
  });

  describe('numeroEntero (Integer duracionMinutos)', () => {
    it('ignora el valor vacio', () => {
      expect(numeroEntero(new FormControl(null))).toBeNull();
      expect(numeroEntero(new FormControl(''))).toBeNull();
    });

    it('acepta minutos enteros', () => {
      expect(numeroEntero(new FormControl(30))).toBeNull();
      expect(numeroEntero(new FormControl('45'))).toBeNull();
    });

    it('rechaza fracciones de minuto', () => {
      expect(numeroEntero(new FormControl(12.5))).toEqual({ entero: true });
    });

    it('combinado con min y max replica @Min(10) y @Max(240)', () => {
      const control = new FormControl<number | null>(DURACION_MINIMA, [
        Validators.min(DURACION_MINIMA),
        Validators.max(DURACION_MAXIMA),
        numeroEntero,
      ]);
      expect(control.valid).toBe(true);
      control.setValue(DURACION_MINIMA - 1);
      expect(control.hasError('min')).toBe(true);
      control.setValue(DURACION_MAXIMA);
      expect(control.valid).toBe(true);
      control.setValue(DURACION_MAXIMA + 1);
      expect(control.hasError('max')).toBe(true);
    });
  });

  describe('patrones de fecha y hora', () => {
    it('PATRON_FECHA acepta AAAA-MM-DD', () => {
      expect(PATRON_FECHA.test('2026-11-14')).toBe(true);
      expect(PATRON_FECHA.test('14/11/2026')).toBe(false);
    });

    it('PATRON_HORA acepta HH:mm de 00:00 a 23:59', () => {
      expect(PATRON_HORA.test('00:00')).toBe(true);
      expect(PATRON_HORA.test('23:59')).toBe(true);
      expect(PATRON_HORA.test('24:00')).toBe(false);
      expect(PATRON_HORA.test('9:00')).toBe(false);
      expect(PATRON_HORA.test('10:00:00')).toBe(false);
    });
  });

  describe('aFechaLocal', () => {
    it('arma la fecha local a partir de fecha y hora', () => {
      expect(aFechaLocal('2026-11-14', '10:30')).toEqual(new Date(2026, 10, 14, 10, 30));
    });

    it('rechaza dias que no existen en el calendario', () => {
      expect(aFechaLocal('2026-02-30', '10:00')).toBeNull();
      expect(aFechaLocal('2026-13-01', '10:00')).toBeNull();
    });
  });

  describe('fechaHoraFutura (@Future)', () => {
    const validador = fechaHoraFutura(ahora);

    it('no opina mientras falte la fecha o la hora', () => {
      expect(validador(grupoFecha('', '10:00'))).toBeNull();
      expect(validador(grupoFecha('2026-11-14', ''))).toBeNull();
    });

    it('rechaza una fecha y hora pasada', () => {
      expect(validador(grupoFecha('2026-11-13', '18:00'))).toEqual({ fechaPasada: true });
      expect(validador(grupoFecha('2026-11-14', '09:59'))).toEqual({ fechaPasada: true });
    });

    it('rechaza el instante actual porque @Future es estricto', () => {
      expect(validador(grupoFecha('2026-11-14', '10:00'))).toEqual({ fechaPasada: true });
    });

    it('acepta una fecha y hora futura', () => {
      expect(validador(grupoFecha('2026-11-14', '10:01'))).toBeNull();
      expect(validador(grupoFecha('2027-01-05', '08:00'))).toBeNull();
    });

    it('marca como invalida una fecha que no existe', () => {
      expect(validador(grupoFecha('2026-02-30', '10:00'))).toEqual({ fechaInvalida: true });
    });

    it('se evalua sobre el FormGroup y se recalcula al cambiar un control', () => {
      const grupo = new FormGroup(
        {
          fecha: new FormControl('2026-11-13', { nonNullable: true }),
          hora: new FormControl('10:00', { nonNullable: true }),
        },
        { validators: fechaHoraFutura(ahora) },
      );
      expect(grupo.hasError('fechaPasada')).toBe(true);
      grupo.controls.fecha.setValue('2026-11-15');
      expect(grupo.valid).toBe(true);
    });

    it('sin reloj usa la hora del sistema', () => {
      const validadorReal = fechaHoraFutura();
      expect(validadorReal(grupoFecha('2000-01-01', '10:00'))).toEqual({ fechaPasada: true });
      expect(validadorReal(grupoFecha('2999-01-01', '10:00'))).toBeNull();
    });
  });

  describe('fechaHoraDistinta (reprogramar)', () => {
    let original: string | null = '2026-11-20T10:00:00';
    const validador = fechaHoraDistinta(() => original);

    it('rechaza la misma fecha y hora de la cita original', () => {
      expect(validador(grupoFecha('2026-11-20', '10:00'))).toEqual({ mismaFechaHora: true });
    });

    it('acepta cuando cambia la fecha o la hora', () => {
      expect(validador(grupoFecha('2026-11-21', '10:00'))).toBeNull();
      expect(validador(grupoFecha('2026-11-20', '10:30'))).toBeNull();
    });

    it('lee la cita original cada vez que valida', () => {
      original = '2026-11-21T10:00:00';
      expect(validador(grupoFecha('2026-11-21', '10:00'))).toEqual({ mismaFechaHora: true });
      original = null;
      expect(validador(grupoFecha('2026-11-21', '10:00'))).toBeNull();
    });

    it('no opina mientras falte la fecha o la hora', () => {
      original = '2026-11-20T10:00:00';
      expect(validador(grupoFecha('', '10:00'))).toBeNull();
    });
  });

  describe('limites de texto', () => {
    it('el motivo de reprogramacion deja sitio al prefijo que agrega el backend', () => {
      const prefijo = `Reprogramada en la cita ${'9'.repeat(19)}. Motivo: `;
      expect(prefijo.length + MAX_MOTIVO_REPROGRAMACION).toBe(MAX_OBSERVACIONES);
    });
  });
});
