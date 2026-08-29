package org.dev.Repository;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.dev.Entity.BlogEntry;
import org.dev.Entity.UserInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@ApplicationScoped
public class CodeforcesRepository {
    private static final Logger LOG = Logger.getLogger(CodeforcesRepository.class);

    private static final String BLOG_ID_FIELD = "uniqueID";
    private static final String USER_HANDLE_FIELD = "handle";

    private MongoCollection<UserInfo> userInfoCollection;
    private MongoCollection<BlogEntry> blogEntryCollection;

    @Inject
    MongoClient mongoClient;

    @ConfigProperty(name = "quarkus.mongodb.database")
    String databaseName;

    public void init(@Observes StartupEvent ev) {
        LOG.info("Connecting to database...");
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        MongoDatabase database = mongoClient.getDatabase(databaseName).withCodecRegistry(pojoCodecRegistry);

        this.userInfoCollection = database.getCollection("user_info", UserInfo.class);
        this.blogEntryCollection = database.getCollection("blog_entries", BlogEntry.class);

        if (isConnected(database)) {
            LOG.info("Connected to database");
            userInfoCollection.createIndex(Indexes.ascending(USER_HANDLE_FIELD));
            blogEntryCollection.createIndex(Indexes.ascending(BLOG_ID_FIELD));
        } else {
            LOG.error("Failed to connect to database");
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

    public void addUserInfo(UserInfo userInfo) {
        userInfoCollection.replaceOne(
                Filters.eq(USER_HANDLE_FIELD, userInfo.getHandle()),
                userInfo,
                new ReplaceOptions().upsert(true));
        LOG.debugf("Stored UserInfo in MongoDB: %s", userInfo.getHandle());
    }

    public void addBlogEntry(BlogEntry blogEntry) {
        if (blogEntry.getId() == null) {
            throw new IllegalArgumentException("BlogEntry has no id, refusing to store it");
        }
        blogEntryCollection.replaceOne(
                Filters.eq(BLOG_ID_FIELD, blogEntry.getId()),
                blogEntry,
                new ReplaceOptions().upsert(true));
        LOG.debugf("Stored BlogEntry in MongoDB: %s", blogEntry.getId());
    }

    public boolean userInfoExists(String handle) {
        return userInfoCollection.find(Filters.eq(USER_HANDLE_FIELD, handle)).first() != null;
    }

    public boolean blogEntryExists(Long blogId) {
        if (blogId == null) {
            return false;
        }
        return blogEntryCollection.find(Filters.eq(BLOG_ID_FIELD, blogId)).first() != null;
    }
}
