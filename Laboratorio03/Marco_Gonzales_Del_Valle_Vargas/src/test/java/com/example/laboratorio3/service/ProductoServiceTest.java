package com.example.laboratorio3.service;

import com.example.laboratorio3.model.Producto;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoServiceTest {

    @Test
    void registrarProductoValido_debeAsignarIdYGuardar() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Laptop", 3500.00, 10);

        Producto registrado = service.registrar(producto);

        assertThat(registrado.getId()).isNotNull();
        assertThat(registrado.getNombre()).isEqualTo("Laptop");
        assertThat(service.listar()).hasSize(1);
    }

    @Test
    void registrarProductoConPrecioCero_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Mouse", 0.00, 5);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio debe ser mayor que cero");
    }

    @Test
    void registrarProductoSinNombre_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "", 100.00, 5);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre es obligatorio");
    }

    @Test
    void registrarProductoConStockNegativo_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Teclado", 150.00, -1);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El stock no puede ser negativo");
    }

    @Test
    void buscarPorIdCuandoExiste_debeRetornarElProducto() {
        ProductoService service = new ProductoService();
        Producto registrado = service.registrar(new Producto(null, "Laptop", 3500.00, 10));

        assertThat(service.buscarPorId(registrado.getId()))
                .isPresent()
                .get()
                .extracting(Producto::getNombre)
                .isEqualTo("Laptop");
    }

    @Test
    void buscarPorIdCuandoNoExiste_debeRetornarVacio() {
        ProductoService service = new ProductoService();

        assertThat(service.buscarPorId(99L)).isEmpty();
    }

    @Test
    void eliminarProductoExistente_debeQuitarloDeLaLista() {
        ProductoService service = new ProductoService();
        Producto registrado = service.registrar(new Producto(null, "Monitor", 900.00, 4));

        boolean eliminado = service.eliminar(registrado.getId());

        assertThat(eliminado).isTrue();
        assertThat(service.listar()).isEmpty();
    }

    @Test
    void eliminarProductoInexistente_debeRetornarFalse() {
        ProductoService service = new ProductoService();

        assertThat(service.eliminar(99L)).isFalse();
    }

    @Test
    void actualizarProductoExistente_debeModificarSusDatos() {
        ProductoService service = new ProductoService();
        Producto registrado = service.registrar(new Producto(null, "Teclado", 150.00, 5));

        Producto datos = new Producto(null, "Teclado mecánico", 250.00, 8);
        Optional<Producto> actualizado = service.actualizar(registrado.getId(), datos);

        assertThat(actualizado).isPresent();
        assertThat(actualizado.get().getId()).isEqualTo(registrado.getId());
        assertThat(actualizado.get().getNombre()).isEqualTo("Teclado mecánico");
        assertThat(actualizado.get().getPrecio()).isEqualTo(250.00);
        assertThat(actualizado.get().getStock()).isEqualTo(8);
        assertThat(service.listar()).hasSize(1);
    }

    @Test
    void actualizarProductoInexistente_debeRetornarVacio() {
        ProductoService service = new ProductoService();

        Optional<Producto> actualizado =
                service.actualizar(99L, new Producto(null, "Teclado", 150.00, 5));

        assertThat(actualizado).isEmpty();
    }

    @Test
    void actualizarProductoConPrecioInvalido_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto registrado = service.registrar(new Producto(null, "Teclado", 150.00, 5));

        Producto datos = new Producto(null, "Teclado", -10.00, 5);

        assertThatThrownBy(() -> service.actualizar(registrado.getId(), datos))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio debe ser mayor que cero");
    }
}
