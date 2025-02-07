package org.dev.openSearch;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.apache.http.HttpHost;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;

@ApplicationScoped
public class openSearchClient {
    RestHighLevelClient client;

    @ConfigProperty(name="openSearch.host")
    String host;

    @ConfigProperty(name="openSearch.port")
    int port;

    public void init(@Observes StartupEvent ev){
        System.out.println("Connecting to OpenSearchClient...");
        this.client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host, port, "http")
                )
        );
        System.out.println("Connected to OpenSearchClient...");
    }

    public RestHighLevelClient getClient(){
        return client;
    }
}
