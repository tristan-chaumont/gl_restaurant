package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.Units;
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

    private String rawMaterialName;

    private Integer stockQuantity;

    private Units unit;

    public RawMaterial() {}

    public RawMaterial(Long rawMaterialId, String rawMaterialName, Integer stockQuantity, Units unit) {
        this.rawMaterialId = rawMaterialId;
        this.rawMaterialName = rawMaterialName;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
    }

    public RawMaterial(String rawMaterialName, Integer stockQuantity, Units unit) {
        this(null, rawMaterialName, stockQuantity, unit);
    }

    public RawMaterial(ResultSet resultSet) throws SQLException {
        rawMaterialId = resultSet.getLong("rmId");
        rawMaterialName = resultSet.getString("rmName");
        stockQuantity = resultSet.getInt("stockQuantity");
        unit = Units.getUnit(resultSet.getString("unit"));
    }
}
