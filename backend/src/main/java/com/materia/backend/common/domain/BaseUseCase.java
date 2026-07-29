package com.materia.backend.common.domain;

import com.materia.backend.common.application.BaseInput;
import com.materia.backend.common.application.BaseOutput;
import java.util.List;

/**
 * Base Use Case interface for common domain operations.
 *
 * @param <REQ> The type of the Request DTO
 * @param <RES> The type of the Response DTO
 * @param <ID> The type of the entity's identifier
 */
public interface BaseUseCase<REQ extends BaseInput, RES extends BaseOutput, ID> {

    RES create(REQ request);

    RES update(ID id, REQ request);

    void delete(ID id);

    RES getById(ID id);

    RES getByCode(String code);

    List<RES> getAll();
}
