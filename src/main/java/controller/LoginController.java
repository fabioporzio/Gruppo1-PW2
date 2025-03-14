package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import logic.EmployeeManager;
import logic.SessionManager;
import model.Employee;
import utilities.validation.CredentialsValidator;

import java.net.URI;

@Path("/")
public class LoginController {
    private final Template login;
    private final EmployeeManager employeeManager;
    private final CredentialsValidator credentialsValidator;
    private final SessionManager sessionManager;

    public LoginController(Template login, EmployeeManager employeeManager, CredentialsValidator credentialsValidator, SessionManager sessionManager) {
        this.login = login;
        this.employeeManager = employeeManager;
        this.credentialsValidator = credentialsValidator;
        this.sessionManager = sessionManager;
    }

    @GET
    public TemplateInstance drawLogin() {
        return login.data("message", null);
    }

    @POST
    public Response processLogin(
            @FormParam("email") String email,
            @FormParam("password") String password
    ) {
        String errorMessage = null;

        if (!credentialsValidator.checkPassword(password)) {
            errorMessage = "Username or Password not valid";
        }

        if (!credentialsValidator.checkEmail(email)) {
            errorMessage = "Username or Password not valid";
        }

        if (errorMessage != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(login.data("message", errorMessage))
                    .build();
        }

        Employee employee = employeeManager.getEmployeeByCredentials(email, password);

        if (employee != null) {
            NewCookie sessionCookie = sessionManager.createSession(email);

            if(employee.getDepartment().equals("Reception")) {
                return Response
                        .seeOther(URI.create("/home-reception"))
                        .cookie(sessionCookie)
                        .build();
            }

            return Response
                    .seeOther(URI.create("/home-employee"))
                    .cookie(sessionCookie)
                    .build();
        }
        else {
            errorMessage = "Username or Password not valid";
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(login.data("message", errorMessage))
                    .build();
        }
    }
}
