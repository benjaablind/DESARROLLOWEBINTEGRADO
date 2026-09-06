package com.utp.odontologia.model;

import java.time.LocalDateTime;

/**
 * Cita de la agenda (Integrante 3).
 * Referencia al paciente y al odontologo por id para no acoplar los modulos.
 */
public class Cita {

    private Long id;
    private Long pacienteId;
    private Long odontologoId;
    private LocalDateTime fechaHora;
    private int duracionMinutos = 30;
    private String motivo;
    private EstadoCita estado = EstadoCita.PENDIENTE;
    private String observaciones;
    private Long citaOriginalId;
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Cita() {
    }

    /** Momento en que termina la cita, usado para detectar cruces de horario. */
    public LocalDateTime getFechaHoraFin() {
        return fechaHora == null ? null : fechaHora.plusMinutes(duracionMinutos);
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /** Si esta cita nacio de una reprogramacion, apunta a la cita anterior. */
    public Long getCitaOriginalId() {
        return citaOriginalId;
    }

    public void setCitaOriginalId(Long citaOriginalId) {
        this.citaOriginalId = citaOriginalId;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
