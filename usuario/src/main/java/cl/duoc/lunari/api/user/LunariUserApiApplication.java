package cl.duoc.lunari.api.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class LunariUserApiApplication {

	public static void main(String[] args) {
		// Load .env file only if it exists (for local development)
		// In production, use environment variables or IAM roles instead
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
		SpringApplication.run(LunariUserApiApplication.class, args);
	}

}
