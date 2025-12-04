package com.bank.education.apxcli.service.navigation;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.service.DeploymentUnitNavigationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for navigation commands (cd, pwd, ls)
 */
@Service
public class NavigationCommandService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final DeploymentUnitNavigationService navigationService;
    
    public NavigationCommandService(ArchitectureOrchestrationService architectureService,
                                   DeploymentUnitNavigationService navigationService) {
        this.architectureService = architectureService;
        this.navigationService = navigationService;
    }
    
    public CommandResponse handleCdCommand(FormState sessionState, String[] args) {
        String currentDir = sessionState.getCurrentDirectory();
        
        if (args.length == 0) {
            // cd without arguments shows current directory
            return CommandResponse.success("Current directory: " + 
                ("root".equals(currentDir) ? "/vether" : "/vether/" + currentDir));
        }
        
        if (args.length != 1) {
            return CommandResponse.error("Usage: cd <directory>, cd .. (go back), or cd (show current directory)");
        }
        
        String target = args[0];
        
        // Handle cd .. (go back)
        if ("..".equals(target)) {
            if ("root".equals(currentDir)) {
                return CommandResponse.error("Already at root directory");
            }
            
            // Retroceder un nivel: quitar el último segmento del path
            if (currentDir.contains("/")) {
                // Estamos en un nivel anidado (nivel 2 o 3+)
                int lastSlashIndex = currentDir.lastIndexOf("/");
                String parentDir = currentDir.substring(0, lastSlashIndex);
                
                sessionState.setCurrentDirectory(parentDir);
                return CommandResponse.success("Changed directory to /vether/" + parentDir);
            } else {
                // Estamos en un DU (nivel 1), retroceder a root
                sessionState.setCurrentDirectory("root");
                return CommandResponse.success("Changed directory to /vether");
            }
        }
        
        // Handle absolute path navigation (du-name/folder)
        if (target.contains("/")) {
            String[] pathParts = target.split("/");
            
            if (pathParts.length != 2) {
                return CommandResponse.error("Invalid path format. Use: <du-name>/<folder>");
            }
            
            String duName = pathParts[0];
            String folder = pathParts[1];
            
            // Validate that the DU exists
            if (!architectureService.containableExists(duName, null)) {
                return CommandResponse.error("Deployment unit '" + duName + "' does not exist");
            }
            
            // Validate folder name using navigation service
            if (!navigationService.isValidFolder(duName, folder)) {
                return CommandResponse.error(navigationService.getInvalidFolderErrorMessage(duName));
            }
            
            sessionState.setCurrentDirectory(duName + "/" + folder);
            return CommandResponse.success("Changed directory to /vether/" + target);
        }
        
        // Handle single directory navigation
        if ("root".equals(currentDir)) {
            // From root, can only navigate to existing deployment units
            if (!architectureService.containableExists(target, null)) {
                return CommandResponse.error("Deployment unit '" + target + "' does not exist. Use 'apx list' to see available deployment units.");
            }
            
            sessionState.setCurrentDirectory(target);
            return CommandResponse.success("Changed directory to /vether/" + target);
        } else if (!currentDir.contains("/")) {
            // From deployment unit, can navigate to folders
            String duName = currentDir;
            
            // Validate folder name using navigation service
            if (!navigationService.isValidFolder(duName, target)) {
                return CommandResponse.error(navigationService.getInvalidFolderErrorMessage(duName));
            }
            
            sessionState.setCurrentDirectory(duName + "/" + target);
            return CommandResponse.success("Changed directory to /vether/" + duName + "/" + target);
        } else {
            // Estamos en nivel 2+ (carpeta o componente), intentar navegar más profundo
            String[] pathParts = currentDir.split("/");
            
            if (pathParts.length == 2) {
                // Estamos en nivel 2 (DU/carpeta), intentar navegar a componente (nivel 3)
                String duName = pathParts[0];
                String folderName = pathParts[1];
                
                System.out.println("DEBUG NavigationService: Attempting to navigate to component. DU=" + duName + ", Folder=" + folderName + ", Component=" + target);
                
                try {
                    // Validar que el componente existe en esta carpeta específica
                    boolean exists = architectureService.componentExistsInFolder(duName, folderName, target);
                    System.out.println("DEBUG NavigationService: componentExistsInFolder returned: " + exists);
                    
                    if (exists) {
                        sessionState.setCurrentDirectory(currentDir + "/" + target);
                        return CommandResponse.success("Changed directory to /vether/" + currentDir + "/" + target);
                    } else {
                        return CommandResponse.error("Component '" + target + "' does not exist in this folder. Use 'ls' to see available components.");
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG NavigationService: Exception caught: " + e.getMessage());
                    e.printStackTrace();
                    return CommandResponse.error("Error checking component existence: " + e.getMessage());
                }
            } else {
                // Estamos en nivel 3+ (componente), no podemos ir más profundo
                return CommandResponse.error("Cannot navigate deeper. Use 'cd ..' to go back.");
            }
        }
    }
    
    public CommandResponse handlePwdCommand(FormState sessionState) {
        String currentDir = sessionState.getCurrentDirectory();
        String displayPath = "root".equals(currentDir) ? "/vether" : "/vether/" + currentDir;
        return CommandResponse.success(displayPath);
    }
    
    public CommandResponse handleLsCommand(FormState sessionState, String[] args) {
        String currentDir = sessionState.getCurrentDirectory();
        
        if ("root".equals(currentDir)) {
            // List deployment units in root
            return architectureService.listDeploymentUnits(null);
        } else if (!currentDir.contains("/")) {
            // In deployment unit, list folders specific to DU type
            List<String> folders = navigationService.getValidFolders(currentDir);
            List<String> formattedFolders = new ArrayList<>();
            
            for (String folder : folders) {
                formattedFolders.add(folder + "/        - " + getFolderDescription(currentDir, folder));
            }
            
            CommandResponse response = new CommandResponse(true, "Contents of " + currentDir + ":", 
                formattedFolders, CommandResponse.ResponseType.INFO, null);
            return response;
        } else {
            // In specific folder, list components within that folder
            String[] pathParts = currentDir.split("/");
            String duName = pathParts[0];
            String folder = pathParts[1];
            
            return architectureService.listComponentsInFolder(duName, folder);
        }
    }
    
    private String getFolderDescription(String duName, String folderName) {
        DeploymentUnit.DeploymentUnitType duType = navigationService.getTypeWithCache(duName);
        
        if (duType == DeploymentUnit.DeploymentUnitType.DU_LIB) {
            if (folderName.endsWith("IMPL")) {
                return "Implementation library components";
            } else {
                return "Base library components";
            }
        } else {
            // DU_ONLINE descriptions
            switch (folderName.toLowerCase()) {
                case "dto": return "Data Transfer Objects folder";
                case "transactions": return "Business transactions folder";
                case "library": return "Library components folder";
                default: return "Component folder";
            }
        }
    }
}
