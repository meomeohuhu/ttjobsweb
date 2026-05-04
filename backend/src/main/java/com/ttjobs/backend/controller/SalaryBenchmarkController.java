package com.ttjobs.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttjobs.backend.dto.SalaryBenchmarkDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tools")
public class SalaryBenchmarkController {
    private final List<SalaryBenchmarkDTO> benchmarks;

    public SalaryBenchmarkController(ObjectMapper objectMapper) throws IOException {
        this.benchmarks = objectMapper.readValue(
                new ClassPathResource("data/salary-benchmark.json").getInputStream(),
                new TypeReference<List<SalaryBenchmarkDTO>>() {}
        );
    }

    @GetMapping("/salary-benchmark")
    public List<SalaryBenchmarkDTO> getSalaryBenchmark(@RequestParam(required = false) String industry,
                                                       @RequestParam(required = false) String location,
                                                       @RequestParam(required = false) String level) {
        return benchmarks.stream()
                .filter(row -> matches(row.getIndustry(), industry))
                .filter(row -> matches(row.getLocation(), location))
                .filter(row -> matches(row.getLevel(), level))
                .toList();
    }

    private boolean matches(String value, String query) {
        return query == null || query.isBlank() || value.equalsIgnoreCase(query.trim());
    }
}
