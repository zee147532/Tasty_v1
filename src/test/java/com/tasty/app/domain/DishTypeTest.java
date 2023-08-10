package com.tasty.app.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tasty.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DishTypeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DishType.class);
        DishType dishType1 = new DishType();
        dishType1.setId(1L);
        DishType dishType2 = new DishType();
        dishType2.setId(dishType1.getId());
        assertThat(dishType1).isEqualTo(dishType2);
        dishType2.setId(2L);
        assertThat(dishType1).isNotEqualTo(dishType2);
        dishType1.setId(null);
        assertThat(dishType1).isNotEqualTo(dishType2);
    }
}
