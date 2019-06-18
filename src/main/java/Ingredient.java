import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Ingredient {
    private int id = 0;
    private String name = "";
    private int quantity;
    private String unit;
    private boolean missing = false;
    
    private Ingredient(int id, int quantity, String unit) {
        this.id = id;
        this.quantity = quantity;
        this.unit = (unit.equals("unit")) ? "" : unit;
    }
    
    Ingredient(String name, int quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = (unit.equals("unit")) ? "" : unit;
    }
    
    Ingredient(String name, int quantity, String unit, boolean
        missing) {
        this(name, quantity, unit);
        this.missing = missing;
    }
    
    public int getId() {
        return id;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public JSONObject encodeIngredient() {
        JSONObject jsonIngredient = new JSONObject();
        jsonIngredient.put("name", name);
        jsonIngredient.put("quantity", quantity);
        jsonIngredient.put("unit", unit);
        jsonIngredient.put("missing", missing);
        return jsonIngredient;
    }
    
    public static List<Ingredient> decodeUsedIngredientsList(JSONArray jsonArray) {
        List<Ingredient> ingredientsUsed = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject ingredient = new JSONObject(jsonArray.get(i));
            ingredientsUsed.add(decodeUsedIngredient(ingredient));
        }
        return ingredientsUsed;
    }
    
    private static Ingredient decodeUsedIngredient(JSONObject object) {
        return new Ingredient(
            object.getInt("id"),
            object.getInt("quantity"),
            object.getString("unit")
        );
    }
    
    public static boolean isIngredientMissing(String ingredient, List<String>
        ownedIngredients) {
        return ownedIngredients.stream().noneMatch(elem -> elem.equals(ingredient));
    }
}
