package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.MenuTypes;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;

@Setter
@Getter
@ToString
public class Dish {

    private static final String TABLE_NAME = "Dishes";

    private Long dishId;

    private String category;

    private MenuTypes menuType;

    private Double price;

    public Dish() {}

    public Dish(Long dishId, String category, MenuTypes menuType, Double price) {
        this.dishId = dishId;
        this.category = category;
        this.menuType = menuType;
        this.price = price;
    }

    public Dish(String text, MenuTypes menuType, Double price) {
        this(null, text, menuType, price);
    }

    public Dish(ResultSet resultSet) throws SQLException {
        dishId = resultSet.getLong("dishId");
        category = resultSet.getString("category");
        menuType = MenuTypes.getMenuType(resultSet.getString("menuType"));
        price = resultSet.getDouble("price");
    }
}
