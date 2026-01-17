package com.bank.education.apxcli.service;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for the refactored architecture services
 * These tests verify that the service decomposition didn't introduce performance regressions
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Disabled("Tests disabled temporarily during refactoring")
class ArchitectureServicesPerformanceTest {
    
    @Autowired
    private ArchitectureOrchestrationService orchestrationService;
    
    @MockBean
    private DiagramService diagramService;
    
    @Autowired
    private DeploymentUnitRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    @Test
    void createOperations_BulkCreation_ShouldMaintainPerformance() {
        StopWatch stopWatch = new StopWatch("Bulk Creation Test");
        
        // Test creating 50 DTOs
        stopWatch.start("Create 50 DTOs");
        for (int i = 1; i <= 50; i++) {
            CommandResponse response = orchestrationService.createDto(
                "PERF", String.format("%03d", i), "PerfDto" + i, "Performance test DTO " + i);
            assertTrue(response.isSuccess(), "DTO creation " + i + " failed");
        }
        stopWatch.stop();
        
        // Test creating 30 Libraries
        stopWatch.start("Create 30 Libraries");
        for (int i = 1; i <= 30; i++) {
            CommandResponse response = orchestrationService.createLib(
                "PERF", String.format("%03d", i + 100), "Performance test Library " + i);
            assertTrue(response.isSuccess(), "Library creation " + i + " failed");
        }
        stopWatch.stop();
        
        // Test creating 20 Transactions
        stopWatch.start("Create 20 Transactions");
        for (int i = 1; i <= 20; i++) {
            CommandResponse response = orchestrationService.createTrx(
                "PERF", String.format("%03d", i + 200), "01", "GL", "Performance test Transaction " + i);
            assertTrue(response.isSuccess(), "Transaction creation " + i + " failed");
        }
        stopWatch.stop();
        
        // Verify total creation time is reasonable (should be under 10 seconds)
        assertTrue(stopWatch.getTotalTimeMillis() < 10000, 
            "Bulk creation took too long: " + stopWatch.getTotalTimeMillis() + "ms\n" + stopWatch.prettyPrint());
        
        // Verify all units were created
        CommandResponse listResponse = orchestrationService.listDeploymentUnits(null);
        assertTrue(listResponse.isSuccess());
        assertEquals("Found 100 deployment unit(s)", listResponse.getMessage());
        
        System.out.println("Bulk Creation Performance:");
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    void validationOperations_BulkValidation_ShouldMaintainPerformance() {
        // First, create some test data
        for (int i = 1; i <= 20; i++) {
            orchestrationService.createDto("VALID", String.format("%03d", i), "ValidDto" + i, "Test DTO " + i);
        }
        
        StopWatch stopWatch = new StopWatch("Bulk Validation Test");
        
        // Test bulk existence checks
        stopWatch.start("Check 1000 existence queries");
        for (int i = 1; i <= 1000; i++) {
            String name = "VALIDC" + String.format("%03d", (i % 20) + 1);
            boolean exists = orchestrationService.deploymentUnitExists(name);
            assertTrue(exists, "Unit " + name + " should exist");
        }
        stopWatch.stop();
        
        // Test bulk code existence checks
        stopWatch.start("Check 1000 code existence queries");
        for (int i = 1; i <= 1000; i++) {
            String code = String.format("%03d", (i % 20) + 1);
            boolean exists = orchestrationService.isCodeExists("dto", code);
            assertTrue(exists, "Code " + code + " should exist for dto");
        }
        stopWatch.stop();
        
        // Validation should be very fast (under 2 seconds for 2000 queries)
        assertTrue(stopWatch.getTotalTimeMillis() < 2000, 
            "Bulk validation took too long: " + stopWatch.getTotalTimeMillis() + "ms\n" + stopWatch.prettyPrint());
        
        System.out.println("Bulk Validation Performance:");
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    void queryOperations_BulkQuerying_ShouldMaintainPerformance() {
        // Create test data across different types
        for (int i = 1; i <= 15; i++) {
            orchestrationService.createDto("QUERY", String.format("%03d", i), "QueryDto" + i, "Query test DTO " + i);
            orchestrationService.createLib("QUERY", String.format("%03d", i + 100), "Query test Library " + i);
            orchestrationService.createTrx("QUERY", String.format("%03d", i + 200), "01", "GL", "Query test Transaction " + i);
        }
        
        StopWatch stopWatch = new StopWatch("Bulk Querying Test");
        
        // Test multiple list operations
        stopWatch.start("100 list all operations");
        for (int i = 1; i <= 100; i++) {
            CommandResponse response = orchestrationService.listDeploymentUnits(null);
            assertTrue(response.isSuccess());
            assertEquals("Found 45 deployment unit(s)", response.getMessage());
        }
        stopWatch.stop();
        
        // Test multiple filtered list operations
        stopWatch.start("300 filtered list operations (100 per type)");
        for (int i = 1; i <= 100; i++) {
            CommandResponse dtoResponse = orchestrationService.listDeploymentUnits("dto");
            CommandResponse libResponse = orchestrationService.listDeploymentUnits("lib");
            CommandResponse trxResponse = orchestrationService.listDeploymentUnits("trx");
            
            assertTrue(dtoResponse.isSuccess());
            assertTrue(libResponse.isSuccess());
            assertTrue(trxResponse.isSuccess());
            
            assertEquals("Found 15 deployment unit(s)", dtoResponse.getMessage());
            assertEquals("Found 15 deployment unit(s)", libResponse.getMessage());
            assertEquals("Found 15 deployment unit(s)", trxResponse.getMessage());
        }
        stopWatch.stop();
        
        // Query operations should be fast (under 3 seconds for 400 queries)
        assertTrue(stopWatch.getTotalTimeMillis() < 3000, 
            "Bulk querying took too long: " + stopWatch.getTotalTimeMillis() + "ms\n" + stopWatch.prettyPrint());
        
        System.out.println("Bulk Querying Performance:");
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    void infoOperations_BulkInformation_ShouldMaintainPerformance() {
        // Create test data
        for (int i = 1; i <= 10; i++) {
            orchestrationService.createDto("INFO", String.format("%03d", i), "InfoDto" + i, "Info test DTO " + i);
        }
        
        StopWatch stopWatch = new StopWatch("Bulk Information Test");
        
        // Test bulk detail queries
        stopWatch.start("100 detail queries");
        for (int i = 1; i <= 100; i++) {
            String name = "INFOC" + String.format("%03d", (i % 10) + 1);
            CommandResponse response = orchestrationService.getDeploymentUnitDetails(name);
            assertTrue(response.isSuccess(), "Detail query for " + name + " failed");
            assertTrue(response.getOutput().contains("Name: " + name));
        }
        stopWatch.stop();
        
        // Test bulk debug queries
        stopWatch.start("50 debug queries");
        for (int i = 1; i <= 50; i++) {
            String name = "INFOC" + String.format("%03d", (i % 10) + 1);
            CommandResponse response = orchestrationService.getCompleteUnitInfo(name);
            assertTrue(response.isSuccess(), "Debug query for " + name + " failed");
            assertTrue(response.getOutput().contains("Complete information for '" + name + "'"));
        }
        stopWatch.stop();
        
        // Info operations should be reasonable (under 5 seconds for 150 info queries)
        assertTrue(stopWatch.getTotalTimeMillis() < 5000, 
            "Bulk information took too long: " + stopWatch.getTotalTimeMillis() + "ms\n" + stopWatch.prettyPrint());
        
        System.out.println("Bulk Information Performance:");
        System.out.println(stopWatch.prettyPrint());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_STRESS_TESTS", matches = "true")
    void stressTest_HighVolumeOperations_ShouldHandleGracefully() {
        StopWatch stopWatch = new StopWatch("Stress Test");
        
        // Create a large number of units
        stopWatch.start("Create 200 deployment units");
        for (int i = 1; i <= 200; i++) {
            if (i % 3 == 0) {
                orchestrationService.createDto("STRESS", String.format("%04d", i), "StressDto" + i, "Stress test DTO " + i);
            } else if (i % 3 == 1) {
                orchestrationService.createLib("STRESS", String.format("%04d", i), "Stress test Library " + i);
            } else {
                orchestrationService.createTrx("STRESS", String.format("%04d", i), "01", "GL", "Stress test Transaction " + i);
            }
        }
        stopWatch.stop();
        
        // Perform many query operations
        stopWatch.start("1000 random queries");
        for (int i = 1; i <= 1000; i++) {
            if (i % 4 == 0) {
                orchestrationService.listDeploymentUnits(null);
            } else if (i % 4 == 1) {
                orchestrationService.listDeploymentUnits("dto");
            } else if (i % 4 == 2) {
                orchestrationService.listDeploymentUnits("lib");
            } else {
                orchestrationService.listDeploymentUnits("trx");
            }
        }
        stopWatch.stop();
        
        // Stress test should complete within 30 seconds
        assertTrue(stopWatch.getTotalTimeMillis() < 30000, 
            "Stress test took too long: " + stopWatch.getTotalTimeMillis() + "ms\n" + stopWatch.prettyPrint());
        
        // Verify final state
        CommandResponse finalCount = orchestrationService.getDeploymentUnitCount(null);
        assertTrue(finalCount.isSuccess());
        assertEquals("Total deployment units: 200", finalCount.getOutput().get(0));
        
        System.out.println("Stress Test Performance:");
        System.out.println(stopWatch.prettyPrint());
    }
}