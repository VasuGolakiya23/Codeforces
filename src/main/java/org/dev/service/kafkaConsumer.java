package org.dev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.dev.Entity.BlogEntry;
import org.dev.Entity.UserInfo;
import org.dev.Repository.CodeforcesRepository;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.apache.kafka.common.TopicPartition;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class kafkaConsumer {
    @Inject
    CodeforcesRepository codeforcesRepository;

    @Inject
    RedisService redisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Incoming("UserInfoIn")
    @Transactional
    public CompletionStage<Void> consumeUserInfo(IncomingKafkaRecord<String, String> record) {
        try {
            String key = record.getKey();
            String value = record.getPayload();
            UserInfo deserialisedMessage = objectMapper.readValue(value, UserInfo.class);
            String handleName=deserialisedMessage.getHandle();
            codeforcesRepository.addUserInfo(deserialisedMessage);
            TopicPartition partition = new TopicPartition(record.getTopic(), record.getPartition());
            updateProcessedOffset(partition, record.getOffset());
            return record.ack().thenRun(() -> {
                System.out.println("UserInfo processed & acknowledged: " + handleName);
            });
        } catch (Exception e) {
            System.out.println("Error in consuming UserInfo: " + e.getMessage());
            return record.nack(e);
        }
    }

    @Incoming("BlogEntryIn")
    @Transactional
    public CompletionStage<Void> consumeBlogEntry(IncomingKafkaRecord<String, String> record) {
        try {
            String key = record.getKey();
            String value = record.getPayload();
            BlogEntry deserializedMessage = objectMapper.readValue(value, BlogEntry.class);
            String blogTitle = deserializedMessage.getTitle();
            codeforcesRepository.addBlogEntry(deserializedMessage);
            TopicPartition partition = new TopicPartition(record.getTopic(), record.getPartition());
            updateProcessedOffset(partition, record.getOffset());
            return record.ack().thenRun(() ->
                    System.out.println("BlogEntry Processed & Acknowledged: " + blogTitle)
            );
        } catch (Exception e) {
            System.out.println("Error in consuming BlogEntry: " + e.getMessage());
            return record.nack(e);
        }
    }

    public void updateProcessedOffset(TopicPartition topicPartition, long offset) {
        String topic=topicPartition.topic();
        String partition=String.valueOf(topicPartition.partition());
        Long redisOffset= 0L;
        if(redisService.checkHashKey(topic, partition)){
            redisOffset = redisService.getHashKey(topic, partition);
        }
        redisService.setHashKey(topic, partition, String.valueOf(offset));
        System.out.println("The topic is "+topic+", the partition is "+partition+", the current message offset is "+offset+ " and the redis offset was "+redisOffset);
    }
}