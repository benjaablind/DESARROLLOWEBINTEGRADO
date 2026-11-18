import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { environment } from '../../../environments/environment';
import { Cita } from '../../core/models';
import { CitasComponent } from './citas';

const API = environment.apiUrl;

/** Cita futura sobre la que se prueba el panel de reprogramacion. */
const CITA: Cita = {
  id: 7,
  pacienteId: 1,
  pacienteNombre: 'Ana Torres',
  odontologoId: 2,
  odontologoNombre: 'Luis Ramos',
  fechaHora: '2099-01-05T10:00:00',
  fechaHoraFin: '2099-01-05T10:30:00',
  duracionMinutos: 30,
  motivo: 'Control',
  estado: 'PENDIENTE',
  observaciones: null,
  citaOriginalId: null,
  fechaRegistro: '2026-11-14T09:00:00',
};

describe('CitasComponent: validacion de formularios', () => {
  let fixture: ComponentFixture<CitasComponent>;
  let componente: CitasComponent;
  let http: HttpTestingController;

  /** Texto visible del componente. */
  const texto = () => (fixture.nativeElement as HTMLElement).textContent ?? '';

  /** Envia el formulario que contiene el campo indicado, como lo haria el boton submit. */
  async function enviar(selectorCampo: string): Promise<void> {
    const campo = (fixture.nativeElement as HTMLElement).querySelector(selectorCampo);
    campo?.closest('form')?.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    await fixture.whenStable();
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CitasComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(CitasComponent);
    componente = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);

    // El constructor pide los catalogos y la agenda del dia.
    http
      .expectOne(`${API}/pacientes`)
      .flush([{ id: 1, nombreCompleto: 'Ana Torres', dni: '12345678' }]);
    http.expectOne(`${API}/usuarios/odontologos`).flush([{ id: 2, nombreCompleto: 'Luis Ramos' }]);
    http.expectOne((r) => r.url === `${API}/citas/agenda`).flush([]);

    componente['abrirNueva']();
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('no muestra errores antes de tocar el formulario', () => {
    expect(texto()).not.toContain('El motivo es obligatorio.');
    expect(texto()).not.toContain('Elija un paciente.');
  });

  it('al enviar vacio marca todo como tocado, muestra cada mensaje y no llama a la API', async () => {
    await enviar('#c-motivo');

    expect(componente['formulario'].controls.motivo.touched).toBe(true);
    expect(texto()).toContain('Elija un paciente.');
    expect(texto()).toContain('Elija un odontólogo.');
    expect(texto()).toContain('Indique la hora.');
    expect(texto()).toContain('El motivo es obligatorio.');
    http.expectNone((r) => r.method === 'POST');
  });

  it('distingue un motivo con solo espacios (@NotBlank)', async () => {
    componente['formulario'].patchValue({
      pacienteId: '1',
      odontologoId: '2',
      fecha: '2099-01-05',
      hora: '09:30',
      motivo: '   ',
    });
    await enviar('#c-motivo');

    expect(texto()).toContain('El motivo no puede contener solo espacios.');
    expect(texto()).not.toContain('El motivo es obligatorio.');
    http.expectNone((r) => r.method === 'POST');
  });

  it('muestra el limite de duracion que aplica @Max(240)', async () => {
    componente['formulario'].controls.duracionMinutos.setValue(300);
    await enviar('#c-motivo');

    expect(texto()).toContain('La duración máxima es de 240 minutos.');
  });

  it('valida la fecha y la hora juntas en el FormGroup (@Future)', async () => {
    componente['formulario'].patchValue({ fecha: '2000-01-01', hora: '10:00' });
    await enviar('#c-motivo');

    expect(componente['formulario'].hasError('fechaPasada')).toBe(true);
    expect(texto()).toContain('La fecha y hora de la cita debe ser futura.');
  });

  it('con datos validos envia el CitaRequest y finalize apaga el indicador de guardado', async () => {
    componente['formulario'].setValue({
      pacienteId: '1',
      odontologoId: '2',
      fecha: '2099-01-05',
      hora: '09:30',
      duracionMinutos: 45,
      motivo: '  Limpieza  ',
      observaciones: '',
    });
    await enviar('#c-motivo');

    const req = http.expectOne(`${API}/citas`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      pacienteId: 1,
      odontologoId: 2,
      fechaHora: '2099-01-05T09:30:00',
      duracionMinutos: 45,
      motivo: 'Limpieza',
      observaciones: null,
    });
    expect(componente['guardando']()).toBe(true);

    req.flush({ ...CITA, id: 9 }, { status: 201, statusText: 'Created' });
    expect(componente['guardando']()).toBe(false);
    expect(componente['formAbierto']()).toBe(false);

    // Tras guardar se recarga la agenda.
    http.expectOne((r) => r.url === `${API}/citas/agenda`).flush([]);
  });

  it('si el backend rechaza la cita apaga el guardado y muestra su mensaje', async () => {
    componente['formulario'].setValue({
      pacienteId: '1',
      odontologoId: '2',
      fecha: '2099-01-05',
      hora: '09:30',
      duracionMinutos: 30,
      motivo: 'Control',
      observaciones: '',
    });
    await enviar('#c-motivo');

    http.expectOne(`${API}/citas`).flush(
      {
        estado: 409,
        mensaje: 'El odontologo ya tiene una cita en ese horario',
        ruta: '/api/citas',
        fechaHora: '2026-11-14T10:00:00',
      },
      { status: 409, statusText: 'Conflict' },
    );
    fixture.detectChanges();
    await fixture.whenStable();

    expect(componente['guardando']()).toBe(false);
    expect(texto()).toContain('El odontologo ya tiene una cita en ese horario');
  });

  it('no deja reprogramar a la misma fecha y hora de la cita', async () => {
    componente['cerrarFormulario']();
    componente['abrirReprogramar'](CITA);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(componente['formReprogramar'].hasError('mismaFechaHora')).toBe(true);
    await enviar('#r-motivo');

    expect(texto()).toContain('Elija una fecha u hora distinta de la actual de la cita.');
    http.expectNone((r) => r.method === 'POST');
  });

  it('reprograma cuando la hora cambia', async () => {
    componente['cerrarFormulario']();
    componente['abrirReprogramar'](CITA);
    componente['formReprogramar'].controls.hora.setValue('11:00');
    fixture.detectChanges();
    await enviar('#r-motivo');

    const req = http.expectOne(`${API}/citas/7/reprogramar`);
    expect(req.request.body).toEqual({ nuevaFechaHora: '2099-01-05T11:00:00', motivo: null });
    req.flush({ ...CITA, id: 8, citaOriginalId: 7 }, { status: 201, statusText: 'Created' });
    expect(componente['procesando']()).toBeNull();

    http.expectOne((r) => r.url === `${API}/citas/agenda`).flush([]);
  });
});
