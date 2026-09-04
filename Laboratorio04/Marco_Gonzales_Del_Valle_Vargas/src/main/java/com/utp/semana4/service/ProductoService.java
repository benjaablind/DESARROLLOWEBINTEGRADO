package com.utp.semana4.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.utp.semana4.dto.ActualizarStockRequest;
import com.utp.semana4.dto.DisminuirStockRequest;
import com.utp.semana4.dto.ProductoRequest;
import com.utp.semana4.exception.ProductoNoEncontradoException;
import com.utp.semana4.exception.StockInsuficienteException;
import com.utp.semana4.model.Producto;

/**
 * Logica de negocio del catalogo. Es un bean singleton de Spring, por eso
 * el almacenamiento en memoria usa estructuras concurrentes: varias
 * solicitudes HTTP pueden ejecutarse al mismo tiempo.
 * En semanas posteriores este mapa se reemplaza por JPA, Hibernate y MySQL.
 */
@Service
public class ProductoService {

    private final Map<Long, Producto> productos = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(1);

    public ProductoService() {
        registrarInicial("Laptop Lenovo", "Tecnologia", 3500.00, 10);
        registrarInicial("Mouse Logitech", "Tecnologia", 80.00, 25);
        registrarInicial("Silla ergonomica", "Muebles", 750.00, 5);
    }

    /** Lista todos los productos; si se envia categoria, filtra sin distinguir mayusculas. */
    public List<Producto> listar(String categoria) {
        return productos.values()
                .stream()
                .filter(producto -> categoria == null
                        || producto.getCategoria().equalsIgnoreCase(categoria))
                .sorted(Comparator.comparing(Producto::getId))
                .toList();
    }

    public Producto buscarPorId(Long id) {
        Producto producto = productos.get(id);

        if (producto == null) {
            throw new ProductoNoEncontradoException(id);
        }

        return producto;
    }

    /** Ejercicio 1: productos cuyo nombre contiene el texto (sin distinguir mayusculas). */
    public List<Producto> buscarPorTexto(String texto) {
        String buscado = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);

        return productos.values()
                .stream()
                .filter(producto -> producto.getNombre().toLowerCase(Locale.ROOT).contains(buscado))
                .sorted(Comparator.comparing(Producto::getId))
                .toList();
    }

    public Producto crear(ProductoRequest request) {
        Long id = secuencia.getAndIncrement();
        Producto producto = new Producto(
                id,
                request.getNombre(),
                request.getCategoria(),
                request.getPrecio(),
                request.getStock());
        productos.put(id, producto);
        return producto;
    }

    /** PUT: reemplaza todos los campos del producto. */
    public Producto actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarPorId(id);
        producto.setNombre(request.getNombre());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        return producto;
    }

    /** PATCH: modifica unicamente el stock. */
    public Producto actualizarStock(Long id, ActualizarStockRequest request) {
        Producto producto = buscarPorId(id);
        producto.setStock(request.getStock());
        return producto;
    }

    /** Ejercicio 2: descuenta unidades; nunca permite que el stock quede negativo. */
    public Producto disminuirStock(Long id, DisminuirStockRequest request) {
        Producto producto = buscarPorId(id);
        int cantidad = request.getCantidad();

        // synchronized sobre el producto evita que dos descuentos simultaneos
        // lean el mismo stock y lo dejen negativo.
        synchronized (producto) {
            if (cantidad > producto.getStock()) {
                throw new StockInsuficienteException(id, producto.getStock(), cantidad);
            }
            producto.setStock(producto.getStock() - cantidad);
        }

        return producto;
    }

    public void eliminar(Long id) {
        Producto eliminado = productos.remove(id);

        if (eliminado == null) {
            throw new ProductoNoEncontradoException(id);
        }
    }

    private void registrarInicial(String nombre, String categoria, double precio, int stock) {
        Long id = secuencia.getAndIncrement();
        productos.put(id, new Producto(id, nombre, categoria, precio, stock));
    }
}
