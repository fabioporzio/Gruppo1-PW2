package controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import logic.EmployeeManager;
import logic.SessionManager;
import model.Employee;
import utility.validation.CredentialsValidator;

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

    /**
     * Displays the login page with an optional error message.
     *
     * @return a TemplateInstance containing the login page template.
     */
    @GET
    public TemplateInstance drawLogin() {
        return login.data("message", null);
    }

    /**
     * Processes the login request, validates credentials, and redirects the user to the appropriate page.
     *
     * @param email    the email entered by the user.
     * @param password the password entered by the user.
     * @return a Response redirecting the user to the appropriate home page or returning an error message.
     */
    @POST
    public Response processLogin(
            @FormParam("email") String email,
            @FormParam("password") String password
    ) {
        String errorMessage = null;

        if (!credentialsValidator.checkPassword(password)) {
            errorMessage = "Username o password non valide";
        }

        if (!credentialsValidator.checkEmail(email)) {
            errorMessage = "Username o password non valide";
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
            errorMessage = "Username o password non valide";
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(login.data("message", errorMessage))
                    .build();
        }
    }
}
