package com.utp.productosapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.productosapi.dto.ReporteResponse;
import com.utp.productosapi.service.ProductoService;

/**
 * Reto de aplicacion (seccion 15.1). La restriccion a ROLE_ADMIN esta en
 * SecurityConfig (/api/admin/**); se conserva Controller -> Service -> Repository.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductoService service;

    public AdminController(ProductoService service) {
        this.service = service;
    }

    @GetMapping("/reporte")
    public ReporteResponse reporte() {
        return service.generarReporte();
    }
}
