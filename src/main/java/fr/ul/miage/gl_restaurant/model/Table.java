package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Table {

    private static final String TABLE_NAME = "Tables";

    private Long tableId;

    private Integer floor;

    private String state;

    private Integer places;

    User user;

    public Table() {}

    public Table(Long tableId, Integer floor, String state, Integer places, User user) {
        this.tableId = tableId;
        this.floor = floor;
        this.state = state;
        this.places = places;
        this.user = user;
    }

    public Table(Integer floor, String state, Integer places, User user) {
        this(null, floor, state, places, user);
    }
}
