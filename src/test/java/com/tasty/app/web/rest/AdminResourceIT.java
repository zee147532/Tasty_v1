package com.tasty.app.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tasty.app.IntegrationTest;
import com.tasty.app.domain.Admin;
import com.tasty.app.repository.AdminRepository;
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
 * Integration tests for the {@link AdminResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AdminResourceIT {

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

    private static final String DEFAULT_PASSORD = "AAAAAAAAAA";
    private static final String UPDATED_PASSORD = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/admins";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAdminMockMvc;

    private Admin admin;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Admin createEntity(EntityManager em) {
        Admin admin = new Admin().username(DEFAULT_USERNAME).passord(DEFAULT_PASSORD);
        return admin;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Admin createUpdatedEntity(EntityManager em) {
        Admin admin = new Admin().username(UPDATED_USERNAME).passord(UPDATED_PASSORD);
        return admin;
    }

    @BeforeEach
    public void initTest() {
        admin = createEntity(em);
    }

    @Test
    @Transactional
    void createAdmin() throws Exception {
        int databaseSizeBeforeCreate = adminRepository.findAll().size();
        // Create the Admin
        restAdminMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(admin)))
            .andExpect(status().isCreated());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeCreate + 1);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getUsername()).isEqualTo(DEFAULT_USERNAME);
        assertThat(testAdmin.getPassord()).isEqualTo(DEFAULT_PASSORD);
    }

    @Test
    @Transactional
    void createAdminWithExistingId() throws Exception {
        // Create the Admin with an existing ID
        admin.setId(1L);

        int databaseSizeBeforeCreate = adminRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAdminMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(admin)))
            .andExpect(status().isBadRequest());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllAdmins() throws Exception {
        // Initialize the database
        adminRepository.saveAndFlush(admin);

        // Get all the adminList
        restAdminMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(admin.getId().intValue())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME)))
            .andExpect(jsonPath("$.[*].passord").value(hasItem(DEFAULT_PASSORD)));
    }

    @Test
    @Transactional
    void getAdmin() throws Exception {
        // Initialize the database
        adminRepository.saveAndFlush(admin);

        // Get the admin
        restAdminMockMvc
            .perform(get(ENTITY_API_URL_ID, admin.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(admin.getId().intValue()))
            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME))
            .andExpect(jsonPath("$.passord").value(DEFAULT_PASSORD));
    }

    @Test
    @Transactional
    void getNonExistingAdmin() throws Exception {
        // Get the admin
        restAdminMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAdmin() throws Exception {
        // Initialize the database
        adminRepository.saveAndFlush(admin);

        int databaseSizeBeforeUpdate = adminRepository.findAll().size();

        // Update the admin
        Admin updatedAdmin = adminRepository.findById(admin.getId()).get();
        // Disconnect from session so that the updates on updatedAdmin are not directly saved in db
        em.detach(updatedAdmin);
        updatedAdmin.username(UPDATED_USERNAME).passord(UPDATED_PASSORD);

        restAdminMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedAdmin.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedAdmin))
            )
            .andExpect(status().isOk());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testAdmin.getPassord()).isEqualTo(UPDATED_PASSORD);
    }

    @Test
    @Transactional
    void putNonExistingAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().size();
        admin.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdminMockMvc
            .perform(
                put(ENTITY_API_URL_ID, admin.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(admin))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().size();
        admin.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdminMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(admin))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().size();
        admin.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdminMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(admin)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAdminWithPatch() throws Exception {
        // Initialize the database
        adminRepository.saveAndFlush(admin);

        int databaseSizeBeforeUpdate = adminRepository.findAll().size();

        // Update the admin using partial update
        Admin partialUpdatedAdmin = new Admin();
        partialUpdatedAdmin.setId(admin.getId());

        partialUpdatedAdmin.username(UPDATED_USERNAME);

        restAdminMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdmin.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAdmin))
            )
            .andExpect(status().isOk());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testAdmin.getPassord()).isEqualTo(DEFAULT_PASSORD);
    }

    @Test
    @Transactional
    void fullUpdateAdminWithPatch() throws Exception {
        // Initialize the database
        adminRepository.saveAndFlush(admin);

        int databaseSizeBeforeUpdate = adminRepository.findAll().size();

        // Update the admin using partial update
        Admin partialUpdatedAdmin = new Admin();
        partialUpdatedAdmin.setId(admin.getId());

        partialUpdatedAdmin.username(UPDATED_USERNAME).passord(UPDATED_PASSORD);

        restAdminMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAdmin.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAdmin))
            )
            .andExpect(status().isOk());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
        Admin testAdmin = adminList.get(adminList.size() - 1);
        assertThat(testAdmin.getUsername()).isEqualTo(UPDATED_USERNAME);
        assertThat(testAdmin.getPassord()).isEqualTo(UPDATED_PASSORD);
    }

    @Test
    @Transactional
    void patchNonExistingAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().size();
        admin.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAdminMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, admin.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(admin))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().size();
        admin.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdminMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(admin))
            )
            .andExpect(status().isBadRequest());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAdmin() throws Exception {
        int databaseSizeBeforeUpdate = adminRepository.findAll().size();
        admin.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAdminMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(admin)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Admin in the database
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAdmin() throws Exception {
        // Initialize the database
        adminRepository.saveAndFlush(admin);

        int databaseSizeBeforeDelete = adminRepository.findAll().size();

        // Delete the admin
        restAdminMockMvc
            .perform(delete(ENTITY_API_URL_ID, admin.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Admin> adminList = adminRepository.findAll();
        assertThat(adminList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
