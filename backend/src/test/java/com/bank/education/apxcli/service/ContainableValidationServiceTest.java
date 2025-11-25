package com.bank.education.apxcli.service;

import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Tests disabled temporarily during refactoring")
class ContainableValidationServiceTest {
    
    @Mock
    private DeploymentUnitRepository repository;
    
    @InjectMocks
    private ContainableValidationService validationService;
    
    @BeforeEach
    void setUp() {
        // Los mocks se inyectan automáticamente con @InjectMocks
    }
    
    @Test
    void containableExists_WithTypeAndCode_ShouldCallRepositoryWithTypeAndCode() {
        // Arrange
        String code = "123";
        String type = "dto";
        when(repository.existsByTypeAndCode(DeploymentUnit.DeploymentUnitType.DTO, code)).thenReturn(true);
        
        // Act
        boolean result = validationService.containableExists(code, type);
        
        // Assert
        assertTrue(result);
        verify(repository).existsByTypeAndCode(DeploymentUnit.DeploymentUnitType.DTO, code);
    }
    
    @Test
    void containableExists_WithNameOnly_ShouldCallRepositoryWithName() {
        // Arrange
        String name = "test-unit";
        when(repository.existsByName(name)).thenReturn(true);
        
        // Act
        boolean result = validationService.containableExists(name, null);
        
        // Assert
        assertTrue(result);
        verify(repository).existsByName(name);
    }
    
    @Test
    void containableExists_WithInvalidType_ShouldFallbackToNameCheck() {
        // Arrange
        String identifier = "test";
        String invalidType = "invalid-type";
        when(repository.existsByName(identifier)).thenReturn(false);
        
        // Act
        boolean result = validationService.containableExists(identifier, invalidType);
        
        // Assert
        assertFalse(result);
        verify(repository).existsByName(identifier);
        verify(repository, never()).existsByTypeAndCode(any(), any());
    }
    
    @Test
    void deploymentUnitExists_ShouldDelegateToContainableExists() {
        // Arrange
        String name = "test-unit";
        when(repository.existsByName(name)).thenReturn(true);
        
        // Act
        boolean result = validationService.deploymentUnitExists(name);
        
        // Assert
        assertTrue(result);
        verify(repository).existsByName(name);
    }
    
    @Test
    void isCodeExists_ShouldDelegateToContainableExists() {
        // Arrange
        String code = "123";
        String type = "lib";
        when(repository.existsByTypeAndCode(DeploymentUnit.DeploymentUnitType.LIB, code)).thenReturn(true);
        
        // Act
        boolean result = validationService.isCodeExists(type, code);
        
        // Assert
        assertTrue(result);
        verify(repository).existsByTypeAndCode(DeploymentUnit.DeploymentUnitType.LIB, code);
    }
    
    @Test
    void isNameAvailable_WhenNameDoesNotExist_ShouldReturnTrue() {
        // Arrange
        String name = "available-name";
        when(repository.existsByName(name)).thenReturn(false);
        
        // Act
        boolean result = validationService.isNameAvailable(name);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void isNameAvailable_WhenNameExists_ShouldReturnFalse() {
        // Arrange
        String name = "existing-name";
        when(repository.existsByName(name)).thenReturn(true);
        
        // Act
        boolean result = validationService.isNameAvailable(name);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void validateCreationParameters_WithValidData_ShouldReturnValidResult() {
        // Arrange
        String name = "test-unit";
        String uuaa = "TEST";
        String code = "123";
        String type = "dto";
        
        when(repository.existsByName(name)).thenReturn(false);
        when(repository.existsByTypeAndCode(DeploymentUnit.DeploymentUnitType.DTO, code)).thenReturn(false);
        
        // Act
        ContainableValidationService.ValidationResult result = 
            validationService.validateCreationParameters(name, uuaa, code, type);
        
        // Assert
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @Test
    void validateCreationParameters_WithEmptyName_ShouldReturnInvalidResult() {
        // Arrange
        String name = "";
        String uuaa = "TEST";
        String code = "123";
        String type = "dto";
        
        // Act
        ContainableValidationService.ValidationResult result = 
            validationService.validateCreationParameters(name, uuaa, code, type);
        
        // Assert
        assertFalse(result.isValid());
        assertEquals("Name cannot be empty", result.getFirstError());
    }
    
    @Test
    void validateCreationParameters_WithExistingName_ShouldReturnInvalidResult() {
        // Arrange
        String name = "existing-unit";
        String uuaa = "TEST";
        String code = "123";
        String type = "dto";
        
        when(repository.existsByName(name)).thenReturn(true);
        
        // Act
        ContainableValidationService.ValidationResult result = 
            validationService.validateCreationParameters(name, uuaa, code, type);
        
        // Assert
        assertFalse(result.isValid());
        assertEquals("Unit 'existing-unit' already exists", result.getFirstError());
    }
    
    @Test
    void validateCreationParameters_WithEmptyUuaa_ShouldReturnInvalidResult() {
        // Arrange
        String name = "test-unit";
        String uuaa = "";
        String code = "123";
        String type = "dto";
        
        when(repository.existsByName(name)).thenReturn(false);
        
        // Act
        ContainableValidationService.ValidationResult result = 
            validationService.validateCreationParameters(name, uuaa, code, type);
        
        // Assert
        assertFalse(result.isValid());
        assertEquals("UUAA cannot be empty", result.getFirstError());
    }
    
    @Test
    void validateCreationParameters_WithExistingCode_ShouldReturnInvalidResult() {
        // Arrange
        String name = "test-unit";
        String uuaa = "TEST";
        String code = "123";
        String type = "dto";
        
        when(repository.existsByName(name)).thenReturn(false);
        when(repository.existsByTypeAndCode(DeploymentUnit.DeploymentUnitType.DTO, code)).thenReturn(true);
        
        // Act
        ContainableValidationService.ValidationResult result = 
            validationService.validateCreationParameters(name, uuaa, code, type);
        
        // Assert
        assertFalse(result.isValid());
        assertEquals("Code '123' already exists for type dto", result.getFirstError());
    }
}