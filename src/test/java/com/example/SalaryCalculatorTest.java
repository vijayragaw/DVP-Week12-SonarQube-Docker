package com.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalaryCalculatorTest {

    private final SalaryCalculator calculator = new SalaryCalculator();

    @Test
    void shouldCalculateAnnualSalaryWithBonus() {
        assertEquals(660000.0, calculator.calculateAnnualSalary(50000, 10));
    }

    @Test
    void shouldCalculateAnnualSalaryWithoutBonus() {
        assertEquals(360000.0, calculator.calculateAnnualSalary(30000, 0));
    }

    @Test
    void shouldRejectZeroSalary() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateAnnualSalary(0, 10));
    }

    @Test
    void shouldRejectNegativeBonus() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateAnnualSalary(50000, -10));
    }
}
