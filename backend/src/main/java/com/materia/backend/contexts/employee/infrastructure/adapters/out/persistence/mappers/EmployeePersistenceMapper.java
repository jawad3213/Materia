package com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.mappers;

import com.materia.backend.contexts.employee.domain.entities.Employee;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeCode;
import com.materia.backend.contexts.employee.domain.valueObjects.EmployeeFullName;
import com.materia.backend.contexts.employee.infrastructure.adapters.out.persistence.entities.EmployeeJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class EmployeePersistenceMapper {

    public Employee toDomain(EmployeeJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        EmployeeCode code = entity.getCode() != null ? EmployeeCode.of(entity.getCode()) : null;
        EmployeeFullName fullName = (entity.getFirstName() != null && entity.getLastName() != null)
                ? EmployeeFullName.of(entity.getFirstName(), entity.getLastName())
                : null;

        return Employee.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .version(entity.getVersion())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .code(code)
                .fullName(fullName)
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .userId(entity.getUserId())
                .hireDate(entity.getHireDate())
                .terminationDate(entity.getTerminationDate())
                .terminationReason(entity.getTerminationReason())
                .build();
    }

    public EmployeeJpaEntity toJpaEntity(Employee domain) {
        if (domain == null) {
            return null;
        }

        EmployeeJpaEntity entity = new EmployeeJpaEntity();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setUpdatedBy(domain.getUpdatedBy());

        if (domain.getCode() != null) {
            entity.setCode(domain.getCode().getValue());
        }
        if (domain.getFullName() != null) {
            entity.setFirstName(domain.getFullName().getFirstName());
            entity.setLastName(domain.getFullName().getLastName());
            entity.setFullName(domain.getFullName().getFullName());
        }
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        entity.setStatus(domain.getStatus());
        entity.setUserId(domain.getUserId());
        entity.setHireDate(domain.getHireDate());
        entity.setTerminationDate(domain.getTerminationDate());
        entity.setTerminationReason(domain.getTerminationReason());

        return entity;
    }
}
