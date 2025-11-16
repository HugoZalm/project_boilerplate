package nl.wateralmanak.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DatabaseConfig {
    
    private static HikariDataSource dataSource;
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
            Class.forName("org.postgis.DriverWrapper");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL/PostGIS driver not found", e);
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv().getOrDefault("DB_URL", 
            "jdbc:postgresql://postgres:5432/wateralmanak"));
        config.setUsername(System.getenv().getOrDefault("DB_USER", "wateralmanak_user"));
        config.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "wateralmanak_pass123"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public static DataSource getDataSource() {
        return dataSource;
    }
    
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
