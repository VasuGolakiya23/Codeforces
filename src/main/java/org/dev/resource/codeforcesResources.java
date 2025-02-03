package org.dev.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
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

import java.util.HashMap;
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
}