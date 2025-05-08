package rizzerve.menuservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) throws Exception {
        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String maskedPassword = environment.getProperty("spring.datasource.password") != null ? "********" : "null";
        
        logger.info("Database connection configured with:");
        logger.info("URL: {}", url);
        logger.info("Username: {}", username);
        logger.info("Password: {}", maskedPassword);

        try {
            String dbName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
            String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
            logger.info("Successfully connected to database: {}", dbName);
            logger.info("Database version: {}", dbVersion);
            
            // Verify the menu_item table existence
            jdbcTemplate.execute("SELECT COUNT(*) FROM menu_item");
            logger.info("Table 'menu_item' exists and is accessible");
        } catch (Exception e) {
            logger.error("Error connecting to database or accessing tables: {}", e.getMessage());
            logger.error("Make sure your Neon PostgreSQL database is configured correctly and accessible");
            logger.error("Stack trace:", e);
        }
    }
}