package com.materia.backend.contexts.purchaseRequisition.domain.ports.out;

import com.materia.backend.common.application.PageResponse;
import com.materia.backend.common.domain.BaseRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.Requisition;
import com.materia.backend.contexts.purchaseRequisition.domain.enums.RequisitionStatus;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.RequisitionSearchFilter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Output port for purchase requisition persistence.
 */
public interface RequisitionRepository extends BaseRepository<Requisition> {

    /**
     * Finds a requisition by its business code.
     */
    Optional<Requisition> findByCode(String code);

    /**
     * Checks whether a requisition code already exists.
     */
    boolean existsByCode(String code);

    /**
     * Finds requisitions by workflow status.
     */
    List<Requisition> findByStatus(RequisitionStatus status);

    /**
     * Finds requisitions created by a requester.
     */
    List<Requisition> findByRequesterId(String requesterId);

    /**
     * Finds the most recent requisition by requester name.
     */
    Optional<Requisition> findFirstByRequesterName(String requesterName);

    /**
     * Finds requisitions assigned to an approver.
     */
    List<Requisition> findByApproverId(String approverId);

    /**
     * Finds requisitions whose required date falls in a date range.
     */
    List<Requisition> findByRequiredDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Finds requisitions whose submitted date falls in a date range.
     */
    List<Requisition> findBySubmittedDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Keyword search on code, title, description, requester name, and justification.
     */
    List<Requisition> search(String keyword);

    /**
     * Advanced paginated search with multiple filters.
     */
    PageResponse<Requisition> searchAdvanced(RequisitionSearchFilter filter, int page, int size);
}
