package com.utp.odontologia.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.utp.odontologia.model.EstadoPaciente;
import com.utp.odontologia.model.Paciente;

/** Repositorio de pacientes (Integrante 2). */
public interface PacienteRepository extends RepositorioBase<Paciente> {

    Optional<Paciente> findByDni(String dni);

    List<Paciente> findByEstadoOrderById(EstadoPaciente estado);

    /**
     * JPQL: coincidencia parcial en el nombre completo o el DNI. Consulta la
     * entidad Paciente y sus atributos; Hibernate la traduce al SQL del motor.
     */
    @Query("""
            SELECT p
            FROM Paciente p
            WHERE LOWER(CONCAT(p.nombres, ' ', p.apellidos)) LIKE CONCAT('%', :texto, '%')
               OR p.dni LIKE CONCAT('%', :texto, '%')
            ORDER BY p.id
            """)
    List<Paciente> buscarCoincidencias(@Param("texto") String textoEnMinusculas);

    @Query("""
            SELECT p
            FROM Paciente p
            WHERE p.fechaRegistro >= :inicio AND p.fechaRegistro < :fin
            ORDER BY p.id
            """)
    List<Paciente> registradosEntre(@Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime finExclusivo);

    default Optional<Paciente> buscarPorDni(String dni) {
        return dni == null ? Optional.empty() : findByDni(dni);
    }

    /** Busca por coincidencia parcial en nombres, apellidos o DNI. */
    default List<Paciente> buscarPorTexto(String texto) {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return buscarCoincidencias(buscado);
    }

    default List<Paciente> listarPorEstado(EstadoPaciente estado) {
        return findByEstadoOrderById(estado);
    }

    /** Pacientes registrados dentro de un rango de fechas, inclusive. */
    default List<Paciente> listarPorRangoDeRegistro(LocalDate desde, LocalDate hasta) {
        return registradosEntre(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
    }
}
