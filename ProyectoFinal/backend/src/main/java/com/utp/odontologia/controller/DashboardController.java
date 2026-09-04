package com.utp.odontologia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.odontologia.dto.DashboardResponse;
import com.utp.odontologia.service.DashboardService;

/**
 * API REST del tablero de control (Integrante 3).
 * Devuelve en una sola llamada los indicadores de la pantalla de inicio.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** GET /api/dashboard: indicadores, listas de detalle y alertas. */
    @GetMapping
    public ResponseEntity<DashboardResponse> obtener() {
        return ResponseEntity.ok(dashboardService.obtener());
    }
}
