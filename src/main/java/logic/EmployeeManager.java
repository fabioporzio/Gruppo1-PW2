package logic;

import jakarta.enterprise.context.ApplicationScoped;
import model.Employee;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EmployeeManager {
    private final String filePath = "C:\\Progetti Java\\Gruppo1-PW2\\data\\employees.csv";

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
                String department = record.get("department");
                String email = record.get("email");
                String password = record.get("password");

                Employee employee = new Employee(id, name, surname, dateOfBirth, department, email, password);
                employees.add(employee);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return employees;

    }

    public void saveEmployee(Employee employee) {
        try (Writer writer = new FileWriter(filePath, true);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.EXCEL.withHeader("id", "name", "surname", "date_of_birth", "department", "email", "password")))
        {
            csvPrinter.printRecord(
                    employee.getId(),
                    employee.getName(),
                    employee.getSurname(),
                    employee.getDateOfBirth(),
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

    public Employee getEmployeeById(String id) {
        List<Employee> employees = getEmployeesFromFile();

        for (Employee employee : employees) {
            if (employee.getId().equals(id)) {
                return employee;
            }
        }

        return null;
    }

    public Employee getEmployeeByCredentials(String email, String password) {
        List<Employee> employees = getEmployeesFromFile();

        for (Employee employee : employees) {
            if (employee.getEmail().equals(email) && employee.getPassword().equals(password)) {
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
