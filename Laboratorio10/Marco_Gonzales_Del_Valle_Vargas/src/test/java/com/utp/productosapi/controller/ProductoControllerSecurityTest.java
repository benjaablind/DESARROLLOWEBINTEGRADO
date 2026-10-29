package com.utp.productosapi.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.productosapi.config.SecurityConfig;
import com.utp.productosapi.service.ProductoService;

/**
 * Reglas por rol de la semana 8, ahora con identidad JWT.
 *
 * jwt() simula un Bearer Token ya verificado con las autoridades indicadas;
 * la verificacion criptografica real se prueba en JwtFlujoCompletoTest.
 * UserDetailsService y JwtDecoder se mockean porque SecurityConfig los
 * necesita y el slice @WebMvcTest no los carga.
 */
@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class ProductoControllerSecurityTest {

    private static final SimpleGrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");
    private static final SimpleGrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void listarSinToken_debeResponder401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarConTokenUser_debeResponder200() throws Exception {
        given(productoService.listar()).willReturn(List.of());

        mockMvc.perform(get("/api/productos").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarConTokenUser_debeResponder403() throws Exception {
        mockMvc.perform(delete("/api/productos/1").with(jwt().authorities(ROLE_USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearConTokenUser_debeResponder403() throws Exception {
        mockMvc.perform(post("/api/productos")
                .with(jwt().authorities(ROLE_USER))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Audifonos USB\",\"categoria\":\"Tecnologia\",\"precio\":190.00,\"stock\":7}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void eliminarConTokenAdmin_debeResponder204() throws Exception {
        given(productoService.eliminar(anyLong())).willReturn(true);

        mockMvc.perform(delete("/api/productos/1").with(jwt().authorities(ROLE_ADMIN)))
                .andExpect(status().isNoContent());
    }
}
