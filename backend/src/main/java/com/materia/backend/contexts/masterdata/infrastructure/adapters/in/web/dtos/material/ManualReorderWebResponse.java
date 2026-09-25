package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualReorderWebResponse {
    private UUID materialId;
    private String materialCode;
    private String requisitionId;
    private Integer quantity;
    private String message;
}
