package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Bill {

    private static final String TABLE_NAME = "Bills";

    private Long billId;

    public Bill() {}

    public Bill(Long billId) {
        this.billId = billId;
    }
}
