import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormularioReactivoComponent } from './formulario-reactivo';

function completarValido(c: FormularioReactivoComponent): void {
  c.registroForm.patchValue({
    nombre: 'Marco Gonzales',
    dni: '12345678',
    correo: 'marco@universidad.edu.pe',
    edad: 21,
    telefono: '987654321',
    password: 'Clave2026',
    confirmarPassword: 'Clave2026',
    aceptaTerminos: true,
    habilidades: ['Angular'],
    direcciones: [{ departamento: 'Lima', provincia: 'Lima', direccion: 'Av. Petit Thouars 116' }]
  });
}

describe('FormularioReactivoComponent', () => {
  let fixture: ComponentFixture<FormularioReactivoComponent>;
  let c: FormularioReactivoComponent;
  let html: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [FormularioReactivoComponent] }).compileComponents();
    fixture = TestBed.createComponent(FormularioReactivoComponent);
    c = fixture.componentInstance;
    html = fixture.nativeElement;
    await fixture.whenStable();
  });

  it('no muestra errores antes de interactuar', () => {
    expect(html.querySelectorAll('.error').length).toBe(0);
  });

  it('al enviar vacío marca todo como tocado y muestra los errores', async () => {
    html.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();

    expect(c.registroForm.touched).toBe(true);
    expect(c.mensajeExito).toBe('');
    const texto = html.textContent ?? '';
    expect(texto).toContain('El nombre es obligatorio.');
    expect(texto).toContain('El DNI es obligatorio.');
    expect(texto).toContain('Debe aceptar los términos y condiciones.');
  });

  it('distingue DNI requerido de DNI con formato inválido', () => {
    const dni = c.registroForm.controls.dni;
    expect(dni.hasError('required')).toBe(true);
    dni.setValue('1234567');
    expect(dni.hasError('pattern')).toBe(true);
    dni.setValue('12345678');
    expect(dni.valid).toBe(true);
  });

  it('exige el correo institucional', () => {
    const correo = c.registroForm.controls.correo;
    correo.setValue('marco@gmail.com');
    expect(correo.hasError('dominioInvalido')).toBe(true);
    correo.setValue('marco@universidad.edu.pe');
    expect(correo.valid).toBe(true);
  });

  it('aplica edad mínima y contraseñas iguales a nivel de grupo', () => {
    completarValido(c);
    c.registroForm.controls.edad.setValue(16);
    expect(c.registroForm.controls.edad.hasError('edadMinima')).toBe(true);

    c.registroForm.controls.edad.setValue(21);
    c.registroForm.controls.confirmarPassword.setValue('Otra2026');
    expect(c.registroForm.hasError('passwordsNoCoinciden')).toBe(true);
    expect(c.registroForm.invalid).toBe(true);
  });

  it('agrega y elimina habilidades sin borrar la última', () => {
    c.agregarHabilidad();
    c.agregarHabilidad();
    expect(c.habilidades.length).toBe(3);
    c.eliminarHabilidad(0);
    c.eliminarHabilidad(0);
    c.eliminarHabilidad(0);
    expect(c.habilidades.length).toBe(1);
  });

  it('cada dirección es un grupo con sus tres campos obligatorios', () => {
    c.agregarDireccion();
    expect(c.direcciones.length).toBe(2);
    const segunda = c.direcciones.at(1);
    expect(Object.keys(segunda.controls)).toEqual(['departamento', 'provincia', 'direccion']);
    expect(segunda.invalid).toBe(true);
    c.eliminarDireccion(1);
    expect(c.direcciones.length).toBe(1);
  });

  it('envía cuando todo es válido y limpiar deja un solo control dinámico', () => {
    const log = vi.spyOn(console, 'log').mockImplementation(() => {});
    completarValido(c);
    c.agregarHabilidad();
    c.habilidades.at(1).setValue('TypeScript');
    expect(c.registroForm.valid).toBe(true);

    c.enviar();
    expect(c.mensajeExito).toBe('Registro preparado para Marco Gonzales.');
    expect(log).toHaveBeenCalledWith('Datos del formulario reactivo:', expect.objectContaining({ dni: '12345678' }));

    c.agregarDireccion();
    c.limpiar();
    expect(c.habilidades.length).toBe(1);
    expect(c.direcciones.length).toBe(1);
    expect(c.registroForm.controls.nombre.value).toBe('');
    expect(c.registroForm.pristine).toBe(true);
    log.mockRestore();
  });
});
