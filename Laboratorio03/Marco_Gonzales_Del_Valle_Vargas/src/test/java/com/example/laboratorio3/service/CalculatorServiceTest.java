package com.example.laboratorio3.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorServiceTest {

    private final CalculatorService calculatorService = new CalculatorService();

    @Test
    void shouldAddTwoNumbers() {
        assertEquals(8, calculatorService.add(3, 5));
    }

    @Test
    void shouldSubtractTwoNumbers() {
        assertEquals(2, calculatorService.subtract(7, 5));
    }

    @Test
    void shouldMultiplyTwoNumbers() {
        assertEquals(15, calculatorService.multiply(3, 5));
    }

    @Test
    void shouldDivideTwoNumbers() {
        assertEquals(3, calculatorService.divide(9, 3));
    }

    @Test
    void shouldThrowExceptionWhenDividingByZero() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> calculatorService.divide(10, 0)
        );

        assertEquals("El divisor no puede ser cero", exception.getMessage());
    }
}
