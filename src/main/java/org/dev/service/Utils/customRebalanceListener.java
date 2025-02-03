package org.dev.service.Utils;

import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.KafkaConsumerRebalanceListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.dev.service.RedisService;

import java.util.Collection;
import java.util.List;

@ApplicationScoped
@Identifier("custom-rebalance-listener")
public class customRebalanceListener implements KafkaConsumerRebalanceListener {
    @Inject
    RedisService redisService;

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        System.out.println("Partitions assigned: " + partitions);
        for (TopicPartition partition : partitions) {
            String topic = partition.topic();
            String partitionkey=String.valueOf(partition.partition());
            if(redisService.checkHashKey(topic,partitionkey)){
                Long offset = redisService.getHashKey(topic, partitionkey);
                System.out.println("Partition assigned: " + partition + " Offset: " + offset);
                if(offset!=null){
                    System.out.println("Seeking to offset " + offset + " for partition " + partition);
                    consumer.seek(partition, offset+1);
                }
            } else{
                System.out.println("No offset found for partition " + partition + " Seeking to beginning");
                consumer.seekToBeginning(List.of(partition));
            }
        }
    }

    @Override
    public void onPartitionsRevoked(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        System.out.println("Partitions revoked: " + partitions);
    }
}