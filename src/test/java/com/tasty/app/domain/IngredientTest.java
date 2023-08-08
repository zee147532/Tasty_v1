package com.tasty.app.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tasty.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class IngredientTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Ingredient.class);
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setId(1L);
        Ingredient ingredient2 = new Ingredient();
        ingredient2.setId(ingredient1.getId());
        assertThat(ingredient1).isEqualTo(ingredient2);
        ingredient2.setId(2L);
        assertThat(ingredient1).isNotEqualTo(ingredient2);
        ingredient1.setId(null);
        assertThat(ingredient1).isNotEqualTo(ingredient2);
    }
}
