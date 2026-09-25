package com.materia.backend.contexts.employee.infrastructure.adapters.in.web.mappers;

import com.materia.backend.contexts.employee.application.dtos.CreateEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.EmployeeOutput;
import com.materia.backend.contexts.employee.application.dtos.OffboardEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.OnboardEmployeeInput;
import com.materia.backend.contexts.employee.application.dtos.UpdateEmployeeInput;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.CreateEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.OffboardEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.OnboardEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.request.UpdateEmployeeWebRequest;
import com.materia.backend.contexts.employee.infrastructure.adapters.in.web.dtos.response.EmployeeWebResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeWebMapper {

    public CreateEmployeeInput toAppCreateInput(CreateEmployeeWebRequest webRequest) {
        if (webRequest == null) return null;
        return new CreateEmployeeInput(
                webRequest.code(),
                webRequest.firstName(),
                webRequest.lastName(),
                webRequest.email(),
                webRequest.phone(),
                webRequest.hireDate(),
                webRequest.status()
        );
    }

    public UpdateEmployeeInput toAppUpdateInput(UpdateEmployeeWebRequest webRequest) {
        if (webRequest == null) return null;
        return new UpdateEmployeeInput(
                webRequest.firstName(),
                webRequest.lastName(),
                webRequest.phone(),
                webRequest.status()
        );
    }

    public OnboardEmployeeInput toAppOnboardInput(OnboardEmployeeWebRequest webRequest) {
        if (webRequest == null) return null;
        return new OnboardEmployeeInput(
                webRequest.code(),
                webRequest.firstName(),
                webRequest.lastName(),
                webRequest.email(),
                webRequest.phone(),
                webRequest.hireDate(),
                webRequest.status(),
                webRequest.provisionCredentials(),
                webRequest.roleCode(),
                webRequest.initialPassword()
        );
    }

    public OffboardEmployeeInput toAppOffboardInput(OffboardEmployeeWebRequest webRequest) {
        if (webRequest == null) return null;
        return new OffboardEmployeeInput(
                webRequest.employeeId(),
                webRequest.terminationDate(),
                webRequest.reason(),
                webRequest.revokeUserAccess()
        );
    }

    public EmployeeWebResponse toWebResponse(EmployeeOutput output) {
        if (output == null) return null;
        return new EmployeeWebResponse(
                output.id(),
                output.code(),
                output.firstName(),
                output.lastName(),
                output.fullName(),
                output.email(),
                output.phone(),
                output.status(),
                output.userId(),
                output.role(),
                output.hireDate(),
                output.terminationDate(),
                output.terminationReason(),
                output.createdAt(),
                output.updatedAt()
        );
    }

    public List<EmployeeWebResponse> toWebResponseList(List<EmployeeOutput> outputs) {
        if (outputs == null || outputs.isEmpty()) return Collections.emptyList();
        return outputs.stream()
                .map(this::toWebResponse)
                .collect(Collectors.toList());
    }
}
