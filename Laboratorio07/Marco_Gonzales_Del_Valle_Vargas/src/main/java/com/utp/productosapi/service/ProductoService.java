package com.utp.productosapi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utp.productosapi.exception.RecursoNoEncontradoException;
import com.utp.productosapi.exception.ReglaNegocioException;
import com.utp.productosapi.model.MovimientoStock;
import com.utp.productosapi.model.Producto;
import com.utp.productosapi.repository.MovimientoStockRepository;
import com.utp.productosapi.repository.ProductoRepository;

/**
 * Aqui vive la frontera transaccional: el Controller solo atiende HTTP y el
 * Repository solo accede a datos. Las consultas se marcan readOnly = true y
 * las operaciones de inventario se ejecutan como una unidad de trabajo.
 */
@Service
public class ProductoService {

    private static final String CATEGORIA_POR_DEFECTO = "General";
    private static final String TIPO_SALIDA = "SALIDA";
    private static final String TIPO_ENTRADA = "ENTRADA";

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;

    public ProductoService(ProductoRepository productoRepository,
            MovimientoStockRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    // ------------------------------------------------------------------
    // CRUD heredado de la semana 6
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto crear(Producto producto) {
        validar(producto);
        producto.setId(null);
        producto.setCategoria(normalizarCategoria(producto.getCategoria()));
        return productoRepository.save(producto);
    }

    @Transactional
    public Optional<Producto> actualizar(Long id, Producto datos) {
        validar(datos);
        return productoRepository.findById(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setPrecio(datos.getPrecio());
            existente.setStock(datos.getStock());
            existente.setCategoria(normalizarCategoria(datos.getCategoria()));
            return existente;
        });
    }

    @Transactional
    public Optional<Producto> actualizarPrecio(Long id, BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor que cero");
        }
        return productoRepository.findById(id).map(existente -> {
            existente.setPrecio(precio);
            return existente;
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }

    // ------------------------------------------------------------------
    // Consultas de la semana 7 (derivadas y JPQL)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String texto) {
        // Actividad de consolidacion 2: el texto no puede venir vacio.
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("El texto de busqueda no puede estar vacio");
        }
        return productoRepository.buscarPorNombre(texto.trim());
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorRango(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) > 0) {
            throw new ReglaNegocioException("El precio minimo no puede superar al maximo");
        }
        return productoRepository.buscarPorRangoPrecio(min, max);
    }

    // Actividad de consolidacion 1.
    @Transactional(readOnly = true)
    public List<Producto> buscarConStockBajo(Integer limite) {
        if (limite == null || limite < 0) {
            throw new ReglaNegocioException("El limite no puede ser negativo");
        }
        return productoRepository.buscarConStockBajo(limite);
    }

    // ------------------------------------------------------------------
    // Operaciones transaccionales de inventario
    // ------------------------------------------------------------------

    /**
     * Descuenta stock y registra el movimiento dentro de la misma transaccion.
     * El UPDATE del producto no necesita save(): la entidad sigue administrada
     * por el contexto de persistencia y Hibernate lo detecta por dirty checking.
     */
    @Transactional
    public Producto registrarSalida(Long productoId, int cantidad) {
        Producto producto = obtenerProducto(productoId);
        validarSalida(producto, cantidad);

        producto.setStock(producto.getStock() - cantidad);
        movimientoRepository.save(new MovimientoStock(
                producto, TIPO_SALIDA, cantidad, LocalDateTime.now()));

        return producto;
    }

    // Actividad de consolidacion 3: la entrada incrementa el stock.
    @Transactional
    public Producto registrarEntrada(Long productoId, int cantidad) {
        Producto producto = obtenerProducto(productoId);
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }

        producto.setStock(producto.getStock() + cantidad);
        movimientoRepository.save(new MovimientoStock(
                producto, TIPO_ENTRADA, cantidad, LocalDateTime.now()));

        return producto;
    }

    /**
     * Identica a registrarSalida(), pero lanza una RuntimeException al final.
     * Spring revierte la transaccion completa: ni el stock ni el movimiento
     * quedan guardados. Sirve para comprobar el rollback.
     */
    @Transactional
    public void simularSalidaConError(Long productoId, int cantidad) {
        Producto producto = obtenerProducto(productoId);
        validarSalida(producto, cantidad);

        producto.setStock(producto.getStock() - cantidad);
        movimientoRepository.save(new MovimientoStock(
                producto, TIPO_SALIDA, cantidad, LocalDateTime.now()));

        throw new IllegalStateException("Error simulado: la transaccion debe hacer rollback");
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + id));
    }

    private void validarSalida(Producto producto, int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
        }
        if (producto.getStock() < cantidad) {
            throw new ReglaNegocioException("Stock insuficiente. Disponible: " + producto.getStock());
        }
    }

    private void validar(Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre es obligatorio");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor que cero");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new ReglaNegocioException("El stock no puede ser negativo");
        }
    }

    private String normalizarCategoria(String categoria) {
        return (categoria == null || categoria.isBlank()) ? CATEGORIA_POR_DEFECTO : categoria.trim();
    }
}
