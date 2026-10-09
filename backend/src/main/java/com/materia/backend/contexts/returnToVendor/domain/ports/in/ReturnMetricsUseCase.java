package com.materia.backend.contexts.returnToVendor.domain.ports.in;

import com.materia.backend.common.application.metrics.RecentDocument;

import java.util.List;

/** Reporting figures of the return-to-vendor context, computed on its own data (status names as in ReturnStatus). */
public interface ReturnMetricsUseCase {

    long countInStatus(String status);

    /** The latest documents, newest first. */
    List<RecentDocument> latest(int limit);
}
