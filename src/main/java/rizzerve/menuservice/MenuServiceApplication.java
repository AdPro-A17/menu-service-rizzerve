package rizzerve.menuservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MenuServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(MenuServiceApplication.class);

    public static void main(String[] args) {
        // Load .env file before Spring Boot starts
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception e) {
            logger.error("Error loading .env file: {}", e.getMessage());
        }
        
        SpringApplication.run(MenuServiceApplication.class, args);
    }

}
