package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Users")
@Data
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long userId;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "role")
    private String role;

    public Users() {}

    public Users(String login, String password, String lastName, String firstName, String role) {
        this.login = login;
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.role = role;
    }
}
