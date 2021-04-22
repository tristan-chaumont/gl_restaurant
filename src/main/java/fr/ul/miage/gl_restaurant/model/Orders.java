package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Orders")
@Data
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long orderId;

    @Column(name = "orderDate")
    private Timestamp orderDate;

    @ManyToOne
    private Meals meal;

    public Orders() {}

    public Orders(Timestamp orderDate) {
        this.orderDate = orderDate;
    }
}
