import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormularioPlantillaComponent } from './formulario-plantilla';

describe('FormularioPlantillaComponent', () => {
  let fixture: ComponentFixture<FormularioPlantillaComponent>;
  let html: HTMLElement;

  const escribir = async (selector: string, valor: string) => {
    const campo = html.querySelector<HTMLInputElement | HTMLTextAreaElement>(selector)!;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
    campo.dispatchEvent(new Event('blur'));
    await fixture.whenStable();
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [FormularioPlantillaComponent] }).compileComponents();
    fixture = TestBed.createComponent(FormularioPlantillaComponent);
    html = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('no muestra errores hasta que el usuario interactúa', () => {
    expect(html.querySelectorAll('.error').length).toBe(0);
  });

  it('al enviar vacío muestra los errores de todos los campos obligatorios', async () => {
    html.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();

    const texto = html.textContent ?? '';
    expect(texto).toContain('El nombre es obligatorio.');
    expect(texto).toContain('El correo es obligatorio.');
    expect(texto).toContain('El mensaje es obligatorio.');
    expect(texto).toContain('Debe aceptar el tratamiento de datos.');
    expect(html.querySelector('.exito')).toBeNull();
  });

  it('valida longitud mínima y formato de correo al salir del campo', async () => {
    await escribir('#nombrePlantilla', 'Ma');
    await escribir('#correoPlantilla', 'marco.correo');
    const texto = html.textContent ?? '';
    expect(texto).toContain('Ingrese al menos 3 caracteres.');
    expect(texto).toContain('Ingrese un correo con formato válido.');
  });

  it('envía con datos válidos y limpiar restablece el modelo', async () => {
    const log = vi.spyOn(console, 'log').mockImplementation(() => {});
    await escribir('#nombrePlantilla', 'Marco Gonzales');
    await escribir('#correoPlantilla', 'marco@correo.com');
    await escribir('#mensaje', 'Consulta sobre el laboratorio 13.');
    const acepta = html.querySelector<HTMLInputElement>('#aceptaPlantilla')!;
    acepta.click();
    await fixture.whenStable();

    html.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();

    expect(html.querySelector('.exito')?.textContent).toContain('Solicitud enviada por Marco Gonzales.');
    expect(log).toHaveBeenCalledWith(
      'Datos del formulario basado en plantilla:',
      expect.objectContaining({ correo: 'marco@correo.com', acepta: true })
    );

    html.querySelector<HTMLButtonElement>('button.secundario')!.click();
    await fixture.whenStable();
    expect(fixture.componentInstance.modelo.nombre).toBe('');
    expect(html.querySelector('.exito')).toBeNull();
    log.mockRestore();
  });
});
