package com.utp.odontologia.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Antecedentes clinicos del paciente (Integrante 2).
 * Se modela aparte para no llenar la entidad Paciente de campos sueltos.
 *
 * Es una entidad propia, unida 1 a 1 con el paciente, y no un @Embeddable: un
 * embeddable con todas sus columnas vacias se carga como null, y los servicios
 * asumen que el paciente siempre tiene sus antecedentes.
 *
 * Cada lista se guarda en su propia tabla (@ElementCollection); @OrderColumn
 * conserva el orden en que se registraron los elementos. Las listas se cargan
 * siempre (EAGER) porque viajan en la respuesta del paciente, que se arma fuera
 * de la transaccion; SUBSELECT las trae con una consulta por lista para todos
 * los pacientes leidos, en lugar de una consulta por paciente.
 */
@Entity
@Table(name = "antecedentes")
public class Antecedentes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "antecedentes_enfermedades", joinColumns = @JoinColumn(name = "antecedentes_id"))
    @OrderColumn(name = "orden")
    @Column(name = "enfermedad", length = 200)
    private List<String> enfermedades = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "antecedentes_alergias", joinColumns = @JoinColumn(name = "antecedentes_id"))
    @OrderColumn(name = "orden")
    @Column(name = "alergia", length = 200)
    private List<String> alergias = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "antecedentes_medicamentos", joinColumns = @JoinColumn(name = "antecedentes_id"))
    @OrderColumn(name = "orden")
    @Column(name = "medicamento", length = 200)
    private List<String> medicamentos = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "antecedentes_habitos", joinColumns = @JoinColumn(name = "antecedentes_id"))
    @OrderColumn(name = "orden")
    @Column(name = "habito", length = 200)
    private List<String> habitos = new ArrayList<>();

    @Column(name = "antecedentes_odontologicos", length = 1000)
    private String antecedentesOdontologicos;

    @Column(length = 1000)
    private String observaciones;

    public Antecedentes() {
    }

    /** Identificador interno de la fila; no forma parte del contrato de la API. */
    @JsonIgnore
    public Long getId() {
        return id;
    }

    public List<String> getEnfermedades() {
        return enfermedades;
    }

    public void setEnfermedades(List<String> enfermedades) {
        this.enfermedades = enfermedades == null ? new ArrayList<>() : enfermedades;
    }

    public List<String> getAlergias() {
        return alergias;
    }

    public void setAlergias(List<String> alergias) {
        this.alergias = alergias == null ? new ArrayList<>() : alergias;
    }

    public List<String> getMedicamentos() {
        return medicamentos;
    }

    public void setMedicamentos(List<String> medicamentos) {
        this.medicamentos = medicamentos == null ? new ArrayList<>() : medicamentos;
    }

    public List<String> getHabitos() {
        return habitos;
    }

    public void setHabitos(List<String> habitos) {
        this.habitos = habitos == null ? new ArrayList<>() : habitos;
    }

    public String getAntecedentesOdontologicos() {
        return antecedentesOdontologicos;
    }

    public void setAntecedentesOdontologicos(String antecedentesOdontologicos) {
        this.antecedentesOdontologicos = antecedentesOdontologicos;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
