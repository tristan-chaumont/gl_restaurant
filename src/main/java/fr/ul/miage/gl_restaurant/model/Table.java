package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.TableStates;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Data
public class Table {

    private static final String TABLE_NAME = "Tables";

    private Long tableId;

    private Integer floor;

    private TableStates state;

    private Integer places;

    User user;

    public Table() {}

    public Table(Long tableId, Integer floor, TableStates state, Integer places, User user) {
        this.tableId = tableId;
        this.floor = floor;
        this.state = state;
        this.places = places;
        this.user = user;
    }

    public Table(Integer floor, TableStates state, Integer places, User user) {
        this(null, floor, state, places, user);
    }
}
