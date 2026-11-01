package com.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class App {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        SalaryCalculator calculator = new SalaryCalculator();

        server.createContext("/health", exchange ->
                sendResponse(exchange, 200, "Application is running!"));

        server.createContext("/salary", exchange -> {
            double annualSalary = calculator.calculateAnnualSalary(50000, 10);
            sendResponse(exchange, 200, "Annual salary: " + annualSalary);
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Employee Salary Calculator started on port 8080");
    }

    private static void sendResponse(HttpExchange exchange, int status, String response)
            throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
