import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { HistoriasComponent } from './historias';

/**
 * Prueba enfocada del formulario de consulta: los errores aparecen solo al
 * intentar guardar o al salir del campo, y un formulario invalido no llega a
 * hacer el POST.
 */
describe('HistoriasComponent (formulario de consulta)', () => {
  let fixture: ComponentFixture<HistoriasComponent>;
  let componente: HistoriasComponent;
  let http: HttpTestingController;
  let pagina: HTMLElement;
  const api = environment.apiUrl;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoriasComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(HistoriasComponent);
    componente = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    pagina = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();

    // Catalogos que pide el constructor.
    http
      .expectOne(`${api}/pacientes`)
      .flush([{ id: 1, nombreCompleto: 'Ana Torres', dni: '12345678' }]);
    http.expectOne(`${api}/usuarios/odontologos`).flush([{ id: 3, nombreCompleto: 'Luis Ramos' }]);
    await fixture.whenStable();

    componente['abrirFormulario']();
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  function enviar(): void {
    pagina.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('no muestra errores hasta que se intenta guardar', () => {
    expect(pagina.querySelectorAll('.form-error').length).toBe(0);
  });

  it('al guardar vacio marca los obligatorios y no envia la peticion', () => {
    enviar();

    const texto = pagina.textContent ?? '';
    expect(texto).toContain('Elija un paciente.');
    expect(texto).toContain('El motivo de consulta es obligatorio.');
    expect(texto).toContain('El diagnóstico es obligatorio.');
    http.expectNone(`${api}/historias`);
  });

  it('un motivo con solo espacios muestra su propio mensaje al salir del campo', () => {
    const motivo = pagina.querySelector<HTMLTextAreaElement>('#hc-motivo')!;
    motivo.value = '   ';
    motivo.dispatchEvent(new Event('input'));
    motivo.dispatchEvent(new Event('blur'));
    fixture.detectChanges();

    expect(pagina.textContent).toContain('El motivo no puede quedar solo con espacios.');
  });

  it('rechaza una cita que no es del paciente de la consulta', async () => {
    const form = componente['formulario'];
    form.controls.pacienteId.setValue('1');
    componente['cargarCitasDelPaciente']();
    // Respuesta que llega tarde de otro paciente: el select la mostraria igual.
    http
      .expectOne((req) => req.url === `${api}/citas` && req.params.get('pacienteId') === '1')
      .flush([{ id: 20, pacienteId: 2, fechaHora: '2026-09-04T10:00:00', motivo: 'Control' }]);

    form.patchValue({
      odontologoId: '3',
      citaId: '20',
      motivoConsulta: 'Dolor',
      diagnostico: 'Caries',
    });
    await fixture.whenStable();
    enviar();

    expect(form.hasError('citaDeOtroPaciente')).toBe(true);
    expect(pagina.textContent).toContain(
      'La cita elegida no pertenece al paciente de la consulta.',
    );
    http.expectNone(`${api}/historias`);
  });

  it('con datos validos hace el POST con los textos recortados', async () => {
    componente['formulario'].patchValue({
      pacienteId: '1',
      odontologoId: '3',
      motivoConsulta: '  Dolor  ',
      diagnostico: 'Caries',
      anamnesis: '   ',
    });
    await fixture.whenStable();
    enviar();

    const req = http.expectOne(`${api}/historias`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(
      expect.objectContaining({
        pacienteId: 1,
        odontologoId: 3,
        citaId: null,
        motivoConsulta: 'Dolor',
        diagnostico: 'Caries',
        anamnesis: null,
      }),
    );
  });
});
