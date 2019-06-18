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
        String query = "SELECT id, name, category, duration FROM ingredients;";
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
        idsList.forEach(id -> recipes.add(getRecipeInformation(id)));
    
        return recipes;
    }
    
    private static List<Recipe> fetchMissingRecipes(String ingredientsList) {
        String list = resultColumnToString(getIngredientIds(ingredientsList));
        ResultSet resultSet = searchMoreRecipesIds(list);
        List<Recipe> recipes = new ArrayList<>();
        List<String> ownedIngredients =
            Arrays.asList(ingredientsList.replaceAll("\'", "").split(", "));

       try {
           while (resultSet.next()) {
               recipes.add(
                   getRecipeInformation(
                       resultSet.getString("id"),
                       resultSet.getInt("missing"),
                       ownedIngredients
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
    public static Recipe getRecipeInformation(String recipeId) {
        final String query =
            "SELECT id, name, directions, image "
                + "FROM dishes WHERE id = " + recipeId + ";";
        ResultSet result = queryDatabase(query);
        Recipe recipeInfo = null;
        try {
            if (result.next())
            recipeInfo = new Recipe(
                result.getInt("id"),
                result.getString("name"),
                result.getString("directions"),
                result.getString("image"),
                getRecipeIngredients(recipeId),
                0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recipeInfo;
    }
    
    // Same as above, but with missing ingredients information
    private static Recipe getRecipeInformation(String recipeId,
        int missing, List<String> ownedIngredients) {
        final String query =
            "SELECT id, name, directions, image "
                + "FROM dishes WHERE id = " + recipeId + ";";
        ResultSet result = queryDatabase(query);
        Recipe recipeInfo = null;
        try {
            if (result.next())
                recipeInfo = new Recipe(
                    result.getInt("id"),
                    result.getString("name"),
                    result.getString("directions"),
                    result.getString("image"),
                    getRecipeIngredients(recipeId, ownedIngredients),
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
                    result.getInt("quantity"),
                    result.getString("unit")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }
    
    // Same as above, but with missing ingredients information
    private static List<Ingredient> getRecipeIngredients(String recipeId,
        List<String> ownedIngredients) {
        final String query =
            "SELECT name, quantity, unit, duration "
                + "FROM recipes r JOIN ingredients i ON (r.ingr_id = i.id) "
                + "WHERE r.dish_id = " + recipeId + ";";
        ResultSet result = queryDatabase(query);
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            while (result.next()) {
                String name = result.getString("name");
                ingredients.add(new Ingredient(
                    name,
                    result.getInt("quantity"),
                    result.getString("unit"),
                    Ingredient.isIngredientMissing(name, ownedIngredients)
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
    
    // Inserts a user submitted recipe into the database
    public static void insertNewRecipe(Recipe recipe) {
        int id = getNewRecipeId();
        String image = (recipe.getImage().isEmpty())
            ? "http://i.insing.com.sg/cms/0f/d7/fa/7c/138122/MELT-Buffetspread.jpg"
            : recipe.getImage();
        final String insert = "INSERT INTO dishes VALUES "
            + "(" + id + ", \'" + recipe.getName() + "\', \'" + recipe
            .getDirections() + "\', \'" + image + "\');";
        queryDatabase(insert);
        insertIngredientsUsed(id, recipe.getIngredientList());
    }
    
    private static void insertIngredientsUsed(int recipeId, List<Ingredient>
        ingredientList) {
        final StringBuilder insert =
            new StringBuilder("INSERT INTO recipes VALUES ");
        
        ingredientList.forEach((ingredient ->
            insert.append("(")
                .append(recipeId)
                .append(", ")
                .append(ingredient.getId())
                .append(", ")
                .append(ingredient.getQuantity())
                .append(", \'")
                .append((ingredient.getUnit().equals("")) ? "unit" : ingredient.getUnit())
                .append("\'),")
        ));
        
        insert.replace(insert.length() - 1, insert.length(), ";");
        queryDatabase(insert.toString());
    }
    
    private static int getNewRecipeId() {
        final String query =
            "SELECT COUNT(*) AS idNum FROM dishes;";
        ResultSet result = queryDatabase(query);
        try {
            return result.getInt("idNum") + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Converts a column from the result set to a string of elements,
    // separated by a comma
    private static String resultColumnToString(ResultSet result) {
        StringBuilder sb = new StringBuilder();
        try {
            if (result.next()) sb.append(result.getString(1));
            while (result.next()) {
                sb.append(", ").append(result.getString(1));
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
