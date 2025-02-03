package org.dev.service;

import io.quarkus.redis.client.RedisClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class RedisService {
    @Inject
    RedisClient redisClient;

    public void init(@Observes StartupEvent ev){
        System.out.println("Checking connection to Redis");
        System.out.println(redisClient.ping(List.of("Pinging redis service")));
    }

    public void setHashKey(String key, String field, String value) {
        redisClient.hset(List.of(key, field, value));
    }

    public boolean checkHashKey(String key, String field) {
        return redisClient.hexists(key, field).toBoolean();
    }

    public Long getHashKey(String key, String field) {
        return redisClient.hget(key, field).toLong();
    }
}