package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material;
import com.materia.backend.common.domain.enums.CurrencyCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReorderRecommendationWebResponse {
    private UUID materialId;
    private String materialCode;
    private String materialName;
    private Integer currentStock;
    private Integer stockOnOrder;
    private Integer virtualStock;
    private Integer reorderPoint;
    private Integer safetyStock;
    private Integer recommendedQuantity;
    private BigDecimal estimatedCost;
    private String currencyCode;
    private String reason;
    private Boolean isUrgent;
    private String stockStatus;
}
