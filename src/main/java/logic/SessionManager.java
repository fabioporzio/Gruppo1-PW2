package logic;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;
import model.Employee;

@ApplicationScoped
public class SessionManager {

    private final EmployeeManager employeeManager;

    public SessionManager(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    public static final String NAME_COOKIE_SESSION = "session";

    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    /**
     * *
     * Creates a new session for an employee by their email.
     *
     * This method generates a new unique session ID, associates it with the
     * given employee's email, and stores it in the session map. It then creates
     * and returns a new cookie containing the session ID.
     *
     * @param email The email of the employee for whom the session is created.
     * @return A NewCookie containing the session ID.
     */
    public NewCookie createSession(String email) {
        String idSession = UUID.randomUUID().toString();
        sessions.put(idSession, email);
        return new NewCookie.Builder(NAME_COOKIE_SESSION).value(idSession).build();
    }

    /**
     * *
     * Retrieves an employee associated with a given session ID.
     *
     * This method checks if the session ID exists in the session map. If it
     * does, it retrieves the associated email and looks up the employee in the
     * employee list. If a matching employee is found, it is returned;
     * otherwise, the method returns null.
     *
     * @param sessionId The session ID associated with the employee.
     * @return The Employee object corresponding to the session ID, or null if
     * no match is found.
     */
    public Employee getEmployeeFromSession(String sessionId) {
        List<Employee> employees = employeeManager.getEmployeesFromFile();

        if (sessions.containsKey(sessionId)) {
            String userEmail = sessions.get(sessionId);
            for (Employee employee : employees) {
                if (employee.getEmail().equals(userEmail)) {
                    return employee;
                }
            }
        }
        return null;
    }

    /**
     * *
     * Removes a session by its session ID.
     *
     * This method removes the session entry from the session map based on the
     * provided session ID, effectively invalidating the session.
     *
     * @param sessionId The session ID to be removed.
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
