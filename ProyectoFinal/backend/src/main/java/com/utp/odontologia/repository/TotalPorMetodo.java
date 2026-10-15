package com.utp.odontologia.repository;

import java.math.BigDecimal;

import com.utp.odontologia.model.MetodoPago;

/**
 * Proyeccion de una consulta JPQL agrupada (Integrante 6, reporte de ingresos).
 *
 * La consulta la construye directamente con "SELECT new ...TotalPorMetodo(...)",
 * asi el repositorio devuelve objetos tipados en lugar de filas Object[].
 */
public record TotalPorMetodo(MetodoPago metodo, BigDecimal total) {
}
