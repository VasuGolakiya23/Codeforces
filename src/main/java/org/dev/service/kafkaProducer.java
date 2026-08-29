package org.dev.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.dev.Entity.BlogEntry;
import org.dev.Entity.UserInfo;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class kafkaProducer {
    private static final Logger LOG = Logger.getLogger(kafkaProducer.class);

    @Inject
    ObjectMapper objectMapper;

    @Channel("CFUserInfo")
    Emitter<String> userInfoEmitter;

    public void sendUserInfo(UserInfo message) {
        try {
            String serialisedMessage = objectMapper.writeValueAsString(message);
            CompletionStage<Void> ackStage = userInfoEmitter.send(serialisedMessage);
            ackStage.whenComplete((ack, err) -> {
                if (err != null) {
                    LOG.errorf(err, "Failed to send UserInfo: %s", message.getHandle());
                } else {
                    LOG.debugf("UserInfo sent to Kafka successfully: %s", message.getHandle());
                }
            });
        } catch (Exception e) {
            LOG.errorf(e, "Failed to serialise UserInfo: %s", message.getHandle());
        }
    }

    @Channel("CFBlogEntry")
    Emitter<String> blogEntryEmitter;

    public void sendBlogEntry(BlogEntry message) {
        try {
            String serialisedMessage = objectMapper.writeValueAsString(message);
            CompletionStage<Void> ackStage = blogEntryEmitter.send(serialisedMessage);
            ackStage.whenComplete((ack, err) -> {
                if (err != null) {
                    LOG.errorf(err, "Failed to send BlogEntry: %s", message.getId());
                } else {
                    LOG.debugf("BlogEntry sent to Kafka successfully: %s", message.getId());
                }
            });
        } catch (Exception e) {
            LOG.errorf(e, "Failed to serialise BlogEntry: %s", message.getId());
        }
    }
}
