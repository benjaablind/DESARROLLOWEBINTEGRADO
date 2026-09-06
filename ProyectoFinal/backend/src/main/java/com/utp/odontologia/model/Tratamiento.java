package com.utp.odontologia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tratamiento indicado a un paciente (Integrante 5).
 * El precio es el total acordado; los pagos se registran aparte y de ahi sale el saldo.
 */
public class Tratamiento {

    private Long id;
    private Long pacienteId;
    private Long odontologoId;
    private String nombre;
    private String descripcion;
    private double precio;
    private EstadoTratamiento estado = EstadoTratamiento.PENDIENTE;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String observaciones;
    private List<SesionTratamiento> sesiones = new ArrayList<>();
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Tratamiento() {
    }

    /** Un tratamiento esta activo mientras no se haya completado ni cancelado. */
    public boolean estaActivo() {
        return estado == EstadoTratamiento.PENDIENTE
                || estado == EstadoTratamiento.APROBADO
                || estado == EstadoTratamiento.EN_PROCESO;
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

    public Long getOdontologoId() {
        return odontologoId;
    }

    public void setOdontologoId(Long odontologoId) {
        this.odontologoId = odontologoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public EstadoTratamiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoTratamiento estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<SesionTratamiento> getSesiones() {
        return sesiones;
    }

    public void setSesiones(List<SesionTratamiento> sesiones) {
        this.sesiones = sesiones == null ? new ArrayList<>() : sesiones;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
