package com.tasty.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tasty.app.IntegrationTest;
import com.tasty.app.domain.DishType;
import com.tasty.app.repository.DishTypeRepository;
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
 * Integration tests for the {@link DishTypeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DishTypeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/dish-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DishTypeRepository dishTypeRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDishTypeMockMvc;

    private DishType dishType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DishType createEntity(EntityManager em) {
        DishType dishType = new DishType().name(DEFAULT_NAME);
        return dishType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static DishType createUpdatedEntity(EntityManager em) {
        DishType dishType = new DishType().name(UPDATED_NAME);
        return dishType;
    }

    @BeforeEach
    public void initTest() {
        dishType = createEntity(em);
    }

    @Test
    @Transactional
    void createDishType() throws Exception {
        int databaseSizeBeforeCreate = dishTypeRepository.findAll().size();
        // Create the DishType
        restDishTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dishType)))
            .andExpect(status().isCreated());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeCreate + 1);
        DishType testDishType = dishTypeList.get(dishTypeList.size() - 1);
        assertThat(testDishType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createDishTypeWithExistingId() throws Exception {
        // Create the DishType with an existing ID
        dishType.setId(1L);

        int databaseSizeBeforeCreate = dishTypeRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDishTypeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dishType)))
            .andExpect(status().isBadRequest());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllDishTypes() throws Exception {
        // Initialize the database
        dishTypeRepository.saveAndFlush(dishType);

        // Get all the dishTypeList
        restDishTypeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dishType.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getDishType() throws Exception {
        // Initialize the database
        dishTypeRepository.saveAndFlush(dishType);

        // Get the dishType
        restDishTypeMockMvc
            .perform(get(ENTITY_API_URL_ID, dishType.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dishType.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getNonExistingDishType() throws Exception {
        // Get the dishType
        restDishTypeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDishType() throws Exception {
        // Initialize the database
        dishTypeRepository.saveAndFlush(dishType);

        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();

        // Update the dishType
        DishType updatedDishType = dishTypeRepository.findById(dishType.getId()).get();
        // Disconnect from session so that the updates on updatedDishType are not directly saved in db
        em.detach(updatedDishType);
        updatedDishType.name(UPDATED_NAME);

        restDishTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedDishType.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedDishType))
            )
            .andExpect(status().isOk());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
        DishType testDishType = dishTypeList.get(dishTypeList.size() - 1);
        assertThat(testDishType.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void putNonExistingDishType() throws Exception {
        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();
        dishType.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDishTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dishType.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dishType))
            )
            .andExpect(status().isBadRequest());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDishType() throws Exception {
        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();
        dishType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDishTypeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dishType))
            )
            .andExpect(status().isBadRequest());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDishType() throws Exception {
        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();
        dishType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDishTypeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dishType)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDishTypeWithPatch() throws Exception {
        // Initialize the database
        dishTypeRepository.saveAndFlush(dishType);

        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();

        // Update the dishType using partial update
        DishType partialUpdatedDishType = new DishType();
        partialUpdatedDishType.setId(dishType.getId());

        restDishTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDishType.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDishType))
            )
            .andExpect(status().isOk());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
        DishType testDishType = dishTypeList.get(dishTypeList.size() - 1);
        assertThat(testDishType.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void fullUpdateDishTypeWithPatch() throws Exception {
        // Initialize the database
        dishTypeRepository.saveAndFlush(dishType);

        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();

        // Update the dishType using partial update
        DishType partialUpdatedDishType = new DishType();
        partialUpdatedDishType.setId(dishType.getId());

        partialUpdatedDishType.name(UPDATED_NAME);

        restDishTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDishType.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDishType))
            )
            .andExpect(status().isOk());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
        DishType testDishType = dishTypeList.get(dishTypeList.size() - 1);
        assertThat(testDishType.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingDishType() throws Exception {
        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();
        dishType.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDishTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dishType.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dishType))
            )
            .andExpect(status().isBadRequest());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDishType() throws Exception {
        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();
        dishType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDishTypeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dishType))
            )
            .andExpect(status().isBadRequest());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDishType() throws Exception {
        int databaseSizeBeforeUpdate = dishTypeRepository.findAll().size();
        dishType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDishTypeMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(dishType)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the DishType in the database
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDishType() throws Exception {
        // Initialize the database
        dishTypeRepository.saveAndFlush(dishType);

        int databaseSizeBeforeDelete = dishTypeRepository.findAll().size();

        // Delete the dishType
        restDishTypeMockMvc
            .perform(delete(ENTITY_API_URL_ID, dishType.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<DishType> dishTypeList = dishTypeRepository.findAll();
        assertThat(dishTypeList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
