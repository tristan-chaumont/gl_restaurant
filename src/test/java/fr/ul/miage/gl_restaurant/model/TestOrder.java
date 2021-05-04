package fr.ul.miage.gl_restaurant.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestOrder {

    @Test
    @DisplayName("La première date est située avant la seconde")
    void verifyCompareToDateIsBefore() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = simpleDateFormat.parse("2021-01-21");
            date2 = simpleDateFormat.parse("2021-01-24");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertNotNull(date1);
        assertNotNull(date2);
        Order order1 = new Order(new Timestamp(date1.getTime()), new Timestamp(date1.getTime()), new Meal());
        Order order2 = new Order(new Timestamp(date2.getTime()), new Timestamp(date2.getTime()), new Meal());
        int result = order1.compareTo(order2);
        assertThat(result, is(-1));
    }

    @Test
    @DisplayName("La première date est située après la première")
    void verifyCompareToDateIsAfter() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = simpleDateFormat.parse("2021-01-21");
            date2 = simpleDateFormat.parse("2021-01-24");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertNotNull(date1);
        assertNotNull(date2);
        Order order1 = new Order(new Timestamp(date1.getTime()), new Timestamp(date1.getTime()), new Meal());
        Order order2 = new Order(new Timestamp(date2.getTime()), new Timestamp(date2.getTime()), new Meal());
        int result = order2.compareTo(order1);
        assertThat(result, is(1));
    }
    @Test
    @DisplayName("La première date est située après la première")
    void verifyCompareToDatesAreEqual() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = simpleDateFormat.parse("2021-01-21");
            assertNotNull(date1);
            date2 = (Date) date1.clone();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Order order1 = new Order(new Timestamp(date1.getTime()), new Timestamp(date1.getTime()), new Meal());
        Order order2 = new Order(new Timestamp(date2.getTime()), new Timestamp(date2.getTime()), new Meal());
        int result = order1.compareTo(order2);
        assertThat(result, is(0));
    }
}
