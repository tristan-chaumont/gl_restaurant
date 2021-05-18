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
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
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
    private static final String ACTION_4 = "4 : Afficher la liste des tables ainsi que leur serveur";

    public MaitreHotelController(Authentification auth) {
        super(auth);
        this.actions.addAll(Arrays.asList(ACTION_1, ACTION_2, ACTION_3, ACTION_4));
    }

    protected long askTableId(List<Table> tables) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        tables.stream().map(t -> t.getTableId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected long askServerId(Set<User> users) {
        return Long.parseLong(
                InputUtils.readInputInArray(
                        users.stream().map(u -> u.getUserId().toString()).collect(Collectors.toList())
                )
        );
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
    public String displayTables(List<Table> tables) {
        var stringBuilder = new TextStringBuilder();
        Map<Integer, Set<Table>> floors = getFloorsTables(tables);
        floors.forEach((k, v) -> {
            if (!v.isEmpty()) {
                stringBuilder.append("Étage %d : ", k);
                v.forEach(t -> {
                    if (t.getTableId() != null) {
                        stringBuilder.append("[n°%d]", t.getTableId());
                    }
                });
                stringBuilder.appendNewLine();
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Affecte des clients à une table.
     * @param table Table à laquelle affecter les clients.
     * @param nbCustomers Nombre de clients.
     * @return Le lien entre les clients et la table.
     */
    public Meal seatClient(Table table, int nbCustomers) {
        Meal meal = null;
        if (table.getState().equals(TableStates.LIBRE) && table.getPlaces() >= nbCustomers) {
            meal = mealRepository.save(new Meal(nbCustomers, Timestamp.from(Instant.now()), table));
            table.setState(TableStates.OCCUPEE);
            tableRepository.update(table);
        }
        return meal;
    }

    /**
     * Interface pour affecter les clients à une table.
     * Appelle la méthode seatClient(table, nbCustomers).
     */
    public void seatClient() {
        List<Table> availableTables = getAvailableTables(tableRepository.findAll());
        PrintUtils.print("%s%n", displayTables(availableTables));
        PrintUtils.print("Veuillez saisir le numéro de la table : ");
        var tableId = askTableId(availableTables);
        Optional<Table> table = tableRepository.findById(tableId);
        if (table.isPresent()) {
            PrintUtils.print("Veuillez saisir le nombre de personnes (de 1 à %d) : ", table.get().getPlaces());
            var nbCustomers = InputUtils.readIntegerInputInRange(1, table.get().getPlaces() + 1);

            if (seatClient(table.get(), nbCustomers) != null) {
                PrintUtils.print("Les clients ont bien été installés.%n%n");
            } else {
                PrintUtils.print("Problème lors de l'installation des clients, veuillez réessayer.%n%n");
            }
        } else {
            PrintUtils.print("Cette table n'existe pas.%n");
        }
    }

    public String displayUsers(Set<User> users) {
        var stringBuilder = new TextStringBuilder();
        users.forEach(u -> stringBuilder.appendln("[%d] %s %s", u.getUserId(), u.getFirstName(), u.getLastName()));
        return stringBuilder.toString();
    }

    /**
     * Affecte un serveur à une table.
     * @param table Table à laquelle affecter un serveur.
     * @param user Serveur à affecter.
     * @return True, si l'affectation a été faite.
     */
    public boolean assignServer(Table table, User user) {
        Optional<User> userTemp = userRepository.findById(user.getUserId());
        if(userTemp.isPresent() && user.getRole().equals(Roles.SERVEUR)) {
            table.setUser(user);
            tableRepository.update(table);
            return true;
        }
        return false;
    }

    /**
     * Interface pour affecter un serveur à une table.
     * Appelle assignServer(table, user).
     */
    public void assignServer() {
        List<Table> tables = tableRepository.findAll();
        PrintUtils.print("%s%n", displayTables(tables));
        PrintUtils.print("Veuillez saisir le numéro de la table : ");
        var tableId = askTableId(tables);
        Optional<Table> table = tableRepository.findById(tableId);
        if (table.isPresent()) {
            Set<User> servers = userRepository.findByRole(Roles.SERVEUR);
            PrintUtils.print("%n%s%n", displayUsers(servers));
            PrintUtils.print("Veuillez saisir le numéro du serveur : ");
            var serverId = askServerId(servers);
            Optional<User> server = userRepository.findById(serverId);
            if (server.isPresent()) {
                if (assignServer(table.get(), server.get())) {
                    PrintUtils.print("Le serveur a bien été affecté à la table.%n%n");
                } else {
                    PrintUtils.print("Problème lors de l'affectation du serveur à la table, veuillez réessayer.%n%n");
                }
            } else {
                PrintUtils.print("Cet utilisateur n'existe pas.%n");
            }
        } else {
            PrintUtils.print("Cette table n'existe pas.%n");
        }
    }

    public String displayTablesAndServers() {
        var stringBuilder = new TextStringBuilder();
        Map<Integer, Set<Table>> floors = getFloorsTables(tableRepository.findAll());
        floors.forEach((k, v) -> {
            stringBuilder.appendln("Étage %d", k);
            v.forEach(t -> {
                if (t.getTableId() != null) {
                    stringBuilder.append("\t[n°%d]", t.getTableId());
                    if (t.getUser() != null) {
                        stringBuilder.appendln(" - Serveur : %s %s", t.getUser().getFirstName(), t.getUser().getLastName());
                    } else {
                        stringBuilder.appendNewLine();
                    }
                }
            });
            stringBuilder.appendNewLine();
        });
        return stringBuilder.toString();
    }

    @Override
    public void callAction(int action) {
        PrintUtils.print("%n");
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            case 1:
                // TODO: Prendre une réservation
                break;
            case 2:
                seatClient();
                break;
            case 3:
                assignServer();
                break;
            case 4:
                System.out.println(displayTablesAndServers());
                break;
            default:
                break;
        }
    }
}
