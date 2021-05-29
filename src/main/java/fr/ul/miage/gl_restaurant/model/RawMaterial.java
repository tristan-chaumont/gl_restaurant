package fr.ul.miage.gl_restaurant.model;

import fr.ul.miage.gl_restaurant.constants.Units;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@Setter
@Data
public class RawMaterial {

    private Long rawMaterialId;

    private String rawMaterialName;

    private Integer stockQuantity;

    private Units unit;

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

    @Override
    public String toString() {
        var stringBuilder = new TextStringBuilder();
        var size = 16;
        if (!StringUtils.isBlank(rawMaterialName) && rawMaterialName.length() > 16) {
            size = rawMaterialName.length();
        }
        stringBuilder.appendln("-".repeat(size + 4))
                .appendln("| " + StringUtils.center(rawMaterialName, size) + " |")
                .appendln("-".repeat(size + 4))
                .appendln("Quantité en stock : %d", stockQuantity)
                .appendln("Unité : %s", unit.toString());
        return stringBuilder.toString();
    }
}
