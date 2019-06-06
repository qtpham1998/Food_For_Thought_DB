import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Recipe {
    private String name;
    private String directions;
    private String image;
    private List<Ingredient> ingredientList;
    private int missing = 0;
    
    Recipe(String name, String directions, String image,
        List<Ingredient> ingredientList, int missing) {
        this.name = name;
        this.directions = directions;
        this.image = image;
        this.ingredientList = ingredientList;
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
    
    public String getMissing() {
        return String.valueOf(missing);
    }
    
    public JSONArray encodeIngredientsList() {
        List<JSONObject> jsonIngredients = new ArrayList<>();
        ingredientList.forEach(i -> jsonIngredients.add(i.encodeIngredient()));
        return new JSONArray(jsonIngredients);
    }
}
