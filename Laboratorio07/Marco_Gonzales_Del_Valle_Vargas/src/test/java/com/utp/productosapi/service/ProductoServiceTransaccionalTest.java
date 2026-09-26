package com.utp.productosapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.utp.productosapi.exception.ReglaNegocioException;
import com.utp.productosapi.model.Producto;
import com.utp.productosapi.repository.MovimientoStockRepository;
import com.utp.productosapi.repository.ProductoRepository;

/**
 * Comprueba commit y rollback reales.
 *
 * La clase NO lleva @Transactional a proposito: si la prueba se ejecutara
 * dentro de su propia transaccion, todo se revertiria al final y no se podria
 * distinguir una salida confirmada de una revertida.
 */
@SpringBootTest
class ProductoServiceTransaccionalTest {

    @Autowired
    private ProductoService service;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoStockRepository movimientoRepository;

    private Long productoId;

    @BeforeEach
    void prepararInventario() {
        movimientoRepository.deleteAll();
        productoRepository.deleteAll();
        productoId = productoRepository.save(
                new Producto("Laptop Lenovo ThinkPad", "Tecnologia", new BigDecimal("4200.00"), 8))
                .getId();
    }

    @Test
    void salidaExitosaDebeDescontarStockYRegistrarMovimiento() {
        service.registrarSalida(productoId, 2);

        assertThat(productoRepository.findById(productoId))
                .get()
                .extracting(Producto::getStock)
                .isEqualTo(6);
        assertThat(movimientoRepository.buscarPorProducto(productoId))
                .singleElement()
                .satisfies(movimiento -> {
                    assertThat(movimiento.getTipo()).isEqualTo("SALIDA");
                    assertThat(movimiento.getCantidad()).isEqualTo(2);
                });
    }

    @Test
    void entradaDebeIncrementarStockYRegistrarMovimiento() {
        service.registrarEntrada(productoId, 5);

        assertThat(productoRepository.findById(productoId))
                .get()
                .extracting(Producto::getStock)
                .isEqualTo(13);
        assertThat(movimientoRepository.buscarPorProducto(productoId))
                .singleElement()
                .extracting(movimiento -> movimiento.getTipo())
                .isEqualTo("ENTRADA");
    }

    @Test
    void errorSimuladoDebeRevertirStockYMovimiento() {
        assertThatThrownBy(() -> service.simularSalidaConError(productoId, 1))
                .isInstanceOf(IllegalStateException.class);

        // Nada quedo a medias: ni el descuento de stock ni el movimiento.
        assertThat(productoRepository.findById(productoId))
                .get()
                .extracting(Producto::getStock)
                .isEqualTo(8);
        assertThat(movimientoRepository.buscarPorProducto(productoId)).isEmpty();
    }

    @Test
    void salidaConStockInsuficienteDebeRechazarseSinTocarElInventario() {
        assertThatThrownBy(() -> service.registrarSalida(productoId, 99))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Stock insuficiente");

        assertThat(productoRepository.findById(productoId))
                .get()
                .extracting(Producto::getStock)
                .isEqualTo(8);
        assertThat(movimientoRepository.buscarPorProducto(productoId)).isEmpty();
    }

    @Test
    void rangoDePreciosInvertidoDebeRechazarse() {
        assertThatThrownBy(() -> service.buscarPorRango(new BigDecimal("2000"), new BigDecimal("100")))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void textoDeBusquedaVacioDebeRechazarse() {
        assertThatThrownBy(() -> service.buscarPorNombre("   "))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
