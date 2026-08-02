package com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.mappers;

import com.materia.backend.common.infrastructure.web.BaseWebMapper;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.CreateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionOutput;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.RequisitionSearchCriteria;
import com.materia.backend.contexts.purchaseRequisition.application.dtos.UpdateRequisitionInput;
import com.materia.backend.contexts.purchaseRequisition.domain.entities.RequisitionLine;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.requisition.CreateRequisitionWebRequest;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.requisition.RequisitionLineWebRequest;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.requisition.RequisitionLineWebResponse;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.requisition.RequisitionSearchWebRequest;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.requisition.RequisitionWebResponse;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.in.web.dtos.requisition.UpdateRequisitionWebRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web mapper for purchase requisitions.
 */
@Component
public class RequisitionWebMapper implements BaseWebMapper<
        CreateRequisitionWebRequest,
        UpdateRequisitionWebRequest,
        CreateRequisitionInput,
        UpdateRequisitionInput,
        RequisitionWebResponse,
        RequisitionOutput> {

    @Override
    public CreateRequisitionInput toAppCreateRequest(CreateRequisitionWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        CreateRequisitionInput request = new CreateRequisitionInput();
        BeanUtils.copyProperties(webRequest, request, "lines", "createdBy");
        request.setLines(toDomainLines(webRequest.getLines()));
        request.setUserId(webRequest.getCreatedBy());
        return request;
    }

    @Override
    public UpdateRequisitionInput toAppUpdateRequest(UpdateRequisitionWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        UpdateRequisitionInput request = new UpdateRequisitionInput();
        BeanUtils.copyProperties(webRequest, request, "lines", "updatedBy");
        request.setLines(toDomainLines(webRequest.getLines()));
        request.setUserId(webRequest.getUpdatedBy());
        return request;
    }

    @Override
    public RequisitionWebResponse toWebResponse(RequisitionOutput appResponse) {
        if (appResponse == null) {
            return null;
        }

        RequisitionWebResponse response = new RequisitionWebResponse();
        BeanUtils.copyProperties(appResponse, response, "totalAmount", "lines");
        response.setTotalAmount(appResponse.getTotalAmount() != null ? appResponse.getTotalAmount().format() : null);
        response.setLines(toWebLines(appResponse.getLines()));
        return response;
    }

    public RequisitionSearchCriteria toAppSearchCriteria(RequisitionSearchWebRequest webRequest) {
        if (webRequest == null) {
            return null;
        }

        RequisitionSearchCriteria criteria = new RequisitionSearchCriteria();
        BeanUtils.copyProperties(webRequest, criteria);
        return criteria;
    }

    private List<RequisitionLine> toDomainLines(List<RequisitionLineWebRequest> webLines) {
        if (webLines == null) {
            return new ArrayList<>();
        }

        return webLines.stream()
                .map(this::toDomainLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private RequisitionLine toDomainLine(RequisitionLineWebRequest webLine) {
        if (webLine == null) {
            return null;
        }

        RequisitionLine line = new RequisitionLine();
        line.setId(webLine.getId());
        line.setMaterialId(webLine.getMaterialId());
        line.setMaterialCode(webLine.getMaterialCode());
        line.setQuantity(webLine.getQuantity());
        line.setRequiredDate(webLine.getRequiredDate());
        line.setSupplierId(webLine.getSupplierId());
        line.setSupplierCode(webLine.getSupplierCode());
        line.setNotes(webLine.getNotes());
        line.setDeliveryTerms(webLine.getDeliveryTerms());
        line.setStorageLocation(webLine.getStorageLocation());
        line.setBatchNumber(webLine.getBatchNumber());
        line.setExpiryDate(webLine.getExpiryDate());
        return line;
    }

    private List<RequisitionLineWebResponse> toWebLines(List<RequisitionLine> appLines) {
        if (appLines == null) {
            return new ArrayList<>();
        }

        return appLines.stream()
                .map(this::toWebLine)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private RequisitionLineWebResponse toWebLine(RequisitionLine appLine) {
        if (appLine == null) {
            return null;
        }

        RequisitionLineWebResponse response = new RequisitionLineWebResponse();
        BeanUtils.copyProperties(appLine, response, "standardPrice", "unitPrice", "lineTotal");
        response.setStandardPrice(appLine.getStandardPrice() != null ? appLine.getStandardPrice().format() : null);
        response.setUnitPrice(appLine.getUnitPrice() != null ? appLine.getUnitPrice().format() : null);
        response.setLineTotal(appLine.getLineTotal() != null ? appLine.getLineTotal().format() : null);
        return response;
    }
}
