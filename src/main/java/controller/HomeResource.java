package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class HomeResource {

    private final Template index;

    public HomeResource(Template index) {
        this.index = index;
    }
    @GET
    public TemplateInstance getIndex() {
        return index.instance();
    }

}
