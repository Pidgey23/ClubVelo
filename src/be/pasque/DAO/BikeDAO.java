package be.pasque.DAO;

import be.pasque.Model.Bike;
import be.pasque.Model.Member;
import be.pasque.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BikeDAO extends DAO<Bike> {

    @Override
    public boolean create(Bike obj) {
        return false;
    }

    @Override
    public boolean delete(Bike obj) {
        return false;
    }

    @Override
    public boolean update(Bike obj) {
    	return false;
    }

    @Override
    public Bike find(int id) {
        return findById((long) id);
    }

    public List<Bike> findByMember(Member member) {
        List<Bike> bikes = new ArrayList<>();
        String sql = """
            SELECT BikeID, MemberID, Weight, Type, Length
            FROM Bike
            WHERE MemberID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, member.getId());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bike bike = new Bike();
                    bike.setId(rs.getLong("BikeID"));
                    bike.setOwner(member);
                    bike.setWeight(rs.getDouble("Weight"));
                    bike.setType(rs.getString("Type"));
                    bike.setLength(rs.getDouble("Length"));
                    bikes.add(bike);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des vélos du membre", e);
        }
        return bikes;
    }

 
    public Bike findById(Long bikeId) {
        String sql = """
            SELECT b.BikeID, b.MemberID, b.Weight, b.Type, b.Length,
                   m.MemberID, p.FirstName, p.LastName, p.Email, p.Phone, p.Password,
                   m.Balance, m.MembershipPaid
            FROM Bike b
            JOIN Member m ON b.MemberID = m.MemberID
            JOIN Member mem ON m.MemberID = mem.MemberID
            JOIN Person p ON mem.PersonID = p.PersonID
            WHERE b.BikeID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bikeId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    Member owner = new Member();
                    owner.setId(rs.getLong("MemberID"));
                    owner.setFirstName(rs.getString("FirstName"));
                    owner.setLastName(rs.getString("LastName"));
                    owner.setEmail(rs.getString("Email"));
                    owner.setPhone(rs.getString("Phone"));
                    owner.setPassword(rs.getString("Password"));
                    owner.setBalance(rs.getDouble("Balance"));
                    owner.setMembershipPaid(rs.getBoolean("MembershipPaid"));

                    Bike bike = new Bike();
                    bike.setId(rs.getLong("BikeID"));
                    bike.setOwner(owner);
                    bike.setWeight(rs.getDouble("Weight"));
                    bike.setType(rs.getString("Type"));
                    bike.setLength(rs.getDouble("Length"));
                    return bike;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du vélo", e);
        }
        return null;
    }


    public Long insert(Bike bike) throws SQLException {
        String sql = "INSERT INTO Bike (MemberID, Weight, Type, Length) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, bike.getOwner().getId());
            ps.setDouble(2, bike.getWeight());
            ps.setString(3, bike.getType());
            ps.setDouble(4, bike.getLength());
            
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    bike.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Échec de l'insertion du vélo, aucun ID généré");
    }


    public void updateBike(Bike bike) throws SQLException {
        String sql = "UPDATE Bike SET MemberID = ?, Weight = ?, Type = ?, Length = ? WHERE BikeID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bike.getOwner().getId());
            ps.setDouble(2, bike.getWeight());
            ps.setString(3, bike.getType());
            ps.setDouble(4, bike.getLength());
            ps.setLong(5, bike.getId());
            
            ps.executeUpdate();
        }
    }


    public void delete(Long bikeId) throws SQLException {
        String sql = "DELETE FROM Bike WHERE BikeID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, bikeId);
            ps.executeUpdate();
        }
    }


    public List<Bike> findAll() {
        List<Bike> bikes = new ArrayList<>();
        String sql = """
            SELECT b.BikeID, b.MemberID, b.Weight, b.Type, b.Length,
                   m.MemberID, p.FirstName, p.LastName
            FROM Bike b
            JOIN Member m ON b.MemberID = m.MemberID
            JOIN Member mem ON m.MemberID = mem.MemberID
            JOIN Person p ON mem.PersonID = p.PersonID
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Member owner = new Member();
                owner.setId(rs.getLong("MemberID"));
                owner.setFirstName(rs.getString("FirstName"));
                owner.setLastName(rs.getString("LastName"));

                Bike bike = new Bike();
                bike.setId(rs.getLong("BikeID"));
                bike.setOwner(owner);
                bike.setWeight(rs.getDouble("Weight"));
                bike.setType(rs.getString("Type"));
                bike.setLength(rs.getDouble("Length"));
                
                bikes.add(bike);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les vélos", e);
        }
        return bikes;
    }
}