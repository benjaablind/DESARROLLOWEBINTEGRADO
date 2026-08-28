package com.example.laboratorio3.controller;

import com.example.laboratorio3.model.Producto;
import com.example.laboratorio3.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @Test
    void listar_debeRetornarProductosEnJson() throws Exception {
        when(productoService.listar()).thenReturn(List.of(
                new Producto(1L, "Laptop", 3500.00, 10),
                new Producto(2L, "Mouse", 80.00, 20)
        ));

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laptop"))
                .andExpect(jsonPath("$[1].nombre").value("Mouse"));
    }

    @Test
    void registrar_debeRetornarProductoCreado() throws Exception {
        Producto productoRegistrado = new Producto(1L, "Laptop", 3500.00, 10);

        when(productoService.registrar(any(Producto.class))).thenReturn(productoRegistrado);

        String json = """
                {
                  "nombre": "Laptop",
                  "precio": 3500.00,
                  "stock": 10
                }
                """;

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"));
    }

    @Test
    void buscarPorIdCuandoNoExiste_debeRetornar404() throws Exception {
        when(productoService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorIdCuandoExiste_debeRetornar200ConElProducto() throws Exception {
        when(productoService.buscarPorId(1L))
                .thenReturn(Optional.of(new Producto(1L, "Laptop", 3500.00, 10)));

        mockMvc.perform(get("/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.precio").value(3500.00))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void registrarProductoInvalido_debeRetornar400ConElMensaje() throws Exception {
        when(productoService.registrar(any(Producto.class)))
                .thenThrow(new IllegalArgumentException("El precio debe ser mayor que cero"));

        String json = """
                {
                  "nombre": "Mouse",
                  "precio": 0.00,
                  "stock": 5
                }
                """;

        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El precio debe ser mayor que cero"));
    }

    @Test
    void eliminarProductoExistente_debeRetornar204() throws Exception {
        when(productoService.eliminar(1L)).thenReturn(true);

        mockMvc.perform(delete("/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarProductoInexistente_debeRetornar404() throws Exception {
        when(productoService.eliminar(99L)).thenReturn(false);

        mockMvc.perform(delete("/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProductoExistente_debeRetornar200ConElProducto() throws Exception {
        when(productoService.actualizar(eq(1L), any(Producto.class)))
                .thenReturn(Optional.of(new Producto(1L, "Laptop Pro", 4200.00, 7)));

        String json = """
                {
                  "nombre": "Laptop Pro",
                  "precio": 4200.00,
                  "stock": 7
                }
                """;

        mockMvc.perform(put("/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop Pro"))
                .andExpect(jsonPath("$.precio").value(4200.00));
    }

    @Test
    void actualizarProductoInexistente_debeRetornar404() throws Exception {
        when(productoService.actualizar(eq(99L), any(Producto.class)))
                .thenReturn(Optional.empty());

        String json = """
                {
                  "nombre": "Laptop Pro",
                  "precio": 4200.00,
                  "stock": 7
                }
                """;

        mockMvc.perform(put("/productos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}
