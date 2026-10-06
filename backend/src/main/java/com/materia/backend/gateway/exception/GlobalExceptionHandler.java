package com.materia.backend.gateway.exception;

import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.application.exceptions.NotFoundException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.common.infrastructure.web.ErrorResponse;
import com.materia.backend.contexts.auth.domain.exceptions.AuthenticationFailedException;
import com.materia.backend.contexts.auth.domain.exceptions.EmailAlreadyExistsException;
import com.materia.backend.contexts.auth.domain.exceptions.InvalidTokenException;
import com.materia.backend.contexts.auth.domain.exceptions.TokenExpiredException;
import com.materia.backend.contexts.auth.domain.exceptions.UserAlreadyExistsException;
import com.materia.backend.contexts.auth.domain.exceptions.UserNotFoundException;
import com.materia.backend.contexts.goodsReceipt.domain.exceptions.GoodsReceiptBusinessException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceAlreadyExistsException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentAlreadyCompletedException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentAlreadyExistsException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentAmountMismatchException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNoInvoicesException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotCancellableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotCompletableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotFoundException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotModifiableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentNotPreparableException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentRuleViolationException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentSupplierMismatchException;
import com.materia.backend.contexts.payment.domain.exceptions.PaymentValidationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceAlreadyPaidException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceAlreadyVerifiedException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceCancellationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceLineValidationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotFoundException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotModifiableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotPayableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceNotVerifiableException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoicePurchaseOrderMismatchException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceRuleViolationException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceSupplierMismatchException;
import com.materia.backend.contexts.invoice.domain.exceptions.InvoiceValidationException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorBusinessException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorNotFoundException;
import com.materia.backend.contexts.returnToVendor.domain.exceptions.ReturnToVendorValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderBusinessException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionBusinessException;
import com.materia.backend.contexts.employee.domain.exceptions.EmailAlreadyInUseException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeCodeAlreadyExistsException;
import com.materia.backend.contexts.employee.domain.exceptions.EmployeeNotFoundException;
import com.materia.backend.contexts.masterData.domain.exceptions.DuplicateMaterialCodeException;
import com.materia.backend.contexts.auth.domain.exceptions.AccountLockedException;
import com.materia.backend.contexts.auth.domain.exceptions.InvalidCredentialsException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 🔹 GLOBAL EXCEPTION HANDLER (API GATEWAY)
 * 
 * Gestionnaire centralisé des exceptions pour l'ensemble de l'application Materia.
 * Capture et normalise toutes les erreurs (métier, validation, sécurité, rate-limiting, infrastructure).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RequisitionBusinessException.class)
    public ResponseEntity<ErrorResponse> handleRequisitionBusinessException(
            RequisitionBusinessException ex, WebRequest request) {
        log.error("Purchase requisition exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Purchase Requisition Error",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(PurchaseOrderBusinessException.class)
    public ResponseEntity<ErrorResponse> handlePurchaseOrderBusinessException(
            PurchaseOrderBusinessException ex, WebRequest request) {
        log.error("Purchase order exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Purchase Order Error",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(GoodsReceiptBusinessException.class)
    public ResponseEntity<ErrorResponse> handleGoodsReceiptBusinessException(
            GoodsReceiptBusinessException ex, WebRequest request) {
        log.error("Goods receipt exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Goods Receipt Error",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException ex, WebRequest request) {
        log.error("Payment not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Payment Not Found", "PAYMENT_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler({PaymentValidationException.class, PaymentNoInvoicesException.class})
    public ResponseEntity<ErrorResponse> handlePaymentValidationException(RuntimeException ex, WebRequest request) {
        log.error("Payment validation error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Payment Validation Error", "PAYMENT_VALIDATION_ERROR",
                ex.getMessage(), request, null);
    }

    /** Payment operations refused by a business rule answer 409, like invoices and purchase orders. */
    @ExceptionHandler({PaymentRuleViolationException.class, PaymentAmountMismatchException.class,
            PaymentSupplierMismatchException.class, PaymentNotModifiableException.class, PaymentNotPreparableException.class,
            PaymentNotCompletableException.class, PaymentNotCancellableException.class, PaymentAlreadyCompletedException.class,
            PaymentAlreadyExistsException.class})
    public ResponseEntity<ErrorResponse> handlePaymentRuleViolation(RuntimeException ex, WebRequest request) {
        log.error("Payment rule violation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Payment Error", "PAYMENT_RULE_VIOLATION", ex.getMessage(), request, null);
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvoiceNotFoundException(InvoiceNotFoundException ex, WebRequest request) {
        log.error("Invoice not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Invoice Not Found", "INVOICE_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler({InvoiceValidationException.class, InvoiceLineValidationException.class})
    public ResponseEntity<ErrorResponse> handleInvoiceValidationException(RuntimeException ex, WebRequest request) {
        log.error("Invoice validation error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invoice Validation Error", "INVOICE_VALIDATION_ERROR",
                ex.getMessage(), request, null);
    }

    /** Invoice operations refused by a business rule answer 409, like purchase orders and goods receipts. */
    @ExceptionHandler({InvoiceRuleViolationException.class, InvoiceNotModifiableException.class,
            InvoiceNotVerifiableException.class, InvoiceNotPayableException.class, InvoiceCancellationException.class,
            InvoiceAlreadyPaidException.class, InvoiceAlreadyVerifiedException.class, InvoiceAlreadyExistsException.class,
            InvoiceSupplierMismatchException.class, InvoicePurchaseOrderMismatchException.class})
    public ResponseEntity<ErrorResponse> handleInvoiceRuleViolation(RuntimeException ex, WebRequest request) {
        log.error("Invoice rule violation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Invoice Error", "INVOICE_RULE_VIOLATION", ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReturnToVendorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReturnToVendorNotFoundException(ReturnToVendorNotFoundException ex, WebRequest request) {
        log.error("Return to vendor not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Return Not Found", "RETURN_TO_VENDOR_NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReturnToVendorValidationException.class)
    public ResponseEntity<ErrorResponse> handleReturnToVendorValidationException(ReturnToVendorValidationException ex, WebRequest request) {
        log.error("Return to vendor validation error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Return Validation Error", "RETURN_TO_VENDOR_VALIDATION_ERROR",
                ex.getMessage(), request, null);
    }

    /** Return operations refused by a business rule (quantities, status, order state) answer 409, like the rest of the chain. */
    @ExceptionHandler(ReturnToVendorBusinessException.class)
    public ResponseEntity<ErrorResponse> handleReturnToVendorBusinessException(ReturnToVendorBusinessException ex, WebRequest request) {
        log.error("Return to vendor rule violation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Return Error", "RETURN_TO_VENDOR_RULE_VIOLATION", ex.getMessage(), request, null);
    }

    @ExceptionHandler(PurchaseOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePurchaseOrderNotFoundException(
            PurchaseOrderNotFoundException ex, WebRequest request) {
        log.error("Purchase order not found exception: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Purchase Order Not Found",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(PurchaseOrderValidationException.class)
    public ResponseEntity<ErrorResponse> handlePurchaseOrderValidationException(
            PurchaseOrderValidationException ex, WebRequest request) {
        log.error("Purchase order validation exception: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Purchase Order Validation Error",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                ex.hasErrors() ? ex.getErrors() : null
        );
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(
            AuthenticationFailedException ex, WebRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication Failed",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        log.warn("User already exists: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "User Already Exists",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex, WebRequest request) {
        log.warn("Invalid token: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Token",
                "AUTH_INVALID_TOKEN",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(
            TokenExpiredException ex, WebRequest request) {
        log.warn("Token expired: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Token Expired",
                "AUTH_TOKEN_EXPIRED",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "User Not Found",
                "AUTH_USER_NOT_FOUND",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, WebRequest request) {
        log.warn("Email already exists: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Email Already Exists",
                "AUTH_EMAIL_ALREADY_EXISTS",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(EmployeeCodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeCodeAlreadyExistsException(
            EmployeeCodeAlreadyExistsException ex, WebRequest request) {
        log.warn("Employee code already exists: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Employee Code Conflict",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyInUseException(
            EmailAlreadyInUseException ex, WebRequest request) {
        log.warn("Employee email already in use: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Employee Email Conflict",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    // ----- FINDING-005: DuplicateMaterialCodeException → 409 -----

    @ExceptionHandler(DuplicateMaterialCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMaterialCodeException(
            DuplicateMaterialCodeException ex, WebRequest request) {
        log.warn("Duplicate material code: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Duplicate Material Code",
                "DUPLICATE_MATERIAL_CODE",
                ex.getMessage(),
                request,
                null
        );
    }

    // ----- FINDING-006: AccountLockedException → 401 -----

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(
            AccountLockedException ex, WebRequest request) {
        log.warn("Account locked: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Account Locked",
                "AUTH_ACCOUNT_LOCKED",
                ex.getMessage(),
                request,
                null
        );
    }

    // ----- FINDING-007: InvalidCredentialsException → 401 -----

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex, WebRequest request) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid Credentials",
                "AUTH_INVALID_CREDENTIALS",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.error("Business exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Business Error",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex, WebRequest request) {
        log.error("Not found exception: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        log.error("Validation exception: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                ex.getErrorCode(),
                ex.getFormattedMessage(),
                request,
                ex.hasErrors() ? ex.getErrors() : null
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        log.error("Constraint violation exception: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(String.valueOf(violation.getPropertyPath()), violation.getMessage())
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "VALIDATION_ERROR",
                "Validation failed",
                request,
                errors
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        log.error("Illegal argument exception: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "VALIDATION_ERROR",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        log.error("Illegal state exception: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Business Error",
                "ILLEGAL_STATE",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(
            RuntimeException ex, WebRequest request) {
        log.error("Optimistic lock exception: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                "CONCURRENT_MODIFICATION",
                "The resource was modified by another request. Please reload and try again.",
                request,
                null
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(
            RateLimitExceededException ex, WebRequest request) {
        log.warn("Rate limit violation: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage(),
                request,
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "FORBIDDEN",
                "You do not have permission to access this resource",
                request,
                null
        );
    }

    /**
     * A database constraint refused the change: most often a delete of a record other documents still reference
     * (a supplier with orders, a material on order lines), or a duplicate unique value.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        String detail = ex.getMostSpecificCause().getMessage();
        log.warn("Data integrity violation: {}", detail);
        boolean referenced = detail != null && detail.contains("foreign key");
        String message = referenced
                ? "This record is referenced by other documents and cannot be changed or deleted"
                : "The change conflicts with existing data (a value that must be unique is already used)";
        return buildErrorResponse(HttpStatus.CONFLICT, "Data Conflict",
                referenced ? "REFERENCED_RECORD" : "DATA_INTEGRITY_VIOLATION", message, request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "INTERNAL_SERVER_ERROR",
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
                request,
                null
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = buildErrorResponseBody(
                status.value(),
                "Validation Error",
                "VALIDATION_ERROR",
                "Validation failed",
                request,
                errors
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,
                                                             String error,
                                                             String errorCode,
                                                             String message,
                                                             WebRequest request,
                                                             Map<String, String> validationErrors) {
        return new ResponseEntity<>(
                buildErrorResponseBody(status.value(), error, errorCode, message, request, validationErrors),
                status
        );
    }

    private ErrorResponse buildErrorResponseBody(int status,
                                                 String error,
                                                 String errorCode,
                                                 String message,
                                                 WebRequest request,
                                                 Map<String, String> validationErrors) {
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .errorCode(errorCode)
                .message(message)
                .path(request.getDescription(false));

        if (validationErrors != null && !validationErrors.isEmpty()) {
            builder.validationErrors(validationErrors);
        }

        return builder.build();
    }
}
