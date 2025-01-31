package org.dev.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.dev.Entity.UserInfo;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class kafkaProducer {
    @Channel("CFUserInfo")
    Emitter<String> emitter;

    public void producer(UserInfo message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String serialisedMessage = objectMapper.writeValueAsString(message);
            CompletionStage<Void> ackStage = emitter.send(serialisedMessage);
            ackStage.whenComplete((ack, err) -> {
                if (err != null) {
                    System.out.println("Error in sending message: " + err.getMessage());
                } else {
                    System.out.println("Message sent to kafka successfully " + message);
                }
            });

        } catch (Exception e) {
            System.out.println("Error in producing message: " + e.getMessage());
        }
    }
}