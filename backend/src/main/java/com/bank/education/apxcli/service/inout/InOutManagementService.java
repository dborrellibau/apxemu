package com.bank.education.apxcli.service.inout;

import com.bank.education.apxcli.dto.CommandResponse;
import com.bank.education.apxcli.model.DeploymentUnit;
import com.bank.education.apxcli.repository.DeploymentUnitRepository;
import com.bank.education.apxcli.service.DiagramService;
import com.bank.education.apxcli.strategy.DeploymentUnitStrategy;
import com.bank.education.apxcli.strategy.DeploymentUnitStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service responsible for managing inputs and outputs for transactions
 * Uses Strategy Pattern to delegate logic to TrxStrategy
 */
@Service
@Transactional
public class InOutManagementService {
    
    private final DeploymentUnitRepository repository;
    private final DiagramService diagramService;
    
    public InOutManagementService(DeploymentUnitRepository repository, DiagramService diagramService) {
        this.repository = repository;
        this.diagramService = diagramService;
    }
    
    /**
     * Adds a DTO as input to a transaction
     * 
     * @param transactionName Name of the transaction
     * @param dtoName Name of the DTO to add
     * @return CommandResponse with result
     */
    public CommandResponse addInput(String transactionName, String dtoName) {
        Optional<DeploymentUnit> transactionOpt = repository.findByName(transactionName);
        Optional<DeploymentUnit> dtoOpt = repository.findByName(dtoName);
        
        if (!transactionOpt.isPresent()) {
            return CommandResponse.error("Transaction '" + transactionName + "' not found");
        }
        
        if (!dtoOpt.isPresent()) {
            return CommandResponse.error("DTO '" + dtoName + "' not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        DeploymentUnit dto = dtoOpt.get();
        
        // Get strategy for transaction type
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(transaction.getType());
        
        if (!strategy.supportsInOutManagement()) {
            return CommandResponse.error("Component '" + transactionName + "' does not support input/output management");
        }
        
        // Delegate to strategy
        CommandResponse result = strategy.addInput(transaction, dto);
        
        if (result.isSuccess()) {
            repository.save(transaction);
            diagramService.notifyDiagramUpdate();
        }
        
        return result;
    }
    
    /**
     * Adds a DTO as output to a transaction
     * 
     * @param transactionName Name of the transaction
     * @param dtoName Name of the DTO to add
     * @return CommandResponse with result
     */
    public CommandResponse addOutput(String transactionName, String dtoName) {
        Optional<DeploymentUnit> transactionOpt = repository.findByName(transactionName);
        Optional<DeploymentUnit> dtoOpt = repository.findByName(dtoName);
        
        if (!transactionOpt.isPresent()) {
            return CommandResponse.error("Transaction '" + transactionName + "' not found");
        }
        
        if (!dtoOpt.isPresent()) {
            return CommandResponse.error("DTO '" + dtoName + "' not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        DeploymentUnit dto = dtoOpt.get();
        
        // Get strategy for transaction type
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(transaction.getType());
        
        if (!strategy.supportsInOutManagement()) {
            return CommandResponse.error("Component '" + transactionName + "' does not support input/output management");
        }
        
        // Delegate to strategy
        CommandResponse result = strategy.addOutput(transaction, dto);
        
        if (result.isSuccess()) {
            repository.save(transaction);
            diagramService.notifyDiagramUpdate();
        }
        
        return result;
    }
    
    /**
     * Removes a DTO from transaction inputs
     * 
     * @param transactionName Name of the transaction
     * @param dtoName Name of the DTO to remove
     * @return CommandResponse with result
     */
    public CommandResponse removeInput(String transactionName, String dtoName) {
        Optional<DeploymentUnit> transactionOpt = repository.findByName(transactionName);
        Optional<DeploymentUnit> dtoOpt = repository.findByName(dtoName);
        
        if (!transactionOpt.isPresent()) {
            return CommandResponse.error("Transaction '" + transactionName + "' not found");
        }
        
        if (!dtoOpt.isPresent()) {
            return CommandResponse.error("DTO '" + dtoName + "' not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        DeploymentUnit dto = dtoOpt.get();
        
        // Get strategy for transaction type
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(transaction.getType());
        
        if (!strategy.supportsInOutManagement()) {
            return CommandResponse.error("Component '" + transactionName + "' does not support input/output management");
        }
        
        // Delegate to strategy
        CommandResponse result = strategy.removeInput(transaction, dto);
        
        if (result.isSuccess()) {
            repository.save(transaction);
            diagramService.notifyDiagramUpdate();
        }
        
        return result;
    }
    
    /**
     * Removes a DTO from transaction outputs
     * 
     * @param transactionName Name of the transaction
     * @param dtoName Name of the DTO to remove
     * @return CommandResponse with result
     */
    public CommandResponse removeOutput(String transactionName, String dtoName) {
        Optional<DeploymentUnit> transactionOpt = repository.findByName(transactionName);
        Optional<DeploymentUnit> dtoOpt = repository.findByName(dtoName);
        
        if (!transactionOpt.isPresent()) {
            return CommandResponse.error("Transaction '" + transactionName + "' not found");
        }
        
        if (!dtoOpt.isPresent()) {
            return CommandResponse.error("DTO '" + dtoName + "' not found");
        }
        
        DeploymentUnit transaction = transactionOpt.get();
        DeploymentUnit dto = dtoOpt.get();
        
        // Get strategy for transaction type
        DeploymentUnitStrategy strategy = DeploymentUnitStrategyFactory.getStrategy(transaction.getType());
        
        if (!strategy.supportsInOutManagement()) {
            return CommandResponse.error("Component '" + transactionName + "' does not support input/output management");
        }
        
        // Delegate to strategy
        CommandResponse result = strategy.removeOutput(transaction, dto);
        
        if (result.isSuccess()) {
            repository.save(transaction);
            diagramService.notifyDiagramUpdate();
        }
        
        return result;
    }
}
