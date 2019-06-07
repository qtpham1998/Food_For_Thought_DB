import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Database {
    
    // Gets a list of all ingredients appearing in the database
    public static ResultSet getAllIngredientsList() {
        String query = "SELECT name, category, duration FROM ingredients;";
        return queryDatabase(query);
    }
    
    // Gets the recipes with matching ingredients to one in given list
    public static List<Recipe> fetchRecipes(String ingredientsList) {
        List<Recipe> recipes = fetchMatchingRecipes(ingredientsList);
        recipes.addAll(fetchMissingRecipes(ingredientsList));
       return recipes;
    }
    
    private static List<Recipe> fetchMatchingRecipes(String ingredientsList) {
        String list = resultColumnToString(getIngredientIds(ingredientsList));
        List<String> idsList = resultColumnToList(searchRecipesIds(list));
        List<Recipe> recipes = new ArrayList<>();
        idsList.forEach(id -> recipes.add(getRecipeInformation(id, 0)));
    
        return recipes;
    }
    
    private static List<Recipe> fetchMissingRecipes(String ingredientsList) {
        String list = resultColumnToString(getIngredientIds(ingredientsList));
        ResultSet resultSet = searchMoreRecipesIds(list);
        List<Recipe> recipes = new ArrayList<>();
    
       try {
           while (resultSet.next()) {
               recipes.add(
                   getRecipeInformation(
                       resultSet.getString("id"),
                       resultSet.getInt("missing")
                   )
               );
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
    
        return recipes;
    }
    
    // Gets the corresponding ids of the given ingredients list
    private static ResultSet getIngredientIds(String ingredientsList) {
        final String query =
            "SELECT id FROM ingredients WHERE name IN (" + ingredientsList +
                ");";
        return queryDatabase(query);
    }
    
    // Given a list of ingredients, returns the recipe IDs that use only them
    private static ResultSet searchRecipesIds(String ingredientsIdsList) {
        final String query =
            "SELECT DISTINCT d.id "
            + "FROM dishes d JOIN recipes r ON (d.id = r.dish_id) "
            + "WHERE (r.ingr_id IN (" + ingredientsIdsList + ")) "
            + "AND (d.id NOT IN "
                + "(SELECT DISTINCT d.id "
                + "FROM dishes d JOIN recipes r ON (d.id = r.dish_id)"
                + "WHERE r.ingr_id NOT IN (" + ingredientsIdsList + ")));";
        return queryDatabase(query);
    }
    
    // Given a list of ingredients, returns the recipe IDs that use them
    private static ResultSet searchMoreRecipesIds(String ingredientsIdsList) {
        final String query =
            "SELECT d.id, COUNT(ingr_id) AS missing "
                + "FROM dishes d JOIN recipes r ON (d.id = r.dish_id) "
                + "WHERE (r.ingr_id NOT IN (" + ingredientsIdsList + ")) "
                + "AND (d.id IN "
                + "(SELECT DISTINCT d.id "
                + "FROM dishes d JOIN recipes r ON (d.id = r.dish_id)"
                + "WHERE r.ingr_id IN (" + ingredientsIdsList + "))) "
                + "GROUP BY d.id "
                + "HAVING COUNT(ingr_id) < 6;";
        return queryDatabase(query);
    }
    
    
    // Given a recipe IDs, returns the recipe's information
    // Includes: recipe name, directions, link to image,
    // name of ingredients, each's quantity and unit
    private static Recipe getRecipeInformation(String recipeId,
        int missing) {
        final String query =
            "SELECT name, directions, image "
                + "FROM dishes WHERE id = " + recipeId + ";";
        ResultSet result = queryDatabase(query);
        Recipe recipeInfo = null;
        try {
            if (result.next())
            recipeInfo = new Recipe(result.getString("name"),
                result.getString("directions"),
                result.getString("image"),
                getRecipeIngredients(recipeId),
                missing);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipeInfo;
    }
    
    // Get ingredients of a recipe from its ID
    private static List<Ingredient> getRecipeIngredients(String recipeId) {
        final String query =
            "SELECT name, quantity, unit, duration "
                + "FROM recipes r JOIN ingredients i ON (r.ingr_id = i.id) "
                + "WHERE r.dish_id = " + recipeId + ";";
        ResultSet result = queryDatabase(query);
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            while (result.next()) {
                ingredients.add(new Ingredient(
                    result.getString("name"),
                    result.getString("quantity"),
                    result.getString("unit")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }
    
    // Connects to the recipe database
    private static Connection getConnection() throws URISyntaxException,
        SQLException {
        URI dbUri = new URI(System.getenv("DATABASE_URL"));

        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':'
            + dbUri.getPort() + dbUri.getPath() +
            "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

        return DriverManager.getConnection(dbUrl, username, password);
    }
    
    // Gets results from the database
    private static ResultSet queryDatabase(String query) {
        ResultSet resultSet = null;
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            resultSet = stmt.executeQuery(query);
            conn.close();
        } catch (SQLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return resultSet;
    }
    
    // Converts a column from the result set to a string of elements,
    // separated by a comma
    private static String resultColumnToString(ResultSet result) {
        StringBuilder sb = new StringBuilder();
        try {
            if (result.next()) sb.append(result.getString(1));
            while (result.next()) {
                sb.append(", " + result.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    // Converts a column from the result set to a list of element
    private static List<String> resultColumnToList(ResultSet result) {
        List<String> list = new ArrayList<>();
        try {
            if (result.next()) list.add(result.getString(1));
            while (result.next()) {
                list.add(result.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
