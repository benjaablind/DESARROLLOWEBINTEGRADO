package com.utp.odontologia.model;

import java.time.LocalDate;

/**
 * Sesion de un tratamiento (Integrante 5).
 * Un tratamiento puede tener varias sesiones asociadas.
 */
public class SesionTratamiento {

    private Long id;
    private int numero;
    private LocalDate fecha;
    private String descripcion;
    private boolean realizada = false;
    private String observaciones;

    public SesionTratamiento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isRealizada() {
        return realizada;
    }

    public void setRealizada(boolean realizada) {
        this.realizada = realizada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
