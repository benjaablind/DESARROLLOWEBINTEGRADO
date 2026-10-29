/** Forma comun de un producto: TypeScript verifica que todos la cumplan. */
export interface Producto {
  id: number;
  nombre: string;
  categoria: string;
  precio: number;
  descripcion: string;
}
