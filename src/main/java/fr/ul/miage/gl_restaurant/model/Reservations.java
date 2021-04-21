package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import fr.ul.miage.gl_restaurant.model.Tables;

import javax.persistence.*;

@Entity
@Table(name = "Reservations")
@Data
public class Reservations {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long reservationId;

    @Column(name = "lunch")
    private boolean lunch;

    @ManyToOne
    private Tables table;

    public Reservations() {}

    public Reservations(boolean lunch) {
        this.lunch = lunch;
    }
}
