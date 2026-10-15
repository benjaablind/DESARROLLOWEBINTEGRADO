package com.utp.productosapi.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Unico recurso accesible sin credenciales: sirve para comprobar que la
 * aplicacion esta operativa sin exponer datos de negocio.
 */
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    @GetMapping("/estado")
    public Map<String, String> estado() {
        return Map.of("estado", "API disponible");
    }
}
