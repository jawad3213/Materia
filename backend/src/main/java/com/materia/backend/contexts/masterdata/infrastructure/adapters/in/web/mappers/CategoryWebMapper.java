package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.masterData.application.dtos.category.CreateCategoryInput;
import com.materia.backend.contexts.masterData.application.dtos.category.UpdateCategoryInput;
import com.materia.backend.contexts.masterData.application.dtos.category.CategoryOutput;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.category.CreateCategoryWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.category.UpdateCategoryWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.category.CategoryWebResponse;
import org.springframework.beans.BeanUtils;
import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoryWebMapper implements BaseWebMapper<CreateCategoryWebRequest, UpdateCategoryWebRequest, CreateCategoryInput, UpdateCategoryInput, CategoryWebResponse, CategoryOutput> {

    @Override
    public CreateCategoryInput toAppCreateRequest(CreateCategoryWebRequest webRequest) {
        if (webRequest == null) return null;
        CreateCategoryInput request = new CreateCategoryInput();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    @Override
    public UpdateCategoryInput toAppUpdateRequest(UpdateCategoryWebRequest webRequest) {
        if (webRequest == null) return null;
        UpdateCategoryInput request = new UpdateCategoryInput();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    @Override
    public CategoryWebResponse toWebResponse(CategoryOutput appResponse) {
        if (appResponse == null) return null;
        CategoryWebResponse response = new CategoryWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    
}
