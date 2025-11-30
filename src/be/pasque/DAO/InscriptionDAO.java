package be.pasque.DAO;

import be.pasque.Model.Inscription;
import be.pasque.Model.Member;
import be.pasque.Model.Ride;
import be.pasque.Model.Vehicle;
import be.pasque.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscriptionDAO extends DAO<Inscription> {

    @Override
    public boolean create(Inscription obj) {
        return false;
    }

    @Override
    public boolean delete(Inscription obj) {
        return false;
    }

    @Override
    public boolean update(Inscription obj) {
        return false;
    }

    @Override
    public Inscription find(int id) {
        return null;
    }

    public List<Inscription> findByMember(Member member) {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = """
            SELECT InscriptionID, RideID, MemberID, IsDriver, IsPassenger, 
                   NeedsBikeTransport, VehicleID
            FROM Inscription
            WHERE MemberID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, member.getId());
            
            try (ResultSet rs = ps.executeQuery()) {
                RideDAO rideDAO = new RideDAO();
                VehicleDAO vehicleDAO = new VehicleDAO();
                
                while (rs.next()) {
                    Inscription insc = new Inscription();
                    insc.setId(rs.getLong("InscriptionID"));
                    insc.setMember(member);
                    

                    Long rideId = rs.getLong("RideID");
                    Ride ride = rideDAO.findByIdWithAllDetails(rideId);
                    insc.setRide(ride);
                    

                    insc.setDriver(rs.getBoolean("IsDriver"));
                    insc.setPassenger(rs.getBoolean("IsPassenger"));
                    insc.setNeedsBikeTransport(rs.getBoolean("NeedsBikeTransport"));
                    

                    Long vehicleId = rs.getLong("VehicleID");
                    if (!rs.wasNull() && vehicleId != null) {
                        Vehicle vehicle = vehicleDAO.findById(vehicleId);
                        insc.setVehicleUsed(vehicle);
                    }
                    
                    inscriptions.add(insc);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des inscriptions du membre", e);
        }
        return inscriptions;
    }


    public List<Inscription> findByRide(Ride ride) {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = """
            SELECT i.InscriptionID, i.RideID, i.MemberID, i.IsDriver, i.IsPassenger, 
                   i.NeedsBikeTransport, i.VehicleID,
                   m.MemberID, p.FirstName, p.LastName, p.Email, p.Phone, p.Password,
                   m.Balance, m.MembershipPaid
            FROM Inscription i
            JOIN Member m ON i.MemberID = m.MemberID
            JOIN Person p ON m.PersonID = p.PersonID
            WHERE i.RideID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, ride.getId());
            
            try (ResultSet rs = ps.executeQuery()) {
                VehicleDAO vehicleDAO = new VehicleDAO();
                
                while (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getLong("MemberID"));
                    member.setFirstName(rs.getString("FirstName"));
                    member.setLastName(rs.getString("LastName"));
                    member.setEmail(rs.getString("Email"));
                    member.setPhone(rs.getString("Phone"));
                    member.setPassword(rs.getString("Password"));
                    member.setBalance(rs.getDouble("Balance"));
                    member.setMembershipPaid(rs.getBoolean("MembershipPaid"));

                    Inscription insc = new Inscription();
                    insc.setId(rs.getLong("InscriptionID"));
                    insc.setMember(member);
                    insc.setRide(ride);
                    
                    insc.setDriver(rs.getBoolean("IsDriver"));
                    insc.setPassenger(rs.getBoolean("IsPassenger"));
                    insc.setNeedsBikeTransport(rs.getBoolean("NeedsBikeTransport"));
                    
                    Long vehicleId = rs.getLong("VehicleID");
                    if (!rs.wasNull() && vehicleId != null) {
                        Vehicle vehicle = vehicleDAO.findById(vehicleId);
                        insc.setVehicleUsed(vehicle);
                    }
                    
                    inscriptions.add(insc);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des inscriptions de la sortie", e);
        }
        return inscriptions;
    }


    public Inscription findById(Long inscriptionId) {
        String sql = """
            SELECT i.InscriptionID, i.RideID, i.MemberID, i.IsDriver, i.IsPassenger, 
                   i.NeedsBikeTransport, i.VehicleID
            FROM Inscription i
            WHERE i.InscriptionID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, inscriptionId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MemberDAO memberDAO = new MemberDAO();
                    RideDAO rideDAO = new RideDAO();
                    VehicleDAO vehicleDAO = new VehicleDAO();
                    
                    Inscription insc = new Inscription();
                    insc.setId(rs.getLong("InscriptionID"));
                    
                    Long memberId = rs.getLong("MemberID");
                    Member member = memberDAO.findAllMembers().stream()
                        .filter(m -> m.getId().equals(memberId))
                        .findFirst()
                        .orElse(null);
                    insc.setMember(member);

                    Long rideId = rs.getLong("RideID");
                    Ride ride = rideDAO.findByIdWithAllDetails(rideId);
                    insc.setRide(ride);

                    insc.setDriver(rs.getBoolean("IsDriver"));
                    insc.setPassenger(rs.getBoolean("IsPassenger"));
                    insc.setNeedsBikeTransport(rs.getBoolean("NeedsBikeTransport"));

                    Long vehicleId = rs.getLong("VehicleID");
                    if (!rs.wasNull() && vehicleId != null) {
                        Vehicle vehicle = vehicleDAO.findById(vehicleId);
                        insc.setVehicleUsed(vehicle);
                    }
                    
                    return insc;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'inscription", e);
        }
        return null;
    }

    public void save(Inscription inscription) throws SQLException {
        if (inscription.getId() == null) {
            insert(inscription);
        } else {
            update(inscription);
        }
    }

    private Long insert(Inscription inscription) throws SQLException {
        String sql = """
            INSERT INTO Inscription 
            (RideID, MemberID, IsDriver, IsPassenger, NeedsBikeTransport, VehicleID) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, inscription.getRide().getId());
            ps.setLong(2, inscription.getMember().getId());
            ps.setBoolean(3, inscription.isDriver());
            ps.setBoolean(4, inscription.isPassenger());
            ps.setBoolean(5, inscription.isNeedsBikeTransport());
            
            if (inscription.getVehicleUsed() != null) {
                ps.setLong(6, inscription.getVehicleUsed().getId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    inscription.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Échec de l'insertion de l'inscription, aucun ID généré");
    }
    
    private void updateInscription(Inscription inscription) throws SQLException {
        String sql = """
            UPDATE Inscription 
            SET RideID = ?, MemberID = ?, IsDriver = ?, IsPassenger = ?, 
                NeedsBikeTransport = ?, VehicleID = ?
            WHERE InscriptionID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, inscription.getRide().getId());
            ps.setLong(2, inscription.getMember().getId());
            ps.setBoolean(3, inscription.isDriver());
            ps.setBoolean(4, inscription.isPassenger());

            ps.setBoolean(5, inscription.isNeedsBikeTransport());
            
            if (inscription.getVehicleUsed() != null) {
                ps.setLong(6, inscription.getVehicleUsed().getId());
            } else {
                ps.setNull(6, Types.BIGINT);
            }
            
            ps.setLong(7, inscription.getId());
            
            ps.executeUpdate();
        }
    }


    public void delete(Long inscriptionId) throws SQLException {
        String sql = "DELETE FROM Inscription WHERE InscriptionID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, inscriptionId);
            ps.executeUpdate();
        }
    }


    public List<Long> findDistinctDriverMemberIds() throws SQLException {
        List<Long> driverIds = new ArrayList<>();
        String sql = """
            SELECT DISTINCT MemberID
            FROM Inscription
            WHERE IsDriver = TRUE
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                driverIds.add(rs.getLong("MemberID"));
            }
        }
        return driverIds;
    }


    public int countByRide(Long rideId) {
        String sql = "SELECT COUNT(*) as total FROM Inscription WHERE RideID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, rideId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du comptage des inscriptions", e);
        }
        return 0;
    }


    public List<Inscription> findAll() {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = """
            SELECT InscriptionID, RideID, MemberID, IsDriver, IsPassenger, 
                   NeedsBikeTransport, VehicleID
            FROM Inscription
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            MemberDAO memberDAO = new MemberDAO();
            RideDAO rideDAO = new RideDAO();
            VehicleDAO vehicleDAO = new VehicleDAO();
            
            while (rs.next()) {
                Inscription insc = new Inscription();
                insc.setId(rs.getLong("InscriptionID"));
                

                Long memberId = rs.getLong("MemberID");
                Member member = memberDAO.findAllMembers().stream()
                    .filter(m -> m.getId().equals(memberId))
                    .findFirst()
                    .orElse(null);
                insc.setMember(member);
                

                Long rideId = rs.getLong("RideID");
                Ride ride = rideDAO.findByIdWithAllDetails(rideId);
                insc.setRide(ride);

                insc.setDriver(rs.getBoolean("IsDriver"));
                insc.setPassenger(rs.getBoolean("IsPassenger"));
                insc.setNeedsBikeTransport(rs.getBoolean("NeedsBikeTransport"));

                Long vehicleId = rs.getLong("VehicleID");
                if (!rs.wasNull() && vehicleId != null) {
                    Vehicle vehicle = vehicleDAO.findById(vehicleId);
                    insc.setVehicleUsed(vehicle);
                }
                
                inscriptions.add(insc);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de toutes les inscriptions", e);
        }
        return inscriptions;
    }
}