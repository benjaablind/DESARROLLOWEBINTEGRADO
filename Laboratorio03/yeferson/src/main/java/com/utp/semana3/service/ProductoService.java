package com.utp.semana3.service;

import com.utp.semana3.model.Producto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {
    private final List<Producto> productos = new ArrayList<>();
    private Long secuencia = 1L;

    public Producto registrar(Producto producto) {
        validar(producto);
        producto.setId(secuencia++);
        productos.add(producto);
        return producto;
    }

    public List<Producto> listar() {
        return productos;
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productos.stream()
                .filter(producto -> producto.getId() != null && producto.getId().equals(id))
                .findFirst();
    }

    public boolean eliminar(Long id) {
        Optional<Producto> existente = buscarPorId(id);
        if (existente.isEmpty()) {
            return false;
        }
        productos.remove(existente.get());
        return true;
    }

    public Producto actualizar(Long id, Producto producto) {
        Producto existente = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        validar(producto);
        existente.setNombre(producto.getNombre());
        existente.setPrecio(producto.getPrecio());
        existente.setStock(producto.getStock());
        return existente;
    }

    private void validar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }
}
