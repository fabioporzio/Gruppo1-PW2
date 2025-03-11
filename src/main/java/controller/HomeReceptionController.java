package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/home-reception")
public class HomeReceptionController {

    private final Template homeReception;

    public HomeReceptionController(Template homeReception) {
        this.homeReception = homeReception;
    }

    @GET
    public TemplateInstance getHomeReception() {
        return homeReception.instance();
    }

}
