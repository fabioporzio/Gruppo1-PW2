package model;

public class Guest {
    private String id;
    private String name;
    private String surname;
    private String role;
    private String company;


    public Guest(String id, String name, String surname, String role, String company) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getRole() {
        return role;
    }

    public String getCompany() {
        return company;
    }
}
