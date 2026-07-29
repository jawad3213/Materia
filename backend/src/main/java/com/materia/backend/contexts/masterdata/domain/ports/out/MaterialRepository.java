package com.materia.backend.contexts.masterdata.domain.ports.out;

import com.materia.backend.contexts.masterdata.domain.entities.Material;
import com.materia.backend.contexts.masterdata.domain.enums.MaterialStatus;


import com.materia.backend.common.domain.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Port de sortie pour la persistance des matÃ©riaux
 * Architecture Hexagonale - Le domaine dÃ©finit le contrat,
 * l'infrastructure l'implÃ©mente
 */
public interface MaterialRepository extends BaseRepository<Material> {

    /**
     * Recherche un matÃ©riau par son code
     */
    Optional<Material> findByCode(String code);

    /**
     * Recherche les matÃ©riaux par catÃ©gorie
     */
    List<Material> findByCategoryId(String categoryId);

    /**
     * Recherche les matÃ©riaux par fournisseur
     */
    List<Material> findBySupplierId(String supplierId);

    /**
     * Recherche les matÃ©riaux par statut
     */
    List<Material> findByStatus(MaterialStatus status);

    /**
     * VÃ©rifie si un code matÃ©riau existe dÃ©jÃ 
     */
    boolean existsByCode(String code);

    /**
     * Recherche par mot-clÃ© (nom, description, keywords)
     */
    List<Material> search(String keyword);

    /**
     * Recherche les matÃ©riaux dont le stock est infÃ©ruier au seuil minimum
     */
    List<Material> findBelowMinimumStock();

    /**
     * Recherche les matÃ©riaux dont le stock est infÃ©rieur au point de rÃ©approvisionnement
     */
    List<Material> findBelowReorderPoint();
}

