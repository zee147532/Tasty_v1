package com.tasty.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tasty.app.IntegrationTest;
import com.tasty.app.domain.TypeOfDish;
import com.tasty.app.repository.TypeOfDishRepository;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TypeOfDishResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TypeOfDishResourceIT {

    private static final String ENTITY_API_URL = "/api/type-of-dishes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TypeOfDishRepository typeOfDishRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTypeOfDishMockMvc;

    private TypeOfDish typeOfDish;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypeOfDish createEntity(EntityManager em) {
        TypeOfDish typeOfDish = new TypeOfDish();
        return typeOfDish;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TypeOfDish createUpdatedEntity(EntityManager em) {
        TypeOfDish typeOfDish = new TypeOfDish();
        return typeOfDish;
    }

    @BeforeEach
    public void initTest() {
        typeOfDish = createEntity(em);
    }

    @Test
    @Transactional
    void createTypeOfDish() throws Exception {
        int databaseSizeBeforeCreate = typeOfDishRepository.findAll().size();
        // Create the TypeOfDish
        restTypeOfDishMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(typeOfDish)))
            .andExpect(status().isCreated());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeCreate + 1);
        TypeOfDish testTypeOfDish = typeOfDishList.get(typeOfDishList.size() - 1);
    }

    @Test
    @Transactional
    void createTypeOfDishWithExistingId() throws Exception {
        // Create the TypeOfDish with an existing ID
        typeOfDish.setId(1L);

        int databaseSizeBeforeCreate = typeOfDishRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTypeOfDishMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(typeOfDish)))
            .andExpect(status().isBadRequest());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllTypeOfDishes() throws Exception {
        // Initialize the database
        typeOfDishRepository.saveAndFlush(typeOfDish);

        // Get all the typeOfDishList
        restTypeOfDishMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(typeOfDish.getId().intValue())));
    }

    @Test
    @Transactional
    void getTypeOfDish() throws Exception {
        // Initialize the database
        typeOfDishRepository.saveAndFlush(typeOfDish);

        // Get the typeOfDish
        restTypeOfDishMockMvc
            .perform(get(ENTITY_API_URL_ID, typeOfDish.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(typeOfDish.getId().intValue()));
    }

    @Test
    @Transactional
    void getNonExistingTypeOfDish() throws Exception {
        // Get the typeOfDish
        restTypeOfDishMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTypeOfDish() throws Exception {
        // Initialize the database
        typeOfDishRepository.saveAndFlush(typeOfDish);

        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();

        // Update the typeOfDish
        TypeOfDish updatedTypeOfDish = typeOfDishRepository.findById(typeOfDish.getId()).get();
        // Disconnect from session so that the updates on updatedTypeOfDish are not directly saved in db
        em.detach(updatedTypeOfDish);

        restTypeOfDishMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTypeOfDish.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTypeOfDish))
            )
            .andExpect(status().isOk());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
        TypeOfDish testTypeOfDish = typeOfDishList.get(typeOfDishList.size() - 1);
    }

    @Test
    @Transactional
    void putNonExistingTypeOfDish() throws Exception {
        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();
        typeOfDish.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypeOfDishMockMvc
            .perform(
                put(ENTITY_API_URL_ID, typeOfDish.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(typeOfDish))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTypeOfDish() throws Exception {
        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();
        typeOfDish.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeOfDishMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(typeOfDish))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTypeOfDish() throws Exception {
        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();
        typeOfDish.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeOfDishMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(typeOfDish)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTypeOfDishWithPatch() throws Exception {
        // Initialize the database
        typeOfDishRepository.saveAndFlush(typeOfDish);

        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();

        // Update the typeOfDish using partial update
        TypeOfDish partialUpdatedTypeOfDish = new TypeOfDish();
        partialUpdatedTypeOfDish.setId(typeOfDish.getId());

        restTypeOfDishMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypeOfDish.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTypeOfDish))
            )
            .andExpect(status().isOk());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
        TypeOfDish testTypeOfDish = typeOfDishList.get(typeOfDishList.size() - 1);
    }

    @Test
    @Transactional
    void fullUpdateTypeOfDishWithPatch() throws Exception {
        // Initialize the database
        typeOfDishRepository.saveAndFlush(typeOfDish);

        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();

        // Update the typeOfDish using partial update
        TypeOfDish partialUpdatedTypeOfDish = new TypeOfDish();
        partialUpdatedTypeOfDish.setId(typeOfDish.getId());

        restTypeOfDishMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTypeOfDish.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTypeOfDish))
            )
            .andExpect(status().isOk());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
        TypeOfDish testTypeOfDish = typeOfDishList.get(typeOfDishList.size() - 1);
    }

    @Test
    @Transactional
    void patchNonExistingTypeOfDish() throws Exception {
        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();
        typeOfDish.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTypeOfDishMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, typeOfDish.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(typeOfDish))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTypeOfDish() throws Exception {
        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();
        typeOfDish.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeOfDishMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(typeOfDish))
            )
            .andExpect(status().isBadRequest());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTypeOfDish() throws Exception {
        int databaseSizeBeforeUpdate = typeOfDishRepository.findAll().size();
        typeOfDish.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTypeOfDishMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(typeOfDish))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TypeOfDish in the database
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTypeOfDish() throws Exception {
        // Initialize the database
        typeOfDishRepository.saveAndFlush(typeOfDish);

        int databaseSizeBeforeDelete = typeOfDishRepository.findAll().size();

        // Delete the typeOfDish
        restTypeOfDishMockMvc
            .perform(delete(ENTITY_API_URL_ID, typeOfDish.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<TypeOfDish> typeOfDishList = typeOfDishRepository.findAll();
        assertThat(typeOfDishList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
