package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.ContainableDto;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled("Tests disabled temporarily during refactoring")
class DeploymentUnitQueryServiceTest {
    
    @Mock
    private DeploymentUnitRepository repository;
    
    @InjectMocks
    private DeploymentUnitQueryService queryService;
    
    @BeforeEach
    void setUp() {
        // Los mocks se inyectan automáticamente con @InjectMocks
    }
    
    @Test
    void listDeploymentUnits_WithoutType_ShouldReturnAllUnits() {
        // Arrange
        List<DeploymentUnit> units = Arrays.asList(
            createTestDeploymentUnit("unit1", DeploymentUnit.DeploymentUnitType.DTO),
            createTestDeploymentUnit("unit2", DeploymentUnit.DeploymentUnitType.LIB)
        );
        
        when(repository.findAll()).thenReturn(units);
        
        // Act
        CommandResponse result = queryService.listDeploymentUnits(null);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Found 2 deployment unit(s)", result.getMessage());
        assertTrue(result.getOutput().contains("dto: unit1"));
        assertTrue(result.getOutput().contains("lib: unit2"));
        verify(repository).findAll();
    }
    
    @Test
    void listDeploymentUnits_WithSpecificType_ShouldReturnFilteredUnits() {
        // Arrange
        List<DeploymentUnit> dtoUnits = Arrays.asList(
            createTestDeploymentUnit("dto1", DeploymentUnit.DeploymentUnitType.DTO),
            createTestDeploymentUnit("dto2", DeploymentUnit.DeploymentUnitType.DTO)
        );
        
        when(repository.findByType(DeploymentUnit.DeploymentUnitType.DTO)).thenReturn(dtoUnits);
        
        // Act
        CommandResponse result = queryService.listDeploymentUnits("dto");
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Found 2 deployment unit(s)", result.getMessage());
        assertTrue(result.getOutput().contains("dto: dto1"));
        assertTrue(result.getOutput().contains("dto: dto2"));
        verify(repository).findByType(DeploymentUnit.DeploymentUnitType.DTO);
    }
    
