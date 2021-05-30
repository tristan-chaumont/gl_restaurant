package fr.ul.miage.gl_restaurant.controller;

import fr.ul.miage.gl_restaurant.auth.Authentification;
import fr.ul.miage.gl_restaurant.model.Table;
import fr.ul.miage.gl_restaurant.repository.*;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.util.*;
import java.util.stream.Collectors;

public abstract class UserController {

    /**
     * Repositories
     */
    protected final RawMaterialRepositoryImpl rawMaterialRepository = RawMaterialRepositoryImpl.getInstance();
    protected final OrderRepositoryImpl orderRepository = OrderRepositoryImpl.getInstance();
    protected final UserRepositoryImpl userRepository = UserRepositoryImpl.getInstance();
    protected final TableRepositoryImpl tableRepository = TableRepositoryImpl.getInstance();
    protected final MealRepositoryImpl mealRepository = MealRepositoryImpl.getInstance();
    protected final DishRepositoryImpl dishRepository = DishRepositoryImpl.getInstance();
    protected final BillRepositoryImpl billRepository = BillRepositoryImpl.getInstance();
    protected final ReservationRepositoryImpl reservationRepository = ReservationRepositoryImpl.getInstance();

    protected Authentification auth;

    protected final Set<String> actions;

    private static final String LOGOUT_STRING = "0 : Se déconnecter";

    protected UserController(Authentification auth) {
        this.auth = auth;
        actions = new LinkedHashSet<>();
        actions.add(LOGOUT_STRING);
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
     * Demande à l'utilisareur de renseigner l'id de la table qu'il veut traiter.
     * @param tables Liste des tables.
     * @return L'id de la table à traiter.
     */
    protected long askTableId(List<Table> tables) {
        PrintUtils.print("Veuillez saisir le numéro de la table : ");
        return Long.parseLong(
                InputUtils.readInputInArray(
                        tables.stream().map(t -> t.getTableId().toString()).collect(Collectors.toList())
                )
        );
    }

    protected String displayTablesByFloor(List<Table> tables) {
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

    public String displayActions() {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("=".repeat(50))
                .appendln(StringUtils.center(String.format("ACCUEIL (%s)", auth.getUser().getRole().toString()), 50))
                .appendln("=".repeat(50));
        for (String action : actions) {
            stringBuilder.appendln(action);
        }
        return stringBuilder.toString();
    }

    protected String displaySubActions(Set<String> subActions) {
        var stringBuilder = new TextStringBuilder();
        for (String subAction : subActions) {
            stringBuilder.appendln(subAction);
        }
        return stringBuilder.toString();
    }

    public abstract void callAction(int action);

    public Set<String> getActions() {
        return actions;
    }
}
