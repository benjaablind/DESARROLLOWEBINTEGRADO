package com.utp.odontologia.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Operaciones con importes en soles, comunes a pagos, tratamientos, dashboard
 * y reportes (semana 7).
 *
 * Hasta la semana 5 cada servicio tenia su propio redondear(double) y el
 * modulo de pagos comparaba saldos con una tolerancia de 0.001 para esquivar
 * los errores de coma flotante (0.1 + 0.2 != 0.3 en double). Con BigDecimal la
 * aritmetica es exacta y esa tolerancia deja de ser necesaria.
 */
final class Montos {

    /** Cero con dos decimales, para que el JSON muestre 0.00 y no 0. */
    static final BigDecimal CERO = new BigDecimal("0.00");

    private Montos() {
    }

    /** Redondeo monetario a 2 decimales (mitad hacia arriba). */
    static BigDecimal redondear(BigDecimal valor) {
        return valor == null ? CERO : valor.setScale(2, RoundingMode.HALF_UP);
    }

    /** Lo que falta pagar: precio menos lo pagado, nunca negativo. */
    static BigDecimal saldo(BigDecimal precio, BigDecimal pagado) {
        BigDecimal diferencia = redondear(precio).subtract(redondear(pagado));
        return diferencia.signum() < 0 ? CERO : diferencia;
    }

    static BigDecimal sumar(BigDecimal a, BigDecimal b) {
        return redondear(a).add(redondear(b));
    }
}
