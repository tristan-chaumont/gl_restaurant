package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class User {

    private static final String TABLE_NAME = "Users";

    private Long userId;

    private String login;

    private String lastName;

    private String firstName;

    private String role;

    public User() {}

    public User(Long userId, String login, String lastName, String firstName, String role) {
        this.userId = userId;
        this.login = login;
        this.lastName = lastName;
        this.firstName = firstName;
        this.role = role;
    }

    public User(String login, String lastName, String firstName, String role) {
        this(null, login, lastName, firstName, role);
    }
}
