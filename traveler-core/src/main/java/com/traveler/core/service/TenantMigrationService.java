package com.traveler.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantMigrationService {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    public void updateAllTenantDatabases() {
        try {
            List<String> tenantDatabases = getAllTenantDatabases();
            
            for (String dbName : tenantDatabases) {
                updateTenantDatabase(dbName);
            }
            
            System.out.println("Updated " + tenantDatabases.size() + " tenant databases");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update tenant databases", e);
        }
    }

    private List<String> getAllTenantDatabases() throws Exception {
        List<String> databases = new ArrayList<>();
        String rootUrl = datasourceUrl.substring(0, datasourceUrl.lastIndexOf("/"));
        
        try (Connection conn = java.sql.DriverManager.getConnection(rootUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
            
            while (rs.next()) {
                String dbName = rs.getString(1);
                if (dbName.startsWith("traveler_") && !dbName.equals("traveler_default")) {
                    databases.add(dbName);
                }
            }
        }
        
        return databases;
    }

    private void updateTenantDatabase(String dbName) throws Exception {
        String dbUrl = datasourceUrl.substring(0, datasourceUrl.lastIndexOf("/")) + "/" + dbName;
        
        DataSource dataSource = DataSourceBuilder.create()
                .url(dbUrl)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
        
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.traveler.common.entity");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        emf.setJpaPropertyMap(props);
        
        emf.afterPropertiesSet();
        emf.getObject().close();
        
        System.out.println("Updated database: " + dbName);
    }
}