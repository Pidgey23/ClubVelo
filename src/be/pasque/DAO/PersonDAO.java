package be.pasque.DAO;

import be.pasque.Model.Person;
import be.pasque.utils.DBConnection;
import java.sql.*;

public class PersonDAO extends DAO<Person> {

    @Override
    public boolean create(Person obj) {
        return false;
    }

    @Override
    public boolean delete(Person obj) {
        return false;
    }

    @Override
    public boolean update(Person obj) {
        return false;
    }

    @Override
    public Person find(int id) {
        return null; 
    }

    public String getRoleByEmailAndPassword(String email, String password) {
        String sql = """
            SELECT 
                tre.PersonID IS NOT NULL AS isTreasurer,
                man.PersonID IS NOT NULL AS isManager,
                mem.PersonID IS NOT NULL AS isMember
            FROM Person p
            LEFT JOIN Member mem ON p.PersonID = mem.PersonID
            LEFT JOIN Manager man ON p.PersonID = man.PersonID
            LEFT JOIN Treasurer tre ON p.PersonID = tre.PersonID
            WHERE p.Email = ? AND p.Password = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (rs.getBoolean("isTreasurer")) return "TREASURER";
                    if (rs.getBoolean("isManager"))   return "MANAGER";
                    if (rs.getBoolean("isMember"))    return "MEMBER";
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du rôle", e);
        }
        return null;
    }

    public Long getPersonIdByEmail(String email) {
        String sql = "SELECT PersonID FROM Person WHERE Email = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("PersonID");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la personne par email", e);
        }
        return null;
    }
}