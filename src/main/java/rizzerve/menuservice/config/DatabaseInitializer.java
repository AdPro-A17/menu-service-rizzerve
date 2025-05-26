package rizzerve.menuservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, Environment environment) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        String maskedPassword = environment.getProperty("spring.datasource.password") != null ? "********" : "null";
        
        logger.info("Database connection configured with:");
        logger.info("URL: {}", url);
        logger.info("Username: {}", username);
        logger.info("Password: {}", maskedPassword);
        logger.info("DDL Auto: {}", ddlAuto);

        try {
            String dbName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
            logger.info("Successfully connected to database: {}", dbName);
            
            // Check if menu_item table exists using safe query
            Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'menu_item'", 
                Integer.class
            );
            
            if (tableExists != null && tableExists > 0) {
                Integer recordCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM menu_item", Integer.class);
                logger.info("Table 'menu_item' exists with {} records - NO RESET WILL OCCUR", recordCount);
                logger.info("DDL mode is '{}' - database structure is preserved", ddlAuto);
            } else {
                logger.warn("Table 'menu_item' does not exist - initializing table structure");
                initializeMenuItemTable();
                logger.info("Table 'menu_item' has been created successfully");
            }
            
        } catch (Exception e) {
            logger.error("Error connecting to database: {}", e.getMessage());
            logger.error("Make sure your PostgreSQL database is configured correctly and accessible");
        }
    }

    private void initializeMenuItemTable() {
        try {
            // Create the menu_item table with proper structure
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS menu_item (
                    id UUID PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    available BOOLEAN NOT NULL DEFAULT true,
                    image VARCHAR(500),
                    item_type VARCHAR(31) NOT NULL,
                    is_spicy BOOLEAN,
                    is_cold BOOLEAN,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            jdbcTemplate.execute(createTableSql);
            logger.info("Table structure created successfully");
            
            // Create indexes for better performance
            String createIndexSql = """
                CREATE INDEX IF NOT EXISTS idx_menu_item_type ON menu_item(item_type);
                CREATE INDEX IF NOT EXISTS idx_menu_item_available ON menu_item(available);
                """;
            
            jdbcTemplate.execute(createIndexSql);
            logger.info("Database indexes created successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize menu_item table: {}", e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}