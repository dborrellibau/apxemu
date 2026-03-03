package com.bank.education.apxcli.model.tutorial;

import javax.persistence.*;

@Entity
@Table(name = "tutorial_step")
public class TutorialStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private TutorialLevel level;
    
    @Column(nullable = false)
    private Integer stepNumber;
    
    @Column(nullable = false, length = 1000)
    private String instruction;
    
    @Column(length = 500)
    private String hint;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepValidationType validationType;
    
    @Column(length = 500)
    private String expectedCommand;
    
    @Column(length = 50)
    private String expectedPathType;
    
    @Column(length = 200)
    private String expectedDirectory;
    
    @Column(length = 500)
    private String successMessage;
    
    @Column(length = 500)
    private String errorMessage;
    
    // Constructors
    public TutorialStep() {
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TutorialLevel getLevel() {
        return level;
    }
    
    public void setLevel(TutorialLevel level) {
        this.level = level;
    }
    
    public Integer getStepNumber() {
        return stepNumber;
    }
    
    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }
    
    public String getInstruction() {
        return instruction;
    }
    
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
    
    public String getHint() {
        return hint;
    }
    
    public void setHint(String hint) {
        this.hint = hint;
    }
    
    public StepValidationType getValidationType() {
        return validationType;
    }
    
    public void setValidationType(StepValidationType validationType) {
        this.validationType = validationType;
    }
    
    public String getExpectedCommand() {
        return expectedCommand;
    }
    
    public void setExpectedCommand(String expectedCommand) {
        this.expectedCommand = expectedCommand;
    }
    
    public String getExpectedPathType() {
        return expectedPathType;
    }
    
    public void setExpectedPathType(String expectedPathType) {
        this.expectedPathType = expectedPathType;
    }
    
    public String getExpectedDirectory() {
        return expectedDirectory;
    }
    
    public void setExpectedDirectory(String expectedDirectory) {
        this.expectedDirectory = expectedDirectory;
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
