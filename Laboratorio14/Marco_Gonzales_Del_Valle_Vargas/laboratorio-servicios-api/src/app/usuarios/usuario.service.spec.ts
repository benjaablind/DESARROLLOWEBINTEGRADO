import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UsuarioService } from './usuario.service';
import { Usuario, UsuarioEntrada } from './usuario.model';

const API = 'https://jsonplaceholder.typicode.com/users';

const leanne: Usuario = {
  id: 1,
  name: 'Leanne Graham',
  username: 'Bret',
  email: 'Sincere@april.biz',
  phone: '1-770-736-8031',
  website: 'hildegard.org'
};

const entrada: UsuarioEntrada = {
  name: 'Marco Gonzales',
  username: 'marco',
  email: 'marco@correo.com',
  phone: '987654321',
  website: 'marco.dev'
};

describe('UsuarioService', () => {
  let servicio: UsuarioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    servicio = TestBed.inject(UsuarioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obtenerTodos() hace GET /users y entrega la lista', () => {
    let recibido: Usuario[] = [];
    servicio.obtenerTodos().subscribe(lista => (recibido = lista));

    const req = http.expectOne(API);
    expect(req.request.method).toBe('GET');
    req.flush([leanne]);

    expect(recibido).toEqual([leanne]);
  });

  it('obtenerPorId() hace GET /users/{id}', () => {
    let recibido: Usuario | undefined;
    servicio.obtenerPorId(1).subscribe(u => (recibido = u));

    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(leanne);

    expect(recibido?.name).toBe('Leanne Graham');
  });

  it('crear() hace POST /users con el cuerpo sin id', () => {
    servicio.crear(entrada).subscribe();

    const req = http.expectOne(API);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(entrada);
    req.flush({ ...entrada, id: 11 }, { status: 201, statusText: 'Created' });
  });

  it('actualizar() hace PUT /users/{id} con todos los campos', () => {
    servicio.actualizar(1, entrada).subscribe();

    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(entrada);
    req.flush({ ...entrada, id: 1 });
  });

  it('actualizarParcial() hace PATCH /users/{id} solo con los cambios', () => {
    servicio.actualizarParcial(1, { email: 'nuevo@correo.com' }).subscribe();

    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ email: 'nuevo@correo.com' });
    req.flush({ ...leanne, email: 'nuevo@correo.com' });
  });

  it('eliminar() hace DELETE /users/{id}', () => {
    let completo = false;
    servicio.eliminar(1).subscribe({ complete: () => (completo = true) });

    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});

    expect(completo).toBe(true);
  });

  it('propaga el error HTTP para que el componente lo trate', () => {
    let estado = 0;
    servicio.obtenerPorId(999).subscribe({ error: e => (estado = e.status) });

    http.expectOne(`${API}/999`).flush({}, { status: 404, statusText: 'Not Found' });

    expect(estado).toBe(404);
  });
});
