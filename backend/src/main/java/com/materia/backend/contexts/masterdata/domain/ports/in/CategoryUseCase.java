package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Category;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrÃ©e (Use Case) pour la gestion des CatÃ©gories
 * Architecture Hexagonale - DÃ©finit les cas d'utilisation disponibles pour l'extÃ©rieur
 */
public interface CategoryUseCase {

    Category createCategory(Category category);

    Category updateCategory(UUID id, Category categoryDetails);

    void deleteCategory(UUID id);

    Optional<Category> getCategory(UUID id);

    Optional<Category> getCategoryByCode(String code);

    List<Category> getAllCategories();

    List<Category> getRootCategories();

    List<Category> getSubCategories(String parentId);
}

