package fr.ul.miage.gl_restaurant.auth;

import fr.ul.miage.gl_restaurant.model.User;
import fr.ul.miage.gl_restaurant.repository.UserRepositoryImpl;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Scanner;

@Slf4j
@Getter
public class Authentification {

    private User user;
    private static final Scanner sc = new Scanner(System.in);

    public Authentification() {
        this.user = null;
    }

    public void signIn(String login) {
        UserRepositoryImpl userRepository = new UserRepositoryImpl();
        Optional<User> loggedUser = userRepository.findByLogin(login);
        loggedUser.ifPresent(value -> this.user = value);
    }

    public void logOut() {
        user = null;
    }

    public void disconnect() {
        logOut();
        System.out.printf("Vous êtes déconnecté. A bientôt !%n%n");
        displayInterface();
    }

    public void displayInterface() {
        System.out.println("=".repeat(50));
        System.out.println(StringUtils.center("Bonjour et bienvenue !", 50));
        System.out.println("=".repeat(50));
        System.out.printf("%nPour vous connecter, veuillez entrer votre login : ");
        signIn(sc.next());
        while (user == null) {
            System.out.print("Login incorrect, veuillez réessayer : ");
            signIn(sc.next());
        }
        System.out.printf("Vous êtes connecté en tant que '%s' (%s %s, %s)", user.getLogin(), user.getFirstName(), user.getLastName(), user.getRole());
    }

    public static void main(String[] args) {
        Authentification auth = new Authentification();
        auth.displayInterface();
    }
}
