package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Reservation;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ReservationRepositoryImpl extends Repository<Reservation, Long> {

    private static ReservationRepositoryImpl instance;

    private static final String FIND_ALL_SQL = "SELECT reservationId, lunch, tableId, reservationDate FROM Reservations";
    private static final String FIND_BY_ID_SQL = "SELECT reservationId, lunch, tableId, reservationDate FROM Reservations WHERE reservationId = ?";
    private static final String FIND_BY_RESERVATIONDATE_SQL = "SELECT reservationId, lunch, tableId, reservationDate FROM Reservations WHERE reservationDate = ?";
    private static final String SAVE_SQL = "INSERT INTO Reservations(lunch, tableId, reservationDate) VALUES(?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Reservations SET lunch = ?, tableId = ?, reservationDate = ? WHERE reservationId = ?";
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
                reservations.add(new Reservation(resultSet));
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
                        reservation = Optional.of(new Reservation(resultSet));
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return reservation;
    }

    public List<Reservation> findByReservationDateEquals(LocalDate date) {
        List<Reservation> reservations = new ArrayList<>();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_RESERVATIONDATE_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setObject(1, date);
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(new Reservation(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return reservations;
    }

    @Override
    public Reservation save(Reservation object) {
        if (object != null && object.getReservationId() == null) {
            try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setBoolean(1, object.isLunch());
                preparedStatement.setLong(2, object.getTable().getTableId());
                preparedStatement.setObject(3, object.getReservationDate());
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
                preparedStatement.setObject(3, object.getReservationDate());
                preparedStatement.setLong(4, object.getReservationId());
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
