package com.utp.semana3.service;

import com.utp.semana3.model.Producto;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoServiceTest {

    @Test
    void registrarProductoValido_debeAsignarIdYGuardar() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Teclado", 199.90, 5);

        Producto guardado = service.registrar(producto);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getNombre()).isEqualTo("Teclado");
        assertThat(service.listar()).hasSize(1);
        assertThat(service.listar().get(0)).isSameAs(guardado);
    }

    @Test
    void registrarProductoConPrecioMenorOIgualAZero_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Mouse", 0, 3);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio debe ser mayor a 0");
    }

    @Test
    void registrarProductoConNombreVacio_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "   ", 25.5, 2);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El nombre no puede estar vacío");
    }

    @Test
    void registrarProductoConStockNegativo_debeLanzarExcepcion() {
        ProductoService service = new ProductoService();
        Producto producto = new Producto(null, "Monitor", 350.0, -1);

        assertThatThrownBy(() -> service.registrar(producto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El stock no puede ser negativo");
    }

    @Test
    void buscarPorId_debeRetornarProductoCuandoExiste() {
        ProductoService service = new ProductoService();
        Producto guardado = service.registrar(new Producto(null, "Laptop", 1200.0, 4));

        Optional<Producto> encontrado = service.buscarPorId(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Laptop");
    }

    @Test
    void buscarPorId_debeRetornarVacioCuandoNoExiste() {
        ProductoService service = new ProductoService();

        Optional<Producto> encontrado = service.buscarPorId(999L);

        assertThat(encontrado).isEmpty();
    }
}
