package repository;

import config.DatabaseConfig;
import model.Member;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemberRepository {

    public List<Member> findAll() {
        List<Member> list = new ArrayList<>();
        String query = "SELECT m.account_id, m.full_name, m.email, m.phone, m.address, m.rank, a.is_active " +
                       "FROM Members m JOIN Accounts a ON m.account_id = a.account_id";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Member(
                        rs.getString("account_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("rank"),
                        rs.getBoolean("is_active") ? "Active" : "Inactive"
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Member> searchByName(String name) {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT account_id, full_name, email, phone, address, rank FROM Members WHERE full_name LIKE ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Member(
                            rs.getString("account_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("rank"),
                            "Active"
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Member> findById(String id) {
        String sql = "SELECT * FROM Members WHERE account_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Member(
                            rs.getString("account_id"),
                            rs.getString("full_name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getString("rank")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean save(Member member) {
        String sql = "INSERT INTO Members (account_id, full_name, phone, email, address, rank) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getAccountId());
            stmt.setString(2, member.getFullName());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getEmail());
            stmt.setString(5, member.getAddress());
            stmt.setString(6, member.getRank());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Member member, String originalId) {
        String sql = "UPDATE Members SET account_id=?, full_name=?, phone=?, email=?, address=?, rank=? WHERE account_id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getAccountId());
            stmt.setString(2, member.getFullName());
            stmt.setString(3, member.getPhone());
            stmt.setString(4, member.getEmail());
            stmt.setString(5, member.getAddress());
            stmt.setString(6, member.getRank());
            stmt.setString(7, originalId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalMembersCount() {
        String query = "SELECT COUNT(*) AS total_members FROM Accounts WHERE role = 'member'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total_members");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getMembersRankDistribution() {
        Map<String, Integer> dist = new HashMap<>();
        String sql = "SELECT rank, COUNT(*) AS total FROM Members GROUP BY rank";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                dist.put(rs.getString("rank"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dist;
    }

    public int getMemberDiscountPercent(String memberId) {
        if (memberId == null || memberId.isEmpty()) return 0;
        String sql = "SELECT discount_percent FROM Rank_Policies WHERE rank = (SELECT rank FROM Members WHERE account_id = ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("discount_percent");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
