package cl.duoc.lunari.api.inventory.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class DotEnvConfig {
    
    @PostConstruct
    public void init() {
        try {
            Dotenv dotenv = Dotenv.configure().load();
            dotenv.entries().forEach(e -> 
                System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception e) {
            System.out.println("No .env file found or error loading environment variables");
        }
    }
}
