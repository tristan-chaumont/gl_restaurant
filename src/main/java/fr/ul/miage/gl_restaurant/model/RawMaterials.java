package fr.ul.miage.gl_restaurant.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "RawMaterials")
@Data
public class RawMaterials {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long rmId;

    @Column(name = "rmLabel")
    private String rmLabel;

    @Column(name = "stockQuantity")
    private long stockQuantity;

    @Column(name = "unit")
    private String unit;

    public RawMaterials() {}

    public RawMaterials(String rmLabel, long stockQuantity, String unit) {
        this.rmLabel = rmLabel;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
    }
}
