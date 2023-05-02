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

    public List<String> searchRecipesByCategory(String category) {
        List<String> recipes = new ArrayList<>();
        String query = category.equals("All") ? "SELECT name FROM recipe" : "SELECT name FROM recipe WHERE category = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            if (!category.equals("All")) {
                stmt.setString(1, category);
            }
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
    
    public List<String> searchRecipesByIngredients(List<String> ingredients) {
        List<String> recipes = new ArrayList<>();
        String query = "SELECT DISTINCT r.name FROM recipe r " +
                "JOIN recipe_ingredients ri ON r.recipe_ID = ri.recipe_ID " +
                "JOIN food f ON f.food_ID = ri.food_ID WHERE f.name IN (";

        for (int i = 0; i < ingredients.size(); i++) {
            query += "?";
            if (i != ingredients.size() - 1) {
                query += ",";
            }
        }

        query += ")";

        System.out.println("Executing query: " + query);

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            for (int i = 0; i < ingredients.size(); i++) {
                String ingredient = ingredients.get(i).trim();
                stmt.setString(i + 1, ingredient);
                System.out.println("Setting parameter " + (i + 1) + " to: " + ingredient);
            }

            // Debug: print the prepared statement
            System.out.println("Prepared statement: " + stmt.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnsNumber = rsmd.getColumnCount();

                // Debug: print the metadata
                System.out.println("Number of columns: " + columnsNumber);
                for (int i = 1; i <= columnsNumber; i++) {
                    System.out.println("Column " + i + ": " + rsmd.getColumnName(i));
                }

                // Debug: print each row of results
                if (!rs.next()) {
                    System.out.println("No recipes found with the given ingredients.");
                } else {
                    do {
                        for (int i = 1; i <= columnsNumber; i++) {
                            System.out.print(rs.getString(i) + " ");
                        }
                        System.out.println();

                        recipes.add(rs.getString("name"));
                    } while (rs.next());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String finalQuery = query;
        for (int i = 0; i < ingredients.size(); i++) {
            String ingredient = ingredients.get(i).trim();
            finalQuery = finalQuery.replaceFirst("\\?", "'" + ingredient + "'");
        }
        System.out.println("Final query: " + finalQuery);
        System.out.println(recipes);
        return recipes;
    }


}
