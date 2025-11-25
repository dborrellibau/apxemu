package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.ContainableDto;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiagramService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final DeploymentUnitRepository repository;
    
    public DiagramService(SimpMessagingTemplate messagingTemplate, DeploymentUnitRepository repository) {
        this.messagingTemplate = messagingTemplate;
        this.repository = repository;
    }
    
    public void notifyDiagramUpdate() {
        try {
            List<ContainableDto> units = repository.findAllWithFolders().stream()
                .filter(du -> du.getParentDeploymentUnit() == null && du.getParentFolder() == null) // Only root level units
                .map(ContainableDto::from)
                .collect(Collectors.toList());
            DiagramData diagramData = new DiagramData(units);
            messagingTemplate.convertAndSend("/topic/diagram-updates", diagramData);
        } catch (Exception e) {
            System.err.println("Error notifying diagram update: " + e.getMessage());
        }
    }
    
    public static class DiagramData {
        private List<ContainableDto> units;
        
        public DiagramData() {}
        
        public DiagramData(List<ContainableDto> units) {
            this.units = units;
        }
        
        public List<ContainableDto> getUnits() {
            return units;
        }
        
        public void setUnits(List<ContainableDto> units) {
            this.units = units;
        }
    }
}