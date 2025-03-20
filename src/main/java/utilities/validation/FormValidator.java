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
     * When called in an if construct the function should be preceded by "not" symbol (!).
     *
     * @param string The string to be validated.
     * @return `true` if the string is not null and not empty, `false` otherwise.
     */
    public boolean checkStringNotNullOrEmpty(String string) {
        boolean valid;

        valid = string != null && !string.isEmpty();

        return valid;
    }

    /***
     * Validates if the provided date.
     *
     * This method checks if the provided date is not null.
     * It ensures that the date has a valid value.
     * When called in an if construct the function should be preceded by "not" symbol (!).
     *
     * @param date The date to be validated.
     * @return `true` if the date is not null, `false` otherwise.
     */
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
     * When called in an if construct the function should be preceded by "not" symbol (!).
     *
     * @param date The date to be validated.
     * @return `true` if the date is in the future, `false` otherwise.
     */
    public boolean checkDateIsAfterToday (LocalDate date) {
        boolean valid;
        LocalDate today = LocalDate.now();

        valid = date.isAfter(today);

        return valid;
    }

    /***
     * Validates the provided LocalTime.
     *
     * This method checks if the provided time is not null.
     * It ensures that the time has a valid value.
     * When called in an if construct the function should be preceded by "not" symbol (!).
     *
     * @param time The date to be validated.
     * @return `true` if the time is not null, `false` otherwise.
     */
    public boolean checkTimeNotNull (LocalTime time) {
        boolean valid;

        valid = time != null;

        return valid;
    }

    /***
     * Validates if the endingTime is after the startingTime.
     *
     * This method checks if the startingTime is after the endingTime.
     * It ensures that the startingTime is in the past compared to the endingTime.
     *
     * @param startingTime The time that must be before the endingTime.
     * @param endingTime The time that must be after the StartingTime.
     * @return `true` if the endingTime is after the startingTime, `false` otherwise.
     */
    public boolean checkStartingTimeIsAfterEndingTime(LocalTime startingTime, LocalTime endingTime) {
        boolean valid;

        valid = startingTime.isAfter(endingTime) || startingTime.equals(endingTime);

        return valid;
    }

    /**
     * Validates if the phone number contains a `+` at the start and then only numbers.
     *
     * @param phoneNumber The phone number that you want to check.
     * @return phoneNumber if the phone number is correct, `` otherwise.
     */
    public String checkPhoneNumber(String phoneNumber){
        if(phoneNumber.isEmpty()){
            return "";
        }

        phoneNumber = phoneNumber.trim();
        if(!(phoneNumber.charAt(0) == '+')){
            phoneNumber = '+' + phoneNumber;
        }

        for(int i = 1; i < phoneNumber.length(); i++){
            if(phoneNumber.charAt(i) < 48 || phoneNumber.charAt(i) > 57){
                return "";
            }
        }

        return phoneNumber;
    }

    /***
     * Validates if the input email contains a @.
     *
     * This method checks if the email in the form contains a '@' character.
     * It ensures that the email is valid even if the user changes the input type in html files.
     * When called in an if construct the function should be preceded by "not" symbol (!).
     *
     * @param email The email used in the corresponding form's field.
     * @return `true` if the email contains @, `false` otherwise
     */
    public boolean isEmailValid(String email) {
        boolean valid;

        valid = email.contains("@");

        return valid;
    }
}
