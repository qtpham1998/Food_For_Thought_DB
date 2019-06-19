import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
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
        } else if (param != null && param.equals("liked")) {
            JSONArray likedRecipes =
                fetchLikedRecipes(new JSONArray(req.getParameter("ids")));
            writer.print(likedRecipes);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        String param = req.getParameter("req");
        if (param != null && param.equals("new")) {
            Database.insertNewRecipe(Recipe.decodeRecipe(
                new JSONObject(req.getParameter("recipe"))
            ));
        }
    }
    
    // Get matching recipes from owned ingredients, in JSON array form
    private JSONArray fetchMatchingRecipes(JSONArray ownedIngredients) {
        String ownedString = decodeOwnedIngrList(ownedIngredients);
        List<Recipe> recipes = Database.fetchRecipes(ownedString);
        List<JSONObject> jsonRecipes = new ArrayList<>();
        recipes.forEach(recipe -> jsonRecipes.add(recipe.encodeRecipe()));
        return new JSONArray(jsonRecipes);
    }
    
    private JSONArray fetchLikedRecipes(JSONArray likedList) {
        List<JSONObject> likedRecipes = new ArrayList<>();
        for (int i = 0; i < likedList.length(); i++) {
            likedRecipes.add(Database.getRecipeInformation(
                String.valueOf(likedList.getInt(i))).encodeRecipe());
        }
        return new JSONArray(likedRecipes);
    }
    
    
    // Convert JSONArray owned ingredients list to String
    private String decodeOwnedIngrList(JSONArray list) {
        StringBuilder sb = new StringBuilder();
        sb. append("\'Salt\', \'Black pepper\', \'Oil\'");
        for (int i = 0; i < list.length(); i++)
            sb.append(", \'").append(list.getString(i)).append("\'");
        return sb.toString();
    }
    
    // Gets list of all possible ingredients in JSON array form
    private JSONArray getAllIngredientsList() {
        ResultSet result = Database.getAllIngredientsList();
        List<JSONObject> ingredients = new ArrayList<>();
        try {
            while (result.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id", result.getInt("id"));
                obj.put("name", result.getString("name"));
                obj.put("category", result.getString("category"));
                obj.put("duration", result.getInt("duration"));
                ingredients.add(obj);
            }
        } catch (SQLException ignored) {}
        return new JSONArray(ingredients);
    }
}