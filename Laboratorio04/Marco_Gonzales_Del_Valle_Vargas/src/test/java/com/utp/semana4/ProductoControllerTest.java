package com.utp.semana4;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de la API con MockMvc. Levanta el contexto completo de Spring pero
 * no abre un puerto real: las solicitudes se simulan en memoria.
 *
 * El servicio es un singleton compartido entre pruebas, por eso las pruebas
 * que modifican datos crean primero su propio producto en lugar de tocar
 * los ids 1, 2 y 3 precargados.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- Pruebas de la guia (seccion 24) ----------

    @Test
    void debeListarProductos() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void debeCrearProducto() throws Exception {
        String json = """
                {
                   "nombre": "Tablet Xiaomi",
                   "categoria": "Tecnologia",
                   "precio": 1200.0,
                   "stock": 7
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/productos/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Tablet Xiaomi"));
    }

    // ---------- Consultas ----------

    @Test
    void debeBuscarProductoPorId() throws Exception {
        mockMvc.perform(get("/api/productos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Mouse Logitech"));
    }

    @Test
    void debeFiltrarPorCategoria() throws Exception {
        mockMvc.perform(get("/api/productos").param("categoria", "muebles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoria").value("Muebles"));
    }

    @Test
    void debeDevolver404SiElProductoNoExiste() throws Exception {
        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe un producto con id: 999"))
                .andExpect(jsonPath("$.ruta").value("/api/productos/999"))
                .andExpect(jsonPath("$.fechaHora").exists());
    }

    // ---------- Validaciones ----------

    @Test
    void debeDevolver400SiLosDatosSonInvalidos() throws Exception {
        String json = """
                {
                   "nombre": "",
                   "categoria": "Tecnologia",
                   "precio": -50,
                   "stock": -1
                }
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value(containsString("nombre: El nombre es obligatorio")))
                .andExpect(jsonPath("$.mensaje").value(containsString("precio: El precio debe ser mayor a cero")))
                .andExpect(jsonPath("$.mensaje").value(containsString("stock: El stock no puede ser negativo")));
    }

    @Test
    void debeDevolver400SiElIdNoEsNumerico() throws Exception {
        mockMvc.perform(get("/api/productos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    // ---------- PUT / PATCH / DELETE ----------

    @Test
    void debeActualizarProductoCompletoConPut() throws Exception {
        long id = crearProducto("Teclado Redragon", "Tecnologia", 150.0, 3);

        String json = """
                {
                   "nombre": "Teclado Redragon K552",
                   "categoria": "Perifericos",
                   "precio": 199.9,
                   "stock": 12
                }
                """;

        mockMvc.perform(put("/api/productos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Teclado Redragon K552"))
                .andExpect(jsonPath("$.categoria").value("Perifericos"))
                .andExpect(jsonPath("$.precio").value(199.9))
                .andExpect(jsonPath("$.stock").value(12));
    }

    @Test
    void debeActualizarSoloElStockConPatch() throws Exception {
        long id = crearProducto("Parlante JBL", "Audio", 300.0, 4);

        mockMvc.perform(patch("/api/productos/" + id + "/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.nombre").value("Parlante JBL"))
                .andExpect(jsonPath("$.precio").value(300.0));
    }

    @Test
    void debeEliminarProductoYLuegoDevolver404() throws Exception {
        long id = crearProducto("Producto temporal", "Otros", 10.0, 1);

        mockMvc.perform(delete("/api/productos/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/productos/" + id))
                .andExpect(status().isNotFound());
    }

    // ---------- Ejercicio 1: busqueda por texto ----------

    @Test
    void debeBuscarProductosPorTextoParcial() throws Exception {
        mockMvc.perform(get("/api/productos/buscar").param("texto", "LAP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value(containsString("Laptop")));
    }

    // ---------- Ejercicio 2: disminuir stock ----------

    @Test
    void debeDisminuirStock() throws Exception {
        long id = crearProducto("Cable HDMI", "Accesorios", 25.0, 10);

        mockMvc.perform(patch("/api/productos/" + id + "/disminuir-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": 4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(6));
    }

    @Test
    void debeDevolver400SiElStockQuedariaNegativo() throws Exception {
        long id = crearProducto("Webcam Logitech", "Accesorios", 180.0, 2);

        mockMvc.perform(patch("/api/productos/" + id + "/disminuir-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value(containsString("Stock insuficiente")));

        // El stock no debe haber cambiado
        mockMvc.perform(get("/api/productos/" + id))
                .andExpect(jsonPath("$.stock").value(2));
    }

    // ---------- Utilitario ----------

    private long crearProducto(String nombre, String categoria, double precio, int stock)
            throws Exception {
        String json = """
                {
                   "nombre": "%s",
                   "categoria": "%s",
                   "precio": %s,
                   "stock": %d
                }
                """.formatted(nombre, categoria, precio, stock);

        String body = mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // El JSON empieza con {"id":N,...}
        String idTexto = body.replaceAll("^\\{\"id\":(\\d+).*$", "$1");
        return Long.parseLong(idTexto);
    }
}
