package be.pasque.DAO;

import be.pasque.Model.Treasurer;
import be.pasque.utils.DBConnection;
import java.sql.*;

public class TreasurerDAO extends DAO<Treasurer> {

    @Override
    public boolean create(Treasurer obj) {
        return false;
    }

    @Override
    public boolean delete(Treasurer obj) {
        return false;
    }

    @Override
    public boolean update(Treasurer obj) {
        return false;
    }

    @Override
    public Treasurer find(int id) {
        return getTreasurerByPersonId((long) id);
    }
    
    public Treasurer getTreasurerByPersonId(Long personId) {
        String sql = "SELECT * FROM Person WHERE PersonID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Treasurer t = new Treasurer();
                    t.setId(rs.getLong("PersonID"));
                    t.setFirstName(rs.getString("FirstName"));
                    t.setLastName(rs.getString("LastName"));
                    t.setPhone(rs.getString("Phone"));
                    t.setEmail(rs.getString("Email"));
                    t.setPassword(rs.getString("Password"));
                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}