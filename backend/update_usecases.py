import os

base_dir = r'c:\Users\elhai\OneDrive\Desktop\materia\backend\src\main\java\com\materia\backend\contexts\masterdata\application'
entities = ['Category', 'Material', 'Supplier']

for entity in entities:
    lower_entity = entity.lower()
    uc_dir = os.path.join(base_dir, 'usecases', lower_entity)
    
    # Create Use Case
    with open(os.path.join(uc_dir, f'Create{entity}UseCaseImp.java'), 'w') as f:
        f.write(f'''package com.materia.backend.contexts.masterdata.application.usecases.{lower_entity};

import com.materia.backend.common.application.BaseUseCaseImp;
import com.materia.backend.contexts.masterdata.application.dtos.{lower_entity}.*;
import com.materia.backend.contexts.masterdata.application.mappers.{entity}Mapper;
import com.materia.backend.contexts.masterdata.domain.entities.{entity};
import com.materia.backend.contexts.masterdata.domain.ports.in.{entity}UseCase;

public class Create{entity}UseCaseImp implements BaseUseCaseImp<Create{entity}Request, {entity}Response> {{

    private final {entity}UseCase useCase;

    public Create{entity}UseCaseImp({entity}UseCase useCase) {{
        this.useCase = useCase;
    }}

    @Override
    public {entity}Response execute(Create{entity}Request request) {{
        try {{
            {entity} entity = {entity}Mapper.toEntity(request.getData());
            {entity} createdEntity = useCase.create(entity);
            return new {entity}Response({entity}Mapper.toDto(createdEntity));
        }} catch (Exception e) {{
            {entity}Response error = new {entity}Response();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return error;
        }}
    }}
}}
''')

    # Update Use Case
    with open(os.path.join(uc_dir, f'Update{entity}UseCaseImp.java'), 'w') as f:
        f.write(f'''package com.materia.backend.contexts.masterdata.application.usecases.{lower_entity};

import com.materia.backend.common.application.BaseUseCaseImp;
import com.materia.backend.contexts.masterdata.application.dtos.{lower_entity}.*;
import com.materia.backend.contexts.masterdata.application.mappers.{entity}Mapper;
import com.materia.backend.contexts.masterdata.domain.entities.{entity};
import com.materia.backend.contexts.masterdata.domain.ports.in.{entity}UseCase;

public class Update{entity}UseCaseImp implements BaseUseCaseImp<Update{entity}Request, {entity}Response> {{

    private final {entity}UseCase useCase;

    public Update{entity}UseCaseImp({entity}UseCase useCase) {{
        this.useCase = useCase;
    }}

    @Override
    public {entity}Response execute(Update{entity}Request request) {{
        try {{
            {entity} entityDetails = {entity}Mapper.toEntity(request.getData());
            {entity} updatedEntity = useCase.update(request.getId(), entityDetails);
            return new {entity}Response({entity}Mapper.toDto(updatedEntity));
        }} catch (Exception e) {{
            {entity}Response error = new {entity}Response();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return error;
        }}
    }}
}}
''')

    # Get Use Case
    with open(os.path.join(uc_dir, f'Get{entity}UseCaseImp.java'), 'w') as f:
        f.write(f'''package com.materia.backend.contexts.masterdata.application.usecases.{lower_entity};

import com.materia.backend.common.application.BaseUseCaseImp;
import com.materia.backend.contexts.masterdata.application.dtos.{lower_entity}.*;
import com.materia.backend.contexts.masterdata.application.mappers.{entity}Mapper;
import com.materia.backend.contexts.masterdata.domain.entities.{entity};
import com.materia.backend.contexts.masterdata.domain.ports.in.{entity}UseCase;

import java.util.Optional;

public class Get{entity}UseCaseImp implements BaseUseCaseImp<Get{entity}Request, {entity}Response> {{

    private final {entity}UseCase useCase;

    public Get{entity}UseCaseImp({entity}UseCase useCase) {{
        this.useCase = useCase;
    }}

    @Override
    public {entity}Response execute(Get{entity}Request request) {{
        try {{
            Optional<{entity}> entityOpt = useCase.getById(request.getId());
            if (entityOpt.isEmpty()) {{
                {entity}Response error = new {entity}Response();
                error.setSuccess(false);
                error.setMessage("{entity} not found");
                return error;
            }}
            return new {entity}Response({entity}Mapper.toDto(entityOpt.get()));
        }} catch (Exception e) {{
            {entity}Response error = new {entity}Response();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return error;
        }}
    }}
}}
''')

    # GetAll Use Case
    with open(os.path.join(uc_dir, f'GetAll{entity}sUseCaseImp.java'), 'w') as f:
        f.write(f'''package com.materia.backend.contexts.masterdata.application.usecases.{lower_entity};

import com.materia.backend.common.application.BaseUseCaseImp;
import com.materia.backend.contexts.masterdata.application.dtos.{lower_entity}.*;
import com.materia.backend.contexts.masterdata.application.mappers.{entity}Mapper;
import com.materia.backend.contexts.masterdata.domain.entities.{entity};
import com.materia.backend.contexts.masterdata.domain.ports.in.{entity}UseCase;

import java.util.List;

public class GetAll{entity}sUseCaseImp implements BaseUseCaseImp<GetAll{entity}sRequest, {entity}ListResponse> {{

    private final {entity}UseCase useCase;

    public GetAll{entity}sUseCaseImp({entity}UseCase useCase) {{
        this.useCase = useCase;
    }}

    @Override
    public {entity}ListResponse execute(GetAll{entity}sRequest request) {{
        try {{
            List<{entity}> entities = useCase.getAll();
            return new {entity}ListResponse({entity}Mapper.toDtoList(entities));
        }} catch (Exception e) {{
            {entity}ListResponse error = new {entity}ListResponse();
            error.setSuccess(false);
            error.setMessage(e.getMessage());
            return error;
        }}
    }}
}}
''')

print('All Use Cases updated with proper Mappers successfully.')
