package com.utp.odontologia.model;

import java.time.LocalDateTime;

/**
 * Pago registrado contra un tratamiento (Integrante 5).
 * Un tratamiento puede recibir varios pagos parciales hasta cubrir su precio.
 */
public class Pago {

    private Long id;
    private Long tratamientoId;
    private Long pacienteId;
    private double monto;
    private MetodoPago metodo;
    private LocalDateTime fecha = LocalDateTime.now();
    private String comprobante;
    private String observaciones;

    public Pago() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTratamientoId() {
        return tratamientoId;
    }

    public void setTratamientoId(Long tratamientoId) {
        this.tratamientoId = tratamientoId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
