package cl.duoc.lunari.api.user.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotEnvConfig {

    static {
        try {
            // Check active profile from system property or environment variable
            String activeProfile = System.getProperty("spring.profiles.active");
            if (activeProfile == null) {
                activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
            }

            // Determine which .env file to load
            String envFileName = ".env";
            if ("prod".equals(activeProfile)) {
                envFileName = ".env.prod";
            }

            // Try to load from current directory first (for JAR deployment)
            // Then fallback to src/main/resources (for local development)
            Dotenv dotenv = null;
            try {
                dotenv = Dotenv.configure()
                        .directory(".")
                        .filename(envFileName)
                        .ignoreIfMissing()
                        .load();
                System.out.println("✓ Environment variables loaded from ./" + envFileName + " (profile: " + (activeProfile != null ? activeProfile : "default") + ")");
            } catch (Exception e1) {
                // Fallback to src/main/resources for local development
                try {
                    dotenv = Dotenv.configure()
                            .directory("src/main/resources")
                            .filename(envFileName)
                            .ignoreIfMissing()
                            .load();
                    System.out.println("✓ Environment variables loaded from src/main/resources/" + envFileName + " (profile: " + (activeProfile != null ? activeProfile : "default") + ")");
                } catch (Exception e2) {
                    System.out.println("⚠ No .env file found, using system environment variables");
                }
            }

            if (dotenv != null) {
                dotenv.entries().forEach(e ->
                    System.setProperty(e.getKey(), e.getValue()));
            }
        } catch (Exception e) {
            System.out.println("Warning: Error loading environment variables: " + e.getMessage());
        }
    }
}
