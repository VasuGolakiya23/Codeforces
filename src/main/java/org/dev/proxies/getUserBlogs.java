package org.dev.proxies;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dev.Entity.BlogEntryResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://codeforces.com/api")
@ApplicationScoped
public interface getUserBlogs {

    @GET
    @Path("/user.blogEntries")
    @Produces(MediaType.APPLICATION_JSON)
    BlogEntryResponse getUserBlogsAPI(
            @QueryParam("handle") String handle,
            @QueryParam("apiKey") String apiKey,
            @QueryParam("time") long time,
            @QueryParam("apiSig") String apiSig
    );
}