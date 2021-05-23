package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Bill;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BillRepositoryImpl extends Repository<Bill, Long> {

    private static BillRepositoryImpl instance;

    private static final String FIND_ALL_SQL = "SELECT billId, total, paid FROM Bills";
    private static final String FIND_BY_ID_SQL = "SELECT billId, total, paid FROM Bills WHERE billId = ?";
    private static final String SAVE_SQL = "INSERT INTO Bills(total, paid) VALUES(?, ?)";
    private static final String UPDATE_SQL = "UPDATE Bills SET total = ?, paid = ? WHERE billId = ?";
    private static final String DELETE_SQL = "DELETE FROM Bills WHERE billId = ?";

    private BillRepositoryImpl() {
        super();
    }

    @Override
    public List<Bill> findAll() {
        return new ArrayList<>();
    }

    @Override
    public Optional<Bill> findById(Long id) {
        Optional<Bill> bill = Optional.empty();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    bill = Optional.of(new Bill(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return bill;
    }

    @Override
    public Bill save(Bill object) {
        if (object != null) {
            try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setDouble(1, object.getTotal());
                preparedStatement.setBoolean(2, object.isPaid());
                preparedStatement.executeUpdate();
                generateKey(object, preparedStatement);
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return object;
    }

    private void generateKey(Bill object, PreparedStatement preparedStatement) {
        try (var resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                object.setBillId(resultSet.getLong(1));
            }
        } catch (SQLException s) {
            log.error(s.getMessage());
        }
    }

    @Override
    public Bill update(Bill object) {
        if (object != null && object.getBillId() != null) {
            try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setDouble(1, object.getTotal());
                preparedStatement.setBoolean(2, object.isPaid());
                preparedStatement.setLong(3, object.getBillId());
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

    public static BillRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new BillRepositoryImpl();
        }
        return instance;
    }
}
