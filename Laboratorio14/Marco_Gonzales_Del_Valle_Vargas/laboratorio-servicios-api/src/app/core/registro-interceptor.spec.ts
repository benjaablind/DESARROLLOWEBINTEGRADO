import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { registroInterceptor } from './registro-interceptor';
import { ContadorSolicitudesService } from './contador-solicitudes.service';

describe('registroInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let contador: ContadorSolicitudesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([registroInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    contador = TestBed.inject(ContadorSolicitudesService);
  });

  afterEach(() => {
    backend.verify();
    vi.restoreAllMocks();
  });

  it('registra la solicitud exitosa en consola y suma al contador', () => {
    const log = vi.spyOn(console, 'log').mockImplementation(() => {});

    http.get('/users').subscribe();
    backend.expectOne('/users').flush([]);

    expect(contador.exitosas()).toBe(1);
    expect(log).toHaveBeenCalledWith(expect.stringMatching(/^\[HTTP\] GET \/users -> 200 \(\d+ ms\)$/));
  });

  it('registra el error con su estado y no lo cuenta como exitoso', () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => {});

    http.get('/inexistente').subscribe({ error: () => {} });
    backend.expectOne('/inexistente').flush({}, { status: 404, statusText: 'Not Found' });

    expect(contador.exitosas()).toBe(0);
    expect(warn).toHaveBeenCalledWith(expect.stringContaining('GET /inexistente -> 404'));
  });
});
