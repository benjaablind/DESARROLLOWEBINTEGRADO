package com.utp.odontologia.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Paciente de la clinica (Integrante 2).
 * Es el recurso central: citas, historias, odontograma, tratamientos y archivos
 * lo referencian por su id.
 */
public class Paciente {

    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String telefono;
    private String email;
    private String direccion;
    private String distrito;
    private String ciudad;
    private EstadoPaciente estado = EstadoPaciente.ACTIVO;
    private Antecedentes antecedentes = new Antecedentes();
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    public Paciente() {
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    /** Edad calculada a partir de la fecha de nacimiento. */
    public Integer getEdad() {
        return fechaNacimiento == null ? null
                : Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public EstadoPaciente getEstado() {
        return estado;
    }

    public void setEstado(EstadoPaciente estado) {
        this.estado = estado;
    }

    public Antecedentes getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(Antecedentes antecedentes) {
        this.antecedentes = antecedentes == null ? new Antecedentes() : antecedentes;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
