package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.Order;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
public class RawMaterialRepositoryImpl extends Repository<RawMaterial, Long> {

    private static RawMaterialRepositoryImpl instance;

    private static final String FIND_ALL_SQL = "SELECT rmId, rmName, stockQuantity, unit FROM RawMaterials";
    private static final String FIND_BY_ID_SQL = "SELECT rmId, rmName, stockQuantity, unit FROM RawMaterials WHERE rmId = ?";
    private static final String FIND_BY_NAME = "SELECT rmId, rmName, stockQuantity, unit FROM RawMaterials WHERE rmName = ?";
    private static final String FIND_OUT_OF_STOCK_SQL = "SELECT rmId, rmName, stockQuantity, unit FROM RawMaterials WHERE stockQuantity < ?";
    private static final String SAVE_SQL = "INSERT INTO RawMaterials(rmName, stockQuantity, unit) VALUES(?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE RawMaterials SET rmName = ?, stockQuantity = ?, unit = ? WHERE rmId = ?";
    private static final String UPDATE_OUT_OF_STOCK_SQL = "UPDATE RawMaterials SET stockQuantity = stockQuantity + ? WHERE stockQuantity < ?";
    private static final String UPDATE_STOCK_SQL = "UPDATE RawMaterials SET stockQuantity = stockQuantity - ? WHERE rmId = ?";
    private static final String DELETE_SQL = "DELETE FROM RawMaterials WHERE rmId = ?";

    private RawMaterialRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<RawMaterial> findAll() {
        List<RawMaterial> rawMaterials = new ArrayList<>();
        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                rawMaterials.add(new RawMaterial(resultSet));
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return rawMaterials;
    }

    @Override
    public Optional<RawMaterial> findById(Long id) {
        Optional<RawMaterial> rawMaterial = Optional.empty();
        if (id != null) {
            try (var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.first()) {
                        rawMaterial = Optional.of(new RawMaterial(resultSet));
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return rawMaterial;
    }

    public Optional<RawMaterial> findByName(String name) {
        Optional<RawMaterial> rawMaterial = Optional.empty();
        try (var preparedStatement = connection.prepareStatement(FIND_BY_NAME, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1,  name);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    rawMaterial = Optional.of(new RawMaterial(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return rawMaterial;
    }

    public List<RawMaterial> findOutOfStock(int threshold) {
        List<RawMaterial> outOfStockRawMaterials = new ArrayList<>();
        try (var preparedStatement = connection.prepareStatement(FIND_OUT_OF_STOCK_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setInt(1,  threshold);
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    outOfStockRawMaterials.add(new RawMaterial(resultSet));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return  outOfStockRawMaterials;
    }

    @Override
    public RawMaterial save(RawMaterial object) {
        if (object != null && object.getRawMaterialId() == null) {
            Optional<RawMaterial> rawMaterial = findByName(object.getRawMaterialName());
            if (rawMaterial.isEmpty()) {
                try (var preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, object.getRawMaterialName());
                    preparedStatement.setInt(2, object.getStockQuantity());
                    preparedStatement.setString(3, object.getUnit().toString());
                    preparedStatement.executeUpdate();
                    generateKey(object, preparedStatement);
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return object;
    }

    private void generateKey(RawMaterial object, PreparedStatement preparedStatement) {
        try (var resultSet = preparedStatement.getGeneratedKeys()) {
            if (resultSet.next()) {
                object.setRawMaterialId(resultSet.getLong(1));
            }
        } catch (SQLException s) {
            log.error(s.getMessage());
        }
    }

    @Override
    public RawMaterial update(RawMaterial object) {
        if (object != null && object.getRawMaterialId() != null) {
            Optional<RawMaterial> rawMaterial = findByName(object.getRawMaterialName());
            if (rawMaterial.isEmpty() || object.getRawMaterialId().equals(rawMaterial.get().getRawMaterialId())) {
                try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                    preparedStatement.setString(1, object.getRawMaterialName());
                    preparedStatement.setInt(2, object.getStockQuantity());
                    preparedStatement.setString(3, object.getUnit().toString());
                    preparedStatement.setLong(4, object.getRawMaterialId());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    log.error(e.getMessage());
                }
            } else {
                // La mise à jour de la matière première a échoué, le nom que vous lui avez affecté est déjà existant.
                Optional<RawMaterial> rawMaterialAlreadyExists = findById(object.getRawMaterialId());
                if (rawMaterialAlreadyExists.isPresent()) {
                    object = rawMaterialAlreadyExists.get();
                }
            }
        }
        return object;
    }

    /**
     * Ajout du stock pour les matières premières en rupture de stock.
     * @param threshold Seuil où l'on considère une rupture de stock.
     * @param restock Quantité de matière première à ajouter.
     */
    public void updateOutOfStock(int threshold, int restock) {
        try (var preparedStatement = connection.prepareStatement(UPDATE_OUT_OF_STOCK_SQL)) {
            preparedStatement.setInt(1, restock);
            preparedStatement.setInt(2, threshold);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Retire la quantité fournie du stock total de la matière première.
     * @param quantityToRemove Quantité à retirer.
     * @param rmId ID de la matière première.
     */
    public void updateStock(int quantityToRemove, Long rmId) {
        try (var preparedStatement = connection.prepareStatement(UPDATE_STOCK_SQL)) {
            preparedStatement.setInt(1, quantityToRemove);
            preparedStatement.setLong(2, rmId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Retire du stock de matières premières total, la quantité utilisée pour chaque plat d'une commande.
     * @param order Commande à traiter.
     */
    public void updateStockBasedOnTakenOrder(Order order) {
        order.getDishes().forEach((dish, qtDishes) ->
                IntStream.range(0, qtDishes).forEach(i ->
                        dish.getRawMaterials().forEach(((rawMaterial, qtRM) ->
                                updateStock(qtRM, rawMaterial.getRawMaterialId())))));
    }

    public void delete(Long id) {
        super.delete(id, DELETE_SQL);
    }

    public static RawMaterialRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new RawMaterialRepositoryImpl(Environment.TEST);
        }
        return instance;
    }
}
