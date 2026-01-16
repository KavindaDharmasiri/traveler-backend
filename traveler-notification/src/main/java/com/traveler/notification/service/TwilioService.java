package com.traveler.notification.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ALL RIGHT RESERVED By kavinda_d
 *
 * @AUTHOR : kavinda_d
 * @PROJECT : traveler backend
 */

@Service
public class TwilioService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number}")
    private String fromWhatsAppNumber;

    public TwilioService() {
        // Initialize Twilio with account credentials
        Twilio.init(accountSid, authToken);
    }

    public String sendWhatsAppMessage(String to, String messageBody) {
        // Send a message via Twilio's API
        Message message = Message.creator(
                        new PhoneNumber("whatsapp:" + to),   // Recipient's WhatsApp number
                        new PhoneNumber(fromWhatsAppNumber), // Twilio WhatsApp number
                        messageBody)                         // Message body
                .create();

        return message.getSid(); // Return message SID to track status
    }
}
