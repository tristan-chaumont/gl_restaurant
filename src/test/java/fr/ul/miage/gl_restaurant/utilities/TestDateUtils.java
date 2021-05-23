package fr.ul.miage.gl_restaurant.utilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TestDateUtils {

    @Test
    @DisplayName("La durée en seconde entre deux dates est correcte")
    void verifyGetDateDiffSucceed() {
        Timestamp start = Timestamp.valueOf("2021-05-23 08:00:00");
        Timestamp end = Timestamp.valueOf("2021-05-23 09:00:00");
        long duration = DateUtils.getDateDiff(start, end, TimeUnit.SECONDS);
        assertThat(duration, equalTo(3600L));
    }

    @Test
    @DisplayName("La durée en seconde entre deux dates est correcte")
    void verifyGetDateDiffInvertedSucceed() {
        Timestamp start = Timestamp.valueOf("2021-05-23 09:00:00");
        Timestamp end = Timestamp.valueOf("2021-05-23 08:00:00");
        long duration = DateUtils.getDateDiff(start, end, TimeUnit.SECONDS);
        assertThat(duration, equalTo(-3600L));
    }

    @Test
    @DisplayName("isDateLunch() retourne vrai car c'est le déjeuner")
    void verifyIsDateLunchReturnsTrue() {
        LocalDateTime date = LocalDateTime.of(2021, 5, 23, 9, 0, 0);
        assertThat(DateUtils.isDateLunch(date), is(true));
    }

    @Test
    @DisplayName("isDateLunch() retourne faux car c'est le dîner")
    void verifyIsDateLunchReturnsFalse() {
        LocalDateTime date = LocalDateTime.of(2021, 5, 23, 17, 0, 0);
        assertThat(DateUtils.isDateLunch(date), is(false));
    }
}
