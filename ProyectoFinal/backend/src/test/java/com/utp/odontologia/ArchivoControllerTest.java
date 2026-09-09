package com.utp.odontologia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.odontologia.dto.ArchivoActualizarRequest;
import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.Paciente;
import com.utp.odontologia.model.Rol;
import com.utp.odontologia.model.TipoArchivo;
import com.utp.odontologia.model.Usuario;
import com.utp.odontologia.repository.ArchivoRepository;
import com.utp.odontologia.repository.PacienteRepository;
import com.utp.odontologia.repository.UsuarioRepository;

/**
 * Pruebas de integracion del modulo de archivos clinicos (Integrante 6).
 *
 * Cada prueba parte de repositorios limpios y de un paciente y un usuario
 * conocidos. Al terminar se borran los binarios que quedaron en la carpeta de
 * subidas para no ensuciar el proyecto.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ArchivoControllerTest {

    /** Debe coincidir con app.archivos.directorio de application.properties. */
    private static final Path CARPETA_SUBIDAS = Paths.get("uploads");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArchivoRepository archivoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long idPaciente;
    private Long idUsuario;

    @BeforeEach
    void prepararDatos() {
        // Obligatorio: los repositorios en memoria son singletons compartidos.
        archivoRepository.limpiar();
        pacienteRepository.limpiar();
        usuarioRepository.limpiar();

        Paciente paciente = new Paciente();
        paciente.setDni("70123456");
        paciente.setNombres("Juan");
        paciente.setApellidos("Perez");
        paciente.setTelefono("987654321");
        idPaciente = pacienteRepository.guardar(paciente).getId();

        idUsuario = usuarioRepository.guardar(new Usuario(null, "Luis", "Ramos",
                "luis@clinica.com", "lramos", "clave123", Rol.ODONTOLOGO)).getId();
    }

    @AfterEach
    void borrarBinariosDePrueba() {
        for (Archivo archivo : archivoRepository.listar()) {
            try {
                Files.deleteIfExists(Paths.get(archivo.getUbicacion()));
            } catch (IOException ignorado) {
                // La prueba no debe fallar por un archivo temporal que no se pudo borrar.
            }
        }
        archivoRepository.limpiar();
    }

    @AfterAll
    static void borrarCarpetaSiQuedoVacia() {
        try {
            Files.deleteIfExists(CARPETA_SUBIDAS);
        } catch (IOException ignorado) {
            // Si la carpeta tiene archivos de la aplicacion se deja tal cual.
        }
    }

    @Test
    @DisplayName("POST /api/archivos sube el archivo y devuelve 201 con los metadatos")
    void subirDevuelve201ConMetadatos() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "radiografia.png",
                "image/png", "contenido-de-prueba".getBytes(StandardCharsets.UTF_8));

        MvcResult resultado = mockMvc.perform(multipart("/api/archivos")
                        .file(archivo)
                        .param("pacienteId", String.valueOf(idPaciente))
                        .param("usuarioId", String.valueOf(idUsuario))
                        .param("tipo", "RADIOGRAFIA")
                        .param("descripcion", "Panoramica inicial"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/archivos/")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.pacienteId").value(idPaciente))
                .andExpect(jsonPath("$.pacienteNombre").value("Juan Perez"))
                .andExpect(jsonPath("$.usuarioNombre").value("Luis Ramos"))
                .andExpect(jsonPath("$.tipo").value("RADIOGRAFIA"))
                .andExpect(jsonPath("$.nombreOriginal").value("radiografia.png"))
                .andExpect(jsonPath("$.extension").value("png"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.tamanoBytes").value(19))
                .andExpect(jsonPath("$.tamanoLegible").value("19 B"))
                .andExpect(jsonPath("$.descripcion").value("Panoramica inicial"))
                .andExpect(jsonPath("$.urlDescarga", containsString("/descargar")))
                .andReturn();

        assertThat(resultado.getResponse().getStatus()).isEqualTo(201);

        // El binario quedo realmente escrito en disco.
        Archivo guardado = archivoRepository.listar().get(0);
        assertThat(Files.exists(Paths.get(guardado.getUbicacion()))).isTrue();
        // El nombre en disco es unico y no repite el nombre original.
        assertThat(guardado.getNombre()).isNotEqualTo(guardado.getNombreOriginal());
    }

    @Test
    @DisplayName("La respuesta nunca expone la ubicacion fisica del archivo")
    void respuestaNoExponeUbicacion() throws Exception {
        Long id = subirArchivoDePrueba("informe.pdf", "application/pdf", "INFORME");

        mockMvc.perform(get("/api/archivos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ubicacion").doesNotExist())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        containsString("uploads"))));
    }

    @Test
    @DisplayName("POST /api/archivos devuelve 400 si el archivo viene vacio")
    void subirArchivoVacioDevuelve400() throws Exception {
        MockMultipartFile vacio = new MockMultipartFile("archivo", "vacio.png",
                "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/archivos")
                        .file(vacio)
                        .param("pacienteId", String.valueOf(idPaciente))
                        .param("tipo", "RADIOGRAFIA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("El archivo esta vacio"));
    }

    @Test
    @DisplayName("POST /api/archivos devuelve 400 si la extension no esta permitida")
    void subirExtensionNoPermitidaDevuelve400() throws Exception {
        MockMultipartFile ejecutable = new MockMultipartFile("archivo", "programa.exe",
                "application/octet-stream", "MZ-binario".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/archivos")
                        .file(ejecutable)
                        .param("pacienteId", String.valueOf(idPaciente))
                        .param("tipo", "DOCUMENTO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("La extension .exe no esta permitida"));

        assertThat(archivoRepository.contar()).isZero();
    }

    @Test
    @DisplayName("POST /api/archivos devuelve 404 si el paciente no existe")
    void subirConPacienteInexistenteDevuelve404() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.jpg",
                "image/jpeg", "foto".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/archivos")
                        .file(archivo)
                        .param("pacienteId", "9999")
                        .param("tipo", "FOTOGRAFIA"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    @Test
    @DisplayName("GET /api/archivos/paciente/{id} lista los archivos del paciente")
    void listarPorPacienteDevuelveArchivos() throws Exception {
        subirArchivoDePrueba("uno.png", "image/png", "RADIOGRAFIA");
        subirArchivoDePrueba("dos.pdf", "application/pdf", "INFORME");

        mockMvc.perform(get("/api/archivos/paciente/" + idPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pacienteId").value(idPaciente));
    }

    @Test
    @DisplayName("GET /api/archivos/paciente/{id} devuelve 404 si el paciente no existe")
    void listarPorPacienteInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/archivos/paciente/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/archivos?tipo= filtra por tipo de documento")
    void filtrarPorTipo() throws Exception {
        subirArchivoDePrueba("radio.png", "image/png", "RADIOGRAFIA");
        subirArchivoDePrueba("receta.pdf", "application/pdf", "RECETA");

        mockMvc.perform(get("/api/archivos").param("tipo", "RECETA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreOriginal").value("receta.pdf"));

        mockMvc.perform(get("/api/archivos")
                        .param("pacienteId", String.valueOf(idPaciente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/archivos/{id}/descargar entrega el binario como adjunto")
    void descargarDevuelveContenidoYHeader() throws Exception {
        byte[] contenido = "contenido binario de prueba".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile archivo = new MockMultipartFile("archivo", "consentimiento.txt",
                "text/plain", contenido);

        String cuerpo = mockMvc.perform(multipart("/api/archivos")
                        .file(archivo)
                        .param("pacienteId", String.valueOf(idPaciente))
                        .param("tipo", "CONSENTIMIENTO"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(cuerpo).get("id").asLong();

        MvcResult descarga = mockMvc.perform(get("/api/archivos/" + id + "/descargar"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("attachment")))
                .andExpect(header().string("Content-Disposition",
                        containsString("consentimiento.txt")))
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andReturn();

        assertThat(descarga.getResponse().getContentAsByteArray()).isEqualTo(contenido);
    }

    @Test
    @DisplayName("GET /api/archivos/{id}/descargar devuelve 404 si el archivo no existe")
    void descargarInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/archivos/9999/descargar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/archivos/{id} actualiza solo los metadatos")
    void actualizarMetadatos() throws Exception {
        Long id = subirArchivoDePrueba("estudio.png", "image/png", "RADIOGRAFIA");
        String nombreEnDisco = archivoRepository.buscarPorId(id).orElseThrow().getNombre();

        ArchivoActualizarRequest cambios = new ArchivoActualizarRequest(
                TipoArchivo.INFORME, "Reclasificado como informe", null);

        mockMvc.perform(put("/api/archivos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("INFORME"))
                .andExpect(jsonPath("$.descripcion").value("Reclasificado como informe"))
                // El binario y su nombre en disco no cambian nunca.
                .andExpect(jsonPath("$.nombreOriginal").value("estudio.png"))
                .andExpect(jsonPath("$.nombre").value(nombreEnDisco));
    }

    @Test
    @DisplayName("PUT /api/archivos/{id} devuelve 400 si falta el tipo y 404 si no existe")
    void actualizarInvalidoDevuelve400Y404() throws Exception {
        Long id = subirArchivoDePrueba("nota.txt", "text/plain", "DOCUMENTO");

        mockMvc.perform(put("/api/archivos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"sin tipo\"}"))
                .andExpect(status().isBadRequest());

        ArchivoActualizarRequest cambios = new ArchivoActualizarRequest(
                TipoArchivo.INFORME, "cualquiera", null);

        mockMvc.perform(put("/api/archivos/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/archivos/{id} devuelve 204, borra el binario y luego responde 404")
    void eliminarDevuelve204YLuego404() throws Exception {
        Long id = subirArchivoDePrueba("borrable.png", "image/png", "FOTOGRAFIA");
        Path ubicacion = Paths.get(archivoRepository.buscarPorId(id).orElseThrow().getUbicacion());
        assertThat(Files.exists(ubicacion)).isTrue();

        mockMvc.perform(delete("/api/archivos/" + id))
                .andExpect(status().isNoContent());

        assertThat(Files.exists(ubicacion)).isFalse();

        mockMvc.perform(get("/api/archivos/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/archivos/{id} devuelve 404 si el id no existe")
    void buscarPorIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/archivos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404));
    }

    /** Sube un archivo del paciente de prueba y devuelve el id generado. */
    private Long subirArchivoDePrueba(String nombre, String contentType, String tipo)
            throws Exception {

        MockMultipartFile archivo = new MockMultipartFile("archivo", nombre, contentType,
                ("contenido de " + nombre).getBytes(StandardCharsets.UTF_8));

        String cuerpo = mockMvc.perform(multipart("/api/archivos")
                        .file(archivo)
                        .param("pacienteId", String.valueOf(idPaciente))
                        .param("usuarioId", String.valueOf(idUsuario))
                        .param("tipo", tipo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(cuerpo).get("id").asLong();
    }
}
