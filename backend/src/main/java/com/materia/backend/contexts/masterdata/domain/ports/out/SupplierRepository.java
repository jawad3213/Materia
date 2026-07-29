package com.materia.backend.contexts.masterdata.domain.ports.out;

import com.materia.backend.contexts.masterdata.domain.entities.Supplier;

import com.materia.backend.common.domain.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Output Port for supplier persistence
 * Hexagonal Architecture - The domain defines the contract,
 * the infrastructure implements it
 */
public interface SupplierRepository extends BaseRepository<Supplier> {

    /**
     * Finds a supplier by its code
     */
    Optional<Supplier> findByCode(String code);

    /**
     * Finds active suppliers
     */
    List<Supplier> findByStatus(String status);

    /**
     * Finds suppliers by country
     */
    List<Supplier> findByCountry(String country);

    /**
     * Finds suppliers by city
     */
    List<Supplier> findByCity(String city);

    /**
     * Checks if a supplier code already exists
     */
    boolean existsByCode(String code);

    /**
     * Searches by keyword (name, description, contact)
     */
    List<Supplier> search(String keyword);
}
