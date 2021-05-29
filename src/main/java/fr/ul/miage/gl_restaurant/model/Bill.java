package fr.ul.miage.gl_restaurant.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;

@Setter
@Getter
@ToString
@Data
public class Bill {

    private Long billId;

    private double total;

    private boolean paid;

    public Bill(Long billId) {
        this.billId = billId;
        total = 0;
        paid = false;
    }

    public Bill(double total, boolean paid) {
        this.total = total;
        this.paid = paid;
    }

    public Bill(ResultSet resultSet) throws SQLException {
        this.billId = resultSet.getLong("billId");
        this.total = resultSet.getDouble("total");
        this.paid = resultSet.getBoolean("paid");
    }
}
