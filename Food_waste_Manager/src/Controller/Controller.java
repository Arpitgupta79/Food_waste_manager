package Controller;

import java.sql.*;
import database.DBConnection;
import model.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class Controller {

 public static void addItem(String name , String category ,String quantity ,String date) {
    String query =" INSERT INTO inventory (Name, Category, Quantity, Expiration) VALUES (?,?,?,?)";
    try (Connection conn =DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)){
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setString(3, quantity);  // Format: 'HH:MM:SS'
            stmt.setString(4, date);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Event added successfully!");
            } else {
                System.out.println("⚠️ Failed to add event.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
         }

    public static List<FoodItem> getAllItem() {
        List <FoodItem> itemList =new ArrayList<>();
        String query = " Select * from inventory";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                String name = rs.getString("Name");
                String category = rs.getString("Category");
                String quantity = rs.getString("Quantity");
                Date expdate = rs.getDate("Expiration");
    
                itemList.add(new FoodItem(name,category,quantity,expdate));
            }
            System.out.println("✅ All Item loaded successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Error fetching all Item: " + e.getMessage());
        }
    
        return itemList;
    }

    
        
 }
    

