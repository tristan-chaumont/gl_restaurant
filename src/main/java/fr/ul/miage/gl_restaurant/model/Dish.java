package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
@Data
public class Dish {

    private static final String TABLE_NAME = "Dishes";

    private Long dishId;

    private String dishName;

    private String category;

    private MenuTypes menuType;

    private Double price;

    private HashMap<RawMaterial, Integer> rawMaterials;

    public Dish() {}

    public Dish(Long dishId, String dishName, String category, MenuTypes menuType, Double price) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.category = category;
        this.menuType = menuType;
        this.price = price;
        this.rawMaterials = new HashMap<>();
    }

    public Dish(String dishName, String category, MenuTypes menuType, Double price) {
        this(null, dishName, category, menuType, price);
    }

    public Dish(ResultSet resultSet, Map<RawMaterial, Integer> rawMaterials) throws SQLException {
        dishId = resultSet.getLong("dishId");
        dishName = resultSet.getString("dishName");
        category = resultSet.getString("category");
        menuType = MenuTypes.getMenuType(resultSet.getString("menuType"));
        price = resultSet.getDouble("price");
        this.rawMaterials = (HashMap<RawMaterial, Integer>) rawMaterials;
    }

    public void addRawMaterial(RawMaterial rawMaterial, Integer quantity) {
        rawMaterials.put(rawMaterial, quantity);
    }

    public void clearRawMaterials() {
        rawMaterials.clear();
    }
}
