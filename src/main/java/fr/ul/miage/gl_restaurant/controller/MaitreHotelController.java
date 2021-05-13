package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.Meal;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.MealRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.TableRepositoryImpl;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class MaitreHotelController extends UserController {

    private final TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();
    private final MealRepositoryImpl mealRepository = MealRepositoryImpl.getInstance();
    private final UserRepositoryImpl userRepository = UserRepositoryImpl.getInstance();

    /**
     * ACTIONS DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Prendre une réservation";
    private static final String ACTION_2 = "2 : Affecter un client à une table";
    private static final String ACTION_3 = "3 : Affecter un serveur à une table";

    public MaitreHotelController(Authentification auth) {
        super(auth);
        this.actions.addAll(Arrays.asList(ACTION_1, ACTION_2, ACTION_3));
    }

    protected List<Table> getAvailableTables(List<Table> tables) {
        return tables.stream().filter(t -> t.getState().equals(TableStates.LIBRE)).collect(Collectors.toList());
    }

    /**
     * Renvoie une hashmap contenant le numéro de l'étage et la liste des tables de cet étage.
     * Permet de faciliter l'affichage des tables dans l'interface.
     */
    protected Map<Integer, Set<Table>> getFloorsTables(List<Table> tables) {
        Map<Integer, Set<Table>> floors = new HashMap<>();
        tables.forEach(t -> {
            if (floors.containsKey(t.getFloor())) {
                floors.get(t.getFloor()).add(t);
            } else {
                floors.put(t.getFloor(), new LinkedHashSet<>(Collections.singletonList(t)));
            }
        });
        return floors;
    }

    /**
     * Renvoie un string contenant l'étage et les tables associées à cet étage.
     */
    public String displayAvailableTables(List<Table> tables) {
        var stringBuilder = new TextStringBuilder();
        Map<Integer, Set<Table>> floors = getFloorsTables(getAvailableTables(tables));
        floors.forEach((k, v) -> {
            if (!v.isEmpty()) {
                stringBuilder.append("Étage %d : ", k);
                v.forEach(t -> {
                    if (t.getTableId() != null) {
                        stringBuilder.append(
                                "[n°%d]",
                                t.getTableId());
                    }
                });
                stringBuilder.appendNewLine();
            }
        });
        return stringBuilder.toString();
    }

    public Meal seatClient(Table table, int nbCustomers) {
        Meal meal = null;
        if (table.getState().equals(TableStates.LIBRE) && table.getPlaces() >= nbCustomers) {
            meal = mealRepository.save(new Meal(nbCustomers, Timestamp.from(Instant.now()), table));
            table.setState(TableStates.OCCUPEE);
            tableRepository.update(table);
        }
        return meal;
    }

    public void seatClient() {
        List<Table> availableTables = getAvailableTables(tableRepository.findAll());
        System.out.println(displayAvailableTables(availableTables));
        System.out.print("Veuillez saisir le numéro de la table : ");
        var tableId = Long.parseLong(
                InputUtils.readInputInArray(
                        availableTables.stream().map(t -> t.getTableId().toString()).collect(Collectors.toList())
                )
        );
        Optional<Table> table = tableRepository.findById(tableId);
        if (table.isPresent()) {
            System.out.printf("Veuillez saisir le nombre de personnes (de 1 à %d) : ", table.get().getPlaces());
            var nbCustomers = InputUtils.readIntegerInputInRange(1, table.get().getPlaces() + 1);

            if (seatClient(table.get(), nbCustomers) != null) {
                System.out.printf("Les clients ont bien été installés.%n%n");
            } else {
                System.out.printf("Problème lors de l'installation des clients, veuillez réessayer.%n%n");
            }
        } else {
            System.out.println("Cette table n'existe pas.");
        }
    }

    /**
     * Affecte un serveur à une table.
     * @param table Table à laquelle affecter un serveur.
     * @param user Serveur à affecter.
     */
    public void assignServer(Table table, User user) {
        Optional<User> userTemp = userRepository.findById(user.getUserId());
        if(userTemp.isPresent() && user.getRole().equals(Roles.SERVEUR)) {
            table.setUser(user);
            tableRepository.update(table);
        }
    }

    @Override
    public String displayActions() {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln(super.displayActions());
        for (String action : actions) {
            stringBuilder.appendln(action);
        }
        return stringBuilder.toString();
    }

    @Override
    public void callAction(int action) {
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            case 2:
                seatClient();
                break;
            case 3:

                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        List<Table> tables = new ArrayList<>() {{
            add(new Table(20L, 1, TableStates.LIBRE, 4, null));
            add(new Table(100L, 2, TableStates.OCCUPEE, 5, null));
            add(new Table(59L, 2, TableStates.LIBRE, 6, null));
            add(new Table(77L, 1, TableStates.LIBRE, 7, null));
            add(new Table(3L, 1, TableStates.SALE, 8, null));
            add(new Table(7L, 3, TableStates.LIBRE, 9, null));
            add(new Table(12L, 3, TableStates.LIBRE, 10, null));
        }};
    }
}
