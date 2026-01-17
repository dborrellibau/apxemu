package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;
import com.bank.education.apxcli.service.dependencies.DependencyCommandService;
import org.springframework.stereotype.Component;

/**
 * Handler for dependency wizard flow (3-step process)
 * 
 * Priority 50 - Handles interactive dependency creation wizard activated by
 * "apx add dep".
 * 
 * Three-step flow:
 * 1. Source component selection (which component will have the dependency)
 * 2. Dependency type selection (COMPILE, MAVEN, NPM)
 * 3. Artifact ID input (groupId:artifactId:version)
 * 
 * Each step is tracked by specific FormState flags:
 * - isAwaitingDependencyTypeSelection()
 * - isAwaitingDependencyArtifactId()
 * 
 * Delegates to DependencyCommandService for all wizard logic.
 */
@Component
public class DependencyFlowHandler extends CommandHandler {

    public static final int PRIORITY = 50;

    private final DependencyCommandService dependencyCommandService;

    public DependencyFlowHandler(DependencyCommandService dependencyCommandService) {
        this.dependencyCommandService = dependencyCommandService;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean canHandle(CommandRequest request, FormState sessionState) {
        return sessionState.isAwaitingDependencyTypeSelection() ||
                sessionState.isAwaitingDependencyArtifactId();
    }

    @Override
    public CommandResponse handle(CommandRequest request, FormState sessionState) {
        String input = request.getCommand();

        // Step 2: Dependency type selection
        if (sessionState.isAwaitingDependencyTypeSelection()) {
            return dependencyCommandService.handleDependencyTypeSelection(sessionState, input);
        }

        // Step 3: Artifact ID input
        if (sessionState.isAwaitingDependencyArtifactId()) {
            return dependencyCommandService.handleArtifactIdInput(sessionState, input);
        }

        return CommandResponse.error("Invalid dependency flow state");
    }
}
