package com.bank.education.apxcli.service.forms;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.form.FormBuilder;
import com.bank.education.apxcli.form.FormField;
import com.bank.education.apxcli.dto.FormState;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for generating form prompts
 */
@Service
public class FormPromptService {
    
    public CommandResponse getNextFormPrompt(FormState formState) {
        String formType = formState.getFormType();
        int step = formState.getCurrentStep();
        
        switch (formType) {
            case "dto":
                return getDtoFormPrompt(step);
            case "lib":
                return getLibFormPrompt(step);
            case "trx":
                return getTrxFormPrompt(step);
            case "du-online":
                return getDuOnlineFormPrompt(step);
            case "du-lib":
                return getDuLibFormPrompt(step);
            default:
                return CommandResponse.error("Unknown form type: " + formType);
        }
    }
    
    private CommandResponse getDtoFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createDtoForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid DTO form step");
    }
    
    private CommandResponse getLibFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createLibForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid LIB form step");
    }
    
    private CommandResponse getTrxFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createTrxForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid TRX form step");
    }
    
    private CommandResponse getDuOnlineFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createDuOnlineForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid DU-ONLINE form step");
    }
    
    private CommandResponse getDuLibFormPrompt(int step) {
        List<FormField> formFields = FormBuilder.createDuLibForm();
        
        if (step >= 0 && step < formFields.size()) {
            FormField field = formFields.get(step);
            return CommandResponse.form(field.getPrompt(), field.getName());
        }
        return CommandResponse.error("Invalid DU-LIB form step");
    }
}
