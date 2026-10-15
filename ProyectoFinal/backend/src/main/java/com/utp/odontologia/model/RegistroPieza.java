package com.utp.odontologia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
@Entity
@Table(name = "odontograma")
public class RegistroPieza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    // Relaciones de solo lectura sobre las mismas columnas: generan las claves
    // foraneas y permiten navegar en JPQL, mientras los servicios siguen
    // asignando las referencias por id.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", insertable = false, updatable = false)
    private Paciente paciente;

    @Column(name = "odontologo_id")
    private Long odontologoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odontologo_id", insertable = false, updatable = false)
    private Usuario odontologo;

    @Column(name = "numero_pieza", nullable = false)
    private int numeroPieza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPieza estado;

    @Column(length = 50)
    private String superficie;

    @Column(length = 500)
    private String diagnostico;

    @Column(length = 500)
    private String tratamiento;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false)
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
