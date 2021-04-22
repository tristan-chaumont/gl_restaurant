package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Dishes_Orders")
@Data
public class DishesOrders {

    @Id
    private long dishId;

    @Id
    private long orderId;

    @Column(name = "quantity")
    private long quantity;

    @ManyToOne
    @MapsId
    @JoinColumn(name = "orderId")
    private Orders order;

    @ManyToOne
    @MapsId
    @JoinColumn(name = "dishId")
    private Dishes dish;

    public DishesOrders() {}

    public DishesOrders(long quantity) {
        this.quantity = quantity;
    }
}
