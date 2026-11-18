import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';

import { environment } from '../../../environments/environment';
import { EstadoCuenta, Tratamiento } from '../../core/models';
import { PagosComponent } from './pagos';

const API = environment.apiUrl;

const TRATAMIENTO: Tratamiento = {
  id: 7,
  pacienteId: 3,
  pacienteNombre: 'Ana Torres',
  odontologoId: 2,
  odontologoNombre: 'Luis Ramos',
  nombre: 'Ortodoncia correctiva',
  descripcion: null,
  precio: 300,
  estado: 'EN_PROCESO',
  fechaInicio: null,
  fechaFin: null,
  observaciones: null,
  sesiones: [],
  totalPagado: 200,
  saldoPendiente: 100,
  fechaRegistro: '2026-11-01T10:00:00',
};

const CUENTA: EstadoCuenta = {
  pacienteId: 3,
  pacienteNombre: 'Ana Torres',
  totalTratamientos: 300,
  totalPagado: 200,
  saldoPendiente: 100,
  tratamientos: [TRATAMIENTO],
};

describe('PagosComponent: formulario de registro de pago', () => {
  let fixture: ComponentFixture<PagosComponent>;
  let http: HttpTestingController;
  let pagina: HTMLElement;

  /** Atiende las dos peticiones de cargar(): estado de cuenta e historial. */
  function responderCarga(): void {
    http.expectOne(`${API}/pagos/estado-cuenta/3`).flush(CUENTA);
    http.expectOne((r) => r.url === `${API}/pagos` && r.method === 'GET').flush([]);
  }

  function escribirMonto(valor: string): void {
    const input = pagina.querySelector<HTMLInputElement>('#pg-monto')!;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new Event('blur'));
    fixture.detectChanges();
  }

  function enviar(): void {
    pagina.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  function errores(): string {
    return Array.from(pagina.querySelectorAll('.form-error'))
      .map((e) => e.textContent?.trim())
      .join(' | ');
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [PagosComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: convertToParamMap({ pacienteId: '3' }) } },
        },
      ],
    });

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(PagosComponent);
    pagina = fixture.nativeElement as HTMLElement;
    fixture.detectChanges();

    http.expectOne((r) => r.url === `${API}/pacientes`).flush([]);
    responderCarga();
    fixture.detectChanges();

    // Abre el dialogo desde la fila del tratamiento: el monto inicial es el saldo.
    pagina.querySelector<HTMLButtonElement>('td.celda-acciones button')!.click();
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
  });

  it('no muestra errores antes de tocar el campo ni de enviar', () => {
    expect(pagina.querySelector('#pg-monto')).toBeTruthy();
    expect(errores()).toBe('');
  });

  it('bloquea un monto mayor al saldo sin llamar al servidor', () => {
    escribirMonto('150');
    expect(errores()).toContain('supera el saldo pendiente del tratamiento');

    enviar();
    http.expectNone((r) => r.method === 'POST');
  });

  it('muestra un mensaje propio para mas de 2 decimales y para cero', () => {
    escribirMonto('10.555');
    expect(errores()).toContain('admite hasta 2 decimales');

    escribirMonto('0');
    expect(errores()).toContain('debe ser mayor a cero');
  });

  it('muestra los errores al enviar aunque el campo no se haya tocado', () => {
    const input = pagina.querySelector<HTMLInputElement>('#pg-monto')!;
    input.value = '';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(errores()).toBe('');

    enviar();
    expect(errores()).toContain('Indique el monto a cobrar');
    http.expectNone((r) => r.method === 'POST');
  });

  it('envia un monto valido y libera el boton cuando el servidor rechaza el pago', () => {
    escribirMonto('80.50');
    enviar();

    const peticion = http.expectOne((r) => r.method === 'POST');
    expect(peticion.request.url).toBe(`${API}/pagos`);
    expect(peticion.request.body).toEqual({
      tratamientoId: 7,
      monto: 80.5,
      metodo: 'EFECTIVO',
      comprobante: null,
      observaciones: null,
    });

    const boton = pagina.querySelector<HTMLButtonElement>('form button[type="submit"]')!;
    fixture.detectChanges();
    expect(boton.disabled).toBe(true);

    peticion.flush(
      {
        estado: 400,
        mensaje: 'El monto excede el saldo pendiente del tratamiento: saldo actual S/ 50.00',
      },
      { status: 400, statusText: 'Bad Request' },
    );
    fixture.detectChanges();

    // finalize() libero el estado de guardado tambien en el error.
    expect(boton.disabled).toBe(false);
    expect(pagina.querySelector('.dialogo .alerta-error')?.textContent).toContain(
      'saldo actual S/ 50.00',
    );
  });
});
