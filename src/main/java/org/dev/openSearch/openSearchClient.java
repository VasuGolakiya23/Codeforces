package org.dev.openSearch;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.apache.http.HttpHost;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

import java.io.IOException;

@ApplicationScoped
public class openSearchClient {
    private static final Logger LOG = Logger.getLogger(openSearchClient.class);

    RestHighLevelClient client;

    @ConfigProperty(name="openSearch.host")
    String host;

    @ConfigProperty(name="openSearch.port")
    int port;

    public void init(@Observes StartupEvent ev){
        LOG.infof("Connecting to OpenSearch at %s:%d...", host, port);
        this.client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host, port, "http")
                )
        );
        LOG.info("Connected to OpenSearch");
    }

    public void close(@Observes ShutdownEvent ev) {
        if (client == null) {
            return;
        }
        try {
            client.close();
            LOG.info("Disconnected from OpenSearch");
        } catch (IOException e) {
            LOG.warn("Failed to close the OpenSearch client cleanly", e);
        }
    }

    public RestHighLevelClient getClient(){
        return client;
    }
}
