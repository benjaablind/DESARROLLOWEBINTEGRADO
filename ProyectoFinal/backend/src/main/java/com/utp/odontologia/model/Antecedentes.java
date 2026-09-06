package com.utp.odontologia.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Antecedentes clinicos del paciente (Integrante 2).
 * Se modela aparte para no llenar la entidad Paciente de campos sueltos.
 */
public class Antecedentes {

    private List<String> enfermedades = new ArrayList<>();
    private List<String> alergias = new ArrayList<>();
    private List<String> medicamentos = new ArrayList<>();
    private List<String> habitos = new ArrayList<>();
    private String antecedentesOdontologicos;
    private String observaciones;

    public Antecedentes() {
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
