package be.pasque.DAO;

import be.pasque.Model.*;
import be.pasque.utils.DBConnection;

import java.sql.*;
import java.util.Date;

public class RideDAO extends DAO<Ride> {

    @Override
    public boolean create(Ride obj) {
        return false;
    }

    @Override
    public boolean delete(Ride obj) {
        return false;
    }

    @Override
    public boolean update(Ride obj) {
        return false;
    }

    @Override
    public Ride find(int id) {
        return findByIdWithAllDetails((long) id);
    }
    public Ride findByIdWithAllDetails(Long rideId) {
        Ride ride = null;
        String sql = """
            SELECT r.RideID, r.StartPlace, r.StartDate, r.Fee, r.CategoryID, c.Type
            FROM Ride r
            JOIN Category c ON r.CategoryID = c.CategoryID
            WHERE r.RideID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, rideId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ride = new Ride();
                    ride.setId(rs.getLong("RideID"));
                    ride.setStartPlace(rs.getString("StartPlace"));
                    ride.setFee(rs.getDouble("Fee"));
                    Timestamp ts = rs.getTimestamp("StartDate");
                    if (ts != null) ride.setStartDate(new Date(ts.getTime()));

                    Category cat = new Category();
                    cat.setId(rs.getLong("CategoryID"));
                    cat.setTypeFromId();
                    ride.setCategory(cat);
                }
            }

            if (ride != null) {
                loadInscriptionsWithVehicles(conn, ride);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la sortie", e);
        }
        return ride;
    }

    private void loadInscriptionsWithVehicles(Connection conn, Ride ride) throws SQLException {
        String sql = """
            SELECT 
                i.InscriptionID, i.IsDriver, i.IsPassenger, i.NeedsBikeTransport,
                mem.MemberID, p.PersonID, p.FirstName, p.LastName, p.Phone, p.Email, p.Password,
                mem.Balance, mem.MembershipPaid,
                v.VehicleID, v.SeatNumber, v.BikeSpotNumber
            FROM Inscription i
            JOIN Member mem ON i.MemberID = mem.MemberID
            JOIN Person p ON mem.PersonID = p.PersonID
            LEFT JOIN Vehicle v ON i.VehicleID = v.VehicleID
            WHERE i.RideID = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, ride.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getLong("MemberID"));
                    member.setFirstName(rs.getString("FirstName"));
                    member.setLastName(rs.getString("LastName"));
                    member.setPhone(rs.getString("Phone"));
                    member.setEmail(rs.getString("Email"));
                    member.setPassword(rs.getString("Password"));
                    member.setBalance(rs.getDouble("Balance"));
                    member.setMembershipPaid(rs.getBoolean("MembershipPaid"));

                    Inscription insc = new Inscription();
                    insc.setId(rs.getLong("InscriptionID"));
                    insc.setDriver(rs.getBoolean("IsDriver"));
                    insc.setPassenger(rs.getBoolean("IsPassenger"));
                    insc.setNeedsBikeTransport(rs.getBoolean("NeedsBikeTransport"));
                    insc.setMember(member);
                    insc.setRide(ride);

                    Long vehId = rs.getLong("VehicleID");
                    if (!rs.wasNull()) {
                        Vehicle veh = new Vehicle();
                        veh.setId(vehId);
                        veh.setSeatNumber(rs.getInt("SeatNumber"));
                        veh.setBikeSpotNumber(rs.getInt("BikeSpotNumber"));
                        veh.setDriver(member);
                        insc.setVehicleUsed(veh);
                    }

                    ride.addInscription(insc);
                }
            }
        }
    }

    public void insert(Ride ride) throws SQLException {
        String sql = "INSERT INTO Ride (StartPlace, StartDate, Fee, CategoryID) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, ride.getStartPlace());
            ps.setTimestamp(2, ride.getStartDate() != null ? new Timestamp(ride.getStartDate().getTime()) : null);
            ps.setDouble(3, ride.getFee());
            ps.setLong(4, ride.getCategory().getId());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ride.setId(keys.getLong(1));
                }
            }
        }
    }
}