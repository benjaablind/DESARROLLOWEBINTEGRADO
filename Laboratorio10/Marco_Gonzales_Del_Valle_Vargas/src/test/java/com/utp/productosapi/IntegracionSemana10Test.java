package com.utp.productosapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;
import com.utp.productosapi.model.Producto;
import com.utp.productosapi.repository.MovimientoStockRepository;
import com.utp.productosapi.repository.ProductoRepository;

/**
 * Semana 10 (guia, seccion 13): matriz de pruebas de integracion, de punta a
 * punta. Login real con BCrypt, JWT firmado de verdad, Resource Server,
 * Controller, Service transaccional, Repository y base de datos.
 *
 * Cada metodo se nombra con su fila de la matriz (e01 ... e12).
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:integracion_s10;DB_CLOSE_DELAY=-1")
class IntegracionSemana10Test {

    private static final String PRODUCTO_JSON = """
            {"nombre":"Webcam HD","categoria":"Tecnologia","precio":260.00,"stock":9}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoStockRepository movimientoRepository;

    private Long laptopId;

    @BeforeEach
    void prepararInventario() {
        movimientoRepository.deleteAll();
        productoRepository.deleteAll();
        laptopId = productoRepository.save(producto("Laptop", "3500.00", 8)).getId();
        productoRepository.save(producto("Teclado mecanico", "250.00", 3));
        productoRepository.save(producto("Monitor 27 pulgadas", "980.00", 4));
    }

    @Test
    void e01_loginUserValido_200ConAccessToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"usuario\",\"password\":\"Usuario123*\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void e02_productosSinToken_401() throws Exception {
        mockMvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
    }

    @Test
    void e03_productosConUser_200() throws Exception {
        mockMvc.perform(get("/api/productos").header(HttpHeaders.AUTHORIZATION, user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void e04_crearProductoConUser_403() throws Exception {
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, user())
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void e05_crearProductoConAdmin_201() throws Exception {
        mockMvc.perform(post("/api/productos").header(HttpHeaders.AUTHORIZATION, admin())
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void e06_rangoJpqlConUser_200SoloDentroDelRango() throws Exception {
        mockMvc.perform(get("/api/productos/rango").param("min", "100").param("max", "1000")
                .header(HttpHeaders.AUTHORIZATION, user()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].precio", everyItem(greaterThanOrEqualTo(100.0))))
                .andExpect(jsonPath("$[*].precio", everyItem(lessThanOrEqualTo(1000.0))))
                // ORDER BY p.precio ASC
                .andExpect(jsonPath("$[0].nombre").value("Teclado mecanico"));
    }

    @Test
    void e07_salidaValidaConAdmin_200DescuentaStockYRegistraMovimiento() throws Exception {
        mockMvc.perform(post("/api/inventario/{id}/salida", laptopId).param("cantidad", "2")
                .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(6));

        assertThat(productoRepository.findById(laptopId).orElseThrow().getStock()).isEqualTo(6);
        assertThat(movimientoRepository.buscarPorProducto(laptopId))
                .singleElement()
                .satisfies(m -> {
                    assertThat(m.getTipo()).isEqualTo("SALIDA");
                    assertThat(m.getCantidad()).isEqualTo(2);
                });
    }

    @Test
    void e08_salidaConStockInsuficiente_409SinCambiosEnLaBase() throws Exception {
        long movimientosAntes = movimientoRepository.count();

        mockMvc.perform(post("/api/inventario/{id}/salida", laptopId).param("cantidad", "99999")
                .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Stock insuficiente. Stock actual: 8, solicitado: 99999"));

        // Evidencia decisiva (seccion 12.3): el estado de la base no cambio.
        assertThat(productoRepository.findById(laptopId).orElseThrow().getStock()).isEqualTo(8);
        assertThat(movimientoRepository.count()).isEqualTo(movimientosAntes);
    }

    @Test
    void e09_resumenConUser_403() throws Exception {
        mockMvc.perform(get("/api/admin/resumen").header(HttpHeaders.AUTHORIZATION, user()))
                .andExpect(status().isForbidden());
    }

    @Test
    void e10_resumenConAdmin_200ConDatosDeLaBase() throws Exception {
        mockMvc.perform(post("/api/inventario/{id}/salida", laptopId).param("cantidad", "1")
                .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/resumen").header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(3))
                // Stock menor que 5: teclado (3) y monitor (4).
                .andExpect(jsonPath("$.productosConStockBajo").value(2))
                .andExpect(jsonPath("$.totalMovimientos").value(1))
                .andExpect(jsonPath("$.estado").value("BACKEND_OPERATIVO"));
    }

    @Test
    void e11_tokenConFirmaAlterada_401() throws Exception {
        String token = admin();
        int inicioFirma = token.lastIndexOf('.') + 1;
        String alterado = token.substring(0, inicioFirma)
                + (token.charAt(inicioFirma) == 'A' ? 'B' : 'A')
                + token.substring(inicioFirma + 1);

        mockMvc.perform(get("/api/productos").header(HttpHeaders.AUTHORIZATION, alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void e12_salidaDeProductoInexistente_404() throws Exception {
        mockMvc.perform(post("/api/inventario/{id}/salida", 999999).param("cantidad", "1")
                .header(HttpHeaders.AUTHORIZATION, admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rangoSinParametroMax_400ConMensaje() throws Exception {
        mockMvc.perform(get("/api/productos/rango").param("min", "100")
                .header(HttpHeaders.AUTHORIZATION, user()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Falta el parametro obligatorio: max"));
    }

    @Test
    void salidaConUser_403() throws Exception {
        mockMvc.perform(post("/api/inventario/{id}/salida", laptopId).param("cantidad", "1")
                .header(HttpHeaders.AUTHORIZATION, user()))
                .andExpect(status().isForbidden());
        assertThat(productoRepository.findById(laptopId).orElseThrow().getStock()).isEqualTo(8);
    }

    // ---------------- Apoyo ----------------

    private String user() throws Exception {
        return "Bearer " + token("usuario", "Usuario123*");
    }

    private String admin() throws Exception {
        return "Bearer " + token("admin", "Admin123*");
    }

    private String token(String username, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.accessToken");
    }

    private static Producto producto(String nombre, String precio, int stock) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria("Tecnologia");
        p.setPrecio(new BigDecimal(precio));
        p.setStock(stock);
        return p;
    }
}
