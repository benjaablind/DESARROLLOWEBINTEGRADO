import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { computed, signal } from '@angular/core';

import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { ArchivosComponent } from './archivos';

const API = environment.apiUrl;

/** File con el peso pedido, sin reservar esa memoria. */
function archivo(nombre: string, bytes = 2048): File {
  const f = new File(['x'], nombre);
  Object.defineProperty(f, 'size', { value: bytes });
  return f;
}

/** Simula elegir archivos en el <input type="file" multiple>. */
function elegir(componente: ArchivosComponent, ...archivos: File[]): void {
  const entrada = { files: archivos, value: '' } as unknown as HTMLInputElement;
  (componente as any).alElegirArchivos({ target: entrada } as unknown as Event);
}

describe('ArchivosComponent: subida con FormArray', () => {
  let fixture: ComponentFixture<ArchivosComponent>;
  let c: any;
  let html: HTMLElement;
  let http: HttpTestingController;

  beforeEach(async () => {
    const sesion = signal({ id: 1, nombreCompleto: 'Marco Gonzales' });
    await TestBed.configureTestingModule({
      imports: [ArchivosComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { usuarioActual: computed(() => sesion()), sesionVigente: () => true },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ArchivosComponent);
    c = fixture.componentInstance;
    html = fixture.nativeElement;
    http = TestBed.inject(HttpTestingController);

    // Carga inicial: pacientes y archivos.
    http.expectOne((r) => r.url === `${API}/pacientes`).flush([
      { id: 5, nombreCompleto: 'Ana Torres', dni: '12345678' },
    ]);
    http.expectOne((r) => r.url === `${API}/archivos`).flush([]);
    c.abrirSubida();
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('cada archivo elegido es una fila del FormArray, con tipo segun su extension', () => {
    elegir(c, archivo('panoramica.png'), archivo('informe.pdf'));

    expect(c.filas.length).toBe(2);
    expect(c.filas.at(0).controls.tipo.value).toBe('RADIOGRAFIA');
    expect(c.filas.at(1).controls.tipo.value).toBe('DOCUMENTO');
  });

  it('marca la fila de un archivo no permitido con su error y bloquea la subida', async () => {
    elegir(c, archivo('virus.exe'), archivo('enorme.png', 11 * 1024 * 1024));
    c.formularioSubida.controls.pacienteId.setValue('5');
    fixture.detectChanges();
    await fixture.whenStable();

    const texto = html.textContent ?? '';
    expect(texto).toContain('La extensión .exe no está permitida.');
    expect(texto).toContain('Pesa 11.0 MB y el máximo es 10 MB.');

    c.subir();
    expect(c.errorSubida()).toBe('Revise el paciente y los archivos marcados antes de subirlos.');
    http.expectNone(`${API}/archivos`);
  });

  it('sin archivos ni paciente no envia nada', () => {
    c.subir();
    expect(c.errorSubida()).toBe('Agregue al menos un archivo.');
    expect(c.formularioSubida.controls.pacienteId.touched).toBe(true);
  });

  it('quitar una fila la saca del FormArray', () => {
    elegir(c, archivo('a.png'), archivo('b.png'));
    c.quitarFila(0);
    expect(c.filas.length).toBe(1);
    expect(c.filas.at(0).controls.archivo.value.name).toBe('b.png');
  });

  it('sube uno por uno; si uno falla, deja solo esa fila con el mensaje del backend', () => {
    elegir(c, archivo('rx1.png'), archivo('rx2.png'));
    c.filas.at(1).controls.descripcion.setValue('  Control  ');
    c.formularioSubida.controls.pacienteId.setValue('5');

    c.subir();
    expect(c.subiendo()).toBe(true);
    expect(c.progreso()).toEqual({ hechos: 0, total: 2 });

    // concatMap: la segunda peticion sale solo cuando responde la primera.
    const primera = http.expectOne(`${API}/archivos`);
    expect((primera.request.body as FormData).get('pacienteId')).toBe('5');
    primera.flush({ id: 1, nombreOriginal: 'rx1.png' }, { status: 201, statusText: 'Created' });

    const segunda = http.expectOne(`${API}/archivos`);
    expect((segunda.request.body as FormData).get('descripcion')).toBe('Control');
    segunda.flush(
      { estado: 400, mensaje: 'El archivo esta vacio' },
      { status: 400, statusText: 'Bad Request' },
    );

    // El listado se recarga en el paciente de los archivos subidos.
    http.expectOne((r) => r.url === `${API}/archivos/paciente/5`).flush([]);

    expect(c.subiendo()).toBe(false);
    expect(c.progreso()).toBeNull();
    expect(c.filas.length).toBe(1);
    expect(c.filas.at(0).controls.archivo.value.name).toBe('rx2.png');
    expect(c.errorSubida()).toBe(
      'Se subieron 1 de 2. No se pudo subir rx2.png: El archivo esta vacio.',
    );
    expect(c.panelSubida()).toBe(true);
  });

  it('si todos suben bien, vacia el FormArray y cierra el panel', () => {
    elegir(c, archivo('rx1.png'));
    c.formularioSubida.controls.pacienteId.setValue('5');

    c.subir();
    http.expectOne(`${API}/archivos`).flush({ id: 1, nombreOriginal: 'rx1.png' });
    http.expectOne((r) => r.url === `${API}/archivos/paciente/5`).flush([]);

    expect(c.filas.length).toBe(0);
    expect(c.panelSubida()).toBe(false);
  });

  it('pide las miniaturas con HttpClient (con token) y las muestra como blob:', async () => {
    const crear = vi.fn(() => 'blob:miniatura-1');
    const liberar = vi.fn();
    Object.assign(URL, { createObjectURL: crear, revokeObjectURL: liberar });

    c.cargar();
    http.expectOne((r) => r.url === `${API}/archivos`).flush([
      { id: 1, tipo: 'RADIOGRAFIA', nombreOriginal: 'rx.png', extension: 'png', pacienteId: 5 },
      { id: 2, tipo: 'INFORME', nombreOriginal: 'informe.pdf', extension: 'pdf', pacienteId: 5 },
    ]);

    // Solo la imagen pide su binario; el PDF muestra el icono del tipo.
    const req = http.expectOne(`${API}/archivos/1/descargar`);
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['png']));
    fixture.detectChanges();
    await fixture.whenStable();

    expect(c.vistasPrevias()).toEqual({ 1: 'blob:miniatura-1' });
    expect(html.querySelector('.vista-previa img')?.getAttribute('src')).toBe('blob:miniatura-1');

    // Al recargar se liberan las URLs anteriores.
    c.cargar();
    http.expectOne((r) => r.url === `${API}/archivos`).flush([]);
    expect(liberar).toHaveBeenCalledWith('blob:miniatura-1');
  });

  it('la descripcion de la edicion admite hasta 300 caracteres, como el backend', () => {
    const descripcion = c.formularioEdicion.controls.descripcion;
    descripcion.setValue('x'.repeat(300));
    expect(descripcion.valid).toBe(true);
    descripcion.setValue('x'.repeat(301));
    expect(descripcion.hasError('maxlength')).toBe(true);
  });
});
