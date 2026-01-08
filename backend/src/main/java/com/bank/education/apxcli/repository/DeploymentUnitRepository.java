package com.bank.education.apxcli.repository;

import com.bank.education.apxcli.model.DeploymentUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeploymentUnitRepository extends JpaRepository<DeploymentUnit, Long> {
    
    Optional<DeploymentUnit> findByName(String name);
    
    List<DeploymentUnit> findByType(DeploymentUnit.DeploymentUnitType type);
    
    @Query("SELECT DISTINCT du FROM DeploymentUnit du LEFT JOIN FETCH du.componentFolders")
    List<DeploymentUnit> findAllWithFolders();
    
    @Query("SELECT DISTINCT du FROM DeploymentUnit du LEFT JOIN FETCH du.dependencies WHERE du.id = :id")
    Optional<DeploymentUnit> findByIdWithDependencies(Long id);
    
    boolean existsByName(String name);
    
    // Banking code validation methods
    @Query("SELECT COUNT(d) > 0 FROM DeploymentUnit d WHERE d.type = :type AND d.code = :code")
    boolean existsByTypeAndCode(@Param("type") DeploymentUnit.DeploymentUnitType type, @Param("code") String code);
    
    // Validates code+UUAA for standalone DTOs (no parentFolder)
    @Query("SELECT COUNT(d) > 0 FROM DeploymentUnit d WHERE d.type = :type AND d.code = :code AND d.uuaa = :uuaa AND d.parentFolder IS NULL")
    boolean existsStandaloneByTypeCodeAndUuaa(@Param("type") DeploymentUnit.DeploymentUnitType type, 
                                              @Param("code") String code, 
                                              @Param("uuaa") String uuaa);
    
    // Validates code+UUAA for DTOs within a specific folder
    @Query("SELECT COUNT(d) > 0 FROM DeploymentUnit d WHERE d.type = :type AND d.code = :code AND d.uuaa = :uuaa AND d.parentFolder.id = :folderId")
    boolean existsInFolderByTypeCodeAndUuaa(@Param("type") DeploymentUnit.DeploymentUnitType type, 
                                           @Param("code") String code, 
                                           @Param("uuaa") String uuaa,
                                           @Param("folderId") Long folderId);
}