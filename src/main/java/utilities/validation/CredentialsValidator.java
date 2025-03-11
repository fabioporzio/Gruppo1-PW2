package utilities.validation;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialsValidator {
    public boolean checkPassword(String password) {
        boolean valid;

        valid = password != null && !password.isEmpty();

        return valid;
    }

    public boolean checkEmail(String email) {
        boolean valid;

        valid = email != null && !email.isEmpty();

        return valid;
    }
}
