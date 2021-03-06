package fr.ul.miage.gl_restaurant.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Data
public class Meal {

    private Long mealId;

    private Integer customersNb;

    private Timestamp startDate;

    private Long mealDuration;

    private Table table;

    private Bill bill;

    public Meal() {}

    public Meal(Long mealId, Integer customersNb, Timestamp startDate, Long mealDuration, Table table, Bill bill) {
        this.mealId = mealId;
        this.customersNb = customersNb;
        this.startDate = startDate;
        this.mealDuration = mealDuration;
        this.table = table;
        this.bill = bill;
    }

    public Meal(Integer customersNb, Timestamp startDate, Long mealDuration, Table table, Bill bill) {
        this(null, customersNb, startDate, mealDuration, table, bill);
    }

    public Meal(Integer customersNb, Timestamp startDate, Table table){
        this(null, customersNb, startDate, null, table, null);
    }

    public Meal(Integer customersNb, Timestamp startDate, Table table, Bill bill){
        this(null, customersNb, startDate, null, table, bill);
    }
}
