# Modular monolith: boundaries and the path to services

The backend is one deployable, but its bounded contexts are kept separable so each group can become its own
service. `ModuleBoundariesTest` (ArchUnit) fails the build when a rule below is broken.

## Target services

| Service | Contexts | Notes |
|---|---|---|
| API gateway | `gateway` | Validates JWTs, routes, rate-limits, traces (`X-Gateway-Trace-Id`). |
| Identity | `auth`, `employee` | Issues tokens (`TokenIssuer`), owns users and employees. |
| Master data | `masterData` | Materials, categories, suppliers, stock levels. |
| Procurement | `purchaseRequisition`, `purchaseOrder`, `goodsReceipt`, `returnToVendor` | One flow: PR → PO → GR → RTV. |
| Finance | `invoice`, `payment` | Three-way match and payments. |
| Analytics (planned) | `analytics` | Read-only, fed by events only. |
| Notification (planned) | `notification` | Reacts to events. |

`common` is the shared kernel (money, currencies, base types, document code sequences, the outbox). Each
extracted service gets its own copy and its own `global_code_sequences` / `outbox_messages` tables.

## Rules

1. **A context's `domain` and `application` layers know no other context and no gateway.** They reach the
   outside through their own outbound ports (`domain/ports/out`) with their own value types.
2. **Other code uses a context only through its public API:** `domain.ports.in`, `application.dtos`, and the
   `domain.enums`, `domain.events` and `domain.exceptions` those expose. In practice only infrastructure
   adapters (`infrastructure/adapters/out/<other context>`) and the gateway do this.
3. **`common` knows no context.**
4. **A plain `@EventListener` only handles its own context's events.** Another context's events are handled
   after commit with `@TransactionalEventListener`, as they will be when they arrive from another service.

## How a cross-context call is wired

```
goodsReceipt.application.GoodsReceiptService
   └─ OrderedGoods                     (goodsReceipt/domain/ports/out, GR-owned records)
        └─ PurchaseOrderGoodsAdapter   (goodsReceipt/infrastructure/adapters/out/purchaseOrder)
             └─ PurchaseOrderLookupUseCase / PurchaseOrderReceivingUseCase   (purchaseOrder public API)
```

To extract a service, replace the adapter's body with an HTTP client (or an event consumer); the service and its
tests do not change.

Public lookups added for this purpose:

| Context | Public use cases |
|---|---|
| auth | `UserDirectoryUseCase` (`UserSummary`), `AccountProvisioningUseCase` |
| masterData | `MaterialInventoryUseCase` (`MaterialReference`, `StockMovement`) |
| purchaseRequisition | `ReplenishmentUseCase`, `RequisitionUseCase` (`RequisitionLineSummary`) |
| purchaseOrder | `PurchaseOrderLookupUseCase` (`PurchaseOrderView`), `PurchaseOrderReceivingUseCase` |
| goodsReceipt | `GoodsReceiptLookupUseCase` (`GoodsReceiptView`) |
| returnToVendor | `ReplacementQuantitiesUseCase` |

In tests, `support/fixtures/CrossContextWiring` builds the real adapter and use case over the other context's
mocked repositories, so service tests keep stubbing what they always did.

## Events and the outbox

- Every `DomainEvent` published through Spring is also written to `outbox_messages` in the same transaction
  (`OutboxRecorder`): it is stored if and only if the change commits.
- `OutboxRelay` publishes pending messages to Kafka, oldest first, on `materia.<context>.events`, keyed by
  aggregate id. Delivery is at least once: consumers must ignore an `eventId` they already processed.
- The relay is off by default. Enable it with `app.outbox.relay.enabled=true` and `app.messaging.type=kafka`
  (tuning: `app.outbox.relay.interval-ms`, `batch-size`, `send-timeout-ms`).
- Events must stay plain data (strings, numbers, ids, enums): they are serialized to JSON.

## Not done yet

- **One database schema per service.** No foreign key crosses a service boundary today, but all tables are
  still in `public`. Moving them (`alter table ... set schema`) relocates live data and needs the migration
  tests reworked, so it is a separate, deliberate step.
- **Master data ↔ procurement call each other synchronously** (replenishment raises requisitions; procurement
  moves stock). Before extracting master data, turn one direction into events (e.g. procurement publishes
  receipts and master data updates stock from them).
- `RequisitionOutput.getLines()` still exposes requisition entities to the requisition web layer; other
  contexts must use `getLineSummaries()`.
- `@Scheduled` is not enabled, so `ReorderService.nightlyReorderCheck` never runs.
