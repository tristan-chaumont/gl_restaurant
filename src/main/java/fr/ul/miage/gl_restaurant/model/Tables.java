package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Tables")
@Data
public class Tables {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long tableId;

    @Column(name = "floor")
    private long floor;

    @Column(name = "state")
    private String state;

    @Column(name = "places")
    private long places;

    @ManyToOne
    private Users user;

    public Tables() {}

    public Tables(long floor, String state, long places) {
        this.floor = floor;
        this.state = state;
        this.places = places;
    }
}
