package com.utp.odontologia;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.odontologia.model.EstadoTratamiento;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Tratamiento;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.PagoRepository;
import com.utp.odontologia.repository.TratamientoRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del modulo de pagos (Integrante 5).
 * Verifican sobre todo la regla del saldo: nunca se cobra de mas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long pacienteUnoId;
    private Long pacienteDosId;
    private Long odontologoId;
    private Long ortodonciaId;
    private Long limpiezaId;
    private Long canceladoId;

    @BeforeEach
    void prepararDatos() {
        pagoRepository.limpiar();
        tratamientoRepository.limpiar();
        pacienteRepository.limpiar();
        usuarioRepository.limpiar();

        pacienteUnoId = pacienteRepository.guardar(paciente("70111222", "Ana", "Torres")).getId();
        pacienteDosId = pacienteRepository.guardar(paciente("70333444", "Luis", "Rojas")).getId();

        Usuario odontologo = new Usuario(null, "Carlos", "Diaz", "carlos.diaz@clinica.pe",
                "cdiaz", "clave123", Rol.ODONTOLOGO);
        odontologoId = usuarioRepository.guardar(odontologo).getId();

        ortodonciaId = tratamientoRepository.guardar(tratamiento(pacienteUnoId, "Ortodoncia",
                1000.0, EstadoTratamiento.EN_PROCESO)).getId();
        limpiezaId = tratamientoRepository.guardar(tratamiento(pacienteUnoId, "Limpieza dental",
                500.0, EstadoTratamiento.PENDIENTE)).getId();
        canceladoId = tratamientoRepository.guardar(tratamiento(pacienteDosId, "Blanqueamiento",
                300.0, EstadoTratamiento.CANCELADO)).getId();
    }

    @Test
    @DisplayName("POST /api/pagos registra un pago parcial y descuenta el saldo")
    void registrarPagoParcialDescuentaElSaldo() throws Exception {
        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pago(ortodonciaId, 300.0, "EFECTIVO")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.pacienteId").value(pacienteUnoId))
                .andExpect(jsonPath("$.pacienteNombre").value("Ana Torres"))
                .andExpect(jsonPath("$.tratamientoNombre").value("Ortodoncia"))
                .andExpect(jsonPath("$.monto").value(300.0));

        mockMvc.perform(get("/api/tratamientos/" + ortodonciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPagado").value(300.0))
                .andExpect(jsonPath("$.saldoPendiente").value(700.0));
    }

    @Test
    @DisplayName("Varios pagos se acumulan y el saldo baja de forma consistente")
    void variosPagosAcumulanElSaldo() throws Exception {
        registrarPago(ortodonciaId, 300.0);
        registrarPago(ortodonciaId, 250.5);
        registrarPago(ortodonciaId, 199.5);

        mockMvc.perform(get("/api/tratamientos/" + ortodonciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPagado").value(750.0))
                .andExpect(jsonPath("$.saldoPendiente").value(250.0));
    }

    @Test
    @DisplayName("POST /api/pagos devuelve 400 si el monto excede el saldo pendiente")
    void pagoQueExcedeElSaldoDevuelve400() throws Exception {
        registrarPago(limpiezaId, 400.0);

        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pago(limpiezaId, 150.0, "TARJETA")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje",
                        org.hamcrest.Matchers.containsString("100.00")));
    }

    @Test
    @DisplayName("POST /api/pagos devuelve 400 si el monto no es positivo")
    void pagoConMontoNegativoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pago(ortodonciaId, -50.0, "EFECTIVO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pagos devuelve 404 si el tratamiento no existe")
    void pagoDeTratamientoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pago(9999L, 50.0, "YAPE")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/pagos devuelve 400 si el tratamiento esta CANCELADO")
    void pagoDeTratamientoCanceladoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pago(canceladoId, 50.0, "PLIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Se acepta un pago exactamente igual al saldo pendiente")
    void pagoExactoDejaSaldoEnCero() throws Exception {
        registrarPago(limpiezaId, 500.0);

        mockMvc.perform(get("/api/tratamientos/" + limpiezaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoPendiente").value(0.0));
    }

    @Test
    @DisplayName("GET /api/pagos/tratamiento/{id} lista el historial del tratamiento")
    void listarPorTratamiento() throws Exception {
        registrarPago(ortodonciaId, 100.0);
        registrarPago(ortodonciaId, 200.0);
        registrarPago(limpiezaId, 50.0);

        mockMvc.perform(get("/api/pagos/tratamiento/" + ortodonciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/pagos/tratamiento/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/pagos?pacienteId= lista los pagos del paciente")
    void listarPorPaciente() throws Exception {
        registrarPago(ortodonciaId, 100.0);
        registrarPago(limpiezaId, 50.0);

        mockMvc.perform(get("/api/pagos").param("pacienteId", pacienteUnoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/pagos").param("pacienteId", pacienteDosId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/pagos/{id} devuelve el pago y 404 si no existe")
    void buscarPagoPorId() throws Exception {
        registrarPago(ortodonciaId, 120.0);

        mockMvc.perform(get("/api/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monto").value(120.0));

        mockMvc.perform(get("/api/pagos/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/pagos/estado-cuenta/{pacienteId} suma los tratamientos no cancelados")
    void estadoDeCuentaConTotalesCorrectos() throws Exception {
        registrarPago(ortodonciaId, 400.0);
        registrarPago(limpiezaId, 100.0);

        mockMvc.perform(get("/api/pagos/estado-cuenta/" + pacienteUnoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteNombre").value("Ana Torres"))
                .andExpect(jsonPath("$.totalTratamientos").value(1500.0))
                .andExpect(jsonPath("$.totalPagado").value(500.0))
                .andExpect(jsonPath("$.saldoPendiente").value(1000.0))
                .andExpect(jsonPath("$.tratamientos.length()").value(2));
    }

    @Test
    @DisplayName("El estado de cuenta ignora los tratamientos cancelados")
    void estadoDeCuentaIgnoraCancelados() throws Exception {
        mockMvc.perform(get("/api/pagos/estado-cuenta/" + pacienteDosId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTratamientos").value(0.0))
                .andExpect(jsonPath("$.tratamientos.length()").value(0));
    }

    @Test
    @DisplayName("DELETE /api/pagos/{id} anula el pago y el saldo vuelve a subir")
    void eliminarPagoDevuelveElSaldo() throws Exception {
        registrarPago(ortodonciaId, 400.0);

        mockMvc.perform(get("/api/tratamientos/" + ortodonciaId))
                .andExpect(jsonPath("$.saldoPendiente").value(600.0));

        mockMvc.perform(delete("/api/pagos/1"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tratamientos/" + ortodonciaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPagado").value(0.0))
                .andExpect(jsonPath("$.saldoPendiente").value(1000.0));

        mockMvc.perform(delete("/api/pagos/1"))
                .andExpect(status().isNotFound());
    }

    /** Registra un pago valido y falla la prueba si la API no devuelve 201. */
    private void registrarPago(Long tratamientoId, double monto) throws Exception {
        mockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pago(tratamientoId, monto, "EFECTIVO")))
                .andExpect(status().isCreated());
    }

    private String pago(Long tratamientoId, double monto, String metodo) {
        return """
                {
                  "tratamientoId": %d,
                  "monto": %s,
                  "metodo": "%s",
                  "comprobante": "B001-100",
                  "observaciones": "Pago de prueba"
                }
                """.formatted(tratamientoId, monto, metodo);
    }

    private Paciente paciente(String dni, String nombres, String apellidos) {
        Paciente paciente = new Paciente();
        paciente.setDni(dni);
        paciente.setNombres(nombres);
        paciente.setApellidos(apellidos);
        paciente.setFechaNacimiento(LocalDate.of(1995, 3, 20));
        paciente.setTelefono("999888777");
        return paciente;
    }

    private Tratamiento tratamiento(Long pacienteId, String nombre, double precio,
            EstadoTratamiento estado) {
        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setPacienteId(pacienteId);
        tratamiento.setOdontologoId(odontologoId);
        tratamiento.setNombre(nombre);
        tratamiento.setDescripcion("Tratamiento de prueba");
        tratamiento.setPrecio(precio);
        tratamiento.setEstado(estado);
        return tratamiento;
    }
}
