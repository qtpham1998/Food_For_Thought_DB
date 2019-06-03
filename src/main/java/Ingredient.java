import org.json.JSONObject;

public class Ingredient {
    private String name;
    private int quantity;
    private String unit;
    
    public Ingredient(String name, String quantity, String unit) {
        this.name = name;
        this.quantity = Integer.parseInt(quantity);
        this.unit = (unit.equals("unit")) ? "" : unit;
    }
    
    public JSONObject encodeIngredient() {
        JSONObject jsonIngredient = new JSONObject();
        jsonIngredient.put("name", name);
        jsonIngredient.put("quantity", quantity);
        jsonIngredient.put("unit", unit);
        return jsonIngredient;
    }
}
