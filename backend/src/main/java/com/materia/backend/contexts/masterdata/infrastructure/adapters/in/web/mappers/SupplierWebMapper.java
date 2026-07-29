package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.requests.CreateSupplierRequest;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.requests.UpdateSupplierRequest;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.responses.SupplierResponseDto;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.requests.CreateSupplierWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.requests.UpdateSupplierWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.responses.SupplierWebResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SupplierWebMapper {

    public CreateSupplierRequest toAppRequest(CreateSupplierWebRequest webRequest) {
        if (webRequest == null) return null;
        CreateSupplierRequest request = new CreateSupplierRequest();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    public UpdateSupplierRequest toAppRequest(UpdateSupplierWebRequest webRequest) {
        if (webRequest == null) return null;
        UpdateSupplierRequest request = new UpdateSupplierRequest();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    public SupplierWebResponse toWebResponse(SupplierResponseDto appResponse) {
        if (appResponse == null) return null;
        SupplierWebResponse response = new SupplierWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    public List<SupplierWebResponse> toWebResponseList(List<SupplierResponseDto> appResponses) {
        if (appResponses == null) return List.of();
        return appResponses.stream().map(this::toWebResponse).collect(Collectors.toList());
    }
}
