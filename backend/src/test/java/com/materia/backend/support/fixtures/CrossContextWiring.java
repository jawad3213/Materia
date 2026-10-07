package com.materia.backend.support.fixtures;

import com.materia.backend.contexts.masterData.application.services.MaterialInventoryService;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseRequisition.domain.ports.out.MaterialCatalog;
import com.materia.backend.contexts.purchaseRequisition.domain.valueObjects.MaterialSnapshot;
import com.materia.backend.contexts.purchaseRequisition.infrastructure.adapters.out.material.MasterDataMaterialCatalog;

/**
 * Wires a context's outbound port to the real adapter and the real public use case of the context it calls, over
 * that context's (usually mocked) repositories. Tests keep stubbing the repositories they always did while the
 * production code only sees its own port.
 */
public final class CrossContextWiring {

    private CrossContextWiring() {
    }

    /** The requisition context's view of the materials held by {@code materials}. */
    public static MaterialCatalog materialCatalogOver(MaterialRepository materials) {
        return new MasterDataMaterialCatalog(new MaterialInventoryService(materials));
    }

    /** What a requisition line records of {@code material}. */
    public static MaterialSnapshot snapshotOf(Material material) {
        return MasterDataMaterialCatalog.toSnapshot(MaterialInventoryService.toReference(material));
    }
}
