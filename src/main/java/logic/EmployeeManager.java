package logic;

import jakarta.enterprise.context.ApplicationScoped;
import model.Employee;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EmployeeManager {
    private final String filePath = "data/employees.csv";


    /***
     * Retrieves a list of employees from the CSV file.
     *
     * This method reads the employee data from the file located at `data/employees.csv`,
     * parses the CSV content, and converts it into a list of Employee objects.
     * Each employee's data is mapped from the CSV columns to the corresponding fields of
     * the Employee class.
     *
     * @return A list of Employee objects representing the employees from the file.
     */
    public List<Employee> getEmployeesFromFile() {
        List<Employee> employees = new ArrayList<Employee>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());)
        {
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return employees;

    }
    /***
     * Saves an employee's data to the CSV file.
     *
     * This method appends a new record with the provided employee's information to
     * the CSV file located at `data/employees.csv`. The file is opened in append mode,
     * and the new record is added at the end.
     *
     * @param employee The Employee object containing the information to be saved.
     */
    public void saveEmployee(Employee employee) {
        try (Writer writer = new FileWriter(filePath, true);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader("id", "name", "surname", "date_of_birth", "department", "email", "password")))
        {
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /***
     * Retrieves an employee by their unique ID.
     *
     * This method searches the list of employees retrieved from the file and returns
     * the Employee object whose ID matches the given ID. If no employee with the given
     * ID is found, the method returns null.
     *
     * @param id The ID of the employee to be retrieved.
     * @return The Employee object corresponding to the given ID, or null if not found.
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

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /***
     * Retrieves an employee by their email and password credentials.
     *
     * This method searches the list of employees retrieved from the file and returns
     * the Employee object whose email and password match the provided credentials. If no
     * matching employee is found, the method returns null.
     *
     * @param email The email of the employee to be retrieved.
     * @param password The password of the employee to be retrieved.
     * @return The Employee object corresponding to the given email and password, or null if not found.
     */
    public Employee getEmployeeByCredentials(String email, String password) {
        List<Employee> employees = getEmployeesFromFile();



        for (Employee employee : employees) {
            System.out.println("Plain Password: " + password);
            System.out.println("Hashed Password from CSV: " + employee.getPassword());
            if (employee.getEmail().equals(email) && checkPassword(password ,employee.getPassword() )) {
                return employee;
            }
        }



        return null;
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
