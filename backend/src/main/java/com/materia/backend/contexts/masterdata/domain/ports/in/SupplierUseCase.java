package com.materia.backend.contexts.masterdata.domain.ports.in;

import com.materia.backend.contexts.masterdata.domain.entities.Supplier;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port d'entrÃ©e (Use Case) pour la gestion des Fournisseurs
 * Architecture Hexagonale - DÃ©finit les cas d'utilisation disponibles pour l'extÃ©rieur
 */
public interface SupplierUseCase {

    Supplier createSupplier(Supplier supplier);

    Supplier updateSupplier(UUID id, Supplier supplierDetails);

    void deleteSupplier(UUID id);

    Optional<Supplier> getSupplier(UUID id);

    Optional<Supplier> getSupplierByCode(String code);

    List<Supplier> getAllSuppliers();

    List<Supplier> searchSuppliers(String keyword);
}

