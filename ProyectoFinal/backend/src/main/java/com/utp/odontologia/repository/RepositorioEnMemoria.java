package com.utp.odontologia.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Base comun de todos los repositorios de esta etapa del curso.
 *
 * Guarda las entidades en un ConcurrentHashMap y genera los identificadores con
 * un AtomicLong, porque varias solicitudes HTTP pueden ejecutarse a la vez sobre
 * el mismo bean singleton.
 *
 * En las semanas 6 a 10 cada repositorio concreto pasara a extender
 * JpaRepository y esta clase desaparecera sin que los servicios cambien.
 *
 * @param <T> tipo de la entidad administrada
 */
public abstract class RepositorioEnMemoria<T> {

    protected final Map<Long, T> datos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    /** Devuelve el id de la entidad, o null si aun no fue asignado. */
    protected abstract Long obtenerId(T entidad);

    /** Asigna el id generado a la entidad. */
    protected abstract void asignarId(T entidad, Long id);

    /** Inserta la entidad si no tiene id, o la reemplaza si ya lo tiene. */
    public T guardar(T entidad) {
        if (obtenerId(entidad) == null) {
            asignarId(entidad, secuencia.getAndIncrement());
        }
        datos.put(obtenerId(entidad), entidad);
        return entidad;
    }

    public Optional<T> buscarPorId(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(datos.get(id));
    }

    /** Lista todas las entidades ordenadas por id. */
    public List<T> listar() {
        List<T> resultado = new ArrayList<>(datos.values());
        resultado.sort(Comparator.comparing(this::obtenerId));
        return resultado;
    }

    /** Lista las entidades que cumplen el filtro, ordenadas por id. */
    public List<T> listarSi(Predicate<T> filtro) {
        return listar().stream().filter(filtro).toList();
    }

    public boolean eliminar(Long id) {
        return id != null && datos.remove(id) != null;
    }

    public boolean existe(Long id) {
        return id != null && datos.containsKey(id);
    }

    public long contar() {
        return datos.size();
    }

    /** Vacia el repositorio. Se usa para dejar las pruebas en un estado conocido. */
    public void limpiar() {
        datos.clear();
        secuencia.set(1);
    }
}
