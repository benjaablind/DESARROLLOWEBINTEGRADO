package com.utp.productosapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.productosapi.dto.ReporteResponse;
import com.utp.productosapi.dto.ResumenBackendResponse;
import com.utp.productosapi.service.AdminResumenService;
import com.utp.productosapi.service.ProductoService;

/**
 * Endpoints de administracion. La restriccion a ROLE_ADMIN esta en
 * SecurityConfig (/api/admin/**); se conserva Controller -> Service -> Repository.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ProductoService service;
    private final AdminResumenService resumenService;

    public AdminController(ProductoService service, AdminResumenService resumenService) {
        this.service = service;
        this.resumenService = resumenService;
    }

    /** Semana 9, reto 15.1. */
    @GetMapping("/reporte")
    public ReporteResponse reporte() {
        return service.generarReporte();
    }

    /** Semana 10 (guia, seccion 8.3): resumen administrativo integrado. */
    @GetMapping("/resumen")
    public ResumenBackendResponse resumen() {
        return resumenService.obtenerResumen();
    }
}
