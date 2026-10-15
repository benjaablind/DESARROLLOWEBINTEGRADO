package com.utp.odontologia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.odontologia.dto.CitaReprogramarRequest;
import com.utp.odontologia.model.Cita;
import com.utp.odontologia.model.EstadoCita;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.CitaRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;
import com.utp.odontologia.service.CitaService;

/**
 * Comportamiento que solo existe desde que hay base de datos (semanas 6 y 7):
 * transacciones con rollback e integridad referencial.
 *
 * Esta clase NO lleva @Transactional a proposito: si cada prueba corriera
 * dentro de su propia transaccion, todo se revertiria al final y no se podria
 * distinguir un cambio confirmado de uno revertido.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = "ADMINISTRADOR")
class TransaccionesYPersistenciaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiadorBaseDatos baseDeDatos;

    @Autowired
    private CitaService citaService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Espia sobre el repositorio real: delega todo a la base de datos salvo lo
     * que la prueba indique explicitamente que falle.
     */
    @MockitoSpyBean
    private CitaRepository citaRepository;

    private Long pacienteId;
    private Long citaId;

    @BeforeEach
    void prepararEscenario() {
        baseDeDatos.limpiar();

        Paciente paciente = new Paciente();
        paciente.setDni("70111222");
        paciente.setNombres("Ana");
        paciente.setApellidos("Torres");
        pacienteId = pacienteRepository.guardar(paciente).getId();

        Usuario odontologo = new Usuario(null, "Carlos", "Diaz", "carlos.diaz@clinica.pe",
                "cdiaz", "clave123", Rol.ODONTOLOGO);
        Long odontologoId = usuarioRepository.guardar(odontologo).getId();

        Cita cita = new Cita();
        cita.setPacienteId(pacienteId);
        cita.setOdontologoId(odontologoId);
        cita.setFechaHora(LocalDate.now().plusDays(2).atTime(10, 0));
        cita.setMotivo("Control");
        citaId = citaRepository.guardar(cita).getId();
    }

    @Test
    @DisplayName("Reprogramar es atomico: si falla el segundo paso, tampoco queda la cita nueva")
    void reprogramarHaceRollbackCompleto() {
        // reprogramar() guarda primero la cita nueva y despues marca la original
        // como REPROGRAMADA. Se hace fallar ese segundo guardado.
        doThrow(new IllegalStateException("Falla simulada al actualizar la cita original"))
                .when(citaRepository)
                .guardar(argThat(c -> c != null && c.getEstado() == EstadoCita.REPROGRAMADA));

        CitaReprogramarRequest peticion = new CitaReprogramarRequest(
                LocalDate.now().plusDays(3).atTime(11, 0), "Paciente pidio otro dia");

        assertThatThrownBy(() -> citaService.reprogramar(citaId, peticion))
                .isInstanceOf(IllegalStateException.class);

        // Sin transaccion quedarian dos citas activas para el mismo paciente.
        // Con @Transactional, el INSERT de la cita nueva tambien se revirtio.
        assertThat(citaRepository.count()).isEqualTo(1);
        assertThat(citaRepository.findById(citaId))
                .get()
                .extracting(Cita::getEstado)
                .isEqualTo(EstadoCita.PENDIENTE);
    }

    @Test
    @DisplayName("Reprogramar sin fallas confirma los dos cambios juntos")
    void reprogramarConfirmaAmbosCambios() {
        LocalDateTime nuevaFecha = LocalDate.now().plusDays(3).atTime(11, 0);

        Long nuevaId = citaService.reprogramar(citaId,
                new CitaReprogramarRequest(nuevaFecha, null)).id();

        assertThat(citaRepository.count()).isEqualTo(2);
        assertThat(citaRepository.findById(citaId).orElseThrow().getEstado())
                .isEqualTo(EstadoCita.REPROGRAMADA);
        assertThat(citaRepository.findById(nuevaId).orElseThrow().getCitaOriginalId())
                .isEqualTo(citaId);
    }

    @Test
    @DisplayName("DELETE de un paciente con citas responde 409 y no borra nada")
    void eliminarPacienteConCitasRespondeConflicto() throws Exception {
        mockMvc.perform(delete("/api/pacientes/{id}", pacienteId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));

        assertThat(pacienteRepository.existe(pacienteId)).isTrue();
        assertThat(citaRepository.count()).isEqualTo(1);
    }
}
