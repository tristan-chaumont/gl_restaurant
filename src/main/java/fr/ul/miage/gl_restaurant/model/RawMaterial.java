package fr.ul.miage.gl_restaurant.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@Setter
@ToString
public class RawMaterial {

    private static final String TABLE_NAME = "RawMaterials";

    private Long rawMaterialId;

    private String rawMaterialLabel;

    private Integer stockQuantity;

    private String unit;

    public RawMaterial() {}

    public RawMaterial(Long rawMaterialId, String rawMaterialLabel, Integer stockQuantity, String unit) {
        this.rawMaterialId = rawMaterialId;
        this.rawMaterialLabel = rawMaterialLabel;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
    }

    public RawMaterial(String rawMaterialLabel, Integer stockQuantity, String unit) {
        this(null, rawMaterialLabel, stockQuantity, unit);
    }

    public RawMaterial(ResultSet resultSet) throws SQLException {
        rawMaterialId = resultSet.getLong("rmId");
        rawMaterialLabel = resultSet.getString("rmLabel");
        stockQuantity = resultSet.getInt("stockQuantity");
        unit = resultSet.getString("unit");
    }
}
