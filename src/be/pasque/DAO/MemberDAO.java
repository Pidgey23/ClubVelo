package be.pasque.DAO;

import be.pasque.Model.*;
import be.pasque.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO extends DAO<Member> {

    @Override
    public boolean create(Member obj) {
        return false;
    }

    @Override
    public boolean delete(Member obj) {
        return false;
    }

    @Override
    public boolean update(Member obj) {
        return false;
    }

    @Override
    public Member find(int id) {
    	return null;
    }
    public Member findByEmailAndPassword(String email, String password) {
        String sql = """
            SELECT p.*, m.Balance, m.MembershipPaid, mem.MemberID
            FROM Person p 
            JOIN Member mem ON p.PersonID = mem.PersonID
            JOIN Member m ON mem.MemberID = m.MemberID
            WHERE p.Email = ? AND p.Password = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToMember(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du membre", e);
        }
        return null;
    }

    private Member mapToMember(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getLong("MemberID"));
        m.setFirstName(rs.getString("FirstName"));
        m.setLastName(rs.getString("LastName"));
        m.setPhone(rs.getString("Phone"));
        m.setEmail(rs.getString("Email"));
        m.setPassword(rs.getString("Password"));
        m.setBalance(rs.getDouble("Balance"));
        m.setMembershipPaid(rs.getBoolean("MembershipPaid"));
        return m;
    }

    public List<Category> findCategoriesByMember(Member member) {
        List<Category> categories = new ArrayList<>();
        String sql = """
            SELECT c.CategoryID
            FROM Category c
            JOIN Member_Category mc ON c.CategoryID = mc.CategoryID
            WHERE mc.MemberID = ?
            ORDER BY c.CategoryID
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, member.getId());
            try (ResultSet rs = ps.executeQuery()) {
                CategoryDAO catDao = new CategoryDAO();
                while (rs.next()) {
                    Long catId = rs.getLong("CategoryID");
                    for (Category cat : catDao.findAllWithFullDetails()) {
                        if (cat.getId().equals(catId)) {
                            categories.add(cat);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des catégories du membre", e);
        }
        return categories;
    }

    public Member getMemberByPersonId(Long personId) {
        String sql = """
            SELECT mem.MemberID, p.*, m.Balance, m.MembershipPaid
            FROM Person p
            JOIN Member mem ON p.PersonID = mem.PersonID
            JOIN Member m ON mem.MemberID = m.MemberID
            WHERE p.PersonID = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, personId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Member member = new Member();
                    member.setId(rs.getLong("MemberID"));
                    member.setFirstName(rs.getString("FirstName"));
                    member.setLastName(rs.getString("LastName"));
                    member.setPhone(rs.getString("Phone"));
                    member.setEmail(rs.getString("Email"));
                    member.setPassword(rs.getString("Password"));
                    member.setBalance(rs.getDouble("Balance"));
                    member.setMembershipPaid(rs.getBoolean("MembershipPaid"));
                    return member;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du membre par PersonID", e);
        }
        return null;
    }

    public List<Member> findAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = """
            SELECT 
                mem.MemberID,
                p.PersonID, p.FirstName, p.LastName, p.Email, p.Phone, p.Password,
                mem.Balance, mem.MembershipPaid
            FROM Person p
            JOIN Member mem ON p.PersonID = mem.PersonID
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
                members.add(member);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de tous les membres", e);
        }
        return members;
    }
    
    public void updateBalance(Long memberId, double newBalance) throws SQLException {
        String sql = "UPDATE Member SET Balance = ? WHERE MemberID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setLong(2, memberId);
            ps.executeUpdate();
        }
    }


    public void updateMembershipStatus(Long memberId, boolean isPaid) throws SQLException {
        String sql = "UPDATE Member SET MembershipPaid = ? WHERE MemberID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, isPaid);
            ps.setLong(2, memberId);
            ps.executeUpdate();
        }
    }


    public Long insert(Member member, Long personId) throws SQLException {
        String sql = "INSERT INTO Member (PersonID, Balance, MembershipPaid) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, personId);
            ps.setDouble(2, member.getBalance());
            ps.setBoolean(3, member.isMembershipPaid());
            
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Échec de l'insertion du membre, aucun ID généré");
    }


    public void updateMember(Member member) throws SQLException {
        String sql = "UPDATE Member SET Balance = ?, MembershipPaid = ? WHERE MemberID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, member.getBalance());
            ps.setBoolean(2, member.isMembershipPaid());
            ps.setLong(3, member.getId());
            
            ps.executeUpdate();
        }
    }
}