package com.bank.education.apxcli.util;

/**
 * Utility class for standardized confirmation messages across all commands
 */
public class ConfirmationMessages {
    
    /**
     * Standard confirmation message used by all commands requiring user confirmation
     * Supports Y/y (yes), n/N (no), or Enter (default yes)
     */
    public static final String STANDARD_CONFIRMATION = 
        "Do you want to continue with the operation? (Y/n): ";
    
    private ConfirmationMessages() {
        // Utility class, prevent instantiation
    }
}
