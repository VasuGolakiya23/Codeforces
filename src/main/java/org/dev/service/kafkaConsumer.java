package org.dev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.dev.Entity.UserInfo;
import org.dev.Repository.CodeforcesRepository;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class kafkaConsumer {
    @Inject
    CodeforcesRepository codeforcesRepository;

    @Incoming("UserInfoIn")
    @Transactional
    public CompletionStage<Void> consume(IncomingKafkaRecord<String, String> record) {
        try {
            String key = record.getKey();
            String value = record.getPayload();
            ObjectMapper mapper = new ObjectMapper();
            UserInfo deserialisedMessage = mapper.readValue(value, UserInfo.class);
            String handleName=deserialisedMessage.getHandle();
            codeforcesRepository.addUserInfo(deserialisedMessage);
            return record.ack().thenRun(() -> {
                System.out.println("Message acknowledged: " + handleName);
            });
        } catch (Exception e) {
            System.out.println("Error in consuming message: " + e.getMessage());
            return record.nack(e);
        }
    }
}