package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class RawMaterialRepositoryImpl extends Repository<RawMaterial, Long> {

    private static final String FIND_ALL_SQL = "SELECT rmId, rmLabel, stockQuantity, unit FROM RawMaterials";
    private static final String FIND_BY_ID_SQL = "SELECT rmId, rmLabel, stockQuantity, unit FROM RawMaterials WHERE rmId = ?";
    private static final String SAVE_SQL = "INSERT INTO RawMaterials(rmLabel, stockQuantity, unit) VALUES(?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE RawMaterials SET rmLabel = ?, stockQuantity = ?, unit = ? WHERE rmId = ?";
    private static final String DELETE_SQL = "DELETE FROM RawMaterials WHERE rmId = ?";

    protected RawMaterialRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<RawMaterial> findAll() {
        List<RawMaterial> rawMaterials = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                Long rmId = resultSet.getLong("rmId");
                String rmLabel = resultSet.getString("rmLabel");
                Integer stockQuantity = resultSet.getInt("stockQuantity");
                String unit = resultSet.getString("unit");
                rawMaterials.add(new RawMaterial(rmId, rmLabel, stockQuantity, unit));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return rawMaterials;
    }

    @Override
    public Optional<RawMaterial> findById(Long id) {
        Optional<RawMaterial> rawMaterial = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    Long rmId = resultSet.getLong("rmId");
                    String rmLabel = resultSet.getString("rmLabel");
                    Integer stockQuantity = resultSet.getInt("stockQuantity");
                    String unit = resultSet.getString("unit");
                    rawMaterial = Optional.of(new RawMaterial(rmId, rmLabel, stockQuantity, unit));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return rawMaterial;
    }

    @Override
    public RawMaterial save(RawMaterial object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, object.getRawMaterialLabel());
                preparedStatement.setInt(2, object.getStockQuantity());
                preparedStatement.setString(3, object.getUnit());
                int numRowsAffected = preparedStatement.executeUpdate();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        object.setRawMaterialId(resultSet.getLong(1));
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
    public RawMaterial update(RawMaterial object) {
        if (object != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                preparedStatement.setString(1, object.getRawMaterialLabel());
                preparedStatement.setInt(2, object.getStockQuantity());
                preparedStatement.setString(3, object.getUnit());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return object;
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
}
