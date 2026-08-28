package com.example.laboratorio3.controller;

import com.example.laboratorio3.service.CalculatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    private final CalculatorService calculatorService = new CalculatorService();

    @GetMapping("/sum")
    public Map<String, Integer> sum(@RequestParam int a, @RequestParam int b) {
        return Map.of("result", calculatorService.add(a, b));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
