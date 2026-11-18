import { FormControl } from '@angular/forms';

import {
  TAMANO_MAXIMO_ARCHIVO,
  archivoPermitidoValidator,
  extensionDe,
} from './archivos.validators';

/** Crea un File con el peso pedido sin reservar esa memoria. */
function archivo(nombre: string, bytes: number): File {
  const f = new File(['x'], nombre);
  Object.defineProperty(f, 'size', { value: bytes });
  return f;
}

describe('extensionDe', () => {
  it('devuelve la ultima extension en minusculas', () => {
    expect(extensionDe('Radiografia.Panoramica.JPG')).toBe('jpg');
  });

  it('devuelve cadena vacia si no hay extension', () => {
    expect(extensionDe('informe')).toBe('');
    expect(extensionDe(null)).toBe('');
  });
});

describe('archivoPermitidoValidator', () => {
  const validar = archivoPermitidoValidator();

  it('deja el valor nulo a Validators.required', () => {
    expect(validar(new FormControl(null))).toBeNull();
  });

  it('acepta las extensiones de la lista blanca del backend', () => {
    for (const nombre of ['a.jpg', 'b.PNG', 'c.pdf', 'd.docx', 'e.txt', 'f.webp']) {
      expect(validar(new FormControl(archivo(nombre, 2048)))).toBeNull();
    }
  });

  it('rechaza extensiones fuera de la lista e informa cual fue', () => {
    const errores = validar(new FormControl(archivo('script.exe', 10)));
    expect(errores?.['extensionNoPermitida']?.extension).toBe('exe');
  });

  it('rechaza un archivo vacio, como el backend', () => {
    expect(validar(new FormControl(archivo('vacio.pdf', 0)))).toEqual({ archivoVacio: true });
  });

  it('rechaza un archivo de mas de 10 MB y acepta exactamente 10 MB', () => {
    const grande = validar(new FormControl(archivo('rx.png', TAMANO_MAXIMO_ARCHIVO + 1)));
    expect(grande?.['archivoMuyGrande']).toEqual({
      tamano: TAMANO_MAXIMO_ARCHIVO + 1,
      maximo: TAMANO_MAXIMO_ARCHIVO,
    });
    expect(validar(new FormControl(archivo('rx.png', TAMANO_MAXIMO_ARCHIVO)))).toBeNull();
  });

  it('admite una lista y un tope propios', () => {
    const soloPdf = archivoPermitidoValidator(['pdf'], 100);
    expect(soloPdf(new FormControl(archivo('a.jpg', 10)))).not.toBeNull();
    expect(soloPdf(new FormControl(archivo('a.pdf', 101)))).not.toBeNull();
    expect(soloPdf(new FormControl(archivo('a.pdf', 100)))).toBeNull();
  });
});
