import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { ReportesComponent } from './reportes';

const API = environment.apiUrl;

/** Respuestas minimas con la forma que pinta la plantilla. */
const PACIENTES = {
  desde: '2026-01-01',
  hasta: '2026-03-31',
  total: 0,
  activos: 0,
  inactivos: 0,
  pacientes: [],
};
const PENDIENTES = { totalPendiente: 0, cantidadTratamientos: 0, items: [] };

describe('ReportesComponent: rango de fechas', () => {
  let fixture: ComponentFixture<ReportesComponent>;
  let c: any;
  let html: HTMLElement;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReportesComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { puedeVerReporte: () => true } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportesComponent);
    c = fixture.componentInstance;
    html = fixture.nativeElement;
    http = TestBed.inject(HttpTestingController);

    fixture.detectChanges();
    http.expectOne((r) => r.url === `${API}/pacientes`).flush([]);
    http.expectOne((r) => r.url === `${API}/reportes/pacientes`).flush(PACIENTES);
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('con el rango invertido no consulta al servidor y muestra el error', async () => {
    c.rango.setValue({ desde: '2026-04-01', hasta: '2026-03-01' });
    c.cargar();
    fixture.detectChanges();
    await fixture.whenStable();

    http.expectNone((r) => r.url.startsWith(`${API}/reportes`));
    expect(c.cargando()).toBe(false);
    expect(html.textContent).toContain('La fecha inicial no puede ser posterior a la fecha final.');
  });

  it('con un rango valido envia desde y hasta y apaga la carga al responder', () => {
    c.rango.setValue({ desde: '2026-01-01', hasta: '2026-03-31' });
    c.cargar();
    expect(c.cargando()).toBe(true);

    const req = http.expectOne((r) => r.url === `${API}/reportes/pacientes`);
    expect(req.request.params.get('desde')).toBe('2026-01-01');
    expect(req.request.params.get('hasta')).toBe('2026-03-31');
    req.flush(PACIENTES);

    expect(c.cargando()).toBe(false);
  });

  it('si el backend falla, muestra su mensaje y no se queda cargando', () => {
    c.cargar();
    http
      .expectOne((r) => r.url === `${API}/reportes/pacientes`)
      .flush({ estado: 500, mensaje: 'Ocurrio un error interno' }, { status: 500, statusText: 'Error' });

    expect(c.cargando()).toBe(false);
    expect(c.error()).toBe('Ocurrio un error interno');
  });

  it('los pagos pendientes no usan el rango aunque este invertido', () => {
    c.rango.setValue({ desde: '2026-04-01', hasta: '2026-03-01' });
    c.irA('pendientes');
    http.expectOne(`${API}/reportes/pagos-pendientes`).flush(PENDIENTES);
  });
});
