import { FormControl, FormGroup } from '@angular/forms';

import { rangoFechasValidator } from './reportes.validators';

const rango = (desde: string, hasta: string) =>
  new FormGroup({ desde: new FormControl(desde), hasta: new FormControl(hasta) });

describe('rangoFechasValidator', () => {
  it('acepta un rango ordenado y un rango de un solo dia', () => {
    expect(rangoFechasValidator(rango('2026-01-01', '2026-03-31'))).toBeNull();
    expect(rangoFechasValidator(rango('2026-02-10', '2026-02-10'))).toBeNull();
  });

  it('marca el grupo cuando desde es posterior a hasta', () => {
    expect(rangoFechasValidator(rango('2026-04-01', '2026-03-31'))).toEqual({
      rangoInvertido: true,
    });
  });

  it('no opina si falta uno de los extremos', () => {
    expect(rangoFechasValidator(rango('', '2026-03-31'))).toBeNull();
    expect(rangoFechasValidator(rango('2026-04-01', ''))).toBeNull();
  });

  it('compara fechas y no solo dias: diciembre de un año va antes de enero del siguiente', () => {
    expect(rangoFechasValidator(rango('2025-12-31', '2026-01-01'))).toBeNull();
  });
});
