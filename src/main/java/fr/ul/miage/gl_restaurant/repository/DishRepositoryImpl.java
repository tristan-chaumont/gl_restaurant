package fr.ul.miage.gl_restaurant.repository;

import fr.ul.miage.gl_restaurant.constants.Environment;
import fr.ul.miage.gl_restaurant.model.Dish;
import fr.ul.miage.gl_restaurant.model.RawMaterial;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Slf4j
public class DishRepositoryImpl extends Repository<Dish, Long> {

    private static DishRepositoryImpl instance;

    private static final String FIND_ALL_SQL = "SELECT dishId, dishName, category, menuType, price, dailyMenu FROM Dishes";
    private static final String FIND_BY_ID_SQL = "SELECT dishId, dishName, category, menuType, price, dailyMenu FROM Dishes WHERE dishId = ?";
    private static final String FIND_BY_NAME_SQL = "SELECT dishId, dishName, category, menuType, price, dailyMenu FROM Dishes WHERE dishName = ?";
    private static final String FIND_BY_CATEGORY_SQL = "SELECT dishId, dishName, category, menuType, price, dailyMenu FROM Dishes WHERE category = ?";
    private static final String SAVE_SQL = "INSERT INTO Dishes(dishName, category, menuType, price, dailyMenu) VALUES(?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE Dishes SET dishName = ?, category = ?, menuType = ?, price = ?, dailyMenu = ? WHERE dishId = ?";
    private static final String UPDATE_DAILY_MENU_SQL = "UPDATE Dishes SET dailyMenu = ? WHERE dishId = ?";
    private static final String DELETE_SQL = "DELETE FROM Dishes WHERE dishId = ?";
    private static final String FIND_BY_RM_SQL = "SELECT Dishes.dishId, dishName, category, menuType, price, dailyMenu FROM Dishes INNER JOIN Dishes_RawMaterials ON Dishes.dishID = Dishes_RawMaterials.dishId WHERE rmId = ?";

    private static final String FIND_RM_BY_DISH_ID_SQL = "SELECT dishId, rmId, quantity FROM Dishes_RawMaterials WHERE dishId = ?";
    private static final String SAVE_RM_SQL = "INSERT INTO Dishes_RawMaterials(dishId, rmId, quantity) VALUES(?, ?, ?)";
    private static final String DELETE_RM_SQL = "DELETE FROM Dishes_RawMaterials WHERE dishId = ?";

    private DishRepositoryImpl(Environment environment) {
        super(environment);
    }

