package com.materia.backend.contexts.purchaseRequisition.domain.ports.in;

import com.materia.backend.common.application.metrics.RecentDocument;

import java.util.List;

/** Reporting figures of the requisition context, computed on its own data (status names as in RequisitionStatus). */
public interface RequisitionMetricsUseCase {

    long countInStatus(String status);

    /** The latest documents, newest first. */
    List<RecentDocument> latest(int limit);
}
