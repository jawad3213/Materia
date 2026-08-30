export interface ErrorResponse {
  /**
   * Timestamp of when the error occurred (ISO 8601 string format).
   */
  timestamp: string;
  
  /**
   * HTTP status code (e.g., 400, 404, 409).
   */
  status: number;
  
  /**
   * High-level error type or title (e.g., "Validation Error", "Business Error").
   */
  error: string;
  
  /**
   * Specific internal error code string (e.g., "VALIDATION_ERROR", "ILLEGAL_STATE").
   */
  errorCode: string;
  
  /**
   * Detailed human-readable error message.
   */
  message: string;
  
  /**
   * The API endpoint path where the error occurred.
   */
  path: string;
  
  /**
   * Map of field names to specific validation error messages.
   * Only populated if the error is a validation failure.
   */
  validationErrors?: Record<string, string>;
}
