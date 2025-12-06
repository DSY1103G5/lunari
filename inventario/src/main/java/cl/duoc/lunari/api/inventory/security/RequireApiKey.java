package cl.duoc.lunari.api.inventory.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark endpoints that require API key authentication
 *
 * Usage:
 * - @RequireApiKey(ApiKeyType.ADMIN) - Requires admin key
 * - @RequireApiKey(ApiKeyType.SERVICE) - Requires service key (also accepts admin key)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireApiKey {
    ApiKeyType value() default ApiKeyType.ADMIN;
}
