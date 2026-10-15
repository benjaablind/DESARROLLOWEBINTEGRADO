package com.utp.productosapi.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.productosapi.config.SecurityConfig;
import com.utp.productosapi.service.ProductoService;

/**
 * Actividad de consolidacion, punto 5: evidencia automatizada de un 401 y un
 * 403 sobre el modulo de inventario.
 */
@WebMvcTest(InventarioController.class)
@Import(SecurityConfig.class)
class InventarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void consultarInventarioSinAutenticacion_debeResponder401() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void consultarInventarioComoUser_debeResponder200() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void ajustarInventarioComoUser_debeResponder403() throws Exception {
        mockMvc.perform(post("/api/inventario/ajustes")
                .param("productoId", "1")
                .param("tipo", "ENTRADA")
                .param("cantidad", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void ajustarInventarioComoAdmin_debeResponder200() throws Exception {
        mockMvc.perform(post("/api/inventario/ajustes")
                .param("productoId", "1")
                .param("tipo", "ENTRADA")
                .param("cantidad", "5"))
                .andExpect(status().isOk());
    }
}
