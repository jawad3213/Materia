package com.materia.backend.common.domain;

import com.materia.backend.common.application.BaseRequest;
import com.materia.backend.common.application.BaseResponse;
import java.util.List;

/**
 * Base Use Case interface for common domain operations.
 *
 * @param <REQ> The type of the Request DTO
 * @param <RES> The type of the Response DTO
 * @param <ID> The type of the entity's identifier
 */
public interface BaseUseCase<REQ extends BaseRequest, RES extends BaseResponse, ID> {

    RES create(REQ request);

    RES update(ID id, REQ request);

    void delete(ID id);

    RES getById(ID id);

    RES getByCode(String code);

    List<RES> getAll();
}
