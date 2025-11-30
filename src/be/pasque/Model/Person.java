package be.pasque.Model;

import be.pasque.DAO.ManagerDAO;
import be.pasque.DAO.MemberDAO;
import be.pasque.DAO.PersonDAO;
import be.pasque.DAO.TreasurerDAO;

public abstract class Person {
    private Long id;
    private String lastName;
    private String firstName;
    private String phone;
    private String email;
    private String password;

    public Person() {}

    public Person(String lastName, String firstName, String phone, String email, String password) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.phone = phone;
        this.email = email;
        this.password = password;
    }

    public static Person authenticate(String email, String password) {
        PersonDAO personDAO = new PersonDAO();
        String role = personDAO.getRoleByEmailAndPassword(email, password);
        if (role == null) {
            return null;
        }
        Long personId = personDAO.getPersonIdByEmail(email);
        if (personId == null) {
            return null;
        }
        return switch (role) {
            case "MEMBER"    -> new MemberDAO().getMemberByPersonId(personId);
            case "MANAGER"   -> new ManagerDAO().getManagerByPersonId(personId);
            case "TREASURER" -> new TreasurerDAO().getTreasurerByPersonId(personId);
            default -> null;
        };
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}