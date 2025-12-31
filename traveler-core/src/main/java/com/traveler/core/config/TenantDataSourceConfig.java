package com.traveler.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class TenantDataSourceConfig {
    
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource(datasourceUrl, username, password, driverClassName);
        
        createDefaultDatabase();
        
        DataSource defaultDataSource = createOptimizedDataSource(datasourceUrl);
        
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("default", defaultDataSource);
        
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.afterPropertiesSet();
        
        return routingDataSource;
    }
    
    private DataSource createOptimizedDataSource(String url) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Minimal pool settings per tenant
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(600000);
        config.setLeakDetectionThreshold(30000);
        
        return new HikariDataSource(config);
    }
    
    private void createDefaultDatabase() {
        try {
            String dbName = extractDatabaseName(datasourceUrl);
            String rootUrl = datasourceUrl.substring(0, datasourceUrl.lastIndexOf("/"));
            
            try (Connection conn = java.sql.DriverManager.getConnection(rootUrl, username, password);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                System.out.println("[DEFAULT-DB] Database created/verified: " + dbName);
            }
        } catch (Exception e) {
            System.err.println("[DEFAULT-DB] Failed to create database: " + e.getMessage());
        }
    }
    
    private String extractDatabaseName(String url) {
        return url.substring(url.lastIndexOf("/") + 1).split("\\?")[0];
    }

    public static class TenantRoutingDataSource extends AbstractRoutingDataSource {
        private final ConcurrentHashMap<String, Boolean> createdTenants = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
        private final String baseUrl;
        private final String username;
        private final String password;
        private final String driverClassName;
        
        public TenantRoutingDataSource(String baseUrl, String username, String password, String driverClassName) {
            this.baseUrl = baseUrl;
            this.username = username;
            this.password = password;
            this.driverClassName = driverClassName;
        }
        
        @Override
        protected Object determineCurrentLookupKey() {
            String tenantId = TenantContext.getCurrentTenant();
            return tenantId != null ? tenantId : "default";
        }
        
        @Override
        protected DataSource determineTargetDataSource() {
            String tenantId = TenantContext.getCurrentTenant();
            System.out.println("[DATASOURCE] Determining datasource for tenant: " + tenantId);
            System.out.println("[DATASOURCE] Available datasources: " + getResolvedDataSources().keySet());
            
            if (tenantId != null && !getResolvedDataSources().containsKey(tenantId)) {
                System.out.println("[DATASOURCE] Tenant " + tenantId + " not found, creating...");
                synchronized (this) {
                    if (!getResolvedDataSources().containsKey(tenantId)) {
                        createTenantDatabaseAndDataSource(tenantId);
                    }
                }
            } else if (tenantId != null) {
                System.out.println("[DATASOURCE] Using existing datasource for tenant: " + tenantId);
            }
            
            DataSource ds = super.determineTargetDataSource();
            System.out.println("[DATASOURCE] Selected datasource: " + ds.getClass().getSimpleName());
            return ds;
        }
        
        private void createTenantDatabaseAndDataSource(String tenantId) {
            if (createdTenants.containsKey(tenantId)) {
                System.out.println("[DB_CREATE] Tenant " + tenantId + " already created, skipping");
                return;
            }
            
            try {
                String dbName = tenantId.toLowerCase().replace("-", "_");
                System.out.println("[DB_CREATE] Creating tenant database: " + dbName + " for tenant: " + tenantId);
                
                String rootUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                try (Connection conn = java.sql.DriverManager.getConnection(rootUrl, username, password);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                    System.out.println("Database created successfully: " + dbName);
                }
                
                String dbUrl = rootUrl + "/" + dbName;
                createTablesForTenant(dbUrl);
                
                // Reuse or create minimal datasource
                DataSource tenantDataSource = tenantDataSources.computeIfAbsent(tenantId, k -> createOptimizedDataSource(dbUrl));
                
                Map<Object, Object> dataSources = new HashMap<>(getResolvedDataSources());
                dataSources.put(tenantId, tenantDataSource);
                setTargetDataSources(dataSources);
                afterPropertiesSet();
                
                createdTenants.put(tenantId, true);
                System.out.println("Successfully created tenant database and datasource: " + tenantId);
                
            } catch (Exception e) {
                System.err.println("Failed to create tenant database: " + tenantId + ", Error: " + e.getMessage());
            }
        }
        
        private DataSource createOptimizedDataSource(String url) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName(driverClassName);
            
            // Minimal pool per tenant
            config.setMaximumPoolSize(2);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10000);
            config.setIdleTimeout(300000);
            config.setMaxLifetime(600000);
            
            return new HikariDataSource(config);
        }
        
        private void createTablesForTenant(String dbUrl) {
            try {
                DataSource tempDataSource = createOptimizedDataSource(dbUrl);
                
                LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
                emf.setDataSource(tempDataSource);
                emf.setPackagesToScan("com.traveler.common.entity");
                emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

                Map<String, Object> props = new HashMap<>();
                props.put("hibernate.hbm2ddl.auto", "update");
                props.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                props.put("hibernate.temp.use_jdbc_metadata_defaults", false);

                emf.setJpaPropertyMap(props);
                emf.afterPropertiesSet();

                EntityManagerFactory factory = emf.getObject();
                if (factory != null) {
                    factory.close();
                }
                
                // Close temp datasource
                if (tempDataSource instanceof HikariDataSource) {
                    ((HikariDataSource) tempDataSource).close();
                }
                
            } catch (Exception e) {
                System.err.println("[SCHEMA_UPDATE] Failed to update schema, Error: " + e.getMessage());
            }
        }
    }
}
