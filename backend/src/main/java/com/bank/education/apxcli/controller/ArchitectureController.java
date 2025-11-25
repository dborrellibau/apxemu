package com.bank.education.apxcli.controller;

import com.bank.education.apxcli.dto.ContainableDto;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/architecture")
@CrossOrigin(origins = "*")
public class ArchitectureController {
    
    private final ArchitectureOrchestrationService architectureService;
    
    public ArchitectureController(ArchitectureOrchestrationService architectureService) {
        this.architectureService = architectureService;
    }
    
    @GetMapping("/units")
    public ResponseEntity<List<ContainableDto>> getAllDeploymentUnits() {
        return ResponseEntity.ok(architectureService.getAllDeploymentUnits());
    }
}