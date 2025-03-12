package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Visit {
    private String id;
    private LocalDate date;
    private LocalTime expectedStartingHour;
    private LocalTime actualStartingHour;
    private LocalTime expectedEndingHour;
    private LocalDateTime actualEndingTime;
    private String guestId;
    private String employeeId;
    private String badgeCode;

    public Visit(String id, LocalDate date, LocalTime expectedStartingHour, LocalTime actualStartingHour,
                 LocalTime expectedEndingHour, LocalDateTime actualEndingTime, String guestId, String employeeId,
                 String badgeCode) {
        this.id = id;
        this.date = date;
        this.expectedStartingHour = expectedStartingHour;
        this.actualStartingHour = actualStartingHour;
        this.expectedEndingHour = expectedEndingHour;
        this.actualEndingTime = actualEndingTime;
        this.guestId = guestId;
        this.employeeId = employeeId;
        this.badgeCode = badgeCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getExpectedStartingHour() {
        return expectedStartingHour;
    }

    public void setExpectedStartingHour(LocalTime expectedStartingHour) {
        this.expectedStartingHour = expectedStartingHour;
    }

    public LocalTime getActualStartingHour() {
        return actualStartingHour;
    }

    public void setActualStartingHour(LocalTime actualStartingHour) {
        this.actualStartingHour = actualStartingHour;
    }

    public LocalTime getExpectedEndingHour() {
        return expectedEndingHour;
    }

    public void setExpectedEndingHour(LocalTime expectedEndingHour) {
        this.expectedEndingHour = expectedEndingHour;
    }

    public LocalDateTime getActualEndingTime() {
        return actualEndingTime;
    }

    public void setActualEndingTime(LocalDateTime actualEndingTime) {
        this.actualEndingTime = actualEndingTime;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getBadgeCode() {
        return badgeCode;
    }

    public void setBadgeCode(String badgeCode) {
        this.badgeCode = badgeCode;
    }
}
