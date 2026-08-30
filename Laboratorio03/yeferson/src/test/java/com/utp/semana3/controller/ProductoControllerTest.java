package com.utp.semana3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.semana3.model.Producto;
import com.utp.semana3.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void listarProductos_debeRetornarListaJson() throws Exception {
        when(productoService.listar()).thenReturn(List.of(new Producto(1L, "Teclado", 199.9, 5)));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Teclado"));
    }

    @Test
    void registrarProducto_debeRetornar201YJsonCreado() throws Exception {
        Producto producto = new Producto(null, "Mouse", 59.9, 10);
        Producto creado = new Producto(1L, "Mouse", 59.9, 10);

        when(productoService.registrar(any(Producto.class))).thenReturn(creado);

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Mouse"));
    }

    @Test
    void buscarProductoPorIdCuandoNoExiste_debeRetornar404() throws Exception {
        when(productoService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarProductoPorIdCuandoExiste_debeRetornar200YJson() throws Exception {
        Producto producto = new Producto(1L, "Monitor", 399.9, 3);
        when(productoService.buscarPorId(1L)).thenReturn(Optional.of(producto));

        mockMvc.perform(get("/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Monitor"));
    }

    @Test
    void eliminarProductoExistente_debeRetornar204() throws Exception {
        when(productoService.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void actualizarProductoExistente_debeRetornar200YJsonActualizado() throws Exception {
        Producto actualizado = new Producto(1L, "Teclado Gamer", 249.9, 8);
        when(productoService.actualizar(eq(1L), any(Producto.class))).thenReturn(actualizado);

        mockMvc.perform(put("/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Producto(null, "Teclado Gamer", 249.9, 8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Teclado Gamer"));
    }
}
