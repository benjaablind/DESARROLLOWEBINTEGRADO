package com.utp.productosapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.productosapi.model.Producto;
import com.utp.productosapi.repository.ProductoRepository;

/**
 * Semana 5: administraba una coleccion en memoria (ConcurrentHashMap + AtomicLong).
 * Semana 6: delega la persistencia al Repository; la logica de negocio no cambia.
 */
@Service
public class ProductoService {

    private static final String CATEGORIA_POR_DEFECTO = "General";

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Producto crear(Producto producto) {
        validar(producto);
        producto.setId(null);
        producto.setCategoria(normalizarCategoria(producto.getCategoria()));
        return repository.save(producto);
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto datos) {
        validar(datos);
        return repository.findById(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setPrecio(datos.getPrecio());
            existente.setStock(datos.getStock());
            existente.setCategoria(normalizarCategoria(datos.getCategoria()));
            return repository.save(existente);
        });
    }

    @Transactional
    public Optional<Producto> actualizarPrecio(Long id, double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        return repository.findById(id).map(existente -> {
            existente.setPrecio(precio);
            return repository.save(existente);
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private void validar(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    /**
     * La columna categoria es NOT NULL. Si el cliente no la envia (como en los
     * ejemplos de Postman de la guia) se usa un valor por defecto en lugar de
     * dejar que falle el INSERT en MySQL.
     */
    private String normalizarCategoria(String categoria) {
        return (categoria == null || categoria.isBlank()) ? CATEGORIA_POR_DEFECTO : categoria.trim();
    }
}
