package com.traveler.payment.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PayHerePaymentRequest {
    private String orderId;
    private String amount;
    private String currency;
    private String items;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;
    private String hash;
    
    public static PayHerePaymentRequest fromMap(Map<String, String> data) {
        PayHerePaymentRequest request = new PayHerePaymentRequest();
        request.setOrderId(data.get("order_id"));
        request.setAmount(data.get("amount"));
        request.setCurrency(data.get("currency"));
        request.setItems(data.get("items"));
        request.setFirstName(data.get("first_name"));
        request.setLastName(data.get("last_name"));
        request.setEmail(data.get("email"));
        request.setPhone(data.get("phone"));
        request.setAddress(data.get("address"));
        request.setCity(data.get("city"));
        request.setCountry(data.get("country"));
        request.setReturnUrl(data.get("return_url"));
        request.setCancelUrl(data.get("cancel_url"));
        request.setNotifyUrl(data.get("notify_url"));
        request.setHash(data.get("hash"));
        return request;
    }
}