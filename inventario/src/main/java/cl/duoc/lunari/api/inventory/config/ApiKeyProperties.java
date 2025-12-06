package cl.duoc.lunari.api.inventory.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for API key authentication
 *
 * Two-tier system:
 * - ADMIN: Full access to all write operations (create, update, delete, stock management)
 * - SERVICE: Limited access for service-to-service calls (reduce-stock only)
 */
@Configuration
@ConfigurationProperties(prefix = "api.security")
@Getter
@Setter
public class ApiKeyProperties {

    /**
     * Admin API key for full access to protected endpoints
     */
    private String adminKey;

    /**
     * Service API key for service-to-service communication (limited access)
     */
    private String serviceKey;

    /**
     * Enable/disable API key authentication (useful for development)
     */
    private boolean enabled = true;
}
