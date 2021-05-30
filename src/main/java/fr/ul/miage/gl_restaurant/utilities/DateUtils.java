package fr.ul.miage.gl_restaurant.utilities;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    private DateUtils() {}

    /**
     * Retourne la durée en <unit> entre deux dates.
     * @param startDate Date de départ.
     * @param endDate Date d'arrivée.
     * @param unit Unité dans laquelle convertir.
     * @return La durée en <unit>
     */
    public static long getDateDiff(Timestamp startDate, Timestamp endDate, TimeUnit unit) {
        long diffInMs = endDate.getTime() - startDate.getTime();
        return unit.convert(diffInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Regarde si la date passée se situe au moment du déjeuner ou du dîner.
     * @param date Date à vérifier.
     * @return True si déjeuner, false sinon.
     */
    public static boolean isDateLunch(LocalDateTime date) {
        return date.getHour() < 17;
    }
}
