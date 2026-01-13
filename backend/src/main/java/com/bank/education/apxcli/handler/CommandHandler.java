package com.bank.education.apxcli.handler;

import com.bank.education.apxcli.dto.CommandRequest;
import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.dto.FormState;

/**
 * Base handler for command processing with priority-based dispatch
 * 
 * Each handler declares its priority (lower number = higher priority) and 
 * implements canHandle() to determine if it should process the current command.
 * 
 * Priority Guidelines:
 * - 10-30: Interactive state handlers (confirmations, menus)
 * - 40-70: Wizard/flow handlers (forms, dependencies)
 * - 90-100: Standard command handlers (apx, cd/ls/pwd)
 * 
 * @see CommandHandlerRegistry for dispatch mechanism
 */
public abstract class CommandHandler {
    
    /**
     * Priority of this handler (10 = highest, 100 = lowest)
     * Handlers are evaluated in ascending priority order.
     * 
     * @return priority value between 10-100
     */
    public abstract int getPriority();
    
    /**
     * Determines if this handler can process the current command/state combination
     * 
     * Implementation should check:
     * - Command syntax/prefix (e.g., command.startsWith("apx "))
     * - Session state flags (e.g., sessionState.isAwaitingConfirmation())
     * - Context requirements (e.g., PathType restrictions)
     * 
     * @param request Command request from user
     * @param sessionState Current session state with navigation/form context
     * @return true if this handler should process the command
     */
    public abstract boolean canHandle(CommandRequest request, FormState sessionState);
    
    /**
     * Processes the command and returns response
     * 
     * Called only if canHandle() returned true.
     * Should delegate to specialized services for business logic.
     * 
     * @param request Command request from user
     * @param sessionState Current session state (may be modified)
     * @return Command response with output, status, and optional hints
     */
    public abstract CommandResponse handle(CommandRequest request, FormState sessionState);
    
    /**
     * Gets handler name for debugging/logging
     * Defaults to class simple name
     * 
     * @return Handler identifier
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
