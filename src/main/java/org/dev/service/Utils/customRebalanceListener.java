package org.dev.service.Utils;

import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.KafkaConsumerRebalanceListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.dev.service.RedisService;
import org.jboss.logging.Logger;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
@Identifier("custom-rebalance-listener")
public class customRebalanceListener implements KafkaConsumerRebalanceListener {
    private static final Logger LOG = Logger.getLogger(customRebalanceListener.class);

    @Inject
    RedisService redisService;

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        LOG.infof("Partitions assigned: %s", partitions);
        for (TopicPartition partition : partitions) {
            String topic = partition.topic();
            String partitionkey = String.valueOf(partition.partition());
            Long offset = redisService.checkHashKey(topic, partitionkey)
                    ? redisService.getHashKey(topic, partitionkey)
                    : null;
            if (offset != null) {
                LOG.infof("Seeking to offset %d for partition %s", offset + 1, partition);
                consumer.seek(partition, offset + 1);
            } else {
                LOG.infof("No offset found for partition %s, seeking to beginning", partition);
                consumer.seekToBeginning(List.of(partition));
            }
        }
    }

    @Override
    public void onPartitionsRevoked(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        LOG.infof("Partitions revoked: %s", partitions);
    }
}
