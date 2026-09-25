package com.ejemplo.laboratorio07.repository;

import com.ejemplo.laboratorio07.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE p.precio > :precioMin")
    List<Producto> findProductosPorPrecioMayorA(@Param("precioMin") Double precioMin);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
