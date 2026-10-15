package com.utp.productosapi.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.productosapi.model.Producto;

/**
 * Las consultas JPQL nombran la entidad (Producto) y sus atributos
 * (p.nombre, p.precio, p.stock), nunca la tabla ni las columnas de MySQL:
 * Hibernate se encarga de traducirlas a SQL.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Consulta derivada: Spring Data interpreta el nombre del metodo.
    List<Producto> findByCategoriaIgnoreCase(String categoria);

    // JPQL: busqueda parcial sin distinguir mayusculas/minusculas.
    @Query("""
            SELECT p
            FROM Producto p
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY p.nombre
            """)
    List<Producto> buscarPorNombre(@Param("texto") String texto);

    // JPQL: rango de precios.
    @Query("""
            SELECT p
            FROM Producto p
            WHERE p.precio BETWEEN :min AND :max
            ORDER BY p.precio ASC
            """)
    List<Producto> buscarPorRangoPrecio(
            @Param("min") BigDecimal min,
            @Param("max") BigDecimal max);

    // JPQL adicional: productos con stock bajo.
    @Query("SELECT p FROM Producto p WHERE p.stock <= :limite ORDER BY p.stock ASC")
    List<Producto> buscarConStockBajo(@Param("limite") Integer limite);
}
