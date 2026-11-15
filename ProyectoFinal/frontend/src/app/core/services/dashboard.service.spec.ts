import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { DashboardService } from './dashboard.service';
import { Dashboard, ErrorApi } from '../models';

const URL_DASHBOARD = `${environment.apiUrl}/dashboard`;

const DASHBOARD: Dashboard = {
  pacientesRegistrados: 12,
  citasHoy: 3,
  citasPendientes: 5,
  tratamientosActivos: 4,
  pagosPendientes: 850,
  ingresosDelMes: 2400.5,
  proximasCitas: [],
  pacientesRecientes: [
    { id: 1, nombreCompleto: 'Ana Torres', telefono: null, fechaRegistro: '2026-11-10T09:00:00' },
  ],
  tratamientosActivosDetalle: [],
  alertas: ['Hay 1 cita marcada como no asistio este mes'],
  resumenActividad: {
    citasAtendidasMes: 8,
    citasCanceladasMes: 1,
    citasNoAsistioMes: 1,
    tratamientosCompletadosMes: 2,
    nuevosPacientesMes: 3,
  },
};

describe('DashboardService', () => {
  let servicio: DashboardService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(DashboardService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obtener hace un unico GET /dashboard sin parametros', () => {
    let respuesta: Dashboard | undefined;
    servicio.obtener().subscribe((d) => (respuesta = d));

    const req = http.expectOne(URL_DASHBOARD);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).toEqual([]);
    req.flush(DASHBOARD);

    expect(respuesta).toEqual(DASHBOARD);
  });

  it('un error con cuerpo JSON conserva el mensaje del backend', () => {
    let error: ErrorApi | undefined;
    servicio.obtener().subscribe({ error: (e: ErrorApi) => (error = e) });

    http.expectOne(URL_DASHBOARD).flush(
      {
        estado: 401,
        mensaje: 'Token invalido o vencido',
        ruta: '/api/dashboard',
        fechaHora: '2026-11-14T10:00:00',
      },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(error?.estado).toBe(401);
    expect(error?.mensaje).toBe('Token invalido o vencido');
    expect(error?.ruta).toBe('/api/dashboard');
  });

  it('un error 500 sin cuerpo usa el mensaje por estado', () => {
    let error: ErrorApi | undefined;
    servicio.obtener().subscribe({ error: (e: ErrorApi) => (error = e) });

    http.expectOne(URL_DASHBOARD).flush(null, { status: 500, statusText: 'Server Error' });

    expect(error?.estado).toBe(500);
    expect(error?.mensaje).toBe('Ocurrio un error interno en el servidor');
    expect(error?.ruta).toBe('/dashboard');
  });
});
