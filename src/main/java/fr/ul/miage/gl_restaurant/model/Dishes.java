package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Dishes")
@Data
public class Dishes {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long dishId;

    @Column(name = "category")
    private String category;

    @Column(name = "menuType")
    private String menuType;

    @Column(name = "price")
    private double price;

    public Dishes() {}

    public Dishes(String category, String menuType, double price) {
        this.category = category;
        this.menuType = menuType;
        this.price = price;
    }
}
