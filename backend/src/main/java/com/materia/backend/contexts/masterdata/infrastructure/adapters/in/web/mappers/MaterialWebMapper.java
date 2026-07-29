package com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.masterdata.application.dtos.material.requests.CreateMaterialRequest;
import com.materia.backend.contexts.masterdata.application.dtos.material.requests.UpdateMaterialRequest;
import com.materia.backend.contexts.masterdata.application.dtos.material.responses.MaterialResponseDto;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material.requests.CreateMaterialWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material.requests.UpdateMaterialWebRequest;
import com.materia.backend.contexts.masterdata.infrastructure.adapters.in.web.dtos.material.responses.MaterialWebResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MaterialWebMapper {

    public CreateMaterialRequest toAppRequest(CreateMaterialWebRequest webRequest) {
        if (webRequest == null) return null;
        CreateMaterialRequest request = new CreateMaterialRequest();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    public UpdateMaterialRequest toAppRequest(UpdateMaterialWebRequest webRequest) {
        if (webRequest == null) return null;
        UpdateMaterialRequest request = new UpdateMaterialRequest();
        BeanUtils.copyProperties(webRequest, request);
        return request;
    }

    public MaterialWebResponse toWebResponse(MaterialResponseDto appResponse) {
        if (appResponse == null) return null;
        MaterialWebResponse response = new MaterialWebResponse();
        BeanUtils.copyProperties(appResponse, response);
        return response;
    }

    public List<MaterialWebResponse> toWebResponseList(List<MaterialResponseDto> appResponses) {
        if (appResponses == null) return List.of();
        return appResponses.stream().map(this::toWebResponse).collect(Collectors.toList());
    }
}
