package com.utp.odontologia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sistema de Gestion Odontologica - Backend.
 *
 * Arquitectura por capas: controller -> service -> repository.
 * En esta etapa del curso (semanas 1 a 4) la persistencia es en memoria.
 */
@SpringBootApplication
public class OdontologiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OdontologiaApplication.class, args);
    }
}
