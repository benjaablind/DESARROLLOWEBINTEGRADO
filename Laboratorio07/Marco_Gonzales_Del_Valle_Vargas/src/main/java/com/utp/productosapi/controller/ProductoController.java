package com.utp.productosapi.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utp.productosapi.model.Producto;
import com.utp.productosapi.service.ProductoService;

/**
 * El Controller no lleva @Transactional: la frontera de negocio pertenece al
 * Service. Aqui solo se traduce HTTP a llamadas de servicio.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    // ---------------- CRUD (semana 6) ----------------

    @GetMapping
    public List<Producto> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto creado = service.crear(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @RequestBody Producto producto) {
        return service.actualizar(id, producto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/precio")
    public ResponseEntity<Producto> actualizarPrecio(
            @PathVariable Long id,
            @RequestParam BigDecimal valor) {
        return service.actualizarPrecio(id, valor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return service.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // ---------------- Consultas JPQL (semana 7) ----------------

    @GetMapping("/buscar")
    public List<Producto> buscarPorNombre(@RequestParam String texto) {
        return service.buscarPorNombre(texto);
    }

    @GetMapping("/categoria/{categoria}")
    public List<Producto> buscarPorCategoria(@PathVariable String categoria) {
        return service.buscarPorCategoria(categoria);
    }

    @GetMapping("/precio")
    public List<Producto> buscarPorRango(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return service.buscarPorRango(min, max);
    }

    // Actividad de consolidacion 1.
    @GetMapping("/stock-bajo")
    public List<Producto> buscarConStockBajo(@RequestParam Integer limite) {
        return service.buscarConStockBajo(limite);
    }

    // ---------------- Operaciones transaccionales ----------------

    @PostMapping("/{id}/salidas")
    public ResponseEntity<Producto> registrarSalida(
            @PathVariable Long id,
            @RequestParam int cantidad) {
        return ResponseEntity.ok(service.registrarSalida(id, cantidad));
    }

    // Actividad de consolidacion 3.
    @PostMapping("/{id}/entradas")
    public ResponseEntity<Producto> registrarEntrada(
            @PathVariable Long id,
            @RequestParam int cantidad) {
        return ResponseEntity.ok(service.registrarEntrada(id, cantidad));
    }

    @PostMapping("/{id}/salidas/simular-error")
    public ResponseEntity<Void> simularError(
            @PathVariable Long id,
            @RequestParam int cantidad) {
        service.simularSalidaConError(id, cantidad);
        return ResponseEntity.noContent().build();
    }
}
