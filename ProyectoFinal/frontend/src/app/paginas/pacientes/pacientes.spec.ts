import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { PacientesComponent } from './pacientes';

const URL_PACIENTES = `${environment.apiUrl}/pacientes`;

describe('PacientesComponent (formulario de registro)', () => {
  let fixture: ComponentFixture<PacientesComponent>;
  let componente: PacientesComponent;
  let http: HttpTestingController;

  /** Texto visible del panel del formulario. */
  const textoFormulario = () =>
    (fixture.nativeElement as HTMLElement).querySelector('form')?.textContent ?? '';

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PacientesComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PacientesComponent);
    componente = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);

    // El constructor pide el listado: se responde vacio.
    http.expectOne((r) => r.method === 'GET' && r.url === URL_PACIENTES).flush([]);

    componente['abrirCreacion']();
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
  });

  /** Llena los campos obligatorios con datos validos. */
  function llenarObligatorios(): void {
    componente['formulario'].patchValue({
      dni: '45678912',
      nombres: 'Ana Lucia',
      apellidos: 'Torres Vega',
      fechaNacimiento: '1992-04-18',
      sexo: 'F',
      telefono: '987654321',
    });
  }

  it('no muestra errores antes de que el usuario toque los campos', () => {
    expect(componente['formulario'].invalid).toBe(true);
    expect(textoFormulario()).not.toContain('El documento es obligatorio.');
  });

  it('al enviar vacio marca todo como tocado, muestra los errores y no llama a la API', () => {
    componente['guardar']();
    fixture.detectChanges();

    const texto = textoFormulario();
    expect(texto).toContain('El documento es obligatorio.');
    expect(texto).toContain('Los nombres son obligatorios.');
    expect(texto).toContain('La fecha de nacimiento es obligatoria.');
    expect(texto).toContain('El teléfono es obligatorio.');
    http.expectNone(URL_PACIENTES);
  });

  it('muestra el mensaje especifico de cada validador propio', () => {
    componente['formulario'].patchValue({
      dni: '1234',
      nombres: '   ',
      telefono: '98-76',
      fechaNacimiento: '2999-01-01',
    });
    componente['formulario'].markAllAsTouched();
    fixture.detectChanges();

    const texto = textoFormulario();
    expect(texto).toContain('Debe tener exactamente 8 dígitos.');
    expect(texto).toContain('Los nombres no pueden ser solo espacios.');
    expect(texto).toContain('Solo dígitos, entre 6 y 15.');
    expect(texto).toContain('La fecha debe ser anterior a hoy.');
  });

  it('envia las listas del FormArray recortadas y sin elementos vacios', () => {
    llenarObligatorios();
    componente['agregarElemento']('alergias', ' Penicilina ');
    componente['agregarElemento']('alergias', '   ');
    componente['agregarElemento']('enfermedades', 'Hipertensión, controlada');

    componente['guardar']();
    expect(componente['guardando']()).toBe(true);

    const req = http.expectOne((r) => r.method === 'POST' && r.url === URL_PACIENTES);
    expect(req.request.body.dni).toBe('45678912');
    expect(req.request.body.antecedentes.alergias).toEqual(['Penicilina']);
    expect(req.request.body.antecedentes.enfermedades).toEqual(['Hipertensión, controlada']);
    expect(req.request.body.antecedentes.medicamentos).toEqual([]);

    req.flush({ id: 7, nombreCompleto: 'Ana Lucia Torres Vega' });
    // finalize() apaga el indicador; despues se recarga el listado.
    expect(componente['guardando']()).toBe(false);
    http.expectOne((r) => r.method === 'GET' && r.url === URL_PACIENTES).flush([]);
  });

  it('apaga el indicador de guardado tambien cuando la API responde con error', () => {
    llenarObligatorios();
    componente['guardar']();

    http
      .expectOne((r) => r.method === 'POST' && r.url === URL_PACIENTES)
      .flush(
        { estado: 409, mensaje: 'Ya existe un paciente registrado con el DNI 45678912' },
        { status: 409, statusText: 'Conflict' },
      );

    expect(componente['guardando']()).toBe(false);
    expect(componente['errorFormulario']()).toBe(
      'Ya existe un paciente registrado con el DNI 45678912',
    );
  });

  it('quitarElemento elimina el control del FormArray', () => {
    componente['agregarElemento']('habitos', 'Bruxismo');
    componente['agregarElemento']('habitos', 'Tabaco');
    componente['quitarElemento']('habitos', 0);

    expect(componente['elementos']('habitos').getRawValue()).toEqual(['Tabaco']);
  });
});
