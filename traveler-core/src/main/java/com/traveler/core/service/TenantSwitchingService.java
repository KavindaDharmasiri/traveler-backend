package com.traveler.core.service;

import com.traveler.core.config.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantSwitchingService {
    
    public <T> T executeInTenant(String tenantId, TenantOperation<T> operation) {
        String originalTenant = TenantContext.getCurrentTenant();
        System.out.println("Original tenant: " + originalTenant);
        System.out.println("Switching to tenant: " + tenantId);
        try {
            TenantContext.setCurrentTenant(tenantId);
            String currentTenant = TenantContext.getCurrentTenant();
            System.out.println("Current tenant after switch: " + currentTenant);
            T result = operation.execute();
            System.out.println("Operation completed in tenant: " + TenantContext.getCurrentTenant());
            return result;
        } finally {
            TenantContext.setCurrentTenant(originalTenant);
            System.out.println("Restored to original tenant: " + TenantContext.getCurrentTenant());
        }
    }
    
    @FunctionalInterface
    public interface TenantOperation<T> {
        T execute();
    }
}