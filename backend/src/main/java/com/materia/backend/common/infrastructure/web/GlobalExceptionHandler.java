package com.materia.backend.common.infrastructure.web;

import com.materia.backend.common.application.exceptions.BusinessException;
import com.materia.backend.common.application.exceptions.NotFoundException;
import com.materia.backend.common.application.exceptions.ValidationException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderBusinessException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderNotFoundException;
import com.materia.backend.contexts.purchaseOrder.domain.exceptions.PurchaseOrderValidationException;
import com.materia.backend.contexts.purchaseRequisition.domain.exceptions.RequisitionBusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * Handles all exceptions across the application and returns consistent error responses
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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            org.springframework.http.HttpHeaders headers,
            org.springframework.http.HttpStatusCode status,
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
