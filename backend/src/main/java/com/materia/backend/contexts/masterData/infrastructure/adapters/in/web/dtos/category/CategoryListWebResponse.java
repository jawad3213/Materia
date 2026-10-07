package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.category;

import lombok.Data;
import java.util.UUID;

@Data
public class CategoryListWebResponse {
    private UUID id;
    private String code;
    private String name;
    private String categoryType;
    private String status;
    private Integer materialCount;
    private Integer subCategoryCount;
    private Integer totalItems;
    private Integer level;
    private String path;
    private String parentCode;
}
