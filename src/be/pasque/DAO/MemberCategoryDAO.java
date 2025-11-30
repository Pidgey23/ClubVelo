package be.pasque.DAO;

import be.pasque.utils.DBConnection;
import java.sql.*;

public class MemberCategoryDAO {

    public void insert(Long memberId, Long categoryId) throws SQLException {
        String sql = "INSERT INTO Member_Category (MemberID, CategoryID) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, categoryId);
            ps.executeUpdate();
        }
    }
}