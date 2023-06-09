package my.CSC545MainProject;

import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;

public class RecipeSearch {
    private Connection con;

    public RecipeSearch() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@csitoracle.eku.edu:1521/cscpdb", "PickettL545", "Pa$$6411");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayResults(List<String> recipes) {
        String message = recipes.isEmpty() ? "No recipes found." : String.join("\n", recipes);
        JOptionPane.showMessageDialog(null, message);
    }

    public List<String> searchRecipes(String category, List<String> ingredients) {
        List<String> recipes = new ArrayList<>();

        String baseQuery = "SELECT DISTINCT r.name FROM recipe r ";
        String whereClause = "WHERE 1=1 ";

        if (!category.equalsIgnoreCase("All")) {
            whereClause += "AND LOWER(r.category) = ? ";
        }
        if (!ingredients.isEmpty()) {
            // Only join with recipe_ingredients and food if ingredients are provided
            baseQuery += "JOIN recipe_ingredients ri ON r.recipe_ID = ri.recipe_ID " +
                         "JOIN food f ON f.food_ID = ri.food_ID ";
            whereClause += "AND LOWER(f.name) IN (";
            for (int i = 0; i < ingredients.size(); i++) {
                whereClause += "?";
                if (i != ingredients.size() - 1) {
                    whereClause += ",";
                }
            }
            whereClause += ") ";
        }

        String query = baseQuery + whereClause;

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            int paramIndex = 1;

            if (!category.equalsIgnoreCase("All")) {
                stmt.setString(paramIndex++, category.toLowerCase());
            }

            if (!ingredients.isEmpty()) {
                for (String ingredient : ingredients) {
                    stmt.setString(paramIndex++, ingredient.trim().toLowerCase());
                }
            }
            System.out.println(query);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recipes.add(rs.getString("name"));
                }
            }

        } catch (SQLException e) {
                e.printStackTrace();
            }
        return recipes;
    }

}
