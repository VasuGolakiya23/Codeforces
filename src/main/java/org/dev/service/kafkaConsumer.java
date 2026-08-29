package org.dev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.dev.Entity.BlogEntry;
import org.dev.Entity.UserInfo;
import org.dev.Repository.CodeforcesRepository;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.apache.kafka.common.TopicPartition;
import org.dev.openSearch.openSearchService;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class kafkaConsumer {
    private static final Logger LOG = Logger.getLogger(kafkaConsumer.class);

    @Inject
    CodeforcesRepository codeforcesRepository;

    @Inject
    RedisService redisService;

    @Inject
    openSearchService openSearchService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("UserInfoIn")
    @Blocking
    public CompletionStage<Void> consumeUserInfo(IncomingKafkaRecord<String, String> record) {
        try {
            UserInfo deserialisedMessage = objectMapper.readValue(record.getPayload(), UserInfo.class);
            String handleName = deserialisedMessage.getHandle();
            codeforcesRepository.addUserInfo(deserialisedMessage);
            openSearchService.createIndexUserInfo(deserialisedMessage);
            TopicPartition partition = new TopicPartition(record.getTopic(), record.getPartition());
            updateProcessedOffset(partition, record.getOffset());
            return record.ack().thenRun(() -> LOG.debugf("UserInfo processed & acknowledged: %s", handleName));
        } catch (Exception e) {
            LOG.errorf(e, "Error consuming UserInfo at %s-%d offset %d",
                    record.getTopic(), record.getPartition(), record.getOffset());
            return record.nack(e);
        }
    }

    @Incoming("BlogEntryIn")
    @Blocking
    public CompletionStage<Void> consumeBlogEntry(IncomingKafkaRecord<String, String> record) {
        try {
            BlogEntry deserializedMessage = objectMapper.readValue(record.getPayload(), BlogEntry.class);
            String blogTitle = deserializedMessage.getTitle();
            codeforcesRepository.addBlogEntry(deserializedMessage);
            openSearchService.createIndexBlogEntry(deserializedMessage);
            TopicPartition partition = new TopicPartition(record.getTopic(), record.getPartition());
            updateProcessedOffset(partition, record.getOffset());
            return record.ack().thenRun(() -> LOG.debugf("BlogEntry processed & acknowledged: %s", blogTitle));
        } catch (Exception e) {
            LOG.errorf(e, "Error consuming BlogEntry at %s-%d offset %d",
                    record.getTopic(), record.getPartition(), record.getOffset());
            return record.nack(e);
        }
    }

    public void updateProcessedOffset(TopicPartition topicPartition, long offset) {
        String topic = topicPartition.topic();
        String partition = String.valueOf(topicPartition.partition());
        redisService.setHashKey(topic, partition, String.valueOf(offset));
        LOG.debugf("Stored offset %d for %s-%s", offset, topic, partition);
    }
}
