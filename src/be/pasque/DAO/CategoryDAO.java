package be.pasque.DAO;

import be.pasque.Model.*;
import be.pasque.utils.DBConnection;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CategoryDAO extends DAO<Category> {

    @Override
    public boolean create(Category obj) {
        return false;
    }

    @Override
    public boolean delete(Category obj) {
        return false;
    }

    @Override
    public boolean update(Category obj) {
        return false;
    }

    @Override
    public Category find(int id) {
        return null;
    }
    public List<Category> findAllWithFullDetails() {
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "SELECT CategoryID FROM Category ORDER BY CategoryID";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Category cat = new Category();
                    cat.setId(rs.getLong("CategoryID"));
                    cat.setTypeFromId();
                    loadManager(conn, cat);
                    categories.add(cat);
                }
            }

            RideDAO rideDAO = new RideDAO();

            for (Category cat : categories) {
                loadMembers(conn, cat);
                loadRidesWithInscriptions(conn, rideDAO, cat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Impossible de se connecter à la base de données.\nErreur : " + e.getMessage(),
                "Erreur critique", JOptionPane.ERROR_MESSAGE);
        }

        return categories;
    }

    private void loadManager(Connection conn, Category cat) throws SQLException {
        String sql = """
            SELECT p.PersonID, p.FirstName, p.LastName, p.Phone, p.Email, p.Password
            FROM Manager m
            JOIN Person p ON m.PersonID = p.PersonID
            WHERE m.CategoryID = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cat.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Manager mgr = new Manager();
                    mgr.setId(rs.getLong("PersonID"));
                    mgr.setFirstName(rs.getString("FirstName"));
                    mgr.setLastName(rs.getString("LastName"));
                    mgr.setPhone(rs.getString("Phone"));
                    mgr.setEmail(rs.getString("Email"));
                    mgr.setPassword(rs.getString("Password"));
                    cat.setManager(mgr);
                }
            }
        }
    }

    private void loadMembers(Connection conn, Category cat) throws SQLException {
        String sql = """
            SELECT 
                mem.MemberID,
                p.PersonID, p.FirstName, p.LastName, p.Email, p.Phone,
                mem.Balance, mem.MembershipPaid
            FROM Member_Category mc
            JOIN Member mem ON mc.MemberID = mem.MemberID
            JOIN Person p ON mem.PersonID = p.PersonID
            WHERE mc.CategoryID = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cat.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Member m = new Member();
                    m.setId(rs.getLong("MemberID"));
                    m.setFirstName(rs.getString("FirstName"));
                    m.setLastName(rs.getString("LastName"));
                    m.setEmail(rs.getString("Email"));
                    m.setPhone(rs.getString("Phone"));
                    m.setBalance(rs.getDouble("Balance"));
                    m.setMembershipPaid(rs.getBoolean("MembershipPaid"));
                    cat.addMember(m);
                }
            }
        }
    }

    private void loadRidesWithInscriptions(Connection conn, RideDAO rideDAO, Category cat) throws SQLException {
        String sql = "SELECT RideID FROM Ride WHERE CategoryID = ? ORDER BY StartDate";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cat.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long rideId = rs.getLong("RideID");
                    Ride ride = rideDAO.findByIdWithAllDetails(rideId);
                    if (ride != null) {
                        ride.setCategory(cat);
                        cat.getCalendar().addRide(ride);
                    }
                }
            }
        }
    }
}