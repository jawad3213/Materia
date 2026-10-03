import type { DeliveryStatus } from './DeliveryStatus';

/**
 * Submit purchase order to supplier
 * Matches backend PurchaseOrderSubmitWebRequest.java
 */
export interface PurchaseOrderSubmitRequest {
  userId: string;
}
export type PurchaseOrderSubmitWebRequest = PurchaseOrderSubmitRequest;

/**
 * Confirm purchase order by supplier
 * Matches backend PurchaseOrderConfirmWebRequest.java
 */
export interface PurchaseOrderConfirmRequest {
  userId: string;
}
export type PurchaseOrderConfirmWebRequest = PurchaseOrderConfirmRequest;

/**
 * Assign warehouse receiver to purchase order
 * Matches backend PurchaseOrderAssignReceiverWebRequest.java
 */
export interface PurchaseOrderAssignReceiverRequest {
  userId: string;
  userName: string;
  assignedUserId: string;
  assignedUserName: string;
}
export type PurchaseOrderAssignReceiverWebRequest = PurchaseOrderAssignReceiverRequest;

/**
 * Confirm receipt of materials
 * Matches backend PurchaseOrderConfirmReceiptWebRequest.java
 */
export interface PurchaseOrderConfirmReceiptRequest {
  receiverId: string;
  receiverName: string;
}
export type PurchaseOrderConfirmReceiptWebRequest = PurchaseOrderConfirmReceiptRequest;

/**
 * Record a supplier's rejection of a submitted order
 * Matches backend PurchaseOrderRejectWebRequest.java
 */
export interface PurchaseOrderRejectRequest {
  reason: string;
}

/**
 * A user the order can be assigned to for receipt
 * Matches backend ReceiverDirectory.Receiver
 */
export interface AssignableReceiver {
  id: string;
  name: string;
  email: string;
}

/**
 * Cancel purchase order with mandatory reason
 * Matches backend PurchaseOrderCancelWebRequest.java
 */
export interface PurchaseOrderCancelRequest {
  userId: string;
  reason: string;
}
export type PurchaseOrderCancelWebRequest = PurchaseOrderCancelRequest;

/**
 * Complete purchase order
 * Matches backend PurchaseOrderCompleteWebRequest.java
 */
export interface PurchaseOrderCompleteRequest {
  userId: string;
}
export type PurchaseOrderCompleteWebRequest = PurchaseOrderCompleteRequest;

/**
 * Update delivery status
 * Matches backend UpdatePurchaseOrderDeliveryStatusWebRequest.java
 */
export interface UpdatePurchaseOrderDeliveryStatusRequest {
  deliveryStatus: DeliveryStatus;
  userId: string;
}
export type UpdatePurchaseOrderDeliveryStatusWebRequest = UpdatePurchaseOrderDeliveryStatusRequest;
