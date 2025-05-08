package rizzerve.menuservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Value("${spring.datasource.url}")
    private String url;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Bean
    public DataSource dataSource() {
        // Get environment variables directly
        String pgHost = System.getenv("PGHOST");
        String pgDatabase = System.getenv("PGDATABASE");
        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");
        
        // Use environment variables if available, otherwise fall back to properties
        String jdbcUrl = (pgHost != null && pgDatabase != null) 
            ? String.format("jdbc:postgresql://%s/%s", pgHost, pgDatabase)
            : url;
        String dbUsername = pgUser != null ? pgUser : username;
        String dbPassword = pgPassword != null ? pgPassword : password;
        
        // Log connection details for debugging (but mask password)
        logger.info("Database connection configured with:");
        logger.info("URL: {}", jdbcUrl);
        logger.info("Username: {}", dbUsername);
        logger.info("Password length: {}", dbPassword != null ? dbPassword.length() : 0);
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(dbUsername.trim()); // Trim to remove any whitespace
        dataSource.setPassword(dbPassword.trim()); // Trim to remove any whitespace
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }
}