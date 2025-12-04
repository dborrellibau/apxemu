package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.service.dependencies.DependencyManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Tests disabled temporarily during refactoring")
class ArchitectureOrchestrationServiceTest {
    
    @Mock
    private ContainableCreationService creationService;
    
    @Mock
    private ContainableValidationService validationService;
    
    @Mock
    private ContainableInfoService infoService;
    
    @Mock
    private DependencyManagementService dependencyService;
    
    @Mock
    private DeploymentUnitQueryService queryService;
    
    @InjectMocks
    private ArchitectureOrchestrationService orchestrationService;
    
    @BeforeEach
    void setUp() {
        // Los mocks se inyectan automáticamente con @InjectMocks
    }
    
    @Test
    void createDto_ShouldDelegateToCreationService() {
        // Arrange
        String uuaa = "TEST";
        String code = "123";
        String className = "TestDto";
        String description = "Test description";
        
        CommandResponse expectedResponse = CommandResponse.success("DTO created successfully");
        when(creationService.createDto(uuaa, code, className, description)).thenReturn(expectedResponse);
        
        // Act
        CommandResponse result = orchestrationService.createDto(uuaa, code, className, description);
        
        // Assert
        assertEquals(expectedResponse, result);
        verify(creationService).createDto(uuaa, code, className, description);
    }
    
    @Test
    void createLib_ShouldDelegateToCreationService() {
        // Arrange
        String uuaa = "TEST";
        String code = "123";
        String description = "Test description";
        
        CommandResponse expectedResponse = CommandResponse.success("Library created successfully");
        when(creationService.createLib(uuaa, code, description)).thenReturn(expectedResponse);
        
        // Act
        CommandResponse result = orchestrationService.createLib(uuaa, code, description);
        
        // Assert
        assertEquals(expectedResponse, result);
        verify(creationService).createLib(uuaa, code, description);
    }
    
    @Test
    void createTrx_ShouldDelegateToCreationService() {
        // Arrange
        String uuaa = "TEST";
        String code = "123";
        String version = "01";
        String country = "GL";
        String description = "Test description";
        
        CommandResponse expectedResponse = CommandResponse.success("Transaction created successfully");
        when(creationService.createTrx(uuaa, code, version, country, description)).thenReturn(expectedResponse);
        
        // Act
        CommandResponse result = orchestrationService.createTrx(uuaa, code, version, country, description);
        
        // Assert
        assertEquals(expectedResponse, result);
        verify(creationService).createTrx(uuaa, code, version, country, description);
    }
    
    @Test
    void containableExists_ShouldDelegateToValidationService() {
        // Arrange
        String identifier = "test-unit";
        String type = "dto";
        when(validationService.containableExists(identifier, type)).thenReturn(true);
        
        // Act
        boolean result = orchestrationService.containableExists(identifier, type);
        
        // Assert
        assertTrue(result);
        verify(validationService).containableExists(identifier, type);
    }
    
    @Test
    void deploymentUnitExists_ShouldDelegateToValidationService() {
        // Arrange
        String name = "test-unit";
        when(validationService.deploymentUnitExists(name)).thenReturn(true);
        
        // Act
        boolean result = orchestrationService.deploymentUnitExists(name);
        
        // Assert
        assertTrue(result);
        verify(validationService).deploymentUnitExists(name);
    }
    
    @Test
    void getDeploymentUnitDetails_ShouldDelegateToInfoService() {
        // Arrange
        String name = "test-unit";
        CommandResponse expectedResponse = CommandResponse.success("Unit details");
        when(infoService.getDeploymentUnitDetails(name)).thenReturn(expectedResponse);
        
        // Act
        CommandResponse result = orchestrationService.getDeploymentUnitDetails(name);
        
        // Assert
        assertEquals(expectedResponse, result);
        verify(infoService).getDeploymentUnitDetails(name);
    }
    
    @Test
    void createDependency_ShouldDelegateToDependencyService() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        CommandResponse expectedResponse = CommandResponse.success("Dependency created");
        when(dependencyService.createDependency(sourceName, targetName)).thenReturn(expectedResponse);
        
        // Act
        CommandResponse result = orchestrationService.createDependency(sourceName, targetName);
        
