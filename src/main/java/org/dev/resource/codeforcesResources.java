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
import org.dev.service.kafkaProducer;
import org.dev.openSearch.openSearchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Path("/")
public class codeforcesResources {
    @ConfigProperty(name="codeforces.apiKey")
    String apiKey;

    @Inject
    apiSigGenerator apiSigGenerator;

    @Inject
    kafkaProducer kafkaProducer;

    @Inject
    CodeforcesRepository codeforcesRepository;

    @Inject
    @RestClient
    getUserInfo getUserInfoProxy;

    @GET
    @Path("user-info/{handles}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response fetchUserInfo(@PathParam("handles") String handles) {
        long time = System.currentTimeMillis() / 1000;
        Map<String, String> params = new HashMap<>();
        params.put("handles", handles);
        String apiSig = apiSigGenerator.createApiSig("user.info", params);
        System.out.println("checking from fetchUserInfo function");

        try {
            UserInfoResponse userInfoResp = getUserInfoProxy.getUserInfoAPI(handles, apiKey, time, apiSig);
            System.out.println(userInfoResp);
            userInfoResp.getResult().forEach(user -> {
                if(codeforcesRepository.userInfoExists(user.getHandle())) {
                    System.out.println("User already exists in the database: " + user.getHandle());
                }
                else {
                    System.out.println(user.getHandle());
                    System.out.println("Sending UserInfo to Kafka");
                    kafkaProducer.sendUserInfo(user);
                }
            });
            return Response.ok(userInfoResp).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user information: " + e.getMessage());
        }
    }

    @Inject
    @RestClient
    getUserBlogs getUserBlogsProxy;

    @GET
    @Path("user-blogs/{handle}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response fetchUserBlogs(@PathParam("handle") String handle) {
        long time = System.currentTimeMillis() / 1000;
        Map<String, String> params = new HashMap<>();
        params.put("handle", handle);
        String apiSig = apiSigGenerator.createApiSig("user.blogEntries", params);
        System.out.println("checking from fetchUserBlog function");

        try {
            BlogEntryResponse blogEntryResp = getUserBlogsProxy.getUserBlogsAPI(handle, apiKey, time, apiSig);
            System.out.println(blogEntryResp);
            blogEntryResp.getResult().forEach(blog -> {
                if(codeforcesRepository.blogEntriesExists(blog.getAuthorHandle())) {
                    System.out.println("Blogs of this user already exists in the database: " + blog.getAuthorHandle());
                }
                else {
                    System.out.println(blog.getAuthorHandle());
                    System.out.println("Sending BlogEntry to kafka");
                    kafkaProducer.sendBlogEntry(blog);
                }
            });
            return Response.ok(blogEntryResp).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user blogs: " + e.getMessage());
        }
    }

    @Inject
    ObjectMapper objectmapper;

    @Inject
    openSearchService serviceClientUserInfo;

    @GET
    @Path("/searchOnUserInfo/{query}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchOnUserInfo(@PathParam("query") String query){
        List<UserInfo> finalData = new ArrayList<>();
        try {
            List<String> openSearchResult = serviceClientUserInfo.searchQueryUserInfo(query);
            openSearchResult.forEach(child->{
                try {
                    UserInfo data = objectmapper.readValue(child, UserInfo.class);
                    finalData.add(data);
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to search user info data", e);
                }
            });
            return Response.ok(finalData).build();
        } catch (Exception e) {
            return Response.ok("Error in searching the user info query: " + e.getMessage()).build();
        }
    }

    @Inject
    openSearchService serviceClientBlogEntry;

    @GET
    @Path("/searchOnBlogEntry/{query}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchOnBlogEntry(@PathParam("query") String query){
        List<BlogEntry> finalData = new ArrayList<>();
        try {
            List<String> openSearchResult = serviceClientBlogEntry.searchQueryBlogEntry(query);
            openSearchResult.forEach(child->{
                try {
                    BlogEntry data = objectmapper.readValue(child, BlogEntry.class);
                    finalData.add(data);
                }
                catch (Exception e) {
                    throw new RuntimeException("Failed to search blog entry data", e);
                }
            });
            return Response.ok(finalData).build();
        } catch (Exception e) {
            return Response.ok("Error in searching the blog entry query: " + e.getMessage()).build();
        }
    }
}