package io.github.paladijn.d2rcharviewer.resource;

import io.github.paladijn.d2rcharviewer.model.diablorun.SyncRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("sync")
@RegisterRestClient(configKey = "diablo-run")
public interface DiabloRunRestClient {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    String sync(SyncRequest syncRequest);
}
