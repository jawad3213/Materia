package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.masterdata.application.dtos.supplier.CreateSupplierInput;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.UpdateSupplierInput;
import com.materia.backend.contexts.masterdata.application.dtos.supplier.SupplierOutput;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.CreateSupplierWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.UpdateSupplierWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.supplier.SupplierWebResponse;
import org.springframework.beans.BeanUtils;
import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    
}
