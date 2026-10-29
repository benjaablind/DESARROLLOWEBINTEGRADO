package com.utp.productosapi.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.utp.productosapi.service.ProductoService;

/**
 * Modulo de inventario de la semana 8, con identidad JWT.
 */
@WebMvcTest(InventarioController.class)
@Import(SecurityConfig.class)
class InventarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void consultarInventarioSinToken_debeResponder401() throws Exception {
        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void consultarInventarioConTokenUser_debeResponder200() throws Exception {
        mockMvc.perform(get("/api/inventario")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void ajustarInventarioConTokenUser_debeResponder403() throws Exception {
        mockMvc.perform(post("/api/inventario/ajustes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .param("productoId", "1")
                .param("tipo", "ENTRADA")
                .param("cantidad", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void ajustarInventarioConTokenAdmin_debeResponder200() throws Exception {
        mockMvc.perform(post("/api/inventario/ajustes")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .param("productoId", "1")
                .param("tipo", "ENTRADA")
                .param("cantidad", "5"))
                .andExpect(status().isOk());
    }
}
