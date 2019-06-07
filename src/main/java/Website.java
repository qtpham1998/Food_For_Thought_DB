import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public class Website extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        String param = req.getParameter("req");
        if (param != null && param.equals("list")) {
            writer.print(getAllIngredientsList());
        } else if (param != null && param.equals("recipe")) {
            JSONArray recipes =
                fetchMatchingRecipes(new JSONArray(req.getParameter("owned")));
            writer.print(recipes);
        } else if (param != null && param.equals("favourite")) {
            Recipe recipe = Database.getRecipeInformation(
                Database.getRecipeIdFromName(req.getParameter("name")), 0);
            writer.print(encodeRecipe(recipe));
        }
    }

    // Get matching recipes from owned ingredients, in JSON array form
    private JSONArray fetchMatchingRecipes(JSONArray ownedIngredients) {
        String ownedString = decodeOwnedIngrList(ownedIngredients);
        List<Recipe> recipes = Database.fetchRecipes(ownedString);
        List<JSONObject> jsonRecipes = new ArrayList<>();
        recipes.forEach(recipe -> jsonRecipes.add(encodeRecipe(recipe)));
        return new JSONArray(jsonRecipes);
    }
    
    // Encode one recipe into a JSON object
    private JSONObject encodeRecipe(Recipe recipe) {
        JSONObject jsonRecipe = new JSONObject();
        jsonRecipe.put("name", recipe.getName());
        jsonRecipe.put("directions", recipe.getDirections());
        jsonRecipe.put("image", recipe.getImage());
        jsonRecipe.put("ingredients", recipe.encodeIngredientsList());
        jsonRecipe.put("missing", recipe.getMissing());
        return jsonRecipe;
    }
    
    // Convert JSONArray owned ingredients list to String
    private String decodeOwnedIngrList(JSONArray list) {
        StringBuilder sb = new StringBuilder();
        sb. append("\'Salt\', \'Black pepper\', \'Oil\'");
        for (int i = 0; i < list.length(); i++)
            sb.append(", " + "\'" + list.getString(i) + "\'");
        return sb.toString();
    }
    
    // Gets list of all possible ingredients in JSON array form
    private JSONArray getAllIngredientsList() {
        ResultSet result = Database.getAllIngredientsList();
        List<JSONObject> ingredients = new ArrayList<>();
        try {
            while (result.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", result.getString("id"));
                obj.put("name", result.getString("name"));
                obj.put("category", result.getString("category"));
                obj.put("duration", result.getString("duration"));
                ingredients.add(obj);
            }
        } catch (SQLException ignored) {}
        return new JSONArray(ingredients);
    }
}