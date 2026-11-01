package com.example;

public class SalaryCalculator {

    public double calculateAnnualSalary(double monthlySalary, double bonusPercentage) {
        if (monthlySalary <= 0) {
            throw new IllegalArgumentException("Monthly salary must be greater than zero");
        }
        if (bonusPercentage < 0) {
            throw new IllegalArgumentException("Bonus percentage cannot be negative");
        }
        return monthlySalary * 12 * (1 + bonusPercentage / 100);
    }
}
