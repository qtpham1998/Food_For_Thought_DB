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
        String query = "SELECT name, duration FROM ingredients;";
        return queryDatabase(query);
    }
    
    // Gets the recipes with matching ingredients to one in given list
    public static List<Recipe> fetchRecipes(String ingredientsList) {
        String list = resultColumnToString(getIngredientIds(ingredientsList));
        String recipesIds = resultColumnToString(searchRecipesIds(list));
        List<String> idsList = Arrays.asList(recipesIds.split(", "));
        List<Recipe> recipes = new ArrayList<>();
        idsList.forEach(id -> recipes.add(getRecipeInformation(id)));
        return recipes;
    }
    
    // Gets the corresponding ids of the given ingredients list
    private static ResultSet getIngredientIds(String ingredientsList) {
        final String query =
            "SELECT id FROM ingredients WHERE name IN (" + ingredientsList +
                ");";
        return queryDatabase(query);
    }
    
    // Given a list of ingredients, returns the recipe IDs that use them
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
    
    // Given a recipe IDs, returns the recipe's information
    // Includes: recipe name, directions, link to image,
    // name of ingredients, each's quantity and unit
    private static Recipe getRecipeInformation(String recipeId) {
        final String query =
            "SELECT name, directions, image "
                + "FROM dishes WHERE id = " + recipeId + ";";
        ResultSet result = queryDatabase(query);
        Recipe recipeInfo = null;
        try {
            result.next();
            recipeInfo = new Recipe(result.getString("name"),
                result.getString("directions"),
                result.getString("image"),
                getRecipeIngredients(recipeId));
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
            stmt.close();
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
            result.next();
            sb.append(result.getString(1));
            while (result.next()) {
                sb.append(", " + result.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
