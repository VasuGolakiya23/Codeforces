package org.dev.proxies;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.Entity.UserInfoResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://codeforces.com/api")
@ApplicationScoped
public interface getUserInfo {

    @GET
    @Path("/user.info")
    @Produces(MediaType.APPLICATION_JSON)
    UserInfoResponse getUserInformation(
            @QueryParam("handles") String handles,
            @QueryParam("apiKey") String apiKey,
            @QueryParam("time") long time,
            @QueryParam("apiSig") String apiSig
    );
}
