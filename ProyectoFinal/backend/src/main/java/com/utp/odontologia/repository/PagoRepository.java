package com.utp.odontologia.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.odontologia.model.Pago;

/** Repositorio de pagos (Integrante 5). */
public interface PagoRepository extends RepositorioBase<Pago> {

    @Query("SELECT p FROM Pago p WHERE p.tratamientoId = :tratamientoId ORDER BY p.fecha, p.id")
    List<Pago> listarPorTratamiento(@Param("tratamientoId") Long tratamientoId);

    @Query("SELECT p FROM Pago p WHERE p.pacienteId = :pacienteId ORDER BY p.id")
    List<Pago> listarPorPaciente(@Param("pacienteId") Long pacienteId);

    /**
     * Filtros opcionales y combinables: un parametro null significa "sin ese
     * filtro". Reemplaza el filtrado en memoria de la semana 5.
     */
    @Query("""
            SELECT p
            FROM Pago p
            WHERE (:pacienteId IS NULL OR p.pacienteId = :pacienteId)
              AND (:tratamientoId IS NULL OR p.tratamientoId = :tratamientoId)
            ORDER BY p.id
            """)
    List<Pago> filtrar(@Param("pacienteId") Long pacienteId,
            @Param("tratamientoId") Long tratamientoId);

    @Query("""
            SELECT p
            FROM Pago p
            WHERE p.fecha >= :inicio AND p.fecha < :fin
            ORDER BY p.id
            """)
    List<Pago> registradosEntre(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime finExclusivo);

    /**
     * JPQL con funcion de agregacion: la suma la calcula la base de datos en
     * lugar de traer todos los pagos a memoria. COALESCE devuelve 0 cuando el
     * tratamiento todavia no tiene pagos (SUM de nada es NULL).
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.tratamientoId = :tratamientoId")
    BigDecimal totalPagadoDeTratamiento(@Param("tratamientoId") Long tratamientoId);

    @Query("""
            SELECT COALESCE(SUM(p.monto), 0)
            FROM Pago p
            WHERE p.fecha >= :inicio AND p.fecha < :fin
            """)
    BigDecimal sumaRegistradaEntre(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime finExclusivo);

    /**
     * JPQL con GROUP BY y expresion constructora: un total por metodo de pago,
     * calculado por la base de datos (reporte de ingresos, Integrante 6).
     */
    @Query("""
            SELECT new com.utp.odontologia.repository.TotalPorMetodo(p.metodo, SUM(p.monto))
            FROM Pago p
            WHERE p.fecha >= :inicio AND p.fecha < :fin
            GROUP BY p.metodo
            ORDER BY p.metodo
            """)
    List<TotalPorMetodo> totalesPorMetodoEntre(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime finExclusivo);

    /** Ingresos cobrados entre dos fechas, inclusive, sumados por la base de datos. */
    default BigDecimal totalEntre(LocalDate desde, LocalDate hasta) {
        return sumaRegistradaEntre(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }

    default List<Pago> listarEntre(LocalDate desde, LocalDate hasta) {
        return registradosEntre(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }
}
