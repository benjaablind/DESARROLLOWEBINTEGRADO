package com.utp.odontologia.model;

/**
 * Roles del sistema. Definen que puede hacer cada usuario.
 * Cuando se incorpore Spring Security, estos roles se mapearan a authorities.
 */
public enum Rol {
    ADMINISTRADOR,
    ODONTOLOGO,
    RECEPCIONISTA,
    ASISTENTE
}
