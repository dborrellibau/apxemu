package com.bank.education.apxcli.service.inout;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.navigation.model.PathType;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.strategy.DeploymentUnitStrategy;
import com.bank.education.apxcli.strategy.DeploymentUnitStrategyFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for handling "apx add in" and "apx add out" interactive command flows
 * Only works when inside a TRX component
 */
@Service
public class InOutCommandService {
    
    private final InOutManagementService inOutManagementService;
    private final DeploymentUnitRepository deploymentUnitRepository;
    private final PathNavigationService pathNavigationService;
    
    public InOutCommandService(
            InOutManagementService inOutManagementService,
            DeploymentUnitRepository deploymentUnitRepository,
            PathNavigationService pathNavigationService) {
        this.inOutManagementService = inOutManagementService;
        this.deploymentUnitRepository = deploymentUnitRepository;
        this.pathNavigationService = pathNavigationService;
    }
    
    /**
     * Main entry point for "apx add in" command
     * Only allowed from TRX component level
     */
    public CommandResponse handleAddIn(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        
        // ROOT: cannot add in/out from root
        if ("root".equals(currentDir)) {
            return CommandResponse.error("Cannot add input from root. Navigate to a transaction first");
        }
        
        // Get PathType and NavigationPath
        PathType pathType = getCurrentPathType(currentDir);
        NavigationPath path = pathNavigationService.createPath(currentDir);
        
        // Only allow from component level
        if (pathType == PathType.COMPONENT_IN_FOLDER || 
            pathType == PathType.COMPONENT_IN_DULIB || 
            pathType == PathType.COMPONENT_STANDALONE) {
            
            String componentName = path.getComponentName();
            
            // Verify component exists and is TRX
            Optional<DeploymentUnit> componentOpt = deploymentUnitRepository.findByName(componentName);
            if (!componentOpt.isPresent()) {
                return CommandResponse.error("Component '" + componentName + "' not found");
            }
            
            DeploymentUnit component = componentOpt.get();
            
            // Check if component supports in/out management (must be TRX)
            DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(component.getType());
            if (!strategy.supportsInOutManagement()) {
                return CommandResponse.error("Command 'apx add in' can only be executed from a transaction component");
            }
            
            // Start input flow
            return startInOutFlow(sessionState, componentName, "IN");
        } else {
            return CommandResponse.error("Command 'apx add in' can only be executed from within a transaction component");
        }
    }
    
    /**
     * Main entry point for "apx add out" command
     * Only allowed from TRX component level
     */
    public CommandResponse handleAddOut(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        
        // ROOT: cannot add in/out from root
        if ("root".equals(currentDir)) {
            return CommandResponse.error("Cannot add output from root. Navigate to a transaction first");
        }
        
        // Get PathType and NavigationPath
        PathType pathType = getCurrentPathType(currentDir);
        NavigationPath path = pathNavigationService.createPath(currentDir);
        
        // Only allow from component level
        if (pathType == PathType.COMPONENT_IN_FOLDER || 
            pathType == PathType.COMPONENT_IN_DULIB || 
            pathType == PathType.COMPONENT_STANDALONE) {
            
            String componentName = path.getComponentName();
            
            // Verify component exists and is TRX
            Optional<DeploymentUnit> componentOpt = deploymentUnitRepository.findByName(componentName);
            if (!componentOpt.isPresent()) {
                return CommandResponse.error("Component '" + componentName + "' not found");
            }
            
            DeploymentUnit component = componentOpt.get();
            
            // Check if component supports in/out management (must be TRX)
            DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(component.getType());
            if (!strategy.supportsInOutManagement()) {
                return CommandResponse.error("Command 'apx add out' can only be executed from a transaction component");
            }
            
            // Start output flow
            return startInOutFlow(sessionState, componentName, "OUT");
        } else {
            return CommandResponse.error("Command 'apx add out' can only be executed from within a transaction component");
        }
    }
    
