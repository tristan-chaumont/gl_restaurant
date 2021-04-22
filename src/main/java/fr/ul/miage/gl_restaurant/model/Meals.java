package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Meals")
@Data
public class Meals {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long mealId;

    @Column(name = "customersNb")
    private long customersNb;

    @Column(name = "startDate")
    private Timestamp startDate;

    @Column(name = "mealDuration")
    private long mealDuration;

    @ManyToOne
    private Tables table;

    @OneToOne
    private Bills bill;

    public Meals() {}

    public Meals(long customersNb, Timestamp startDate, long mealDuration) {
        this.customersNb = customersNb;
        this.startDate = startDate;
        this.mealDuration = mealDuration;
    }
}
