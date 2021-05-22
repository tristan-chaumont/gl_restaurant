package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Reservation;
import fr.ul.miage.gl_restaurant.model.Table;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ReservationRepositoryImpl extends Repository<Reservation, Long> {

    private static ReservationRepositoryImpl instance;

    private static final String FIND_ALL_SQL = "SELECT reservationId, lunch, tableId FROM Reservations";
    private static final String FIND_BY_ID_SQL = "SELECT reservationId, lunch, tableId FROM Reservations WHERE reservationId = ?";
    private static final String SAVE_SQL = "INSERT INTO Reservations(lunch, tableId) VALUES(?, ?)";
    private static final String UPDATE_SQL = "UPDATE Reservations SET lunch = ?, tableId = ? WHERE reservationId = ?";
    private static final String DELETE_SQL = "DELETE FROM Reservations WHERE reservationId = ?";

    private ReservationRepositoryImpl() {
        super();
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long reservationId = resultSet.getLong("reservationId");
                var lunch = resultSet.getBoolean("lunch");
                Optional<Table> table = TableRepositoryImpl.getInstance().findById(resultSet.getLong("tableId"));
                table.ifPresent(value -> reservations.add(new Reservation(reservationId, lunch, value)));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return reservations;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        Optional<Reservation> reservation = Optional.empty();
        if (id != null) {
            try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.first()) {
                        Long reservationId = resultSet.getLong("reservationId");
                        var lunch = resultSet.getBoolean("lunch");
                        Optional<Table> table = TableRepositoryImpl.getInstance().findById(resultSet.getLong("tableId"));
                        if (table.isPresent()) {
                            reservation = Optional.of(new Reservation(reservationId, lunch, table.get()));
                        }
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return reservation;
    }

    @Override
    public Reservation save(Reservation object) {
        if (object != null && object.getReservationId() == null) {
            try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setBoolean(1, object.isLunch());
                preparedStatement.setLong(2, object.getTable().getTableId());
                preparedStatement.executeUpdate();
                generateKey(object, preparedStatement);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    private void generateKey(Reservation object, PreparedStatement preparedStatement) {
        try (var resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                object.setReservationId(resultSet.getLong(1));
            }
        } catch (SQLException s) {
            log.error(s.getMessage());
        }
    }

    @Override
    public Reservation update(Reservation object) {
        if (object != null && object.getReservationId() != null) {
            try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setBoolean(1, object.isLunch());
                preparedStatement.setLong(2, object.getTable().getTableId());
                preparedStatement.setLong(3, object.getReservationId());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    public void delete(Long id) {
        super.delete(id, DELETE_SQL);
    }

    public static ReservationRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new ReservationRepositoryImpl();
        }
        return instance;
    }
}
