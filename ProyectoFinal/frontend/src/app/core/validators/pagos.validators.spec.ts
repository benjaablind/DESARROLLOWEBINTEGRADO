import { FormControl, FormGroup } from '@angular/forms';

import { montoDentroDelSaldoValidator } from './pagos.validators';

/** Saldos de ejemplo por tratamiento; 3 no existe. */
const SALDOS: Record<number, number> = { 1: 150, 2: 0.3, 4: 0 };

function grupo(tratamientoId: string, monto: number | string | null): FormGroup {
  return new FormGroup(
    {
      tratamientoId: new FormControl(tratamientoId),
      monto: new FormControl(monto),
    },
    { validators: montoDentroDelSaldoValidator((id) => SALDOS[id] ?? null) },
  );
}

describe('montoDentroDelSaldoValidator', () => {
  it('acepta un monto menor o igual al saldo del tratamiento', () => {
    expect(grupo('1', 100).errors).toBeNull();
    expect(grupo('1', 150).errors).toBeNull();
  });

  it('rechaza un monto mayor al saldo e informa ambos importes', () => {
    expect(grupo('1', 150.01).errors).toEqual({
      montoExcedeSaldo: { saldo: 150, monto: 150.01 },
    });
  });

  it('compara en centimos para evitar errores de coma flotante', () => {
    // 0.1 + 0.2 da 0.30000000000000004 en double; el saldo 0.30 debe admitirlo.
    expect(grupo('2', 0.1 + 0.2).errors).toBeNull();
    expect(grupo('2', 0.31).hasError('montoExcedeSaldo')).toBe(true);
  });

  it('rechaza cualquier monto si el saldo ya es cero', () => {
    expect(grupo('4', 0.01).hasError('montoExcedeSaldo')).toBe(true);
  });

  it('no opina sin tratamiento, sin monto o con un saldo desconocido', () => {
    expect(grupo('', 500).errors).toBeNull();
    expect(grupo('1', null).errors).toBeNull();
    expect(grupo('1', '').errors).toBeNull();
    expect(grupo('3', 500).errors).toBeNull();
  });

  it('se vuelve a evaluar al cambiar de tratamiento', () => {
    const formulario = grupo('1', 100);
    expect(formulario.valid).toBe(true);

    formulario.controls['tratamientoId'].setValue('2');
    expect(formulario.hasError('montoExcedeSaldo')).toBe(true);
  });
});
