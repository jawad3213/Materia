package com.materia.backend.contexts.masterData.infrastructure.adapters.out.persistence;

import com.materia.backend.contexts.masterData.domain.entities.Category;
import com.materia.backend.contexts.masterData.domain.ports.out.CategoryRepository;
import com.materia.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static com.materia.backend.support.fixtures.CatalogueFixtures.aCategory;
import static com.materia.backend.support.fixtures.CatalogueFixtures.uniqueCategoryCode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Category persistence against real PostgreSQL schema (T102, US5).
 */
class CategoryPersistenceIT extends AbstractIntegrationTest {

    @Autowired private CategoryRepository categories;
    @PersistenceContext private EntityManager em;

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("save and findById: category survives round-trip with all attributes intact")
    void category_roundTrip() {
        Category saved = categories.save(aCategory().name("Raw Materials").build());
        flushAndClear();

        Optional<Category> reloaded = categories.findById(saved.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(saved.getCode(), reloaded.get().getCode());
        assertEquals("Raw Materials", reloaded.get().getName());
        assertEquals(Category.STATUS_ACTIVE, reloaded.get().getStatus());
    }

    @Test
    @DisplayName("hierarchy: root and subcategory queries return expected nodes")
    void category_hierarchyQueries() {
        Category root = categories.save(aCategory().name("Root Node").parentId(null).build());
        flushAndClear();

        Category child = categories.save(aCategory().name("Child Node")
                .parentId(root.getId().toString())
                .parentCode(root.getCode())
                .level(1)
                .build());
        flushAndClear();

        List<Category> roots = categories.findRootCategories();
        assertTrue(roots.stream().anyMatch(r -> r.getId().equals(root.getId())));

        List<Category> children = categories.findByParentId(root.getId().toString());
        assertTrue(children.stream().anyMatch(c -> c.getId().equals(child.getId())));
    }

    @Test
    @DisplayName("uniqueness: saving duplicate category code is rejected by the database")
    void category_duplicateCode_rejected() {
        String code = uniqueCategoryCode();
        categories.save(aCategory().code(code).build());
        flushAndClear();

        assertThrows(DataIntegrityViolationException.class, () -> {
            categories.save(aCategory().code(code).build());
            flushAndClear();
        });
    }
}
