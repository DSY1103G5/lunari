package cl.duoc.lunari.api.inventory.security;

/**
 * API key types for different access levels
 */
public enum ApiKeyType {
    /**
     * Admin key - Full access to all protected endpoints
     */
    ADMIN,

    /**
     * Service key - Limited access for service-to-service communication
     * Can only access specific endpoints (e.g., reduce-stock)
     * Admin key also works for service endpoints
     */
    SERVICE
}
