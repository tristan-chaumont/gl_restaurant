package fr.ul.miage.gl_restaurant.utilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class TestInputUtils {

    @Test
    @DisplayName("readInput retourne la valeur entrée")
    void verifyReadInputSucceed() {
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInput).thenReturn("Test");
            assertThat(InputUtils.readInput(), equalTo("Test"));
        }
    }

    /* @Test
    @DisplayName("readInput retourne la valeur entrée")
    void verifyReadInputInArraySucceed() {
        try (MockedStatic<InputUtils> utilities = Mockito.mockStatic(InputUtils.class)) {
            utilities.when(InputUtils::readInput).thenReturn("5", "2");
            String actual = InputUtils.readInputInArray(Arrays.asList("1", "2", "3"));
            assertThat(actual, equalTo("2"));
        }
    }*/
}
