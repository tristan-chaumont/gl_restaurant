package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@ToString
@Data
public class Dish {

    private Long dishId;

    private String dishName;

    private String category;

    private MenuTypes menuType;

    private Double price;

    private boolean dailyMenu;

    private Map<RawMaterial, Integer> rawMaterials;

    public Dish() {}

    public Dish(Long dishId, String dishName, String category, MenuTypes menuType, Double price, boolean dailyMenu) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.category = category;
        this.menuType = menuType;
        this.price = price;
        this.dailyMenu = dailyMenu;
        this.rawMaterials = new HashMap<>();
    }

    public Dish(String dishName, String category, MenuTypes menuType, Double price, boolean dailyMenu) {
        this(null, dishName, category, menuType, price, dailyMenu);
    }

    public Dish(String dishName, String category, MenuTypes menuType, Double price, boolean dailyMenu, Map<RawMaterial, Integer> rawMaterials) {
        this(null, dishName, category, menuType, price, dailyMenu);
        this.rawMaterials = rawMaterials;
    }

    public Dish(ResultSet resultSet, Map<RawMaterial, Integer> rawMaterials) throws SQLException {
        dishId = resultSet.getLong("dishId");
        dishName = resultSet.getString("dishName");
        category = resultSet.getString("category");
        menuType = MenuTypes.getMenuType(resultSet.getString("menuType"));
        price = resultSet.getDouble("price");
        dailyMenu = resultSet.getBoolean("dailyMenu");
        this.rawMaterials = rawMaterials;
    }

    public void addRawMaterial(RawMaterial rawMaterial, Integer quantity) {
        rawMaterials.put(rawMaterial, quantity);
    }

    public void clearRawMaterials() {
        rawMaterials.clear();
    }

    @Override
    public String toString() {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("-".repeat(20))
                .appendln("|" + StringUtils.center(dishName, 18) + "|")
                .appendln("-".repeat(20))
                .appendln("Catégorie : %s", category)
                .appendln("Prix : %d", price)
                .appendln("Ingrédients : ");
                rawMaterials.forEach((rm,quantity) -> stringBuilder.appendln(" - %d %s", quantity, rm.getRawMaterialName()));
        return stringBuilder.toString();
    }
}
