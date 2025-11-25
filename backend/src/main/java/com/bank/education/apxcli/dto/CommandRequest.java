package com.bank.education.apxcli.dto;

public class CommandRequest {
    private String sessionId;
    private String command;
    private String[] args;
    
    public CommandRequest() {}
    
    public CommandRequest(String sessionId, String command, String[] args) {
        this.sessionId = sessionId;
        this.command = command;
        this.args = args;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String[] getArgs() {
        return args;
    }
    
    public void setArgs(String[] args) {
        this.args = args;
    }
}