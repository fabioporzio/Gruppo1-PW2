package utilities.validation;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;

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

    public boolean checkStringForm(String string){
        boolean valid;

        valid = string != null && !string.isEmpty();

        return valid;
    }

    public boolean checkDate (LocalDate date) {
        LocalDate today = LocalDate.now();

        return date.isBefore(today.minusDays(1));
    }
}
