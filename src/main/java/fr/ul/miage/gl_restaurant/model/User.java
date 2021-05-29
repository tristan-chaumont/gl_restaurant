package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.Roles;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

@Setter
@Getter
@Data
public class User {

    private Long userId;

    private String login;

    private String lastName;

    private String firstName;

    private Roles role;

    public User(Long userId, String login, String lastName, String firstName, Roles role) {
        this.userId = userId;
        this.login = login;
        this.lastName = lastName;
        this.firstName = firstName;
        this.role = role;
    }

    public User(String login, String lastName, String firstName, Roles role) {
        this(null, login, lastName, firstName, role);
    }

    public User(ResultSet resultSet) throws SQLException {
        userId = resultSet.getLong("userId");
        login = resultSet.getString("login");
        lastName = resultSet.getString("lastName");
        firstName = resultSet.getString("firstName");
        role = Roles.getRole(resultSet.getString("role"));
    }

    @Override
    public String toString() {
        var stringBuilder = new TextStringBuilder();
        stringBuilder.appendln("-".repeat(25))
                .appendln("| " + StringUtils.center("Utilisateur n°" + userId, 21) + " |")
                .appendln("-".repeat(25))
                .appendln("Login : %s", login)
                .appendln("Nom : %s", lastName)
                .appendln("Prénom : %s", firstName)
                .appendln("Rôle : %s", role.toString());
        return stringBuilder.toString();
    }
}
