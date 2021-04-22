package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Dishes_RawMaterials")
@Data
public class DishesRawMaterials {

    @Id
    private long dishId;

    @Id
    private long rmId;

    @Column(name = "quantity")
    private long quantity;

    @ManyToOne
    @MapsId
    @JoinColumn(name = "dishId")
    private Dishes dish;

    @ManyToOne
    @MapsId
    @JoinColumn(name = "rmId")
    private RawMaterials rm;

    public DishesRawMaterials() {}

    public DishesRawMaterials(long quantity) {
        this.quantity = quantity;
    }
}
