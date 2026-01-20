package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Tests disabled temporarily during refactoring")
class ArchitectureServicesIntegrationTest {
    
    @Autowired
    private ArchitectureOrchestrationService orchestrationService;
    
    @Autowired
    private ContainableValidationService validationService;
    
    @Autowired
    private ContainableInfoService infoService;
    
    @MockBean
    private DiagramService diagramService;
    
    @Autowired
    private DeploymentUnitRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @Test
    void completeWorkflow_CreateValidateAndQueryDeploymentUnits_ShouldWorkEndToEnd() {
        // Step 1: Create a DTO through orchestration service
        CommandResponse createResponse = orchestrationService.createDto("TEST", "123", "TestDto", "Test DTO description");
        assertTrue(createResponse.isSuccess());
        assertTrue(createResponse.getOutput().get(0).contains("Created DTO"));
        
        // Step 2: Validate that the DTO exists
        assertTrue(validationService.deploymentUnitExists("TESTC123"));
        assertTrue(validationService.containableExists("123", "dto"));
        assertTrue(validationService.isCodeExists("dto", "123"));
        
        // Step 3: Create a Library
        CommandResponse createLibResponse = orchestrationService.createLib("TEST", "456", "Test Library description");
        assertTrue(createLibResponse.isSuccess());
        
        // Step 4: List all deployment units
        CommandResponse listResponse = orchestrationService.listDeploymentUnits(null);
        assertTrue(listResponse.isSuccess());
        assertEquals("Found 2 deployment unit(s)", listResponse.getMessage());
        assertTrue(listResponse.getOutput().contains("dto: TESTC123"));
        assertTrue(listResponse.getOutput().contains("lib: TESTR456"));
        
        // Step 5: Get detailed information about the DTO
        CommandResponse detailsResponse = orchestrationService.getDeploymentUnitDetails("TESTC123");
        assertTrue(detailsResponse.isSuccess());
        assertTrue(detailsResponse.getOutput().contains("Name: TESTC123"));
        assertTrue(detailsResponse.getOutput().contains("Type: DTO"));
        
        // Step 6: Create a dependency
        CommandResponse depResponse = orchestrationService.createDependency("TESTR456", "TESTC123");
        assertTrue(depResponse.isSuccess());
        assertEquals("Created dependency: TESTR456 -> TESTC123", depResponse.getOutput().get(0));
        
        // Step 7: Verify the dependency exists in the database
        DeploymentUnit libUnit = repository.findByName("TESTR456").orElse(null);
        DeploymentUnit dtoUnit = repository.findByName("TESTC123").orElse(null);
        assertNotNull(libUnit);
        assertNotNull(dtoUnit);
        assertTrue(libUnit.getDependencies().contains(dtoUnit));
        
        // Verify diagram service was notified for all creation and dependency operations
        verify(diagramService, atLeast(3)).notifyDiagramUpdate();
    }
    
    @Test
    void validationWorkflow_DuplicateCreation_ShouldBePreventedByValidation() {
        // Step 1: Create initial DTO
        CommandResponse firstCreate = orchestrationService.createDto("TEST", "999", "FirstDto", "First DTO");
        assertTrue(firstCreate.isSuccess());
        
        // Step 2: Try to create duplicate DTO with same code
        CommandResponse secondCreate = orchestrationService.createDto("TEST", "999", "SecondDto", "Second DTO");
        assertFalse(secondCreate.isSuccess());
        assertTrue(secondCreate.getOutput().get(0).contains("already exists"));
        
        // Step 3: Verify only one unit was created
        CommandResponse listResponse = orchestrationService.listDeploymentUnits("dto");
        assertTrue(listResponse.isSuccess());
        assertEquals("Found 1 deployment unit(s)", listResponse.getMessage());
    }
    
    @Test
    void dependencyWorkflow_CircularDependencyPrevention_ShouldWork() {
        // Step 1: Create two DTOs
        orchestrationService.createDto("TEST", "111", "Dto1", "First DTO");
        orchestrationService.createDto("TEST", "222", "Dto2", "Second DTO");
        
        // Step 2: Create dependency A -> B
        CommandResponse firstDep = orchestrationService.createDependency("TESTC111", "TESTC222");
        assertTrue(firstDep.isSuccess());
        
        // Step 3: Try to create circular dependency B -> A
        CommandResponse circularDep = orchestrationService.createDependency("TESTC222", "TESTC111");
        assertFalse(circularDep.isSuccess());
        assertTrue(circularDep.getOutput().get(0).contains("circular dependency"));
    }
    
    @Test
    void containerWorkflow_CreateDuWithObjectsInFolders_ShouldWork() {
        // Step 1: Create DU-ONLINE container
        CommandResponse duCreate = orchestrationService.createDuOnline("TEST", "test-container", "Test container");
        assertTrue(duCreate.isSuccess());
        
        // Step 2: Create DTO in the container's DTO folder
        CommandResponse dtoInFolder = orchestrationService.createDtoInFolder("test-container", "TEST", "333", "ContainerDto", "DTO in container");
        assertTrue(dtoInFolder.isSuccess());
        assertTrue(dtoInFolder.getOutput().get(0).contains("Created DTO 'TESTC333' in test-container/dto"));
        
        // Step 3: Create Library in the container's library folder
        CommandResponse libInFolder = orchestrationService.createLibInFolder("test-container", "TEST", "444", "Library in container");
        assertTrue(libInFolder.isSuccess());
        assertTrue(libInFolder.getOutput().get(0).contains("Created LIB 'TESTR444' in test-container/library"));
        
        // Step 4: Debug the container to see its contents
        CommandResponse debugResponse = orchestrationService.debugDeploymentUnit("test-container");
        assertTrue(debugResponse.isSuccess());
        String debugOutput = debugResponse.getOutput().get(0);
        assertTrue(debugOutput.contains("Component Folders"));
        assertTrue(debugOutput.contains("DTO:"));
        assertTrue(debugOutput.contains("LIBRARY:"));
    }
    