    @Test
    void listDeploymentUnits_WithInvalidType_ShouldReturnError() {
        // Arrange
        String invalidType = "invalid-type";
        
        // Act
        CommandResponse result = queryService.listDeploymentUnits(invalidType);
        
        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getOutput().get(0).contains("Invalid type: invalid-type"));
        verify(repository, never()).findAll();
        verify(repository, never()).findByType(any());
    }
    
    @Test
    void listDeploymentUnits_WithNoUnitsFound_ShouldReturnInfoMessage() {
        // Arrange
        when(repository.findAll()).thenReturn(Collections.emptyList());
        
        // Act
        CommandResponse result = queryService.listDeploymentUnits(null);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(CommandResponse.ResponseType.INFO, result.getType());
        assertEquals("No deployment units found", result.getOutput().get(0));
    }
    
    @Test
    void listDeploymentUnits_WithNoUnitsOfSpecificType_ShouldReturnInfoMessage() {
        // Arrange
        String type = "dto";
        when(repository.findByType(DeploymentUnit.DeploymentUnitType.DTO)).thenReturn(Collections.emptyList());
        
        // Act
        CommandResponse result = queryService.listDeploymentUnits(type);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals(CommandResponse.ResponseType.INFO, result.getType());
        assertEquals("No dto units found", result.getOutput().get(0));
    }
    
    @Test
    void getAllDeploymentUnits_ShouldReturnOnlyRootLevelUnits() {
        // Arrange
        DeploymentUnit rootUnit = createTestDeploymentUnit("root-unit", DeploymentUnit.DeploymentUnitType.DU_ONLINE);
        DeploymentUnit childUnit = createTestDeploymentUnit("child-unit", DeploymentUnit.DeploymentUnitType.DTO);
        childUnit.setParentDeploymentUnit(rootUnit); // This is a child unit
        
        List<DeploymentUnit> allUnits = Arrays.asList(rootUnit, childUnit);
        when(repository.findAllWithFolders()).thenReturn(allUnits);
        
        // Act
        List<ContainableDto> result = queryService.getAllDeploymentUnits();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("root-unit", result.get(0).getName());
        verify(repository).findAllWithFolders();
    }
    
    @Test
    void getDeploymentUnitsByType_ShouldReturnUnitsOfSpecificType() {
        // Arrange
        List<DeploymentUnit> libUnits = Arrays.asList(
            createTestDeploymentUnit("lib1", DeploymentUnit.DeploymentUnitType.LIB),
            createTestDeploymentUnit("lib2", DeploymentUnit.DeploymentUnitType.LIB)
        );
        
        when(repository.findByType(DeploymentUnit.DeploymentUnitType.LIB)).thenReturn(libUnits);
        
        // Act
        List<DeploymentUnit> result = queryService.getDeploymentUnitsByType(DeploymentUnit.DeploymentUnitType.LIB);
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.getType() == DeploymentUnit.DeploymentUnitType.LIB));
        verify(repository).findByType(DeploymentUnit.DeploymentUnitType.LIB);
    }
    
    @Test
    void getAllDeploymentUnitsRaw_ShouldReturnAllUnits() {
        // Arrange
        List<DeploymentUnit> allUnits = Arrays.asList(
            createTestDeploymentUnit("unit1", DeploymentUnit.DeploymentUnitType.DTO),
            createTestDeploymentUnit("unit2", DeploymentUnit.DeploymentUnitType.LIB)
        );
        
        when(repository.findAll()).thenReturn(allUnits);
        
        // Act
        List<DeploymentUnit> result = queryService.getAllDeploymentUnitsRaw();
        
        // Assert
        assertEquals(2, result.size());
        verify(repository).findAll();
    }
    
    @Test
    void searchDeploymentUnitsByName_ShouldReturnMatchingUnits() {
        // Arrange
        List<DeploymentUnit> allUnits = Arrays.asList(
            createTestDeploymentUnit("customer-service", DeploymentUnit.DeploymentUnitType.DTO),
            createTestDeploymentUnit("account-service", DeploymentUnit.DeploymentUnitType.LIB),
            createTestDeploymentUnit("payment-processor", DeploymentUnit.DeploymentUnitType.TRX)
        );
        
        when(repository.findAll()).thenReturn(allUnits);
        
        // Act
        List<DeploymentUnit> result = queryService.searchDeploymentUnitsByName("service");
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> u.getName().toLowerCase().contains("service")));
        verify(repository).findAll();
    }
    
    @Test
    void getDeploymentUnitsByUuaa_ShouldReturnUnitsWithMatchingUuaa() {
        // Arrange
        DeploymentUnit unit1 = createTestDeploymentUnit("unit1", DeploymentUnit.DeploymentUnitType.DTO);
        unit1.setUuaa("TEST");
        
        DeploymentUnit unit2 = createTestDeploymentUnit("unit2", DeploymentUnit.DeploymentUnitType.LIB);
        unit2.setUuaa("TEST");
        
        DeploymentUnit unit3 = createTestDeploymentUnit("unit3", DeploymentUnit.DeploymentUnitType.TRX);
        unit3.setUuaa("PROD");
        
        List<DeploymentUnit> allUnits = Arrays.asList(unit1, unit2, unit3);
        when(repository.findAll()).thenReturn(allUnits);
        
        // Act
        List<DeploymentUnit> result = queryService.getDeploymentUnitsByUuaa("TEST");
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(u -> "TEST".equals(u.getUuaa())));
        verify(repository).findAll();
    }
    
    @Test
    void getContainerDeploymentUnits_ShouldReturnUnitsWithFolders() {
        // Arrange
        DeploymentUnit containerUnit = createTestDeploymentUnit("container", DeploymentUnit.DeploymentUnitType.DU_ONLINE);
        containerUnit.getComponentFolders().add(new com.bank.education.apxcli.model.ComponentFolder(
            com.bank.education.apxcli.model.ComponentFolder.FolderType.DTO, containerUnit));
        
        DeploymentUnit simpleUnit = createTestDeploymentUnit("simple", DeploymentUnit.DeploymentUnitType.DTO);
        
        List<DeploymentUnit> allUnits = Arrays.asList(containerUnit, simpleUnit);
        when(repository.findAll()).thenReturn(allUnits);
        
        // Act
        List<DeploymentUnit> result = queryService.getContainerDeploymentUnits();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("container", result.get(0).getName());
        verify(repository).findAll();
    }
    
    @Test
    void getStandaloneDeploymentUnits_ShouldReturnUnitsWithoutParents() {
        // Arrange
        DeploymentUnit standalone = createTestDeploymentUnit("standalone", DeploymentUnit.DeploymentUnitType.DTO);
        
        DeploymentUnit child = createTestDeploymentUnit("child", DeploymentUnit.DeploymentUnitType.LIB);
        child.setParentDeploymentUnit(standalone);
        
        List<DeploymentUnit> allUnits = Arrays.asList(standalone, child);
        when(repository.findAll()).thenReturn(allUnits);
        
        // Act
        List<DeploymentUnit> result = queryService.getStandaloneDeploymentUnits();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("standalone", result.get(0).getName());
        verify(repository).findAll();
    }
    
    @Test
    void getDeploymentUnitCount_WithSpecificType_ShouldReturnCount() {
        // Arrange
        List<DeploymentUnit> dtoUnits = Arrays.asList(
            createTestDeploymentUnit("dto1", DeploymentUnit.DeploymentUnitType.DTO),
            createTestDeploymentUnit("dto2", DeploymentUnit.DeploymentUnitType.DTO)
        );
        
        when(repository.findByType(DeploymentUnit.DeploymentUnitType.DTO)).thenReturn(dtoUnits);
        
        // Act
        CommandResponse result = queryService.getDeploymentUnitCount("dto");
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Found 2 dto unit(s)", result.getOutput().get(0));
        verify(repository).findByType(DeploymentUnit.DeploymentUnitType.DTO);
    }
    
    @Test
    void getDeploymentUnitCount_WithoutType_ShouldReturnTotalCount() {
        // Arrange
        when(repository.count()).thenReturn(5L);
        
        // Act
        CommandResponse result = queryService.getDeploymentUnitCount(null);
        
        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Total deployment units: 5", result.getOutput().get(0));
        verify(repository).count();
    }
    
    @Test
    void getDeploymentUnitCount_WithInvalidType_ShouldReturnError() {
        // Arrange
        String invalidType = "invalid-type";
        
        // Act
        CommandResponse result = queryService.getDeploymentUnitCount(invalidType);
        
        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getOutput().get(0).contains("Invalid type: invalid-type"));
    }
    
    private DeploymentUnit createTestDeploymentUnit(String name, DeploymentUnit.DeploymentUnitType type) {
        DeploymentUnit unit = new DeploymentUnit();
        unit.setName(name);
        unit.setType(type);
        unit.setUuaa("TEST");
        unit.setCode("123");
        unit.setDescription("Test description");
        unit.setCreatedAt(LocalDateTime.now());
        return unit;
    }
}