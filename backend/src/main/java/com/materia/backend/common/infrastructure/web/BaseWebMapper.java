package com.materia.backend.common.infrastructure.web;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base interface for mapping between Web DTOs and Application Inputs/Outputs.
 *
 * @param <CWR> Create Web Request type
 * @param <UWR> Update Web Request type
 * @param <CI>  Create Application Input type
 * @param <UI>  Update Application Input type
 * @param <WR>  Web Response type
 * @param <O>   Application Output type
 */
public interface BaseWebMapper<CWR, UWR, CI, UI, WR, O> {

    CI toAppCreateRequest(CWR webRequest);

    UI toAppUpdateRequest(UWR webRequest);

    WR toWebResponse(O appResponse);

    default List<WR> toWebResponseList(List<O> appResponses) {
        if (appResponses == null) {
            return List.of();
        }
        return appResponses.stream()
                .map(this::toWebResponse)
                .collect(Collectors.toList());
    }
}
