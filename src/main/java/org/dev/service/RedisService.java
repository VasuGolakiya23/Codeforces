package org.dev.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

@Singleton
public class RedisService {
    private static final Logger LOG = Logger.getLogger(RedisService.class);

    @Inject
    RedisDataSource redisDataSource;

    private HashCommands<String, String, String> hashCommands;

    @PostConstruct
    void setup() {
        this.hashCommands = redisDataSource.hash(String.class, String.class, String.class);
    }

    public void init(@Observes StartupEvent ev) {
        LOG.info("Checking connection to Redis...");
        LOG.infof("Redis ping response: %s", redisDataSource.execute("PING").toString());
    }

    public void setHashKey(String key, String field, String value) {
        hashCommands.hset(key, field, value);
    }

    public boolean checkHashKey(String key, String field) {
        return hashCommands.hexists(key, field);
    }

    public Long getHashKey(String key, String field) {
        String value = hashCommands.hget(key, field);
        return value == null ? null : Long.valueOf(value);
    }
}
