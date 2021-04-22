package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "Bills")
@Data
public class Bills {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long billId;
}
