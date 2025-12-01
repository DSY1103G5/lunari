package cl.duoc.lunari.api.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuración de Transbank WebPay Plus
 */
@Configuration
public class TransbankConfig {

    @Value("${transbank.api.key}")
    private String apiKey;

    @Value("${transbank.commerce.code}")
    private String commerceCode;

    @Value("${transbank.environment:TEST}")
    private String environment;

    @PostConstruct
    public void init() {
        System.out.println("Transbank configurado - Ambiente: " + environment);
        System.out.println("Commerce Code: " + commerceCode);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCommerceCode() {
        return commerceCode;
    }

    public String getEnvironment() {
        return environment;
    }

    public boolean isProduction() {
        return "PROD".equalsIgnoreCase(environment) || "PRODUCTION".equalsIgnoreCase(environment);
    }

    public boolean isTest() {
        return "TEST".equalsIgnoreCase(environment);
    }
}
