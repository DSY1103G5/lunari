package cl.duoc.lunari.api.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class LunariCartApiApplication {

	public static void main(String[] args) {
		try {
			System.out.println("Loading .env file...");
			Dotenv dotenv = Dotenv.configure()
					.directory(".")
					.ignoreIfMissing()
					.load();

			System.out.println("Found " + dotenv.entries().size() + " environment variables in .env");
			dotenv.entries().forEach(e -> {
				System.setProperty(e.getKey(), e.getValue());
				System.out.println("  - " + e.getKey() + " = " + (e.getKey().contains("PASSWORD") ? "****" : e.getValue()));
			});
		} catch (Exception e) {
			System.out.println("No .env file found, using application.properties defaults");
			System.out.println("Error: " + e.getMessage());
		}
		SpringApplication.run(LunariCartApiApplication.class, args);
	}

}
