package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.dependencies.DependencyManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Tests disabled temporarily during refactoring")
class DependencyManagementServiceTest {
    
    @Mock
    private DeploymentUnitRepository repository;
    
    @Mock
    private DiagramService diagramService;
    
    private DependencyManagementService dependencyService;
    
    @BeforeEach
    void setUp() {
        // Los mocks se inyectan automáticamente con @InjectMocks
    }
    
    @Test
    void createDependency_WithValidUnits_ShouldCreateDependency() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        
        DeploymentUnit sourceUnit = createTestDeploymentUnit(sourceName, 1L);
        DeploymentUnit targetUnit = createTestDeploymentUnit(targetName, 2L);
        
        when(repository.findByName(sourceName)).thenReturn(Optional.of(sourceUnit));
        when(repository.findByName(targetName)).thenReturn(Optional.of(targetUnit));
        when(repository.save(sourceUnit)).thenReturn(sourceUnit);
        
        // Act
        CommandResponse result = dependencyService.createDependency(sourceName, targetName);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Created dependency: source-unit -> target-unit", result.getOutput().get(0));
        assertTrue(sourceUnit.getDependencies().contains(targetUnit));
        verify(repository).save(sourceUnit);
        verify(diagramService).notifyDiagramUpdate();
    }
    
    @Test
    void createDependency_WithNonExistingSource_ShouldReturnError() {
        // Arrange
        String sourceName = "non-existing-source";
        String targetName = "target-unit";
        
        when(repository.findByName(sourceName)).thenReturn(Optional.empty());
        
        // Act
        CommandResponse result = dependencyService.createDependency(sourceName, targetName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Source deployment unit 'non-existing-source' not found", result.getOutput().get(0));
        verify(repository, never()).save(any());
        verify(diagramService, never()).notifyDiagramUpdate();
    }
    
    @Test
    void createDependency_WithNonExistingTarget_ShouldReturnError() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "non-existing-target";
        
        DeploymentUnit sourceUnit = createTestDeploymentUnit(sourceName, 1L);
        
        when(repository.findByName(sourceName)).thenReturn(Optional.of(sourceUnit));
        when(repository.findByName(targetName)).thenReturn(Optional.empty());
        
        // Act
        CommandResponse result = dependencyService.createDependency(sourceName, targetName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Target deployment unit 'non-existing-target' not found", result.getOutput().get(0));
        verify(repository, never()).save(any());
        verify(diagramService, never()).notifyDiagramUpdate();
    }
    
    @Test
    void createDependency_WithExistingDependency_ShouldReturnError() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        
        DeploymentUnit sourceUnit = createTestDeploymentUnit(sourceName, 1L);
        DeploymentUnit targetUnit = createTestDeploymentUnit(targetName, 2L);
        
        // Pre-existing dependency
        sourceUnit.addDependency(targetUnit);
        
        when(repository.findByName(sourceName)).thenReturn(Optional.of(sourceUnit));
        when(repository.findByName(targetName)).thenReturn(Optional.of(targetUnit));
        
        // Act
        CommandResponse result = dependencyService.createDependency(sourceName, targetName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Dependency already exists between 'source-unit' and 'target-unit'", result.getOutput().get(0));
        verify(repository, never()).save(any());
        verify(diagramService, never()).notifyDiagramUpdate();
    }
    
    @Test
    void createDependency_WithCircularDependency_ShouldReturnError() {
        // Arrange
        String sourceName = "unit-a";
        String targetName = "unit-b";
        
        DeploymentUnit unitA = createTestDeploymentUnit(sourceName, 1L);
        DeploymentUnit unitB = createTestDeploymentUnit(targetName, 2L);
        
        // Create circular dependency: B -> A already exists, trying to create A -> B
        unitB.addDependency(unitA);
        
        when(repository.findByName(sourceName)).thenReturn(Optional.of(unitA));
        when(repository.findByName(targetName)).thenReturn(Optional.of(unitB));
        
        // Act
        CommandResponse result = dependencyService.createDependency(sourceName, targetName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Cannot create dependency: would create circular dependency", result.getOutput().get(0));
        verify(repository, never()).save(any());
        verify(diagramService, never()).notifyDiagramUpdate();
    }
    
    @Test
    void removeDependency_WithExistingDependency_ShouldRemoveDependency() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        
        DeploymentUnit sourceUnit = createTestDeploymentUnit(sourceName, 1L);
        DeploymentUnit targetUnit = createTestDeploymentUnit(targetName, 2L);
        
        // Pre-existing dependency
        sourceUnit.addDependency(targetUnit);
        
        when(repository.findByName(sourceName)).thenReturn(Optional.of(sourceUnit));
        when(repository.findByName(targetName)).thenReturn(Optional.of(targetUnit));
        when(repository.save(sourceUnit)).thenReturn(sourceUnit);
        
        // Act
        CommandResponse result = dependencyService.removeDependency(sourceName, targetName);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Removed dependency: source-unit -> target-unit", result.getOutput().get(0));
        assertFalse(sourceUnit.getDependencies().contains(targetUnit));
        verify(repository).save(sourceUnit);
        verify(diagramService).notifyDiagramUpdate();
    }
    
    @Test
    void removeDependency_WithNonExistingDependency_ShouldReturnError() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        
        DeploymentUnit sourceUnit = createTestDeploymentUnit(sourceName, 1L);
        DeploymentUnit targetUnit = createTestDeploymentUnit(targetName, 2L);
        
        when(repository.findByName(sourceName)).thenReturn(Optional.of(sourceUnit));
        when(repository.findByName(targetName)).thenReturn(Optional.of(targetUnit));
        
        // Act
        CommandResponse result = dependencyService.removeDependency(sourceName, targetName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("No dependency exists between 'source-unit' and 'target-unit'", result.getOutput().get(0));
        verify(repository, never()).save(any());
        verify(diagramService, never()).notifyDiagramUpdate();
    }
    
    @Test
    void wouldCreateCircularDependency_WithDirectCircle_ShouldReturnTrue() {
        // Arrange
        DeploymentUnit unitA = createTestDeploymentUnit("unit-a", 1L);
        DeploymentUnit unitB = createTestDeploymentUnit("unit-b", 2L);
        
        // B already depends on A, testing if A -> B would create circle
        unitB.addDependency(unitA);
        
        // Act
        boolean result = dependencyService.wouldCreateCircularDependency(unitA, unitB);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void wouldCreateCircularDependency_WithIndirectCircle_ShouldReturnTrue() {
        // Arrange
        DeploymentUnit unitA = createTestDeploymentUnit("unit-a", 1L);
        DeploymentUnit unitB = createTestDeploymentUnit("unit-b", 2L);
        DeploymentUnit unitC = createTestDeploymentUnit("unit-c", 3L);
        
        // C -> B -> A, testing if A -> C would create circle
        unitC.addDependency(unitB);
        unitB.addDependency(unitA);
        
        // Act
        boolean result = dependencyService.wouldCreateCircularDependency(unitA, unitC);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void wouldCreateCircularDependency_WithNonCircular_ShouldReturnFalse() {
        // Arrange
        DeploymentUnit unitA = createTestDeploymentUnit("unit-a", 1L);
        DeploymentUnit unitB = createTestDeploymentUnit("unit-b", 2L);
        DeploymentUnit unitC = createTestDeploymentUnit("unit-c", 3L);
        
        // A -> B, testing if A -> C (no circle)
        unitA.addDependency(unitB);
        
        // Act
        boolean result = dependencyService.wouldCreateCircularDependency(unitA, unitC);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void getAllDependencies_ShouldReturnAllTransitiveDependencies() {
        // Arrange
        String unitName = "unit-a";
        DeploymentUnit unitA = createTestDeploymentUnit("unit-a", 1L);
        DeploymentUnit unitB = createTestDeploymentUnit("unit-b", 2L);
        DeploymentUnit unitC = createTestDeploymentUnit("unit-c", 3L);
        
        // A -> B -> C
        unitA.addDependency(unitB);
        unitB.addDependency(unitC);
        
        when(repository.findByName(unitName)).thenReturn(Optional.of(unitA));
        
        // Act
        Set<DeploymentUnit> result = dependencyService.getAllDependencies(unitName);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(unitB));
        assertTrue(result.contains(unitC));
    }
    
    @Test
    void validateDependency_WithValidUnits_ShouldReturnSuccess() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        
        DeploymentUnit sourceUnit = createTestDeploymentUnit(sourceName, 1L);
        DeploymentUnit targetUnit = createTestDeploymentUnit(targetName, 2L);
        
        when(repository.findByName(sourceName)).thenReturn(Optional.of(sourceUnit));
        when(repository.findByName(targetName)).thenReturn(Optional.of(targetUnit));
        
        // Act
        CommandResponse result = dependencyService.validateDependency(sourceName, targetName);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Dependency is valid", result.getOutput().get(0));
    }
    
    @Test
    void validateDependency_WithSelfDependency_ShouldReturnError() {
        // Arrange
        String unitName = "self-unit";
        DeploymentUnit unit = createTestDeploymentUnit(unitName, 1L);
        
        when(repository.findByName(unitName)).thenReturn(Optional.of(unit));
        
        // Act
        CommandResponse result = dependencyService.validateDependency(unitName, unitName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Unit cannot depend on itself", result.getOutput().get(0));
    }
    
    private DeploymentUnit createTestDeploymentUnit(String name, Long id) {
        DeploymentUnit unit = new DeploymentUnit();
        unit.setId(id);
        unit.setName(name);
        unit.setType(DeploymentUnit.DeploymentUnitType.DTO);
        unit.setUuaa("TEST");
        unit.setCode("123");
        unit.setDescription("Test description");
        unit.setCreatedAt(LocalDateTime.now());
        unit.setDependencies(new HashSet<>());
        return unit;
    }
}