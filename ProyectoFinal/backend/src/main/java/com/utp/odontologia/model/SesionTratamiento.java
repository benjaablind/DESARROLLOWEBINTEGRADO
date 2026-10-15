package com.utp.odontologia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;

/**
 * Sesion de un tratamiento (Integrante 5).
 * Un tratamiento puede tener varias sesiones asociadas.
 *
 * Es un @Embeddable: se guarda en la tabla tratamiento_sesiones junto al id de
 * su tratamiento. El campo id no es clave primaria sino el numero local que le
 * asigna el servicio dentro del tratamiento.
 */
@Embeddable
public class SesionTratamiento {

    @Column(name = "sesion_id")
    private Long id;

    @Column(nullable = false)
    private int numero;

    private LocalDate fecha;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean realizada = false;

    @Column(length = 1000)
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
