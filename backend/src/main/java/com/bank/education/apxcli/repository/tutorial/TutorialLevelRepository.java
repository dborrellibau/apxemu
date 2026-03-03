package com.bank.education.apxcli.repository.tutorial;

import com.bank.education.apxcli.model.tutorial.TutorialLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for TutorialLevel entities.
 * Provides database access for tutorial levels.
 */
@Repository
public interface TutorialLevelRepository extends JpaRepository<TutorialLevel, Long> {
    
    /**
     * Finds a level by its order number.
     * 
     * @param orderNumber Order number of the level
     * @return Optional containing the level if found
     */
    Optional<TutorialLevel> findByOrderNumber(Integer orderNumber);
    
    /**
     * Finds all levels ordered by their order number.
     * 
     * @return List of levels in order
     */
    List<TutorialLevel> findAllByOrderByOrderNumberAsc();
}
