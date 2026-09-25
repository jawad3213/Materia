package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualReorderWebRequest {
    private Integer quantity;
    private String reason;
}
