package com.traveler.core.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import com.traveler.common.entity.Trip;
import com.traveler.common.entity.Booking;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.EnumSet;
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
        
        // Create default database if not exists
        createDefaultDatabase();
        
        DataSource defaultDataSource = DataSourceBuilder.create()
                .url(datasourceUrl)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
        
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("default", defaultDataSource);
        
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.afterPropertiesSet();
        
        return routingDataSource;
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
                // Force schema update for existing tenant databases
                updateTenantSchema(tenantId);
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
                
                // Create database
                String rootUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                try (Connection conn = java.sql.DriverManager.getConnection(rootUrl, username, password);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                    System.out.println("Database created successfully: " + dbName);
                }
                
                // Create tables using JPA
                String dbUrl = rootUrl + "/" + dbName;
                createTablesForTenant(dbUrl);
                
                // Create datasource
                DataSource tenantDataSource = DataSourceBuilder.create()
                        .url(dbUrl)
                        .username(username)
                        .password(password)
                        .driverClassName(driverClassName)
                        .build();
                
                // Register the new datasource
                Map<Object, Object> dataSources = new HashMap<>(getResolvedDataSources());
                dataSources.put(tenantId, tenantDataSource);
                setTargetDataSources(dataSources);
                afterPropertiesSet();
                
                // Verify datasource is registered
                if (!getResolvedDataSources().containsKey(tenantId)) {
                    throw new RuntimeException("Failed to register tenant datasource: " + tenantId);
                }
                
                createdTenants.put(tenantId, true);
                System.out.println("Successfully created tenant database and datasource: " + tenantId);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to create tenant database: " + tenantId, e);
            }
        }
        
        private void updateTenantSchema(String tenantId) {
            try {
                String dbName = tenantId.toLowerCase().replace("-", "_");
                String rootUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
                String dbUrl = rootUrl + "/" + dbName;
                
                createTablesForTenant(dbUrl);
                System.out.println("[SCHEMA_UPDATE] Updated schema for tenant: " + tenantId);
            } catch (Exception e) {
                System.err.println("[SCHEMA_UPDATE] Failed to update schema for tenant: " + tenantId + ", Error: " + e.getMessage());
            }
        }
        
//        private void createTablesForTenant(String dbUrl) throws Exception {
//            DataSource tempDataSource = DataSourceBuilder.create()
//                    .url(dbUrl)
//                    .username(username)
//                    .password(password)
//                    .driverClassName(driverClassName)
//                    .build();
//
//            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
//            emf.setDataSource(tempDataSource);
//            emf.setPackagesToScan("com.traveler.common.entity");
//            emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//
//            Map<String, Object> props = new HashMap<>();
//            props.put("hibernate.hbm2ddl.auto", "update");
//            props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
//            props.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
//            emf.setJpaPropertyMap(props);
//
//            emf.afterPropertiesSet();
//            EntityManagerFactory factory = emf.getObject();
//            factory.close();
//        }

        private synchronized void createTablesForTenant(String dbUrl) {
            LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
            emf.setDataSource(DataSourceBuilder.create()
                    .url(dbUrl)
                    .username(username)
                    .password(password)
                    .driverClassName(driverClassName)
                    .build());

            emf.setPackagesToScan("com.traveler.common.entity");
            emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

            Map<String, Object> props = new HashMap<>();
            props.put("hibernate.hbm2ddl.auto", "update");
            props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
            props.put("hibernate.temp.use_jdbc_metadata_defaults", false);

            emf.setJpaPropertyMap(props);
            emf.afterPropertiesSet();

            emf.getObject().close();
        }
    }
}
