package com.utp.productosapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.utp.productosapi.model.Producto;

/**
 * Prueba de integracion de la capa de persistencia (seccion 18 de la guia).
 * Con Spring Boot 4.x, @DataJpaTest vive en
 * org.springframework.boot.data.jpa.test.autoconfigure y lo aporta el modulo
 * spring-boot-starter-data-jpa-test con alcance test.
 */
@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository repository;

    @Test
    void debeGuardarYRecuperarProducto() {
        Producto producto = new Producto(null, "Monitor", 900.0, 5);

        Producto guardado = repository.save(producto);

        assertThat(guardado.getId()).isNotNull();
        assertThat(repository.findById(guardado.getId())).isPresent();
    }

    @Test
    void debeActualizarYEliminarProducto() {
        Producto guardado = repository.save(new Producto(null, "Teclado", 120.0, 12, "Tecnologia"));

        guardado.setPrecio(150.0);
        repository.save(guardado);
        assertThat(repository.findById(guardado.getId()))
                .get()
                .extracting(Producto::getPrecio)
                .isEqualTo(150.0);

        repository.deleteById(guardado.getId());
        assertThat(repository.existsById(guardado.getId())).isFalse();
    }
}
