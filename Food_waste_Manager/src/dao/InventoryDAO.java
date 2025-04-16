package dao;

import java.sql.*;
import java.util.*;
import database.DBConnection;

public class InventoryDAO {

    public static List<String> getAllItemNames() {
        List<String> items = new ArrayList<>();
        String query = "SELECT name FROM inventory";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                items.add(rs.getString("name").trim());
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching inventory items: " + e.getMessage());
        }

        return items;
    }
}