    @Test
    void queryWorkflow_FilteringAndSearching_ShouldWork() {
        // Step 1: Create various types of deployment units
        orchestrationService.createDto("TEST", "100", "SearchDto", "Searchable DTO");
        orchestrationService.createLib("TEST", "200", "Search Library");
        orchestrationService.createTrx("TEST", "300", "01", "GL", "Search Transaction");
        
        // Step 2: Query by specific type
        CommandResponse dtoList = orchestrationService.listDeploymentUnits("dto");
        assertTrue(dtoList.isSuccess());
        assertEquals("Found 1 deployment unit(s)", dtoList.getMessage());
        assertTrue(dtoList.getOutput().contains("dto: TESTC100"));
        
        // Step 3: Query all units
        CommandResponse allList = orchestrationService.listDeploymentUnits(null);
        assertTrue(allList.isSuccess());
        assertEquals("Found 3 deployment unit(s)", allList.getMessage());
        
        // Step 4: Get count by type
        CommandResponse countResponse = orchestrationService.getDeploymentUnitCount("lib");
        assertTrue(countResponse.isSuccess());
        assertEquals("Found 1 lib unit(s)", countResponse.getOutput().get(0));
    }
    
    @Test
    void infoWorkflow_StandardAndDebugInfo_ShouldProvideDetailedInformation() {
        // Step 1: Create a deployment unit
        orchestrationService.createDto("INFO", "555", "InfoDto", "Information test DTO");
        
        // Step 2: Get standard information
        CommandResponse standardInfo = orchestrationService.getDeploymentUnitDetails("INFOC555");
        assertTrue(standardInfo.isSuccess());
        assertTrue(standardInfo.getOutput().contains("Name: INFOC555"));
        assertTrue(standardInfo.getOutput().contains("Type: DTO"));
        assertTrue(standardInfo.getOutput().contains("Dependencies: 0"));
        
        // Step 3: Get debug information
        CommandResponse debugInfo = infoService.getContainableInfo("INFOC555", true);
        assertTrue(debugInfo.isSuccess());
        assertTrue(debugInfo.getOutput().get(0).contains("Debug info for DU: INFOC555"));
        
        // Step 4: Get complete information (combined)
        CommandResponse completeInfo = orchestrationService.getCompleteUnitInfo("INFOC555");
        assertTrue(completeInfo.isSuccess());
        assertEquals("Complete information for 'INFOC555'", completeInfo.getMessage());
        assertTrue(completeInfo.getOutput().contains("--- Debug Information ---"));
    }
    
    @Test
    void errorHandling_NonExistentOperations_ShouldReturnAppropriateErrors() {
        // Test non-existent unit operations
        assertFalse(validationService.deploymentUnitExists("NON-EXISTENT"));
        
        CommandResponse infoResponse = orchestrationService.getDeploymentUnitDetails("NON-EXISTENT");
        assertFalse(infoResponse.isSuccess());
        assertTrue(infoResponse.getOutput().get(0).contains("not found"));
        
        CommandResponse depResponse = orchestrationService.createDependency("NON-EXISTENT-1", "NON-EXISTENT-2");
        assertFalse(depResponse.isSuccess());
        assertTrue(depResponse.getOutput().get(0).contains("not found"));
    }
    
    @Test
    void performanceTest_MultipleOperations_ShouldCompleteWithinReasonableTime() {
        long startTime = System.currentTimeMillis();
        
        // Create 10 DTOs, 5 Libraries, and dependencies between them
        for (int i = 1; i <= 10; i++) {
            orchestrationService.createDto("PERF", String.format("%03d", i), "PerfDto" + i, "Performance test DTO " + i);
        }
        
        for (int i = 1; i <= 5; i++) {
            orchestrationService.createLib("PERF", String.format("%03d", i + 100), "Performance test Library " + i);
        }
        
        // Create some dependencies
        for (int i = 1; i <= 5; i++) {
            orchestrationService.createDependency("PERFR" + String.format("%03d", i + 100), "PERFC" + String.format("%03d", i));
        }
        
        // Query all units
        CommandResponse listAll = orchestrationService.listDeploymentUnits(null);
        assertTrue(listAll.isSuccess());
        assertEquals("Found 15 deployment unit(s)", listAll.getMessage());
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Should complete within 5 seconds (generous threshold for CI environments)
        assertTrue(executionTime < 5000, "Operations took too long: " + executionTime + "ms");
        
        // Verify diagram service was called appropriately (15 creates + 5 dependency creates = 20 calls)
        verify(diagramService, times(20)).notifyDiagramUpdate();
    }
}