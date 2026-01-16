package com.traveler.notification.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Service
public class TwilioService {

//    @Value("${twilio.account.sid}")
    private String accountSid = "ACd491f7c737d419707ca232960dea9a0c";

//    @Value("${twilio.auth.token}")
    private String authToken = "6852f8a3ad56065062788d8167cf0936";

//    @Value("${twilio.whatsapp.number}")
    private String fromWhatsAppNumber = "+14155238886";

    @PostConstruct
    public void init() {
        System.out.println("Initializing Twilio with SID: " + accountSid);
        if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
            throw new IllegalStateException("Twilio credentials not configured. SID: " + accountSid + ", Token: " + (authToken != null ? "[SET]" : "[NULL]"));
        }
        Twilio.init(accountSid, authToken);
        System.out.println("Twilio initialized successfully");
    }

    public String sendWhatsAppMessage(String to, String messageBody) {
        try {
            Message message = Message.creator(
                            new PhoneNumber("whatsapp:" + to),
                            new PhoneNumber(fromWhatsAppNumber),
                            messageBody)
                    .create();
            return message.getSid();
        }catch (Exception e){
            System.err.println("Error sending WhatsApp message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
