package com.traveler.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PayHereConfig {
    
    @Value("${payhere.merchant.id}")
    private String merchantId;
    
    @Value("${payhere.merchant.secret}")
    private String merchantSecret;
    
    @Value("${payhere.api.base-url}")
    private String baseUrl;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public String getMerchantId() { return merchantId; }
    public String getMerchantSecret() { return merchantSecret; }
    public String getBaseUrl() { return baseUrl; }
}