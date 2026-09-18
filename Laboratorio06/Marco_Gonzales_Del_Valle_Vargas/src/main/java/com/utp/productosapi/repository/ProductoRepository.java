package com.utp.productosapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.utp.productosapi.model.Producto;

/**
 * No se implementa manualmente. Spring Data JPA crea el objeto en tiempo de
 * ejecucion con save(), findAll(), findById(), existsById() y deleteById().
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
