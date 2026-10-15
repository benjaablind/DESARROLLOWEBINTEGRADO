package com.utp.productosapi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utp.productosapi.model.Producto;
import com.utp.productosapi.service.ProductoService;

/**
 * Actividad de consolidacion (seccion 17): modulo de inventario con tres
 * niveles de acceso.
 *
 * - GET /api/inventario/**     -> ROLE_USER o ROLE_ADMIN
 * - POST /api/inventario/ajustes -> solo ROLE_ADMIN
 *
 * Las reglas estan declaradas en SecurityConfig, no aqui: el Controller no
 * decide quien entra, solo atiende HTTP.
 */
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final ProductoService service;

    public InventarioController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Producto> existencias() {
        return service.listar();
    }

    @GetMapping("/stock-bajo")
    public List<Producto> stockBajo(@RequestParam Integer limite) {
        return service.buscarConStockBajo(limite);
    }

    @PostMapping("/ajustes")
    public ResponseEntity<Producto> ajustar(
            @RequestParam Long productoId,
            @RequestParam String tipo,
            @RequestParam int cantidad) {
        return ResponseEntity.ok(service.ajustarInventario(productoId, tipo, cantidad));
    }
}
