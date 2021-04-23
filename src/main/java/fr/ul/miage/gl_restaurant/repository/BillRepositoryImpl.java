package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.model.Bill;
import fr.ul.miage.gl_restaurant.model.User;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class BillRepositoryImpl implements Repository<Bill, Long> {

    private static final String FIND_ALL_SQL = "SELECT billId FROM Bills";
    private static final String FIND_BY_ID_SQL = "SELECT billId FROM Bills FROM Users WHERE billId = ?";
    private static final String SAVE_SQL = "INSERT INTO Users(billId) VALUES(?)";
    private static final String UPDATE_SQL = "UPDATE Users SET billId = ? WHERE billId = ?";
    private static final String DELETE_SQL = "DELETE FROM Users WHERE billId = ?";


    @Override
    public List<Bill> findAll() {
        List<Bill> bills = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long billId = resultSet.getLong("userId");
                bills.add(new Bill(billId));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return bills;
    }

    @Override
    public Optional<Bill> findById(Long id) {
        Optional<Bill> bill = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long billId = resultSet.getLong("userId");
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
                preparedStatement.setString(1, object.getLogin());
                preparedStatement.setString(2, object.getLastName());
                preparedStatement.setString(3, object.getFirstName());
                preparedStatement.setString(4, object.getRole());
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
        return null;
    }
}
