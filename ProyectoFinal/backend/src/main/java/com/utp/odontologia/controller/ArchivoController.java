package com.utp.odontologia.controller;

import java.net.URI;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.utp.odontologia.dto.ArchivoActualizarRequest;
import com.utp.odontologia.dto.ArchivoResponse;
import com.utp.odontologia.model.Archivo;
import com.utp.odontologia.model.TipoArchivo;
import com.utp.odontologia.service.ArchivoService;

import jakarta.validation.Valid;

/**
 * API REST de archivos clinicos (Integrante 6).
 *
 * Solo traduce HTTP: delega las reglas en ArchivoService y deja que
 * ApiExceptionHandler convierta las excepciones en respuestas de error.
 * El binario nunca viaja en el JSON, se entrega por el endpoint de descarga.
 */
@RestController
@RequestMapping("/api/archivos")
public class ArchivoController {

    private final ArchivoService archivoService;

    public ArchivoController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    /** GET /api/archivos?pacienteId=&tipo= - listado con filtros opcionales. */
    @GetMapping
    public ResponseEntity<List<ArchivoResponse>> listar(
            @RequestParam(name = "pacienteId", required = false) Long pacienteId,
            @RequestParam(name = "tipo", required = false) TipoArchivo tipo) {

        return ResponseEntity.ok(
                archivoService.aResponses(archivoService.listar(pacienteId, tipo)));
    }

    /** GET /api/archivos/{id} - metadatos de un archivo. */
    @GetMapping("/{id}")
    public ResponseEntity<ArchivoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(archivoService.aResponse(archivoService.buscarPorId(id)));
    }

    /** GET /api/archivos/paciente/{pacienteId} - archivos de un paciente. */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ArchivoResponse>> listarPorPaciente(
            @PathVariable Long pacienteId) {

        return ResponseEntity.ok(
                archivoService.aResponses(archivoService.listarPorPaciente(pacienteId)));
    }

    /** POST /api/archivos - sube un archivo clinico y devuelve 201 con Location. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArchivoResponse> subir(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("pacienteId") Long pacienteId,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId,
            @RequestParam("tipo") TipoArchivo tipo,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "historiaClinicaId", required = false) Long historiaClinicaId) {

        Archivo guardado = archivoService.subir(archivo, pacienteId, usuarioId, tipo,
                descripcion, historiaClinicaId);

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(guardado.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(archivoService.aResponse(guardado));
    }

    /** GET /api/archivos/{id}/descargar - entrega el binario como adjunto. */
    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id) {
        Archivo archivo = archivoService.buscarPorId(id);
        Resource recurso = archivoService.descargar(id);

        String contentType = archivo.getContentType() == null || archivo.getContentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : archivo.getContentType();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.getNombreOriginal() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(archivo.getTamanoBytes())
                .body(recurso);
    }

    /** PUT /api/archivos/{id} - corrige los metadatos, nunca el contenido. */
    @PutMapping("/{id}")
    public ResponseEntity<ArchivoResponse> actualizar(@PathVariable Long id,
            @Valid @RequestBody ArchivoActualizarRequest request) {

        return ResponseEntity.ok(
                archivoService.aResponse(archivoService.actualizar(id, request)));
    }

    /** DELETE /api/archivos/{id} - borra el registro y el archivo en disco. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        archivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
