package fr.ul.miage.gl_restaurant.auth;

import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import fr.ul.miage.gl_restaurant.utilities.InputUtils;
import fr.ul.miage.gl_restaurant.utilities.PrintUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Slf4j
@Getter
@Setter
public class Authentification {

    private User user;

    public Authentification() {
        this.user = null;
    }

    public void signIn(String login) {
        var userRepository = UserRepositoryImpl.getInstance();
        Optional<User> loggedUser = userRepository.findByLogin(login);
        loggedUser.ifPresentOrElse(value -> this.user = value,
                () -> PrintUtils.print("Login incorrect, veuillez réessayer : "));
    }

    public void logOut() {
        user = null;
    }

    public boolean isConnected() {
        return user != null;
    }

    public void disconnect() {
        logOut();
        PrintUtils.println("Vous êtes déconnecté. A bientôt !%n");
    }

    public boolean displayInterface() {
        PrintUtils.println("%s", "=".repeat(50));
        PrintUtils.println("%s", StringUtils.center("Bonjour et bienvenue !", 50));
        PrintUtils.println("%s", "=".repeat(50));
        PrintUtils.println("Pour quitter l'application, tapez : !q");
        PrintUtils.print("Pour vous connecter, veuillez entrer votre login : ");
        String input = InputUtils.readInput();
        if (input.equals("!q")) {
            PrintUtils.println("À bientôt !");
            return false;
        }
        signIn(input);
        while (user == null) {
            signIn(InputUtils.readInput());
        }
        PrintUtils.println("Vous êtes connecté en tant que '%s' (%s %s, %s)%n", user.getLogin(), user.getFirstName(), user.getLastName(), user.getRole());
        return true;
    }
}
