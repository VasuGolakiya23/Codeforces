package org.dev.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.dev.Entity.BlogEntry;
import org.dev.Entity.UserInfo;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class kafkaProducer {
    @Channel("CFUserInfo")
    Emitter<String> userInfoEmitter;

    public void sendUserInfo(UserInfo message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String serialisedMessage = objectMapper.writeValueAsString(message);
            CompletionStage<Void> ackStage = userInfoEmitter.send(serialisedMessage);
            ackStage.whenComplete((ack, err) -> {
                if (err != null) {
                    System.out.println("Error in sending UserInfo: " + err.getMessage());
                } else {
                    System.out.println("UserInfo sent to kafka successfully " + message);
                }
            });
        } catch (Exception e) {
            System.out.println("Error in producing UserInfo: " + e.getMessage());
        }
    }

    @Channel("CFBlogEntry")
    Emitter<String> blogEntryEmitter;

    public void sendBlogEntry(BlogEntry message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String serialisedMessage = objectMapper.writeValueAsString(message);
            CompletionStage<Void> ackStage = blogEntryEmitter.send(serialisedMessage);
            ackStage.whenComplete((ack, err) -> {
                if (err != null) {
                    System.out.println("Error in sending BlogEntry: " + err.getMessage());
                } else {
                    System.out.println("BlogEntry sent to kafka successfully " + message);
                }
            });
        } catch (Exception e) {
            System.out.println("Error in producing BlogEntry: " + e.getMessage());
        }
    }
}