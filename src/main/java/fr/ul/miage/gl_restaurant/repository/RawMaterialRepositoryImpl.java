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

    private static final String FIND_ALL_SQL = "SELECT rmId, rmName, stockQuantity, unit FROM RawMaterials";
    private static final String FIND_BY_ID_SQL = "SELECT rmId, rmName, stockQuantity, unit FROM RawMaterials WHERE rmId = ?";
    private static final String FIND_BY_NAME = "SELECT rmId, rmName, stockQuantity, unit FROM RawMaterials WHERE rmName = ?";
    private static final String SAVE_SQL = "INSERT INTO RawMaterials(rmName, stockQuantity, unit) VALUES(?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE RawMaterials SET rmName = ?, stockQuantity = ?, unit = ? WHERE rmId = ?";
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
                rawMaterials.add(new RawMaterial(resultSet));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return rawMaterials;
    }

    @Override
    public Optional<RawMaterial> findById(Long id) {
        Optional<RawMaterial> rawMaterial = Optional.empty();
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.first()) {
                        rawMaterial = Optional.of(new RawMaterial(resultSet));
                    }
                }
            } catch (SQLException e) {
                log.error("Exception: " + e.getMessage());
            }
        }
        return rawMaterial;
    }

    public Optional<RawMaterial> findByName(String name) {
        Optional<RawMaterial> rawMaterial = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_NAME, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1,  name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    rawMaterial = Optional.of(new RawMaterial(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return rawMaterial;
    }

    @Override
    public RawMaterial save(RawMaterial object) {
        if (object != null && object.getRawMaterialId() == null) {
            Optional<RawMaterial> rawMaterial = findByName(object.getRawMaterialName());
            if (rawMaterial.isEmpty()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, object.getRawMaterialName());
                    preparedStatement.setInt(2, object.getStockQuantity());
                    preparedStatement.setString(3, object.getUnit().toString());
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
        }
        return object;
    }

    @Override
    public RawMaterial update(RawMaterial object) {
        if (object != null && object.getRawMaterialId() != null) {
            Optional<RawMaterial> rawMaterial = findByName(object.getRawMaterialName());
            if (rawMaterial.isEmpty() || object.getRawMaterialId().equals(rawMaterial.get().getRawMaterialId())) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                    preparedStatement.setString(1, object.getRawMaterialName());
                    preparedStatement.setInt(2, object.getStockQuantity());
                    preparedStatement.setString(3, object.getUnit().toString());
                    preparedStatement.setLong(4, object.getRawMaterialId());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("La mise à jour de la matière première a échoué, le nom que vous lui avez affecté est déjà existant.");
                Optional<RawMaterial> rawMaterialAlreadyExists = findById(object.getRawMaterialId());
                if (rawMaterialAlreadyExists.isPresent()) {
                    object = rawMaterialAlreadyExists.get();
                }
            }
        }
        return object;
    }

    @Override
    public void delete(Long id) {
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {
                preparedStatement.setLong(1, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
