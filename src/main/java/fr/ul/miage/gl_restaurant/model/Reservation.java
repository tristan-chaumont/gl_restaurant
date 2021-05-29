package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

@Getter
@Setter
@ToString
@Data
public class Reservation {

    private Long reservationId;

    private boolean lunch;

    private Table table;

    private LocalDate reservationDate;

    public Reservation(Long reservationId, boolean lunch, Table table, LocalDate reservationDate) {
        this.reservationId = reservationId;
        this.lunch = lunch;
        this.table = table;
        this.reservationDate = reservationDate;
    }

    public Reservation(boolean lunch, Table table, LocalDate reservationDate) {
        this(null, lunch, table, reservationDate);
    }

    public Reservation(ResultSet resultSet) throws SQLException {
        this.reservationId = resultSet.getLong("reservationId");
        this.lunch = resultSet.getBoolean("lunch");
        this.reservationDate = resultSet.getDate("reservationDate").toLocalDate();
        Optional<Table> optionalTable = TableRepositoryImpl.getInstance().findById(resultSet.getLong("tableId"));
        this.table = optionalTable.orElse(null);
    }
}
