package utility.validation;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CredentialsValidator {

    /**
     * *
     * Validates the provided password.
     *
     * This method checks if the provided password is not null and not empty. It
     * ensures that the password has a valid non-empty value.
     *
     * @param password The password to be validated.
     * @return `true` if the password is not null and not empty, `false`
     * otherwise.
     */
    public boolean checkPassword(String password) {
        boolean valid;

        valid = password != null && !password.isEmpty();

        return valid;
    }

    /**
     * *
     * Validates the provided email.
     *
     * This method checks if the provided email is not null and not empty. It
     * ensures that the email has a valid non-empty value.
     *
     * @param email The email to be validated.
     * @return `true` if the email is not null and not empty, `false` otherwise.
     */
    public boolean checkEmail(String email) {
        boolean valid;

        valid = email != null && !email.isEmpty();

        return valid;
    }
}
