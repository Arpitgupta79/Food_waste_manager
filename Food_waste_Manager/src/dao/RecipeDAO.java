package dao;

import database.DBConnection;
import model.Recipe;

import java.sql.*;
import java.util.*;

public class RecipeDAO {
    public static List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String query = "SELECT * FROM recipes";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                recipes.add(new Recipe(
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("ingredients")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching recipes: " + e.getMessage());
        }
        return recipes;
    }
    
}