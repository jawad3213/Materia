package com.materia.backend.contexts.masterData.application.dtos.material;

import com.materia.backend.common.application.BaseOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManualReorderOutput extends BaseOutput {
    private UUID materialId;
    private String materialCode;
    private String requisitionId;
    private Integer quantity;
    private String message;
}
