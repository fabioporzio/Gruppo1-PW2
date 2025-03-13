package logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;
import model.Employee;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessionManager {
    private final EmployeeManager employeeManager;

    public SessionManager(EmployeeManager employeeManager) {
        this.employeeManager = employeeManager;
    }

    public static final String NAME_COOKIE_SESSION = "session";

    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public NewCookie createSession(String email) {
        String idSession = UUID.randomUUID().toString();
        sessions.put(idSession, email);
        return new NewCookie.Builder(NAME_COOKIE_SESSION).value(idSession).build();
    }

    public NewCookie getSession(String idSession) {
        return new NewCookie.Builder(NAME_COOKIE_SESSION).value(idSession).build();
    }

    public Employee getEmployeeFromSession(String sessionId) {
        List<Employee> employees = employeeManager.getEmployeesFromFile();

        if(sessions.containsKey(sessionId)) {
            String userEmail = sessions.get(sessionId);
            for(Employee employee : employees) {
                if(employee.getEmail().equals(userEmail)) {
                    return employee;
                }
            }
        }
        return null;
    }

    public void removeSession (String sessionId) {
        sessions.remove(sessionId);
    }
}
