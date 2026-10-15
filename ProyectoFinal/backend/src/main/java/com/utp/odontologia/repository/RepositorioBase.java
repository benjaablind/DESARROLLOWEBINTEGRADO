package com.utp.odontologia.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base comun de todos los repositorios (semana 6).
 *
 * Hasta la semana 5 los repositorios extendian RepositorioEnMemoria, que guardaba
 * las entidades en un ConcurrentHashMap. Ahora extienden JpaRepository y Spring
 * Data genera la implementacion contra la base de datos.
 *
 * Los metodos por defecto conservan los nombres que ya usaban los servicios
 * (guardar, buscarPorId, listar...) y los traducen a la API de JpaRepository
 * (save, findById, findAll...). Gracias a eso, el cambio de almacenamiento no
 * obligo a reescribir los servicios: esa era la promesa de la arquitectura por
 * capas desde la semana 2.
 *
 * @NoRepositoryBean evita que Spring intente crear un repositorio para esta
 * interfaz generica: solo se instancian las concretas.
 *
 * @param <T> tipo de la entidad administrada
 */
@NoRepositoryBean
public interface RepositorioBase<T> extends JpaRepository<T, Long> {

    /** Inserta la entidad si no tiene id, o la actualiza si ya lo tiene. */
    default T guardar(T entidad) {
        return save(entidad);
    }

    default Optional<T> buscarPorId(Long id) {
        return id == null ? Optional.empty() : findById(id);
    }

    /** Lista todas las entidades ordenadas por id. */
    default List<T> listar() {
        return findAll(Sort.by("id"));
    }

    /**
     * Filtra en memoria sobre listar(). Se conserva para los filtros que
     * dependen de calculos en Java; las busquedas frecuentes tienen su propia
     * consulta en cada repositorio.
     */
    default List<T> listarSi(Predicate<T> filtro) {
        return listar().stream().filter(filtro).toList();
    }

    default boolean eliminar(Long id) {
        if (!existe(id)) {
            return false;
        }
        deleteById(id);
        return true;
    }

    default boolean existe(Long id) {
        return id != null && existsById(id);
    }

    default long contar() {
        return count();
    }
}
