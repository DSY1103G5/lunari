package cl.duoc.lunari.api.inventory.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        // Get error attributes safely
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        // Use HashMap instead of Map.of() to handle null values
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", statusCode != null ? statusCode : 500);
        errorResponse.put("error", HttpStatus.valueOf(statusCode != null ? statusCode : 500).getReasonPhrase());
        errorResponse.put("message", errorMessage != null ? errorMessage : "Internal Server Error");
        errorResponse.put("path", requestUri != null ? requestUri : "unknown");

        HttpStatus status = statusCode != null ? HttpStatus.valueOf(statusCode) : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(errorResponse);
    }
}
