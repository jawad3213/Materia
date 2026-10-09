package com.materia.backend.contexts.goodsReceipt.domain.ports.in;

import com.materia.backend.common.application.metrics.RecentDocument;
import com.materia.backend.contexts.goodsReceipt.application.dtos.ReceiptTotals;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Reporting figures of the goods receipt context, computed on its own data (status names as in ReceiptStatus). */
public interface GoodsReceiptMetricsUseCase {

    long count(Set<String> statuses, LocalDate from, LocalDate to, boolean withRejectionOnly);

    ReceiptTotals totals(Set<String> statuses);

    Map<UUID, ReceiptTotals> totalsBySupplier(Set<String> statuses);

    /** The latest documents, newest first. */
    List<RecentDocument> latest(int limit);
}
