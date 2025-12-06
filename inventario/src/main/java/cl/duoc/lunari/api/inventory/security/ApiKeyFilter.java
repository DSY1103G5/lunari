package cl.duoc.lunari.api.inventory.security;

import cl.duoc.lunari.api.inventory.config.ApiKeyProperties;
import cl.duoc.lunari.api.payload.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

/**
 * Filter to validate API keys for protected endpoints
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final ApiKeyProperties apiKeyProperties;
    private final RequestMappingHandlerMapping handlerMapping;
    private final ObjectMapper objectMapper;

    public ApiKeyFilter(ApiKeyProperties apiKeyProperties,
                       RequestMappingHandlerMapping handlerMapping,
                       ObjectMapper objectMapper) {
        this.apiKeyProperties = apiKeyProperties;
        this.handlerMapping = handlerMapping;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        // Skip if API key authentication is disabled
        if (!apiKeyProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Get the handler method for this request
            HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);

            if (handlerChain != null && handlerChain.getHandler() instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();

                // Check if method requires API key
                RequireApiKey requireApiKey = handlerMethod.getMethodAnnotation(RequireApiKey.class);

                if (requireApiKey != null) {
                    // Get API key from header
                    String providedKey = request.getHeader(API_KEY_HEADER);

                    if (providedKey == null || providedKey.trim().isEmpty()) {
                        sendUnauthorizedResponse(response, "API key is required. Include 'X-API-Key' header.");
                        return;
                    }

                    // Validate API key based on required type
                    if (!isValidApiKey(providedKey, requireApiKey.value())) {
                        sendUnauthorizedResponse(response, "Invalid or insufficient API key permissions.");
                        return;
                    }
                }
            }

            // Continue with the request
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // If handler mapping fails, continue (might be static resource or error endpoint)
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Validates if the provided API key has sufficient permissions
     */
    private boolean isValidApiKey(String providedKey, ApiKeyType requiredType) {
        String adminKey = apiKeyProperties.getAdminKey();
        String serviceKey = apiKeyProperties.getServiceKey();

        // Admin key has access to everything
        if (adminKey != null && adminKey.equals(providedKey)) {
            return true;
        }

        // Service key only works for SERVICE level endpoints
        if (requiredType == ApiKeyType.SERVICE && serviceKey != null && serviceKey.equals(providedKey)) {
            return true;
        }

        return false;
    }

    /**
     * Sends a 401 Unauthorized response with ApiResponse format
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse = ApiResponse.error(message, HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
