package com.utp.semana3.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.utp.semana3.exception.ProductoNoEncontradoException;
import com.utp.semana3.model.Producto;

@Service
public class ProductoService {

    private final List<Producto> productos = new ArrayList<>();
    private long secuencia = 1;

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
                .filter(producto -> producto.getId().equals(id))
                .findFirst();
    }

    public Producto actualizar(Long id, Producto datos) {
        validar(datos);
        Producto existente = buscarPorId(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));

        existente.setNombre(datos.getNombre());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        return existente;
    }

    public void eliminar(Long id) {
        Producto existente = buscarPorId(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
        productos.remove(existente);
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
}
