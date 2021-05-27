package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.constants.Roles;
import fr.ul.miage.gl_restaurant.constants.TableStates;
import fr.ul.miage.gl_restaurant.model.*;
import fr.ul.miage.gl_restaurant.repository.*;
import fr.ul.miage.gl_restaurant.utilities.DateUtils;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MaitreHotelController extends UserController {

    private final TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();
    private final MealRepositoryImpl mealRepository = MealRepositoryImpl.getInstance();
    private final UserRepositoryImpl userRepository = UserRepositoryImpl.getInstance();
    private final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();
    private final BillRepositoryImpl billRepository = BillRepositoryImpl.getInstance();
    private final ReservationRepositoryImpl reservationRepository = ReservationRepositoryImpl.getInstance();

    /**
     * ACTIONS DE L'UTILISATEUR
     */
    private static final String ACTION_1 = "1 : Créer une réservation";
    private static final String ACTION_2 = "2 : Affecter un client à une table";
    private static final String ACTION_3 = "3 : Affecter un serveur à une table";
    private static final String ACTION_4 = "4 : Afficher la liste des tables ainsi que leur serveur";
    private static final String ACTION_5 = "5 : Créer une facture";
    private static final String ACTION_6 = "6 : Faire payer une facture";

    public MaitreHotelController(Authentification auth) {
        super(auth);
        this.actions.addAll(
            Arrays.asList(
                ACTION_1,
                ACTION_2,
                ACTION_3,
                ACTION_4,
                ACTION_5,
                ACTION_6
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

    /**
     * Vérifie si une table est libre maintenant.
     * Une table est libre si elle a l'état LIBRE ou si elle n'a pas de réservation actuellement.
     * @return True si elle est libre, false sinon.
     */
    protected boolean verifyTableIsFree(Table table, LocalDateTime date) {
        boolean isLunch = DateUtils.isDateLunch(date);
        List<Reservation> reservations = reservationRepository.findByReservationDateEquals(date.toLocalDate());
        return table.getState().equals(TableStates.LIBRE) ||
                (table.getState().equals(TableStates.RESERVEE) &&
                        reservations.stream().noneMatch(r -> r.getTable().getTableId().equals(table.getTableId()) && r.isLunch() == isLunch));
    }

    protected List<Table> getAvailableTables(List<Table> tables) {
        return tables.stream().filter(t -> verifyTableIsFree(t, LocalDateTime.now())).collect(Collectors.toList());
    }

    /**
     * Affecte des clients à une table.
     * @param table Table à laquelle affecter les clients.
     * @param nbCustomers Nombre de clients.
     * @return Le lien entre les clients et la table.
     */
    public Meal seatClient(Table table, int nbCustomers) {
        Meal meal = null;
        if (verifyTableIsFree(table, LocalDateTime.now()) && table.getPlaces() >= nbCustomers) {
            meal = mealRepository.save(new Meal(nbCustomers, Timestamp.from(Instant.now()), table));
            table.setState(TableStates.OCCUPEE);
            tableRepository.update(table);
        }
        return meal;
    }

    protected void printTableDoesNotExist() {
        PrintUtils.print("%nCette table n'existe pas.%n");
    }

    protected void printNoTableIsAvailable() {
        PrintUtils.println("%nAucune table n'est disponible.%n");
    }

    /**
     * Interface pour affecter les clients à une table.
     * Appelle la méthode seatClient(table, nbCustomers).
     */
    public void seatClient() {
        List<Table> availableTables = getAvailableTables(tableRepository.findAll());
        if (!availableTables.isEmpty()) {
            PrintUtils.print("%s%n", displayTablesByFloor(availableTables));
            var tableId = askTableId(availableTables);
            Optional<Table> table = tableRepository.findById(tableId);
            if (table.isPresent()) {
                PrintUtils.print("Veuillez saisir le nombre de personnes (de 1 à %d) : ", table.get().getPlaces());
                var nbCustomers = InputUtils.readIntegerInputInRange(1, table.get().getPlaces() + 1);

                if (seatClient(table.get(), nbCustomers) != null) {
                    PrintUtils.print("%nLes clients ont bien été installés.%n%n");
                } else {
                    PrintUtils.print("%nProblème lors de l'installation des clients, veuillez réessayer.%n%n");
                }
            } else {
                printTableDoesNotExist();
            }
        } else {
            printNoTableIsAvailable();
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
        if (!tables.isEmpty()) {
            PrintUtils.println("%s", displayTablesByFloor(tables));
            var tableId = askTableId(tables);
            Optional<Table> table = tableRepository.findById(tableId);
            if (table.isPresent()) {
                Set<User> servers = userRepository.findByRole(Roles.SERVEUR);
                PrintUtils.println("%n%s", displayUsers(servers));
                PrintUtils.print("Veuillez saisir le numéro du serveur : ");
                var serverId = askServerId(servers);
                Optional<User> server = userRepository.findById(serverId);
                if (server.isPresent()) {
                    if (assignServer(table.get(), server.get())) {
                        PrintUtils.println("%nLe serveur a bien été affecté à la table.%n");
                    } else {
                        PrintUtils.println("%nProblème lors de l'affectation du serveur à la table, veuillez réessayer.%n");
                    }
                } else {
                    PrintUtils.println("%nCet utilisateur n'existe pas.");
                }
            } else {
                PrintUtils.println("%nCette table n'existe pas.");
            }
        } else {
            PrintUtils.println("Aucune table n'est disponible.%n");
        }
    }

    public String displayTablesAndServers() {
        var stringBuilder = new TextStringBuilder();
        Map<Integer, Set<Table>> floors = getFloorsTables(tableRepository.findAll());
        if (!floors.isEmpty()) {
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
        } else {
            stringBuilder.appendln("Aucune table n'est disponible.");
        }
        return stringBuilder.toString();
    }

    /**
     * Calcule le prix total d'une commande.
     * @param order Commande.
     * @return Prix total.
     */
    protected double calculateBillTotal(Order order) {
        var total = 0.0;
        for (var dish : order.getDishes().entrySet()) {
            total += dish.getKey().getPrice() * dish.getValue();
        }
        return total;
    }

    protected String displayOrderRecap(Order order) {
        var stringBuilder = new TextStringBuilder();
        order.getDishes().forEach((k, v) ->
                stringBuilder.appendln("- %s (x%d) : %.2f€ (%.2f€)", k.getDishName(), v, k.getPrice(), k.getPrice() * v)
        );
        return stringBuilder.toString();
    }

    protected String displayBillRecap(Meal meal, Order order, double total) {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("Numéro de la table : %d", meal.getTable().getTableId())
                .appendln("Nombre de couverts : %d", meal.getCustomersNb())
                .appendln("Numéro de la commande : %d", order.getOrderId())
                .appendln("Récap. de la commande : ")
                .append(displayOrderRecap(order))
                .appendln("Prix total : %.2f€", total);
        return stringBuilder.toString();
    }

    protected boolean createBill(Meal meal, Order order, Table table) {
        var total = calculateBillTotal(order);
        PrintUtils.println("-".repeat(50));
        PrintUtils.println(StringUtils.center("Récap. de la facture", 50));
        PrintUtils.println("-".repeat(50));
        PrintUtils.println(displayBillRecap(meal, order, total));
        PrintUtils.print("Validez-vous la facture ? [y]es ou [n]o : ");
        String input = InputUtils.readInputConfirmation();
        if (input.equalsIgnoreCase("y")) {
            PrintUtils.print("Voulez-vous marquer la facture comme payée immédiatement ? [y]es ou [n]o : ");
            input = InputUtils.readInputConfirmation();
            var bill = new Bill(
                    total,
                    input.equalsIgnoreCase("y")
            );
            bill = billRepository.save(bill);
            if (bill.getBillId() == null) {
                PrintUtils.println("%nProblème lors de la création de la facture, veuillez réessayer.");
                return false;
            }
            if (input.equalsIgnoreCase("y")) {
                long mealDuration = DateUtils.getDateDiff(meal.getStartDate(), Timestamp.from(Instant.now()), TimeUnit.SECONDS);
                meal.setMealDuration(mealDuration);
                table.setState(TableStates.SALE);
                tableRepository.update(table);
            } else {
                meal.setMealDuration(null);
            }
            meal.setBill(bill);
            mealRepository.update(meal);
            return true;
        }
        return false;
    }

    protected void createBill() {
        List<Table> tables = tableRepository.findByState(TableStates.OCCUPEE);
        if (!tables.isEmpty()) {
            PrintUtils.println(displayTablesByFloor(tables));
            var tableId = askTableId(tables);
            Optional<Table> table = tableRepository.findById(tableId);
            if (table.isPresent()) {
                Optional<Meal> meal = mealRepository.findAll().stream().filter(m ->
                        m.getTable().getTableId().equals(table.get().getTableId()) && m.getBill() == null).findFirst();
                if (meal.isPresent()) {
                    handleBillCreation(table.get(), meal.get());
                } else {
                    PrintUtils.println("%nLa facture de cette table a déjà été créée.%n");
                }
            } else {
                printTableDoesNotExist();
            }
        } else {
            printNoTableIsAvailable();
        }
    }

    private void handleBillCreation(Table table, Meal meal) {
        Optional<Order> order = orderRepository.findByMeal(meal.getMealId());
        if (order.isPresent()) {
            if (createBill(meal, order.get(), table)) {
                PrintUtils.println("%nLa facture a bien été créée.%n");
            } else {
                PrintUtils.println("%nLa création de la facture a été annulée");
            }
        } else {
            PrintUtils.println("%nIl n'existe aucune commande pour cette table.%n");
        }
    }

    protected void payBill(Meal meal, Bill bill, Table table) {
        if (bill.isPaid()) {
            PrintUtils.println("%nLa facture de cette table a déjà été payée.%n");
        } else {
            bill.setPaid(true);
            long mealDuration = DateUtils.getDateDiff(meal.getStartDate(), Timestamp.from(Instant.now()), TimeUnit.SECONDS);
            meal.setMealDuration(mealDuration);
            table.setState(TableStates.SALE);
            bill = billRepository.update(bill);
            meal = mealRepository.update(meal);
            tableRepository.update(table);
            if (!bill.isPaid() || meal.getMealDuration() == null) {
                PrintUtils.println("%nProblème lors du paiement de la facture, veuillez réessayer.%n");
            } else {
                PrintUtils.println("%nPaiement de la facture effectué.%n");
            }
        }
    }

    protected void payBill() {
        List<Table> tables = tableRepository.findByState(TableStates.OCCUPEE);
        if (!tables.isEmpty()) {
            PrintUtils.println(displayTablesByFloor(tables));
            var tableId = askTableId(tables);
            Optional<Table> table = tableRepository.findById(tableId);
            if (table.isPresent()) {
                Optional<Meal> meal = mealRepository.findAll().stream().filter(m ->
                        m.getTable().getTableId().equals(table.get().getTableId()) && m.getBill() != null).findFirst();
                if (meal.isPresent()) {
                    Optional<Bill> bill = billRepository.findById(meal.get().getBill().getBillId());
                    if (bill.isPresent()) {
                        payBill(meal.get(), bill.get(), table.get());
                    } else {
                        PrintUtils.println("%nIl n'y aucune facture en cours pour cette table.%n");
                    }
                } else {
                    PrintUtils.println("%nIl n'y aucune facture en cours pour cette table.%n");
                }
            }
        } else {
            printNoTableIsAvailable();
        }
    }

    protected boolean verifyReservationIsPossible(Table table, LocalDate reservationDate, boolean lunchReservation) {
        boolean isLunch = DateUtils.isDateLunch(LocalDateTime.now());
        if (reservationDate.isEqual(LocalDate.now()) && (!isLunch && lunchReservation)) {
            return false;
        }
        if (LocalDate.now().plus(1, ChronoUnit.MONTHS).isBefore(reservationDate)) {
            PrintUtils.println("%nInfo : Vous ne pouvez réserver que dans la limite d'un mois.");
            return false;
        }
        return reservationRepository.findByReservationDateEquals(reservationDate).stream().noneMatch(r -> r.getTable().getTableId().equals(table.getTableId()) && r.isLunch() == lunchReservation);
    }

    protected void makeReservation() {
        List<Table> availableTables = getAvailableTables(tableRepository.findAll());
        if (!availableTables.isEmpty()) {
            PrintUtils.print("%s%n", displayTablesByFloor(availableTables));
            var tableId = askTableId(availableTables);
            Optional<Table> table = tableRepository.findById(tableId);
            if (table.isPresent()) {
                PrintUtils.print("Veuillez choisir une date (aaaa-mm-jj) : ");
                var dateInput = InputUtils.readDate();
                PrintUtils.print("Créer la réservation pour le déjeuner [1] ou pour le dîner [2] : ");
                var input = InputUtils.readIntegerInputInRange(1, 3);
                handleReservationCreation(table.get(), dateInput, input);
            } else {
                printTableDoesNotExist();
            }
        } else {
            printNoTableIsAvailable();
        }
    }

    private void handleReservationCreation(Table table, LocalDate dateInput, int input) {
        if (verifyReservationIsPossible(table, dateInput, input == 1)) {
            var reservation = reservationRepository.save(new Reservation(input == 1, table, dateInput));
            if (reservation.getReservationId() != null) {
                if (LocalDate.now().isEqual(dateInput) && DateUtils.isDateLunch(LocalDateTime.now()) == (input == 1)) {
                    table.setState(TableStates.RESERVEE);
                    tableRepository.update(table);
                }
                PrintUtils.println("%nRéservation créée avec succès.%n");
            } else {
                PrintUtils.println("%nProblème lors de la création de la réservation, veuillez réessayer.");
            }
        } else {
            PrintUtils.println("%nRéservation impossible pour cette date, veuillez réessayer.%n");
        }
    }

    @Override
    public void callAction(int action) {
        PrintUtils.println();
        switch (action) {
            case 0:
                auth.disconnect();
                break;
            case 1:
                makeReservation();
                break;
            case 2:
                seatClient();
                break;
            case 3:
                assignServer();
                break;
            case 4:
                PrintUtils.print(displayTablesAndServers());
                break;
            case 5:
                createBill();
                break;
            case 6:
                payBill();
                break;
            default:
                break;
        }
    }
}
