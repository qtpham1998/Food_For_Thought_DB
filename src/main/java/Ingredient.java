import java.util.List;
import org.json.JSONObject;

public class Ingredient {
    private String name;
    private int quantity;
    private String unit;
    private boolean missing;
    
    public Ingredient(String name, String quantity, String unit) {
        this.name = name;
        this.quantity = Integer.parseInt(quantity);
        this.unit = (unit.equals("unit")) ? "" : unit;
        this.missing = false;
    }
    
    public Ingredient(String name, String quantity, String unit, boolean
        missing) {
        this(name, quantity, unit);
        this.missing = missing;
    }
    
    public JSONObject encodeIngredient() {
        JSONObject jsonIngredient = new JSONObject();
        jsonIngredient.put("name", name);
        jsonIngredient.put("quantity", quantity);
        jsonIngredient.put("unit", unit);
        jsonIngredient.put("missing", missing);
        return jsonIngredient;
    }
    
    public static boolean isIngredientMissing(String ingredient, List<String>
        ownedIngredients) {
        return ownedIngredients.stream().noneMatch(elem -> elem.equals(ingredient));
    }
}
