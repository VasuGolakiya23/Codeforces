package org.dev.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.dev.Entity.UserInfoResponse;
import org.dev.Repository.CodeforcesRepository;
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
    @RestClient
    getUserInfo getUserInfoProxy;

    @Inject
    apiSigGenerator apiSigGenerator;

    @Inject
    kafkaProducer kafkaProducer;

    @Inject
    CodeforcesRepository codeforcesRepository;

    @GET
    @Path("/user-info/{handles}")
    public Response fetchUserInfo(@PathParam("handles") String handles) {
        long time = System.currentTimeMillis() / 1000;
//        System.out.println(handles);
        Map<String, String> params = new HashMap<>();
        params.put("handles", handles);

        String apiSig = apiSigGenerator.createApiSig("user.info", params);

        try {
            UserInfoResponse userInfoResp = getUserInfoProxy.getUserInformation(handles, apiKey, time, apiSig);
            System.out.println(userInfoResp);
            userInfoResp.getResult().forEach(user -> {

                if(codeforcesRepository.userExists(user.getHandle())) {

                    System.out.println("User already exists in the database: " + user.getHandle());
                }
                else {
                    System.out.println(user.getHandle());
                    System.out.println("Produced by kafka");
                    kafkaProducer.producer(user);
                }
            });
            return Response.ok(userInfoResp).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user information: " + e.getMessage());
        }
    }
}
