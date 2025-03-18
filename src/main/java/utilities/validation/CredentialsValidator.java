package utilities.validation;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;

@ApplicationScoped
public class CredentialsValidator {

    /***
     * Validates the provided password.
     *
     * This method checks if the provided password is not null and not empty.
     * It ensures that the password has a valid non-empty value.
     *
     * @param password The password to be validated.
     * @return `true` if the password is not null and not empty, `false` otherwise.
     */
    public boolean checkPassword(String password) {
        boolean valid;

        valid = password != null && !password.isEmpty();

        return valid;
    }

    /***
     * Validates the provided email.
     *
     * This method checks if the provided email is not null and not empty.
     * It ensures that the email has a valid non-empty value.
     *
     * @param email The email to be validated.
     * @return `true` if the email is not null and not empty, `false` otherwise.
     */
    public boolean checkEmail(String email) {
        boolean valid;

        valid = email != null && !email.isEmpty();

        return valid;
    }

    /***
     * Validates the provided string.
     *
     * This method checks if the provided string is not null and not empty.
     * It ensures that the string has a valid non-empty value.
     *
     * @param string The string to be validated.
     * @return `true` if the string is not null and not empty, `false` otherwise.
     */
    public boolean checkStringForm(String string){
        boolean valid;

        valid = string != null && !string.isEmpty();

        return valid;
    }

    /***
     * Validates if the provided date is in the future.
     *
     * This method checks if the provided date is after the current date.
     * It ensures that the date is in the future, not today or in the past.
     *
     * @param date The date to be validated.
     * @return `true` if the date is in the future, `false` otherwise.
     */
    public boolean checkDate (LocalDate date) {
        LocalDate today = LocalDate.now();

        return date.isAfter(today);
    }
}
