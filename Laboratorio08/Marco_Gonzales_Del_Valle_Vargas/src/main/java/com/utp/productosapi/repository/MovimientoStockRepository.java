package com.utp.productosapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.productosapi.model.MovimientoStock;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {

    // JPQL sobre la relacion: se navega por el atributo producto, no por producto_id.
    @Query("""
            SELECT m
            FROM MovimientoStock m
            WHERE m.producto.id = :productoId
            ORDER BY m.fecha DESC
            """)
    List<MovimientoStock> buscarPorProducto(@Param("productoId") Long productoId);
}
