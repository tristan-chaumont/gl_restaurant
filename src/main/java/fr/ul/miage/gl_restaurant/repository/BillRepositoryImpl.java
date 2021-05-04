package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
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

    private static final String FIND_ALL_SQL = "SELECT billId FROM Bills";
    private static final String FIND_BY_ID_SQL = "SELECT billId FROM Bills WHERE billId = ?";
    private static final String SAVE_SQL = "INSERT INTO Bills(billId) VALUES(?)";
    private static final String UPDATE_SQL = "UPDATE Bills SET billId = ? WHERE billId = ?";
    private static final String DELETE_SQL = "DELETE FROM Bills WHERE billId = ?";

    private BillRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Bill> findAll() {
        return new ArrayList<>();
    }

    @Override
    public Optional<Bill> findById(Long id) {
        Optional<Bill> bill = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL,ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long billId = resultSet.getLong("billId");

                    bill = Optional.of(new Bill(billId));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return bill;
    }

    @Override
    public Bill save(Bill object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setLong(1, object.getBillId());
                int numRowsAffected = preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setBillId(resultSet.getLong(1));
                    }
                } catch (SQLException s) {
                    s.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    @Override
    public Bill update(Bill object) {
        return null;
    }

    @Override
    public void delete(Long id) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static BillRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new BillRepositoryImpl(Environment.TEST);
        }
        return instance;
    }
}
