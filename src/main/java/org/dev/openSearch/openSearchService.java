package org.dev.openSearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.dev.Entity.BlogEntry;
import org.dev.Entity.UserInfo;
import org.jboss.logging.Logger;
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

@ApplicationScoped
public class openSearchService {
    private static final Logger LOG = Logger.getLogger(openSearchService.class);

    private static final String INDEX_NAME_UserInfo = "user_info";
    private static final String INDEX_NAME_BlogEntry = "blog_entry";
    private static final int MAX_SEARCH_RESULTS = 100;

    private static final String LUCENE_RESERVED = "+-&|!(){}[]^\"~*?:\\/";

    @Inject
    openSearchClient openSearchClient;

    @Inject
    ObjectMapper objectMapper;

    public void createIndexUserInfo(UserInfo data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            LOG.debugf("Indexing UserInfo: %s", data.getHandle());

            IndexRequest request = new IndexRequest(INDEX_NAME_UserInfo)
                    .id(data.getHandle())
                    .source(json, XContentType.JSON);

            IndexResponse response = openSearchClient.getClient().index(request, RequestOptions.DEFAULT);
            LOG.debugf("UserInfo indexed successfully: %s", response.getId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to index UserInfo: %s", data.getHandle());
            throw new RuntimeException("Failed to index UserInfo", e);
        }
    }

    public void createIndexBlogEntry(BlogEntry data) {
        try {
            if (data.getId() == null) {
                throw new IllegalArgumentException("BlogEntry has no id, refusing to index it");
            }
            String json = objectMapper.writeValueAsString(data);
            LOG.debugf("Indexing BlogEntry: %s", data.getId());

            IndexRequest request = new IndexRequest(INDEX_NAME_BlogEntry)
                    .id(data.getId().toString())
                    .source(json, XContentType.JSON);

            IndexResponse response = openSearchClient.getClient().index(request, RequestOptions.DEFAULT);
            LOG.debugf("BlogEntry indexed successfully: %s", response.getId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to index BlogEntry: %s", data.getId());
            throw new RuntimeException("Failed to index BlogEntry", e);
        }
    }

    public List<String> searchQueryUserInfo(String queryText) throws IOException {
        return search(INDEX_NAME_UserInfo, queryText);
    }

    public List<String> searchQueryBlogEntry(String queryText) throws IOException {
        return search(INDEX_NAME_BlogEntry, queryText);
    }

    private List<String> search(String index, String queryText) throws IOException {
        SearchRequest request = new SearchRequest(index);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.queryStringQuery("*" + escapeLucene(queryText) + "*"));
        sourceBuilder.size(MAX_SEARCH_RESULTS);
        request.source(sourceBuilder);

        SearchResponse response = openSearchClient.getClient().search(request, RequestOptions.DEFAULT);

        List<String> results = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            results.add(hit.getSourceAsString());
        }
        LOG.debugf("Search on %s for '%s' returned %d hit(s)", index, queryText, results.size());
        return results;
    }

    private static String escapeLucene(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            if (LUCENE_RESERVED.indexOf(c) >= 0) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