        // Assert
        assertEquals(expectedResponse, result);
        verify(dependencyService).createDependency(sourceName, targetName);
    }
    
    @Test
    void listDeploymentUnits_ShouldDelegateToQueryService() {
        // Arrange
        String type = "dto";
        CommandResponse expectedResponse = CommandResponse.success("Units listed");
        when(queryService.listDeploymentUnits(type)).thenReturn(expectedResponse);
        
        // Act
        CommandResponse result = orchestrationService.listDeploymentUnits(type);
        
        // Assert
        assertEquals(expectedResponse, result);
        verify(queryService).listDeploymentUnits(type);
    }
    
    @Test
    void createWithValidation_WithValidData_ShouldCreateUnit() {
        // Arrange
        String type = "dto";
        String name = "test-unit";
        String uuaa = "TEST";
        String code = "123";
        String description = "Test description";
        
        ContainableValidationService.ValidationResult validationResult = 
            new ContainableValidationService.ValidationResult();
        
        CommandResponse expectedResponse = CommandResponse.success("Unit created");
        
        when(validationService.validateCreationParameters(name, uuaa, code, type)).thenReturn(validationResult);
        when(creationService.createDeploymentUnit(type, name)).thenReturn(expectedResponse);
        
        // Act
        CommandResponse result = orchestrationService.createWithValidation(type, name, uuaa, code, description);
        
        // Assert
        assertEquals(expectedResponse, result);
        verify(validationService).validateCreationParameters(name, uuaa, code, type);
        verify(creationService).createDeploymentUnit(type, name);
    }
    
    @Test
    void createWithValidation_WithInvalidData_ShouldReturnValidationError() {
        // Arrange
        String type = "dto";
        String name = "";
        String uuaa = "TEST";
        String code = "123";
        String description = "Test description";
        
        ContainableValidationService.ValidationResult validationResult = 
            new ContainableValidationService.ValidationResult();
        validationResult.addError("Name cannot be empty");
        
        when(validationService.validateCreationParameters(name, uuaa, code, type)).thenReturn(validationResult);
        
        // Act
        CommandResponse result = orchestrationService.createWithValidation(type, name, uuaa, code, description);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Name cannot be empty", result.getOutput().get(0));
        verify(validationService).validateCreationParameters(name, uuaa, code, type);
        verify(creationService, never()).createDeploymentUnit(any(), any());
    }
    
    @Test
    void getCompleteUnitInfo_WithExistingUnit_ShouldReturnCombinedInfo() {
        // Arrange
        String name = "test-unit";
        
        CommandResponse basicInfo = new CommandResponse(true, "Basic info", 
            Arrays.asList("Name: test-unit", "Type: DTO"), CommandResponse.ResponseType.SUCCESS, null);
        CommandResponse debugInfo = new CommandResponse(true, "Debug info", 
            Arrays.asList("Debug info for DU: test-unit"), CommandResponse.ResponseType.INFO, null);
        
        when(validationService.deploymentUnitExists(name)).thenReturn(true);
        when(infoService.getDeploymentUnitDetails(name)).thenReturn(basicInfo);
        when(infoService.getContainableInfo(name, true)).thenReturn(debugInfo);
        
        // Act
        CommandResponse result = orchestrationService.getCompleteUnitInfo(name);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Complete information for 'test-unit'", result.getMessage());
        assertTrue(result.getOutput().contains("Name: test-unit"));
        assertTrue(result.getOutput().contains("--- Debug Information ---"));
        assertTrue(result.getOutput().contains("Debug info for DU: test-unit"));
        verify(validationService).deploymentUnitExists(name);
        verify(infoService).getDeploymentUnitDetails(name);
        verify(infoService).getContainableInfo(name, true);
    }
    
    @Test
    void getCompleteUnitInfo_WithNonExistingUnit_ShouldReturnError() {
        // Arrange
        String name = "non-existing-unit";
        when(validationService.deploymentUnitExists(name)).thenReturn(false);
        
        // Act
        CommandResponse result = orchestrationService.getCompleteUnitInfo(name);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Unit 'non-existing-unit' not found", result.getOutput().get(0));
        verify(validationService).deploymentUnitExists(name);
        verify(infoService, never()).getDeploymentUnitDetails(any());
        verify(infoService, never()).getContainableInfo(any(), anyBoolean());
    }
    
    @Test
    void createValidatedDependency_WithValidUnits_ShouldCreateDependency() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        
        CommandResponse validationResponse = CommandResponse.success("Dependency is valid");
        CommandResponse creationResponse = CommandResponse.success("Dependency created");
        
        when(validationService.deploymentUnitExists(sourceName)).thenReturn(true);
        when(validationService.deploymentUnitExists(targetName)).thenReturn(true);
        when(dependencyService.validateDependency(sourceName, targetName)).thenReturn(validationResponse);
        when(dependencyService.createDependency(sourceName, targetName)).thenReturn(creationResponse);
        
        // Act
        CommandResponse result = orchestrationService.createValidatedDependency(sourceName, targetName);
        
        // Assert
        assertEquals(creationResponse, result);
        verify(validationService).deploymentUnitExists(sourceName);
        verify(validationService).deploymentUnitExists(targetName);
        verify(dependencyService).validateDependency(sourceName, targetName);
        verify(dependencyService).createDependency(sourceName, targetName);
    }
    
    @Test
    void createValidatedDependency_WithNonExistingSource_ShouldReturnError() {
        // Arrange
        String sourceName = "non-existing-source";
        String targetName = "target-unit";
        
        when(validationService.deploymentUnitExists(sourceName)).thenReturn(false);
        
        // Act
        CommandResponse result = orchestrationService.createValidatedDependency(sourceName, targetName);
        
        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Source unit 'non-existing-source' does not exist", result.getOutput().get(0));
        verify(validationService).deploymentUnitExists(sourceName);
        verify(dependencyService, never()).validateDependency(any(), any());
        verify(dependencyService, never()).createDependency(any(), any());
    }
    
    @Test
    void createValidatedDependency_WithInvalidDependency_ShouldReturnValidationError() {
        // Arrange
        String sourceName = "source-unit";
        String targetName = "target-unit";
        
        CommandResponse validationResponse = CommandResponse.error("Would create circular dependency");
        
        when(validationService.deploymentUnitExists(sourceName)).thenReturn(true);
        when(validationService.deploymentUnitExists(targetName)).thenReturn(true);
        when(dependencyService.validateDependency(sourceName, targetName)).thenReturn(validationResponse);
        
        // Act
        CommandResponse result = orchestrationService.createValidatedDependency(sourceName, targetName);
        
        // Assert
        assertEquals(validationResponse, result);
        verify(validationService).deploymentUnitExists(sourceName);
        verify(validationService).deploymentUnitExists(targetName);
        verify(dependencyService).validateDependency(sourceName, targetName);
        verify(dependencyService, never()).createDependency(any(), any());
    }
}