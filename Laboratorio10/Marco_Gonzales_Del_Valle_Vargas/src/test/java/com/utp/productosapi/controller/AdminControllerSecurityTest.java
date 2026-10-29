package com.utp.productosapi.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.productosapi.config.SecurityConfig;
import com.utp.productosapi.dto.ResumenBackendResponse;
import com.utp.productosapi.service.AdminResumenService;
import com.utp.productosapi.service.ProductoService;

/**
 * Semana 10 (guia, seccion 14): contrato de seguridad del resumen
 * administrativo, sin depender de un token real emitido por /login.
 * Spring Security Test fabrica la autenticacion JWT con jwt().
 *
 * Como indica el recuadro "Si tu test no arranca", el slice necesita
 * @Import(SecurityConfig.class) y mocks del UserDetailsService y del
 * JwtDecoder que SecurityConfig espera encontrar.
 */
@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminResumenService resumenService;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void adminPuedeConsultarResumen() throws Exception {
        when(resumenService.obtenerResumen())
                .thenReturn(new ResumenBackendResponse(5, 1, 3, "BACKEND_OPERATIVO"));

        mockMvc.perform(get("/api/admin/resumen")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(5))
                .andExpect(jsonPath("$.productosConStockBajo").value(1))
                .andExpect(jsonPath("$.totalMovimientos").value(3))
                .andExpect(jsonPath("$.estado").value("BACKEND_OPERATIVO"));
    }

    @Test
    void userNoPuedeConsultarResumen() throws Exception {
        mockMvc.perform(get("/api/admin/resumen")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenNoPuedeConsultarResumen() throws Exception {
        mockMvc.perform(get("/api/admin/resumen"))
                .andExpect(status().isUnauthorized());
    }
}
