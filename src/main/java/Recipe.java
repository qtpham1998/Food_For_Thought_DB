import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Recipe {
    private int id;
    private String name;
    private String directions;
    private String image;
    private List<Ingredient> ingredientList;
    private int missing;
    
    Recipe(String id, String name, String directions, String image,
        List<Ingredient> ingredientList, int missing) {
        this.id = Integer.parseInt(id);
        this.name = name;
        this.directions = directions;
        this.image = image;
        this.ingredientList = ingredientList;
        this.missing = missing;
    }
    
    // Encode the recipe into a JSON object
    public JSONObject encodeRecipe() {
        JSONObject jsonRecipe = new JSONObject();
        jsonRecipe.put("id", id);
        jsonRecipe.put("name", name);
        jsonRecipe.put("directions", directions);
        jsonRecipe.put("image", image);
        jsonRecipe.put("ingredients", encodeIngredientsList());
        jsonRecipe.put("missing", missing);
        return jsonRecipe;
    }
    
    private JSONArray encodeIngredientsList() {
        List<JSONObject> jsonIngredients = new ArrayList<>();
        ingredientList.forEach(i -> jsonIngredients.add(i.encodeIngredient()));
        return new JSONArray(jsonIngredients);
    }
}
