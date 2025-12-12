package cl.duoc.lunari.api.user.exception;

import cl.duoc.lunari.api.payload.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Catches exceptions and returns properly formatted error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle invalid credentials exceptions
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials attempt: {}", ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error_code", "INVALID_CREDENTIALS");
        errorDetails.put("message", "Credenciales inválidas");
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setResponse(errorDetails);
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle inactive account exceptions
     */
    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleAccountInactive(AccountInactiveException ex) {
        log.warn("Inactive account login attempt: {}", ex.getMessage());

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error_code", "ACCOUNT_INACTIVE");
        errorDetails.put("message", "Cuenta inactiva. Por favor contacta a soporte.");
        errorDetails.put("status", HttpStatus.FORBIDDEN.value());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setResponse(errorDetails);
        response.setStatusCode(HttpStatus.FORBIDDEN.value());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle generic runtime exceptions (fallback)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error_code", "INTERNAL_ERROR");
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        ApiResponse<Map<String, Object>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setResponse(errorDetails);
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
