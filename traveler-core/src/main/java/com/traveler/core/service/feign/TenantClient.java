package com.traveler.core.service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "traveler-tenant")
public interface TenantClient {
    
    @PostMapping("/tenant/validate")
    String validateTenant(@RequestHeader("X-Tenant-Id") String tenantId);
}
