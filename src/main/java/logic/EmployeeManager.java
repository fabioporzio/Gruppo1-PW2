package logic;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.enterprise.context.ApplicationScoped;
import model.Employee;

@ApplicationScoped
public class EmployeeManager {

    private final String FILE_PATH = "data/employees.csv";

    /**
     * *
     * Retrieves a list of employees from the CSV file.
     *
     * This method reads the employee data from the file located at
     * `data/employees.csv`, parses the CSV content, and converts it into a list
     * of Employee objects. Each employee's data is mapped from the CSV columns
     * to the corresponding fields of the Employee class.
     *
     * @return A list of Employee objects representing the employees from the
     * file.
     */
    public List<Employee> getEmployeesFromFile() {
        List<Employee> employees = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Reader reader = new FileReader(FILE_PATH); CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());) {
            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                String name = record.get("name");
                String surname = record.get("surname");
                LocalDate dateOfBirth = LocalDate.parse(record.get("date_of_birth"), formatter);
                String phoneNumber = record.get("phone_number");
                String department = record.get("department");
                String email = record.get("email");
                String password = record.get("password");

                Employee employee = new Employee(id, name, surname, dateOfBirth, phoneNumber, department, email, password);
                employees.add(employee);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return employees;
    }

    /**
     * *
     * Saves an employee's data to the CSV file.
     *
     * This method appends a new record with the provided employee's information
     * to the CSV file located at `data/employees.csv`. The file is opened in
     * append mode, and the new record is added at the end.
     *
     * @param employee The Employee object containing the information to be
     * saved.
     */
    public void saveEmployee(Employee employee) {
        try (Writer writer = new FileWriter(FILE_PATH, true); CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader("id", "name", "surname", "date_of_birth", "department", "email", "password"))) {
            csvPrinter.printRecord(
                    employee.getId(),
                    employee.getName(),
                    employee.getSurname(),
                    employee.getDateOfBirth(),
                    employee.getPhoneNumber(),
                    employee.getDepartment(),
                    employee.getEmail(),
                    employee.getPassword()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * *
     * Retrieves an employee by their unique ID.
     *
     * This method searches the list of employees retrieved from the file and
     * returns the Employee object whose ID matches the given ID. If no employee
     * with the given ID is found, the method returns null.
     *
     * @param id The ID of the employee to be retrieved.
     * @return The Employee object corresponding to the given ID, or null if not
     * found.
     */
    public Employee getEmployeeById(String id) {
        List<Employee> employees = getEmployeesFromFile();

        for (Employee employee : employees) {
            if (employee.getId().equals(id)) {
                return employee;
            }
        }
        return null;
    }

    /**
     * Check if the plain password and the hashed password are equal
     *
     * @param plainPassword The password written in the login form
     * @param hashedPassword The password written in the file
     * @return `true` if are equal, `false` otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * *
     * Retrieves an employee by their email and password credentials.
     *
     * This method searches the list of employees retrieved from the file and
     * returns the Employee object whose email and password match the provided
     * credentials. If no matching employee is found, the method returns null.
     *
     * @param email The email of the employee to be retrieved.
     * @param password The password of the employee to be retrieved.
     * @return The Employee object corresponding to the given email and
     * password, or null if not found.
     */
    public Employee getEmployeeByCredentials(String email, String password) {
        List<Employee> employees = getEmployeesFromFile();

        for (Employee employee : employees) {
            if (employee.getEmail().equals(email) && checkPassword(password, employee.getPassword())) {
                return employee;
            }
        }
        return null;
    }

    /**
     * Retrieves all employees excluding receptionists.
     *
     * This method searches the list of employees retrieved from the file and
     * returns the Employee object whose department doesn't match "Portineria". If no employee
     * is found, the method returns null.
     *
     * @return The Employee object corresponding to the given ID, or null if not
     * found.
     */
    public List<Employee> getEmployeesExcludingReception() {
        List<Employee> filteredEmployees = new ArrayList<>();

        List<Employee> employees = getEmployeesFromFile();

        for (Employee employee : employees) {
            if (!employee.getDepartment().equals("Reception")) {
                filteredEmployees.add(employee);
            }
        }
        return filteredEmployees;
    }

    /*
    Useful for registration employees.

    public boolean isEmailUnivocal(String email) {
        List<Employee> employees = getEmployeesFromFile();

        for (Employee employee : employees) {
            if (employee.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

     */
}
