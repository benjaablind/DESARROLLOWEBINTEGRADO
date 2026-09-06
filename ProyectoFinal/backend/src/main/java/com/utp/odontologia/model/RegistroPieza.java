package com.utp.odontologia.model;

import java.time.LocalDateTime;

/**
 * Registro de una pieza dental en el odontograma (Integrante 4).
 *
 * Cada cambio de estado de una pieza crea un registro nuevo, nunca se edita el
 * anterior. El odontograma actual de un paciente es el ultimo registro de cada
 * pieza; el historial de una pieza son todos sus registros ordenados por fecha.
 *
 * La numeracion de piezas sigue la notacion FDI: 11 a 48 en denticion
 * permanente y 51 a 85 en denticion decidua.
 */
public class RegistroPieza {

    private Long id;
    private Long pacienteId;
    private Long odontologoId;
    private int numeroPieza;
    private EstadoPieza estado;
    private String superficie;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;
    private LocalDateTime fecha = LocalDateTime.now();

    public RegistroPieza() {
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

    public int getNumeroPieza() {
        return numeroPieza;
    }

    public void setNumeroPieza(int numeroPieza) {
        this.numeroPieza = numeroPieza;
    }

    public EstadoPieza getEstado() {
        return estado;
    }

    public void setEstado(EstadoPieza estado) {
        this.estado = estado;
    }

    /** Superficie afectada: oclusal, mesial, distal, vestibular o palatina/lingual. */
    public String getSuperficie() {
        return superficie;
    }

    public void setSuperficie(String superficie) {
        this.superficie = superficie;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
