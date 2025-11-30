package be.pasque.DAO;

import be.pasque.Model.Manager;
import be.pasque.utils.DBConnection;
import java.sql.*;

public class ManagerDAO extends DAO<Manager> {

    @Override
    public boolean create(Manager obj) {
        return false;
    }

    @Override
    public boolean delete(Manager obj) {
        return false;
    }

    @Override
    public boolean update(Manager obj) {
        return false;
    }

    @Override
    public Manager find(int id) {
        return getManagerByPersonId((long) id);
    }
    
    public Manager getManagerByPersonId(Long personId) {
        String sql = "SELECT p.* FROM Person p JOIN Manager m ON p.PersonID = m.PersonID WHERE p.PersonID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Manager manager = new Manager();
                    manager.setId(rs.getLong("PersonID"));
                    manager.setFirstName(rs.getString("FirstName"));
                    manager.setLastName(rs.getString("LastName"));
                    manager.setPhone(rs.getString("Phone"));
                    manager.setEmail(rs.getString("Email"));
                    manager.setPassword(rs.getString("Password"));
                    return manager;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}