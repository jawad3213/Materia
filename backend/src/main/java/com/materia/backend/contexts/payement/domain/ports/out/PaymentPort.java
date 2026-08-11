package com.materia.backend.contexts.payement.domain.ports.out;

import com.materia.backend.common.domain.BaseRepository;
import com.materia.backend.contexts.payement.domain.entities.Payment;
import com.materia.backend.contexts.payement.domain.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port (repository interface) for Payment entity.
 */
public interface PaymentPort extends BaseRepository<Payment> {

    Optional<Payment> findByCode(String code);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findBySupplierId(String supplierId);

    List<Payment> searchByKeyword(String keyword);
    
    boolean existsByCode(String code);
}
