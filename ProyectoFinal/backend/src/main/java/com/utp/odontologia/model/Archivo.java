package com.utp.odontologia.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Metadatos de un archivo clinico (Integrante 6).
 *
 * El contenido binario nunca viaja en el JSON: se guarda en disco y solo se
 * entrega por el endpoint de descarga. La ubicacion fisica no se expone al
 * cliente para que los archivos no queden accesibles de forma directa.
 */
public class Archivo {

    private Long id;
    private Long pacienteId;
    private Long usuarioId;
    private Long historiaClinicaId;
    private TipoArchivo tipo;
    private String nombre;
    private String nombreOriginal;
    private String extension;
    private String contentType;
    private long tamanoBytes;
    private String descripcion;
    private String ubicacion;
    private LocalDateTime fechaSubida = LocalDateTime.now();

    public Archivo() {
    }

    /** Tamano legible para mostrar en el frontend. */
    public String getTamanoLegible() {
        if (tamanoBytes < 1024) {
            return tamanoBytes + " B";
        }
        if (tamanoBytes < 1024 * 1024) {
            return String.format("%.1f KB", tamanoBytes / 1024.0);
        }
        return String.format("%.1f MB", tamanoBytes / (1024.0 * 1024.0));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    /** Consulta de la historia clinica con la que se relaciona el archivo. */
    public Long getHistoriaClinicaId() {
        return historiaClinicaId;
    }

    public void setHistoriaClinicaId(Long historiaClinicaId) {
        this.historiaClinicaId = historiaClinicaId;
    }

    public TipoArchivo getTipo() {
        return tipo;
    }

    public void setTipo(TipoArchivo tipo) {
        this.tipo = tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Ruta interna en disco. No se serializa para no exponerla al cliente. */
    @JsonIgnore
    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
}
