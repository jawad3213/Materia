    package com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.masterData.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.CreateSupplierWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.UpdateSupplierWebRequest;
import com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierWebResponse;
import org.springframework.beans.BeanUtils;
import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import org.springframework.stereotype.Component;

    @Component
public class SupplierWebMapper implements BaseWebMapper<CreateSupplierWebRequest, UpdateSupplierWebRequest, CreateSupplierInput, UpdateSupplierInput, SupplierWebResponse, SupplierOutput> {

    @Override
    public CreateSupplierInput toAppCreateRequest(CreateSupplierWebRequest webRequest) {
        if (webRequest == null) return null;
        CreateSupplierInput request = new CreateSupplierInput();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    @Override
    public UpdateSupplierInput toAppUpdateRequest(UpdateSupplierWebRequest webRequest) {
        if (webRequest == null) return null;
        UpdateSupplierInput request = new UpdateSupplierInput();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    @Override
    public SupplierWebResponse toWebResponse(SupplierOutput appResponse) {
        if (appResponse == null) return null;
        SupplierWebResponse response = new SupplierWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    public com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierSearchCriteria toAppSearchCriteria(
            com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierSearchWebRequest webRequest) {
        if (webRequest == null) return null;
        com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierSearchCriteria criteria =
                new com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierSearchCriteria();
        BeanUtils.copyProperties(webRequest, criteria);
        return criteria;
    }

    public com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria toAppFilterCriteria(
            com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierFilterWebRequest webRequest) {
        if (webRequest == null) return null;
        com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria criteria =
                new com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierFilterCriteria();
        BeanUtils.copyProperties(webRequest, criteria);
        return criteria;
    }

    public com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse toWebListResponse(
            com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierListOutput appResponse) {
        if (appResponse == null) return null;
        com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse response =
                new com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    public java.util.List<com.materia.backend.contexts.masterData.infrastructure.adapters.in.web.dtos.supplier.SupplierListWebResponse> toWebListResponseList(
            java.util.List<com.materia.backend.contexts.masterData.application.dtos.supplier.SupplierListOutput> appResponses) {
        if (appResponses == null) return java.util.List.of();
        return appResponses.stream().map(this::toWebListResponse).collect(java.util.stream.Collectors.toList());
    }
}
