package com.materia.backend.support.fixtures;

import com.materia.backend.contexts.goodsReceipt.domain.ports.out.OrderedGoods;
import com.materia.backend.contexts.goodsReceipt.domain.ports.out.ReceivedStock;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.purchaseOrder.PurchaseOrderGoodsAdapter;
import com.materia.backend.contexts.goodsReceipt.infrastructure.adapters.out.stock.MaterialReceivedStock;
import com.materia.backend.contexts.masterData.application.services.MaterialInventoryService;
import com.materia.backend.contexts.masterData.domain.entities.Material;
import com.materia.backend.contexts.masterData.domain.ports.out.MaterialRepository;
import com.materia.backend.contexts.purchaseOrder.application.services.PurchaseOrderReceivingService;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderEventPublisher;
import com.materia.backend.contexts.purchaseOrder.domain.ports.out.PurchaseOrderRepository;
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

    /** The goods receipt context's view of the purchase orders held by {@code orders}. */
    public static OrderedGoods orderedGoodsOver(PurchaseOrderRepository orders, PurchaseOrderEventPublisher orderEvents) {
        return new PurchaseOrderGoodsAdapter(new PurchaseOrderReceivingService(orders, orderEvents));
    }

    /** The goods receipt context's view of the stock of the materials held by {@code materials}. */
    public static ReceivedStock receivedStockOver(MaterialRepository materials) {
        return new MaterialReceivedStock(new MaterialInventoryService(materials));
    }
}
