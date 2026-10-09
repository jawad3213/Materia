# Modular monolith: boundaries and the path to services

The backend is one deployable, but its bounded contexts are kept separable so each group can become its own
service. `ModuleBoundariesTest` (ArchUnit) fails the build when a rule below is broken, and `DataOwnershipTest` fails
it when a SQL or JPQL query names another context's table or entity.

## Target services

| Service | Contexts | Notes |
|---|---|---|
| API gateway | `gateway` | Validates JWTs, routes, rate-limits, traces (`X-Gateway-Trace-Id`). |
| Identity | `auth`, `employee` | Issues tokens (`TokenIssuer`), owns users and employees. |
| Master data | `masterData` | Materials, categories, suppliers, stock levels. |
| Procurement | `purchaseRequisition`, `purchaseOrder`, `goodsReceipt`, `returnToVendor` | One flow: PR → PO → GR → RTV. |
| Finance | `invoice`, `payment`, `settings` | Three-way match, payments, and the settings they use (price tolerance). |
| Analytics | `dashboard` (`analytics` planned) | Read-only. The dashboard composes each service's reporting use case (API composition); event-fed read models can replace calls where needed. |
| Notification (planned) | `notification` | Reacts to events. |

`common` is the shared kernel (money, currencies, base types, document code sequences, the outbox and inbox, the
change history, attachments, FX valuation). It is a library: each extracted service gets its own copy and its own
`global_code_sequences`, `outbox_messages`, `processed_events`, `audit_log`, `attachments` and `exchange_rates`
tables. The shared code names no context's table; each context plugs its own data in:

| Shared feature | What each context provides |
|---|---|
| Change history | `@Audited` on its own JPA entities (history type, code, parent, read permission). |
| Attachments | An `AttachmentOwnerLookup` per document type it owns (does the document exist?). |
| FX valuation | A `SqlDocumentFxStore` per document type it owns (its table, amount and date). |
| User names | `UserDisplayNames`, provided by identity (`AuthUserDisplayNames`). |

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

Reporting use cases (each computes its own figures on its own data; the dashboard composes them):
`RequisitionMetricsUseCase`, `PurchaseOrderMetricsUseCase`, `GoodsReceiptMetricsUseCase`, `InvoiceMetricsUseCase`,
`PaymentMetricsUseCase`, `MaterialMetricsUseCase`, `ReturnMetricsUseCase`.

In tests, `support/fixtures/CrossContextWiring` builds the real adapter and use case over the other context's
mocked repositories, so service tests keep stubbing what they always did.

## Events, the outbox and the inbox

- Every `DomainEvent` published through Spring is also written to `outbox_messages` in the same transaction
  (`OutboxRecorder`): it is stored if and only if the change commits. Events are always published through Spring,
  whatever `app.messaging.type` is; nothing sends to Kafka directly.
- `OutboxRelay` publishes pending messages to Kafka, oldest first, on `materia.<context>.events`, keyed by
  aggregate id, in an envelope `{eventId, eventType, sourceContext, aggregateId, occurredAt, payload}`. Delivery is
  at least once. Enable it with `app.outbox.relay.enabled=true` and `app.messaging.type=kafka` (tuning:
  `app.outbox.relay.interval-ms`, `batch-size`, `send-timeout-ms`).
- A context reacts to another context's events with an `IntegrationEventHandler`: it reads the envelope's JSON
  payload, never the publisher's Java class, so it works unchanged once the publisher is a separate service.
  `IntegrationEventDispatcher` runs each handler in its own transaction and records the event in
  `processed_events` (the inbox) in that transaction: a repeated delivery is skipped, a failed one is retried.
- Delivery today is in-process, after the publisher's commit (`InProcessIntegrationEvents`). With
  `app.messaging.type=kafka`, `KafkaIntegrationEventConsumer` also consumes `materia.*.events` (raw JSON, no type
  information from messages); the inbox makes the double delivery harmless. An extracted service sets
  `app.events.in-process.enabled=false` and receives only from Kafka. Tests use `app.events.in-process.mode=immediate`
  (inside the test transaction); `IntegrationEventsIT` and `ReceiptStockEventsIT` check the after-commit delivery.
- Events must stay plain data (strings, numbers, ids, enums): they are serialized to JSON.

### Procurement and master data

Procurement changes stock only through events; master data applies them:

| Event (publisher) | Handler (consumer) | Effect |
|---|---|---|
| `StockOnOrderChanged` (purchaseOrder) | `StockOnOrderHandler` (masterData) | Adds or releases stock on order. |
| `GoodsReceiptLineReceived` (goodsReceipt) | `ReceivedGoodsStockHandler` (masterData) | Accepted goods enter stock; the received quantity leaves stock on order. |
| `ReceiptStockApplied` (masterData) | `ReceiptStockLevelsHandler` (goodsReceipt) | The receipt line records the stock before and after. |

Procurement still reads master data synchronously (material lookups, which become HTTP queries), and master data
raises replenishment requisitions in procurement (one direction). No transaction spans the two.

## Not done yet

- **One database schema per service.** No foreign key and no query crosses a service boundary (`DataOwnershipTest`),
  but all tables are still in `public`. Moving them (`alter table ... set schema`) relocates live data and needs the
  migration tests reworked, so it is a separate, deliberate step.
- **The extraction itself:** a build and image per service, gateway routing to them, and service-to-service
  authentication. Each cross-context adapter (`infrastructure/adapters/out/<context>`) becomes an HTTP client; the
  services and their tests do not change.
- **Older cross-context listeners** (`@TransactionalEventListener` on another context's event class, e.g. the
  employee record following a profile edit) still use the publisher's Java class. Move them to
  `IntegrationEventHandler` when their context is extracted.
- `RequisitionOutput.getLines()` still exposes requisition entities to the requisition web layer; other
  contexts must use `getLineSummaries()`.
- `@Scheduled` is not enabled, so `ReorderService.nightlyReorderCheck` never runs.
