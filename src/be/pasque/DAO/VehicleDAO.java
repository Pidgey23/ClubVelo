package be.pasque.DAO;

import be.pasque.Model.Member;
import be.pasque.Model.Vehicle;
import be.pasque.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO extends DAO<Vehicle> {

    @Override
    public boolean create(Vehicle obj) {
        return false;
    }

    @Override
    public boolean delete(Vehicle obj) {
        return false;
    }

    @Override
    public boolean update(Vehicle obj) {
        return false;
    }

    @Override
    public Vehicle find(int id) {
        return findById((long) id);
    }
    public Vehicle findById(Long vehicleId) {
        String sql = """
            SELECT v.VehicleID, v.SeatNumber, v.BikeSpotNumber, v.DriverID,
                   m.MemberID, p.FirstName, p.LastName, p.Email, p.Phone, p.Password,
                   m.Balance, m.MembershipPaid
            FROM Vehicle v
            JOIN Member m ON v.DriverID = m.MemberID
            JOIN Person p ON m.PersonID = p.PersonID
            WHERE v.VehicleID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
  
                    Member driver = new Member();
                    driver.setId(rs.getLong("MemberID"));
                    driver.setFirstName(rs.getString("FirstName"));
                    driver.setLastName(rs.getString("LastName"));
                    driver.setEmail(rs.getString("Email"));
                    driver.setPhone(rs.getString("Phone"));
                    driver.setPassword(rs.getString("Password"));
                    driver.setBalance(rs.getDouble("Balance"));
                    driver.setMembershipPaid(rs.getBoolean("MembershipPaid"));

                    Vehicle vehicle = new Vehicle();
                    vehicle.setId(rs.getLong("VehicleID"));
                    vehicle.setSeatNumber(rs.getInt("SeatNumber"));
                    vehicle.setBikeSpotNumber(rs.getInt("BikeSpotNumber"));
                    vehicle.setDriver(driver);
                    
                    return vehicle;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du véhicule", e);
        }
        return null;
    }

    public List<Vehicle> findByMember(Member member) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = """
            SELECT v.VehicleID, v.SeatNumber, v.BikeSpotNumber
            FROM Vehicle v
            WHERE v.DriverID = ?
            ORDER BY v.VehicleID
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, member.getId());
            try (ResultSet rs = ps.executeQuery()) {
                int position = 1;
                while (rs.next()) {
                    Vehicle v = new Vehicle();
                    v.setId(rs.getLong("VehicleID"));
                    v.setSeatNumber(rs.getInt("SeatNumber"));
                    v.setBikeSpotNumber(rs.getInt("BikeSpotNumber"));
                    v.setDriver(member);
                    v.setCustomLabel("Véhicule " + position);
                    vehicles.add(v);
                    position++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public void insert(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO Vehicle (DriverID, SeatNumber, BikeSpotNumber) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, vehicle.getDriver().getId());
            ps.setInt(2, vehicle.getSeatNumber());
            ps.setInt(3, vehicle.getBikeSpotNumber());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    vehicle.setId(keys.getLong(1));
                }
            }
        }
    }
}