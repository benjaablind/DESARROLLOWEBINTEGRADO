package com.utp.semana3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.utp.semana3.exception.ProductoNoEncontradoException;
import com.utp.semana3.model.Producto;

class ProductoServiceTest {

    private ProductoService service;

    @BeforeEach
    void setUp() {
        service = new ProductoService();
    }

    @Test
    void registrarProductoValido_debeAsignarIdYGuardar() {
        Producto producto = new Producto(null, "Laptop", 3500.00, 10);

        Producto registrado = service.registrar(producto);

        assertThat(registrado.getId()).isNotNull();
        assertThat(registrado.getNombre()).isEqualTo("Laptop");
        assertThat(service.listar()).hasSize(1);
    }

    @Test
    void registrarProductoConPrecioCero_debeLanzarExcepcion() {
        Producto producto = new Producto(null, "Mouse", 0.00, 5);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio debe ser mayor que cero");
    }

    @Test
    void registrarProductoSinNombre_debeLanzarExcepcion() {
        Producto producto = new Producto(null, "", 100.00, 5);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre es obligatorio");
    }

    @Test
    void registrarProductoConStockNegativo_debeLanzarExcepcion() {
        Producto producto = new Producto(null, "Teclado", 150.00, -1);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El stock no puede ser negativo");
    }

    @Test
    void buscarPorId_cuandoExiste_debeRetornarElProducto() {
        Producto registrado = service.registrar(new Producto(null, "Laptop", 3500.00, 10));

        assertThat(service.buscarPorId(registrado.getId())).isPresent();
    }

    @Test
    void buscarPorId_cuandoNoExiste_debeRetornarVacio() {
        assertThat(service.buscarPorId(99L)).isEmpty();
    }

    @Test
    void eliminar_debeQuitarElProductoDeLaLista() {
        Producto registrado = service.registrar(new Producto(null, "Mouse", 80.00, 20));

        service.eliminar(registrado.getId());

        assertThat(service.listar()).isEmpty();
    }

    @Test
    void eliminar_cuandoNoExiste_debeLanzarExcepcion() {
        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(ProductoNoEncontradoException.class);
    }

    @Test
    void actualizar_debeModificarProductoExistente() {
        Producto registrado = service.registrar(new Producto(null, "Mouse", 80.00, 20));

        Producto actualizado = service.actualizar(registrado.getId(), new Producto(null, "Mouse Pro", 100.00, 15));

        assertThat(actualizado.getNombre()).isEqualTo("Mouse Pro");
        assertThat(actualizado.getPrecio()).isEqualTo(100.00);
        assertThat(actualizado.getStock()).isEqualTo(15);
    }

    @Test
    void actualizar_cuandoNoExiste_debeLanzarExcepcion() {
        assertThatThrownBy(() -> service.actualizar(99L, new Producto(null, "Mouse Pro", 100.00, 15)))
                .isInstanceOf(ProductoNoEncontradoException.class);
    }

    @Test
    void actualizar_debeValidarReglasDeNegocio() {
        Producto registrado = service.registrar(new Producto(null, "Mouse", 80.00, 20));

        assertThatThrownBy(() -> service.actualizar(registrado.getId(), new Producto(null, "Mouse Pro", -5.00, 15)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
