package com.utp.odontologia.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.Tratamiento;

/** Repositorio de tratamientos (Integrante 5). */
public interface TratamientoRepository extends RepositorioBase<Tratamiento> {

    List<Tratamiento> findByEstadoOrderById(EstadoTratamiento estado);

    /**
     * Lee el tratamiento bloqueando su fila hasta el fin de la transaccion
     * (SELECT ... FOR UPDATE). Lo usa el registro de pagos: si dos cobros al
     * mismo tratamiento llegan a la vez, el segundo espera a que el primero
     * confirme y recien entonces calcula el saldo, asi nunca se cobra de mas.
     *
     * Es SQL nativo y no JPQL con @Lock a proposito: con @Lock, Hibernate
     * genera "FOR UPDATE OF t1_0" (sintaxis de MySQL 8), que el MariaDB de
     * XAMPP no acepta. Un "FOR UPDATE" simple funciona igual en MySQL, MariaDB
     * y H2. Es el caso en que SQL nativo se justifica: una caracteristica
     * propia del motor (guia 07).
     */
    @Query(value = "SELECT * FROM tratamientos WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Tratamiento> buscarParaActualizar(@Param("id") Long id);

    @Query("SELECT t FROM Tratamiento t WHERE t.pacienteId = :pacienteId ORDER BY t.id")
    List<Tratamiento> listarPorPaciente(@Param("pacienteId") Long pacienteId);

    @Query("SELECT t FROM Tratamiento t WHERE t.estado IN :estados ORDER BY t.id")
    List<Tratamiento> listarPorEstados(@Param("estados") Collection<EstadoTratamiento> estados);

    @Query("""
            SELECT t
            FROM Tratamiento t
            WHERE t.estado = :estado AND t.fechaFin BETWEEN :desde AND :hasta
            ORDER BY t.id
            """)
    List<Tratamiento> listarPorEstadoYFinEntre(@Param("estado") EstadoTratamiento estado,
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    default List<Tratamiento> listarPorEstado(EstadoTratamiento estado) {
        return findByEstadoOrderById(estado);
    }

    /** Tratamientos pendientes, aprobados o en proceso. */
    default List<Tratamiento> listarActivos() {
        return listarPorEstados(List.of(
                EstadoTratamiento.PENDIENTE, EstadoTratamiento.APROBADO, EstadoTratamiento.EN_PROCESO));
    }

    default List<Tratamiento> listarCompletadosEntre(LocalDate desde, LocalDate hasta) {
        return listarPorEstadoYFinEntre(EstadoTratamiento.COMPLETADO, desde, hasta);
    }
}
