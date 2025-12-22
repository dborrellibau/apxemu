package com.bank.education.apxcli.service.navigation;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.navigation.PathNavigationService;
import com.bank.education.apxcli.navigation.model.NavigationPath;
import com.bank.education.apxcli.service.ArchitectureOrchestrationService;
import com.bank.education.apxcli.service.DeploymentUnitNavigationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for navigation commands (cd, pwd, ls)
 * Integrado con PathNavigationService para centralizar lógica de navegación
 */
@Service
public class NavigationCommandService {
    
    private final ArchitectureOrchestrationService architectureService;
    private final DeploymentUnitNavigationService navigationService;
    private final PathNavigationService pathNavigationService;
    
    public NavigationCommandService(ArchitectureOrchestrationService architectureService,
                                   DeploymentUnitNavigationService navigationService,
                                   PathNavigationService pathNavigationService) {
        this.architectureService = architectureService;
        this.navigationService = navigationService;
        this.pathNavigationService = pathNavigationService;
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
        
        // Convertir currentDir string a NavigationPath
        NavigationPath currentPath = convertToNavigationPath(currentDir);
        
        // Usar PathNavigationService para navegar
        NavigationPath newPath = pathNavigationService.navigate(target, currentPath);
        
        if (newPath == null) {
            // Navegación falló, dar mensaje específico
            if ("..".equals(target)) {
                return CommandResponse.error("Already at root directory");
            }
            return CommandResponse.error("Cannot navigate to '" + target + "'. Path does not exist or is invalid.");
        }
        
        // Convertir NavigationPath de vuelta a string para sessionState
        String newDir = convertToDirectoryString(newPath);
        sessionState.setCurrentDirectory(newDir);
        
        String displayPath = "root".equals(newDir) ? "/vether" : "/vether/" + newDir;
        return CommandResponse.success("Changed directory to " + displayPath);
    }
    
    /**
     * Convierte el string currentDirectory (legacy) a NavigationPath
     */
    private NavigationPath convertToNavigationPath(String currentDir) {
        if (currentDir == null || "root".equals(currentDir) || currentDir.trim().isEmpty()) {
            return pathNavigationService.navigateToRoot();
        }
        
        return pathNavigationService.createPath(currentDir);
    }
    
    /**
     * Convierte NavigationPath a string currentDirectory (legacy)
     */
    private String convertToDirectoryString(NavigationPath path) {
        if (path == null || path.getSegments().isEmpty()) {
            return "root";
        }
        
        return String.join("/", path.getSegments());
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
            NavigationPath path = pathNavigationService.createPath(currentDir);
            String duName = path.getDuName();
            String folder = path.getFolderName();
            
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
