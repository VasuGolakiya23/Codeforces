package org.dev.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.Entity.BlogEntry;
import org.dev.Entity.BlogEntryResponse;
import org.dev.Entity.UserInfo;
import org.dev.Entity.UserInfoResponse;
import org.dev.Repository.CodeforcesRepository;
import org.dev.proxies.getUserBlogs;
import org.dev.proxies.getUserInfo;
import org.dev.apiSigGenerator.apiSigGenerator;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.dev.service.kafkaProducer;
import org.dev.openSearch.openSearchService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Path("/")
public class codeforcesResources {
    private static final Logger LOG = Logger.getLogger(codeforcesResources.class);

    @ConfigProperty(name="codeforces.apiKey")
    String apiKey;

    @Inject
    apiSigGenerator apiSigGenerator;

    @Inject
    kafkaProducer kafkaProducer;

    @Inject
    CodeforcesRepository codeforcesRepository;

    @Inject
    ObjectMapper objectmapper;

    @Inject
    openSearchService openSearchService;

    @Inject
    @RestClient
    getUserInfo getUserInfoProxy;

    @GET
    @Path("user-info/{handles}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response fetchUserInfo(@PathParam("handles") String handles) {
        long time = Instant.now().getEpochSecond();
        Map<String, String> params = new HashMap<>();
        params.put("handles", handles);
        String apiSig = apiSigGenerator.createApiSig("user.info", params, time);

        try {
            UserInfoResponse userInfoResp = getUserInfoProxy.getUserInfoAPI(handles, apiKey, time, apiSig);
            userInfoResp.getResult().forEach(user -> {
                if (codeforcesRepository.userInfoExists(user.getHandle())) {
                    LOG.debugf("UserInfo already stored, skipping: %s", user.getHandle());
                } else {
                    LOG.debugf("Sending UserInfo to Kafka: %s", user.getHandle());
                    kafkaProducer.sendUserInfo(user);
                }
            });
            return Response.ok(userInfoResp).build();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to fetch user information for handles=%s", handles);
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(Map.of("error", "Failed to fetch user information"))
                    .build();
        }
    }

    @Inject
    @RestClient
    getUserBlogs getUserBlogsProxy;

    @GET
    @Path("user-blogs/{handle}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response fetchUserBlogs(@PathParam("handle") String handle) {
        long time = Instant.now().getEpochSecond();
        Map<String, String> params = new HashMap<>();
        params.put("handle", handle);
        String apiSig = apiSigGenerator.createApiSig("user.blogEntries", params, time);

        try {
            BlogEntryResponse blogEntryResp = getUserBlogsProxy.getUserBlogsAPI(handle, apiKey, time, apiSig);
            blogEntryResp.getResult().forEach(blog -> {
                if (codeforcesRepository.blogEntryExists(blog.getId())) {
                    LOG.debugf("BlogEntry already stored, skipping: %s", blog.getId());
                } else {
                    LOG.debugf("Sending BlogEntry to Kafka: %s", blog.getId());
                    kafkaProducer.sendBlogEntry(blog);
                }
            });
            return Response.ok(blogEntryResp).build();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to fetch user blogs for handle=%s", handle);
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity(Map.of("error", "Failed to fetch user blogs"))
                    .build();
        }
    }

    @GET
    @Path("/searchOnUserInfo/{query}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchOnUserInfo(@PathParam("query") String query) {
        try {
            List<UserInfo> finalData = new ArrayList<>();
            for (String hit : openSearchService.searchQueryUserInfo(query)) {
                finalData.add(objectmapper.readValue(hit, UserInfo.class));
            }
            return Response.ok(finalData).build();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to search user info for query=%s", query);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to search user info"))
                    .build();
        }
    }

    @GET
    @Path("/searchOnBlogEntry/{query}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchOnBlogEntry(@PathParam("query") String query) {
        try {
            List<BlogEntry> finalData = new ArrayList<>();
            for (String hit : openSearchService.searchQueryBlogEntry(query)) {
                finalData.add(objectmapper.readValue(hit, BlogEntry.class));
            }
            return Response.ok(finalData).build();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to search blog entries for query=%s", query);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to search blog entries"))
                    .build();
        }
    }
}
