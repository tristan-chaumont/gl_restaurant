package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Reservation {

    private Long reservationId;

    private boolean lunch;

    private Table table;

    public Reservation() {}

    public Reservation(Long reservationId, boolean lunch, Table table) {
        this.reservationId = reservationId;
        this.lunch = lunch;
        this.table = table;
    }

    public Reservation(boolean lunch, Table table) {
        this(null, lunch, table);
    }
}
