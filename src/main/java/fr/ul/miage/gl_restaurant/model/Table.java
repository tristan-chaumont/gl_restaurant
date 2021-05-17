package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


@Getter
@Setter
@Data
public class Table {

    private Long tableId;

    private Integer floor;

    private TableStates state;

    private Integer places;

    private User user;

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
    public Table(ResultSet resultSet) throws SQLException {
        tableId = resultSet.getLong("tableId");
        floor = resultSet.getInt("floor");
        state = TableStates.getState(resultSet.getString("state"));
        places = resultSet.getInt("places");
        Optional<User> optionalUser = UserRepositoryImpl.getInstance().findById(resultSet.getLong("userId"));
        user = optionalUser.orElse(null);
    }

    public void changeState(TableStates tableStates) {
        this.setState(tableStates);
        TableRepositoryImpl.getInstance().update(this);
    }

    @Override
    public String toString() {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("-".repeat(20))
                .appendln("|" + StringUtils.center("Table " +tableId, 18) + "|")
                .appendln("-".repeat(20))
                .appendln("Étage : %d", floor)
                .appendln("Serveur : %s %s", user.getFirstName(), user.getLastName())
                .appendln("Nombre de places : %d", places)
                .appendln("Statut : %s", state.toString());
        return stringBuilder.toString();
    }
}
