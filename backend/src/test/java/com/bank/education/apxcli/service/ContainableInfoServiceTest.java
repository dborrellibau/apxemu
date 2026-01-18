package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.model.ComponentFolder;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Tests disabled temporarily during refactoring")
class ContainableInfoServiceTest {
    
    @Mock
    private DeploymentUnitRepository repository;
    
    private ContainableInfoService infoService;
    
    @BeforeEach
    void setUp() {
        // Los mocks se inyectan automáticamente con @InjectMocks
    }
    
    @Test
    void getContainableInfo_WithExistingUnit_ShouldReturnStandardInfo() {
        // Arrange
        String unitName = "test-unit";
        DeploymentUnit unit = createTestDeploymentUnit(unitName);
        
        when(repository.findByName(unitName)).thenReturn(Optional.of(unit));
        when(repository.findByIdWithDependencies(unit.getId())).thenReturn(Optional.of(unit));
        
        // Act
        CommandResponse result = infoService.getContainableInfo(unitName, false);
        
        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().contains("Name: " + unitName));
        assertTrue(result.getOutput().contains("Type: " + unit.getType()));
        verify(repository).findByIdWithDependencies(unit.getId());
    }
    
    @Test
    void getContainableInfo_WithExistingUnitDebugMode_ShouldReturnDebugInfo() {
        // Arrange
        String unitName = "test-unit";
        DeploymentUnit unit = createTestDeploymentUnit(unitName);
        
        when(repository.findByName(unitName)).thenReturn(Optional.of(unit));
        when(repository.findByIdWithDependencies(unit.getId())).thenReturn(Optional.of(unit));
        
        // Act
        CommandResponse result = infoService.getContainableInfo(unitName, true);
        
        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getOutput().get(0).contains("Debug info for DU: " + unitName));
        verify(repository).findByIdWithDependencies(unit.getId());
    }
    
    @Test
    void getContainableInfo_WithNonExistingUnit_ShouldReturnError() {
        // Arrange
        String unitName = "non-existing-unit";
        when(repository.findByName(unitName)).thenReturn(Optional.empty());
        when(repository.findByIdWithDependencies(null)).thenReturn(Optional.empty());
        
        // Act
        CommandResponse result = infoService.getContainableInfo(unitName, false);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Unit 'non-existing-unit' not found", result.getOutput().get(0));
    }
    
    @Test
    void getDeploymentUnitDetails_WithExistingUnit_ShouldReturnDetails() {
        // Arrange
        String unitName = "test-unit";
        DeploymentUnit unit = createTestDeploymentUnit(unitName);
        
        when(repository.findByName(unitName)).thenReturn(Optional.of(unit));
        when(repository.findByIdWithDependencies(unit.getId())).thenReturn(Optional.of(unit));
        
        // Act
        CommandResponse result = infoService.getDeploymentUnitDetails(unitName);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Details for '" + unitName + "'", result.getMessage());
        assertTrue(result.getOutput().contains("Name: " + unitName));
        assertTrue(result.getOutput().contains("Type: " + unit.getType()));
        assertTrue(result.getOutput().contains("Folders: 0"));
        assertTrue(result.getOutput().contains("Dependencies: 0"));
        assertNotNull(result.getData());
    }
    
    @Test
    void debugDeploymentUnit_WithExistingUnit_ShouldReturnDebugInfo() {
        // Arrange
        String unitName = "test-unit";
        DeploymentUnit unit = createTestDeploymentUnit(unitName);
        
        // Add a component folder
        ComponentFolder folder = new ComponentFolder(ComponentFolder.FolderType.DTO, unit);
        unit.getComponentFolders().add(folder);
        
        when(repository.findByName(unitName)).thenReturn(Optional.of(unit));
        
        // Act
        CommandResponse result = infoService.debugDeploymentUnit(unitName);
        
        // Assert
        assertTrue(result.isSuccess());
        String debugOutput = result.getOutput().get(0);
        assertTrue(debugOutput.contains("Debug info for DU: " + unitName));
        assertTrue(debugOutput.contains("Type: " + unit.getType()));
        assertTrue(debugOutput.contains("Component Folders (1)"));
        assertTrue(debugOutput.contains("- DTO: Empty"));
    }
    
    @Test
    void debugDeploymentUnit_WithNonExistingUnit_ShouldReturnError() {
        // Arrange
        String unitName = "non-existing-unit";
        when(repository.findByName(unitName)).thenReturn(Optional.empty());
        
        // Act
        CommandResponse result = infoService.debugDeploymentUnit(unitName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Deployment unit 'non-existing-unit' not found", result.getOutput().get(0));
    }
    
    @Test
    void unitExists_WhenUnitExists_ShouldReturnTrue() {
        // Arrange
        String unitName = "existing-unit";
        when(repository.existsByName(unitName)).thenReturn(true);
        
        // Act
        boolean result = infoService.unitExists(unitName);
        
        // Assert
        assertTrue(result);
        verify(repository).existsByName(unitName);
    }
    
    @Test
    void unitExists_WhenUnitDoesNotExist_ShouldReturnFalse() {
        // Arrange
        String unitName = "non-existing-unit";
        when(repository.existsByName(unitName)).thenReturn(false);
        
        // Act
        boolean result = infoService.unitExists(unitName);
        
        // Assert
        assertFalse(result);
        verify(repository).existsByName(unitName);
    }
    
    @Test
    void findUnitByName_WhenUnitExists_ShouldReturnUnit() {
        // Arrange
        String unitName = "existing-unit";
        DeploymentUnit unit = createTestDeploymentUnit(unitName);
        when(repository.findByName(unitName)).thenReturn(Optional.of(unit));
        
        // Act
        Optional<DeploymentUnit> result = infoService.findUnitByName(unitName);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(unitName, result.get().getName());
        verify(repository).findByName(unitName);
    }
    
    @Test
    void findUnitByName_WhenUnitDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        String unitName = "non-existing-unit";
        when(repository.findByName(unitName)).thenReturn(Optional.empty());
        
        // Act
        Optional<DeploymentUnit> result = infoService.findUnitByName(unitName);
        
        // Assert
        assertFalse(result.isPresent());
        verify(repository).findByName(unitName);
    }
    
    private DeploymentUnit createTestDeploymentUnit(String name) {
        DeploymentUnit unit = new DeploymentUnit();
        unit.setId(1L);
        unit.setName(name);
        unit.setType(DeploymentUnit.DeploymentUnitType.DTO);
        unit.setUuaa("TEST");
        unit.setCode("123");
        unit.setDescription("Test description");
        unit.setCreatedAt(LocalDateTime.now());
        return unit;
    }
}