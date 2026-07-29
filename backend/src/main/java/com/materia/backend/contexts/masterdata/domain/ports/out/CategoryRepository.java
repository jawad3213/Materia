package com.materia.backend.contexts.masterdata.domain.ports.out;

import com.materia.backend.contexts.masterdata.domain.entities.Category;
import com.materia.backend.contexts.masterdata.domain.enums.CategoryType;


import com.materia.backend.common.domain.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Port de sortie pour la persistance des catÃ©gories
 * Architecture Hexagonale - Le domaine dÃ©finit le contrat,
 * l'infrastructure l'implÃ©mente
 */
public interface CategoryRepository extends BaseRepository<Category> {

    /**
     * Recherche une catÃ©gorie par son code
     */
    Optional<Category> findByCode(String code);

    /**
     * Recherche les catÃ©gories racines (sans parent)
     */
    List<Category> findRootCategories();

    /**
     * Recherche les sous-catÃ©gories d'un parent
     */
    List<Category> findByParentId(String parentId);

    /**
     * Recherche les catÃ©gories par type
     */
    List<Category> findByCategoryType(CategoryType categoryType);

    /**
     * Recherche les catÃ©gories actives
     */
    List<Category> findByStatus(String status);

    /**
     * VÃ©rifie si un code catÃ©gorie existe dÃ©jÃ 
     */
    boolean existsByCode(String code);

    /**
     * Recherche par mot-clÃ© (nom, description)
     */
    List<Category> search(String keyword);
}

