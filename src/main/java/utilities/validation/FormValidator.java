package utilities.validation;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalTime;

@ApplicationScoped
public class FormValidator {

    /***
     * Validates the provided string.
     *
     * This method checks if the provided string is not null and not empty.
     * It ensures that the string has a valid non-empty value.
     *
     * @param string The string to be validated.
     * @return `true` if the string is not null and not empty, `false` otherwise.
     */
    public boolean checkStringForm(String string) {
        boolean valid;

        valid = string != null && !string.isEmpty();

        return valid;
    }

    public boolean checkDateNotNull (LocalDate date) {
        boolean valid;

        valid = date != null;

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
        boolean valid;
        LocalDate today = LocalDate.now();

        valid = date.isAfter(today);

        return valid;
    }

    public boolean checkTimeNotNull (LocalTime time) {
        boolean valid;

        valid = time != null;

        return valid;
    }

    public boolean checkTimeIsValid(LocalTime startingTime, LocalTime endingTime) {
        boolean valid;

        valid = startingTime.isAfter(endingTime) || startingTime.equals(endingTime);

        return valid;
    }
}
