package com.tasty.app.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tasty.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TypeOfDishTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TypeOfDish.class);
        TypeOfDish typeOfDish1 = new TypeOfDish();
        typeOfDish1.setId(1L);
        TypeOfDish typeOfDish2 = new TypeOfDish();
        typeOfDish2.setId(typeOfDish1.getId());
        assertThat(typeOfDish1).isEqualTo(typeOfDish2);
        typeOfDish2.setId(2L);
        assertThat(typeOfDish1).isNotEqualTo(typeOfDish2);
        typeOfDish1.setId(null);
        assertThat(typeOfDish1).isNotEqualTo(typeOfDish2);
    }
}