    /**
     * Starts the in/out flow by showing option menu
     */
    private CommandResponse startInOutFlow(FormState sessionState, String componentName, String mode) {
        // Store mode and component in session
        sessionState.addData("inOutMode", mode);
        sessionState.addData("inOutComponent", componentName);
        sessionState.setInOutSelectionMode(true);
        
        // Build menu
        StringBuilder menu = new StringBuilder();
        menu.append("Select ").append(mode.toLowerCase()).append(" type for ").append(componentName).append(":\n");
        menu.append("1. dto\n");
        menu.append("2. group\n");
        menu.append("3. list\n");
        menu.append("4. parameters\n");
        menu.append("\nEnter type number or name:");
        
        return CommandResponse.info(menu.toString());
    }
    
    /**
     * Handles user selection of option type from menu
     */
    public CommandResponse handleOptionSelection(FormState sessionState, String input) {
        String mode = sessionState.getData("inOutMode");
        String componentName = sessionState.getData("inOutComponent");
        
        if (mode == null || componentName == null) {
            sessionState.clearInOutFlowData();
            return CommandResponse.error("Session error: in/out data not found. Please try again.");
        }
        
        String selectedOption = null;
        
        // Check if input is a number (menu selection)
        if (input.matches("^[1-4]$")) {
            int selection = Integer.parseInt(input);
            switch (selection) {
                case 1: selectedOption = "dto"; break;
                case 2: selectedOption = "group"; break;
                case 3: selectedOption = "list"; break;
                case 4: selectedOption = "parameters"; break;
            }
        } else {
            // Check if input matches an option name (case insensitive)
            String lowerInput = input.toLowerCase();
            if ("dto".equals(lowerInput) || "group".equals(lowerInput) || 
                "list".equals(lowerInput) || "parameters".equals(lowerInput)) {
                selectedOption = lowerInput;
            }
        }
        
        if (selectedOption == null) {
            return CommandResponse.error("Invalid selection. Please enter a number between 1 and 4 or a valid option name");
        }
        
        // Handle selection
        if ("dto".equals(selectedOption)) {
            // Ask for DTO name
            sessionState.setInOutSelectionMode(false);
            sessionState.setAwaitingInOutDtoName(true);
            return CommandResponse.info("Enter DTO name:");
        } else {
            // Not implemented options
            sessionState.clearInOutFlowData();
            return CommandResponse.error("Aún no implementado en V-Ether");
        }
    }
    
    /**
     * Handles user input of DTO name
     */
    public CommandResponse handleDtoNameInput(FormState sessionState, String dtoName) {
        String mode = sessionState.getData("inOutMode");
        String componentName = sessionState.getData("inOutComponent");
        
        if (mode == null || componentName == null) {
            sessionState.clearInOutFlowData();
            return CommandResponse.error("Session error: in/out data not found. Please try again.");
        }
        
        dtoName = dtoName.trim();
        
        // Validate DTO exists
        Optional<DeploymentUnit> dtoOpt = deploymentUnitRepository.findByName(dtoName);
        if (!dtoOpt.isPresent()) {
            sessionState.clearInOutFlowData();
            return CommandResponse.error("DTO '" + dtoName + "' not found");
        }
        
        DeploymentUnit dto = dtoOpt.get();
        
        // Validate it's actually a DTO
        if (dto.getType() != DeploymentUnit.DeploymentUnitType.DTO) {
            sessionState.clearInOutFlowData();
            return CommandResponse.error("Component '" + dtoName + "' is not a DTO");
        }
        
        // Execute based on mode
        CommandResponse result;
        if ("IN".equals(mode)) {
            result = inOutManagementService.addInput(componentName, dtoName);
        } else {
            result = inOutManagementService.addOutput(componentName, dtoName);
        }
        
        // Clear session data
        sessionState.clearInOutFlowData();
        
        // Set prompt
        result.setPrompt(sessionState.getCurrentPrompt());
        
        return result;
    }
    
    /**
     * Gets current path type from directory string
     */
    private PathType getCurrentPathType(String currentDir) {
        NavigationPath path = pathNavigationService.createPath(currentDir);
        return path.getType();
    }
}
