package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class HomeResource {

    private final Template home;

    public HomeResource(Template home) {
        this.home = home;
    }
    @GET
    public TemplateInstance getHome() {
        return home.instance();
    }

}
