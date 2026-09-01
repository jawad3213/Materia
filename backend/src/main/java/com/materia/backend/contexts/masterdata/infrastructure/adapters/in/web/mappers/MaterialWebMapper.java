package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.masterData.application.dtos.material.CreateMaterialInput;
import com.materia.backend.contexts.masterData.application.dtos.material.UpdateMaterialInput;
import com.materia.backend.contexts.masterData.application.dtos.material.MaterialOutput;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.CreateMaterialWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.UpdateMaterialWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialWebResponse;
import org.springframework.beans.BeanUtils;
import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import org.springframework.stereotype.Component;

@Component
public class MaterialWebMapper implements BaseWebMapper<CreateMaterialWebRequest, UpdateMaterialWebRequest, CreateMaterialInput, UpdateMaterialInput, MaterialWebResponse, MaterialOutput> {

    @Override
    public CreateMaterialInput toAppCreateRequest(CreateMaterialWebRequest webRequest) {
        if (webRequest == null) return null;
        CreateMaterialInput request = new CreateMaterialInput();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    @Override
    public UpdateMaterialInput toAppUpdateRequest(UpdateMaterialWebRequest webRequest) {
        if (webRequest == null) return null;
        UpdateMaterialInput request = new UpdateMaterialInput();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    @Override
    public MaterialWebResponse toWebResponse(MaterialOutput appResponse) {
        if (appResponse == null) return null;
        MaterialWebResponse response = new MaterialWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    public com.materia.backend.contexts.masterData.application.dtos.material.MaterialSearchCriteria toAppSearchCriteria(
            com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialSearchWebRequest webRequest) {
        if (webRequest == null) return null;
        com.materia.backend.contexts.masterData.application.dtos.material.MaterialSearchCriteria criteria =
            new com.materia.backend.contexts.masterData.application.dtos.material.MaterialSearchCriteria();
        BeanUtils.copyProperties(webRequest, criteria);
        return criteria;
    }

    public com.materia.backend.contexts.masterData.application.dtos.material.MaterialFilterCriteria toAppFilterCriteria(
            com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialFilterWebRequest webRequest) {
        if (webRequest == null) return null;
        com.materia.backend.contexts.masterData.application.dtos.material.MaterialFilterCriteria criteria =
            new com.materia.backend.contexts.masterData.application.dtos.material.MaterialFilterCriteria();
        BeanUtils.copyProperties(webRequest, criteria);
        return criteria;
    }

    public com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse toWebListResponse(
            com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput appResponse) {
        if (appResponse == null) return null;
        com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse response =
            new com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    public java.util.List<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.material.MaterialListWebResponse> toWebListResponseList(
            java.util.List<com.materia.backend.contexts.masterData.application.dtos.material.MaterialListOutput> appResponses) {
        if (appResponses == null) return java.util.List.of();
        return appResponses.stream().map(this::toWebListResponse).collect(java.util.stream.Collectors.toList());
    }
}
