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
    
    public Recipe(String name, String directions, String image,
        List<Ingredient> ingredientList) {
        this.name = name;
        this.directions = directions;
        this.image = image;
        this.ingredientList = ingredientList;
    }
    
    Recipe(int id, String name, String directions, String image,
        List<Ingredient> ingredientList, int missing) {
        this(name, directions, image, ingredientList);
        this.id = id;
        this.missing = missing;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDirections() {
        return directions;
    }
    
    public String getImage() {
        return image;
    }
    
    public List<Ingredient> getIngredientList() {
        return ingredientList;
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
    
    public static Recipe decodeRecipe(JSONObject jsonObject) {
        return new Recipe(
            jsonObject.getString("name"),
            jsonObject.getString("directions"),
            jsonObject.getString("image"),
            Ingredient.decodeUsedIngredientsList(
                new JSONArray(jsonObject.getString("ingredients")))
        );
    }
    
    private JSONArray encodeIngredientsList() {
        List<JSONObject> jsonIngredients = new ArrayList<>();
        ingredientList.forEach(i -> jsonIngredients.add(i.encodeIngredient()));
        return new JSONArray(jsonIngredients);
    }
}
