package cl.duoc.lunari.api.cart.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DotEnvConfig {

    @PostConstruct
    public void init() {
        // Load .env file if present (local development)
        // Ignore if missing (EC2 deployment with environment variables)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Set environment variables as system properties
        dotenv.entries().forEach(e ->
            System.setProperty(e.getKey(), e.getValue())
        );

        System.out.println("Environment configuration loaded successfully");
    }
}
