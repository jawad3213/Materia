package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.masterdata.application.dtos.category.requests.CreateCategoryRequest;
import com.materia.backend.contexts.masterdata.application.dtos.category.requests.UpdateCategoryRequest;
import com.materia.backend.contexts.masterdata.application.dtos.category.responses.CategoryResponseDto;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.category.requests.CreateCategoryWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.category.requests.UpdateCategoryWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.category.responses.CategoryWebResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryWebMapper {

    public CreateCategoryRequest toAppRequest(CreateCategoryWebRequest webRequest) {
        if (webRequest == null) return null;
        CreateCategoryRequest request = new CreateCategoryRequest();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    public UpdateCategoryRequest toAppRequest(UpdateCategoryWebRequest webRequest) {
        if (webRequest == null) return null;
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    public CategoryWebResponse toWebResponse(CategoryResponseDto appResponse) {
        if (appResponse == null) return null;
        CategoryWebResponse response = new CategoryWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    public List<CategoryWebResponse> toWebResponseList(List<CategoryResponseDto> appResponses) {
        if (appResponses == null) return List.of();
        return appResponses.stream().map(this::toWebResponse).collect(Collectors.toList());
    }
}
