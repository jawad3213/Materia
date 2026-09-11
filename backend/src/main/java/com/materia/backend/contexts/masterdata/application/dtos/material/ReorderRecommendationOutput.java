package com.materia.backend.contexts.masterData.application.dtos.material;

import com.materia.backend.common.application.BaseOutput;
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
public class ReorderRecommendationOutput extends BaseOutput {
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