    @Override
    public List<Dish> findAll() {
        List<Dish> dishes = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_SQL)) {
            while (resultSet.next()) {
                dishes.add(new Dish(resultSet, findRawMaterialsByDishId(resultSet.getLong("dishId"))));
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dishes;
    }

    @Override
    public Optional<Dish> findById(Long id) {
        Optional<Dish> dish = Optional.empty();
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.first()) {
                        dish = Optional.of(new Dish(resultSet, findRawMaterialsByDishId(resultSet.getLong("dishId"))));
                    }
                }
            } catch (SQLException e) {
                log.error("Exception: " + e.getMessage());
            }
        }
        return dish;
    }

    public Optional<Dish> findByName(String name) {
        Optional<Dish> dish = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_NAME_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    dish = Optional.of(new Dish(resultSet, findRawMaterialsByDishId(resultSet.getLong("dishId"))));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dish;
    }

    public List<Dish> findByCategory(String category) {
        List<Dish> dishes = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_CATEGORY_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1, category);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    dishes.add(new Dish(resultSet, findRawMaterialsByDishId(resultSet.getLong("dishId"))));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dishes;
    }

    public List<Dish> findByRM(Long rmId) {
        List<Dish> dishes = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_RM_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setLong(1, rmId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    dishes.add(new Dish(resultSet, findRawMaterialsByDishId(resultSet.getLong("dishId"))));
                }
            }
        } catch (SQLException e) {
            log.error("Exception: " + e.getMessage());
        }
        return dishes;
    }

    @Override
    public Dish save(Dish object) {
        if (object != null && object.getDishId() == null) {
            Optional<Dish> dish = findByName(object.getDishName());
            if(dish.isEmpty()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, object.getDishName());
                    preparedStatement.setString(2, object.getCategory());
                    preparedStatement.setString(3, object.getMenuType().toString());
                    preparedStatement.setDouble(4, object.getPrice());
                    preparedStatement.setBoolean(5, object.isDailyMenu());
                    preparedStatement.executeUpdate();
                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                        if (resultSet.next()) {
                            object.setDishId(resultSet.getLong(1));
                            saveRawMaterialsByDishId(object.getDishId(), object.getRawMaterials());
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
    public Dish update(Dish object) {
        if (object != null && object.getDishId() != null) {
            Optional<Dish> dish = findByName(object.getDishName());
            if (dish.isEmpty() || object.getDishId().equals(dish.get().getDishId())) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
                    preparedStatement.setString(1, object.getDishName());
                    preparedStatement.setString(2, object.getCategory());
                    preparedStatement.setString(3, object.getMenuType().toString());
                    preparedStatement.setDouble(4, object.getPrice());
                    preparedStatement.setBoolean(5, object.isDailyMenu());
                    preparedStatement.setLong(6, object.getDishId());
                    updateRawMaterialsByDishId(object.getDishId(), object.getRawMaterials());
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("La mise à jour du plat a échoué, le nom que vous lui avez affecté est déjà existant.");
                Optional<Dish> dishAlreadyExists = findById(object.getDishId());
                if (dishAlreadyExists.isPresent()) {
                    object = dishAlreadyExists.get();
                }
            }
        }
        return object;
    }

    public Dish updateDailyMenu(Long id, boolean dailyMenu) {
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_DAILY_MENU_SQL)) {
                preparedStatement.setBoolean(1, dailyMenu);
                preparedStatement.setLong(2, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Erreur : Impossible de mettre à jour un plat qui n'existe pas.");
        }
        Optional<Dish> dish = findById(id);
        return dish.orElse(null);
    }

    @Override
    public void delete(Long id) {
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_SQL)) {
                deleteRawMaterialsByDishId(id);
                preparedStatement.setLong(1, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /* RAW MATERIAL */

    public Map<RawMaterial, Integer> findRawMaterialsByDishId(Long id) {
        Map<RawMaterial, Integer> rawMaterials = new HashMap<>();
        if (id != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_RM_BY_DISH_ID_SQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Optional<RawMaterial> rawMaterial = RawMaterialRepositoryImpl.getInstance().findById(resultSet.getLong("rmId"));
                        if (rawMaterial.isPresent()) {
                            rawMaterials.put(rawMaterial.get(), resultSet.getInt("quantity"));
                        }
                    }
                }
            } catch (SQLException e) {
                log.error("Exception: " + e.getMessage());
            }
        }
        return rawMaterials;
    }

    private void saveRawMaterialsByDishId(Long dishId, Map<RawMaterial, Integer> rawMaterials) {
        if (dishId != null && rawMaterials != null) {
            rawMaterials.forEach((rm, quantity) -> {
                if (rm.getRawMaterialId() != null) {
                    try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_RM_SQL)) {
                        preparedStatement.setLong(1, dishId);
                        preparedStatement.setLong(2, rm.getRawMaterialId());
                        preparedStatement.setInt(3, quantity);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void updateRawMaterialsByDishId(Long dishId, Map<RawMaterial, Integer> rawMaterials) {
        deleteRawMaterialsByDishId(dishId);
        saveRawMaterialsByDishId(dishId, rawMaterials);
    }

    public void deleteRawMaterialsByDishId(Long dishId) {
        if (dishId != null) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(DELETE_RM_SQL)) {
                preparedStatement.setLong(1, dishId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static DishRepositoryImpl getInstance() {
        if (instance == null) {
            instance = new DishRepositoryImpl(Environment.TEST);
        }
        return instance;
    }
}
