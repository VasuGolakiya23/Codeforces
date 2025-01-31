package org.dev.Repository;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.dev.Entity.UserInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.search.Document;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@ApplicationScoped
public class CodeforcesRepository {
    private MongoCollection<UserInfo> codeforcesCollection;
    private MongoClient mongoClient;

    @ConfigProperty(name = "quarkus.mongodb.connection-string")
    String uri;

    public void init(@Observes StartupEvent ev) {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        this.mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("codeforces").withCodecRegistry(pojoCodecRegistry);
        this.codeforcesCollection=database.getCollection("codeforces", UserInfo.class);
        if(!isConnected(database)){
            System.out.println("Failed to connect to database");
        }
        else {
            System.out.println("Connected to database");
        }
    }

    private boolean isConnected(MongoDatabase database) {
        Bson command = new BsonDocument("ping", new BsonInt64(1));
        try {
            database.runCommand(command);
        } catch (MongoTimeoutException e) {
            return false;
        }
        return true;
    }

    public void close(@Observes ShutdownEvent ev){
        System.out.println("Disconnecting from MongoClient");
        mongoClient.close();
    }

    public void addUserInfo(UserInfo userInfo) {
        codeforcesCollection.insertOne(userInfo);
    }

    public boolean userExists(String handle) {
        return codeforcesCollection.find(Filters.eq("handle", handle)).first() != null;
    }
}