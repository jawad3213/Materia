package com.materia.backend.contexts.masterdata.domain.ports.out;

import com.materia.backend.contexts.masterdata.domain.entities.Supplier;


import com.materia.backend.common.domain.BaseRepository;


import java.util.List;
import java.util.Optional;

/**
 * Port de sortie pour la persistance des fournisseurs
 * Architecture Hexagonale - Le domaine dÃ©finit le contrat,
 * l'infrastructure l'implÃ©mente
 */
public interface SupplierRepository extends BaseRepository<Supplier> {

    /**
     * Recherche un fournisseur par son code
     */
    Optional<Supplier> findByCode(String code);

    /**
     * Recherche les fournisseurs actifs
     */
    List<Supplier> findByStatus(String status);

    /**
     * Recherche les fournisseurs par pays
     */
    List<Supplier> findByCountry(String country);

    /**
     * Recherche les fournisseurs par ville
     */
    List<Supplier> findByCity(String city);

    /**
     * VÃ©rifie si un code fournisseur existe dÃ©jÃ 
     */
    boolean existsByCode(String code);

    /**
     * Recherche par mot-clÃ© (nom, description, contact)
     */
    List<Supplier> search(String keyword);
}

