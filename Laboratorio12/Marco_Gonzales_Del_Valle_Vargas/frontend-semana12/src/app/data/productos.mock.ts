import { Producto } from '../models/producto';

/**
 * Datos locales simulados. Todavia no se consume el backend: en la semana 14
 * esta lista vendra de GET /api/productos y los componentes no cambiaran.
 * Los ids 1 a 3 son los de la guia; 4 y 5 amplian las categorias (Reto A).
 */
export const PRODUCTOS: Producto[] = [
  {
    id: 1,
    nombre: 'Laptop Pro',
    categoria: 'Tecnología',
    precio: 3699,
    descripcion: 'Equipo para desarrollo y productividad.',
  },
  {
    id: 2,
    nombre: 'Monitor 27 pulgadas',
    categoria: 'Tecnología',
    precio: 1199,
    descripcion: 'Monitor QHD para trabajo profesional.',
  },
  {
    id: 3,
    nombre: 'Teclado mecánico',
    categoria: 'Accesorios',
    precio: 329,
    descripcion: 'Teclado compacto para programación.',
  },
  {
    id: 4,
    nombre: 'Mouse inalámbrico',
    categoria: 'Accesorios',
    precio: 149,
    descripcion: 'Mouse ergonómico con conexión Bluetooth.',
  },
  {
    id: 5,
    nombre: 'Silla ergonómica',
    categoria: 'Mobiliario',
    precio: 899,
    descripcion: 'Soporte lumbar ajustable para jornadas largas.',
  },
];
