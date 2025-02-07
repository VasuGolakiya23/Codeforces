package org.dev.openSearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.dev.Entity.BlogEntry;
import org.dev.Entity.UserInfo;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class openSearchService {
    private static final Logger logger = Logger.getLogger(openSearchService.class.getName());

    @Inject
    openSearchClient openSearchClientUserInfo;

    private static final String INDEX_NAME_UserInfo = "user_info";

    public void createIndexUserInfo(UserInfo data) {
        try {
            RestHighLevelClient client = openSearchClientUserInfo.getClient();
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);
            logger.info("Creating index for: " + data.getHandle());

            // Create index for the data
            IndexRequest request = new IndexRequest(INDEX_NAME_UserInfo)
                    .id(data.getHandle())
                    .source(json, XContentType.JSON);

            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            System.out.println("UserInfo Indexed Successfully " + response.getId());
        } catch (Exception e) {
            logger.severe("Error in creating index: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<String> searchQueryUserInfo(String queryText) throws IOException {
        RestHighLevelClient client = openSearchClientUserInfo.getClient();

        //Create the search request for particular INDEX
        SearchRequest request = new SearchRequest(INDEX_NAME_UserInfo);

        //Creating source builder for creating query
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.queryStringQuery("*" + queryText + "*"));

        sourceBuilder.size(100);
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        //Extracting the result from the response
        List<String> results = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            results.add(hit.getSourceAsString()); // Returns the full document as JSON
        }
        System.out.println(results);
        return results;
    }

    @Inject
    openSearchClient openSearchClientBlogEntry;

    private static final String INDEX_NAME_BlogEntry = "blog_entry";

    public void createIndexBlogEntry(BlogEntry data) {
        try {
            RestHighLevelClient client = openSearchClientBlogEntry.getClient();
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(data);
            logger.info("Creating index for: " + data.getId());

            // Create index for the data
            IndexRequest request = new IndexRequest(INDEX_NAME_BlogEntry)
                    .id(data.getId().toString())
                    .source(json, XContentType.JSON);

            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            System.out.println("BlogEntry Indexed Successfully " + response.getId());
        } catch (Exception e) {
            logger.severe("Error in creating index: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<String> searchQueryBlogEntry(String queryText) throws IOException {
        RestHighLevelClient client = openSearchClientBlogEntry.getClient();

        //Create the search request for particular INDEX
        SearchRequest request = new SearchRequest(INDEX_NAME_BlogEntry);

        //Creating source builder for creating query
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.queryStringQuery("*" + queryText + "*"));

        sourceBuilder.size(100);
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        //Extracting the result from the response
        List<String> results = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            results.add(hit.getSourceAsString()); // Returns the full document as JSON
        }
        System.out.println(results);
        return results;
    }
}
