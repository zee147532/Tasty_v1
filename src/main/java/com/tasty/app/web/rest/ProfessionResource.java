package com.tasty.app.web.rest;

import com.tasty.app.domain.Profession;
import com.tasty.app.repository.ProfessionRepository;
import com.tasty.app.service.ProfessionService;
import com.tasty.app.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.tasty.app.domain.Profession}.
 */
@RestController
@RequestMapping("/api")
public class ProfessionResource {

    private final Logger log = LoggerFactory.getLogger(ProfessionResource.class);

    private static final String ENTITY_NAME = "profession";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProfessionService professionService;

    private final ProfessionRepository professionRepository;

    public ProfessionResource(ProfessionService professionService, ProfessionRepository professionRepository) {
        this.professionService = professionService;
        this.professionRepository = professionRepository;
    }

    /**
     * {@code POST  /professions} : Create a new profession.
     *
     * @param profession the profession to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new profession, or with status {@code 400 (Bad Request)} if the profession has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/professions")
    public ResponseEntity<Profession> createProfession(@RequestBody Profession profession) throws URISyntaxException {
        log.debug("REST request to save Profession : {}", profession);
        if (profession.getId() != null) {
            throw new BadRequestAlertException("A new profession cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Profession result = professionService.save(profession);
        return ResponseEntity
            .created(new URI("/api/professions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /professions/:id} : Updates an existing profession.
     *
     * @param id the id of the profession to save.
     * @param profession the profession to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated profession,
     * or with status {@code 400 (Bad Request)} if the profession is not valid,
     * or with status {@code 500 (Internal Server Error)} if the profession couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/professions/{id}")
    public ResponseEntity<Profession> updateProfession(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Profession profession
    ) throws URISyntaxException {
        log.debug("REST request to update Profession : {}, {}", id, profession);
        if (profession.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, profession.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Profession result = professionService.update(profession);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, profession.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /professions/:id} : Partial updates given fields of an existing profession, field will ignore if it is null
     *
     * @param id the id of the profession to save.
     * @param profession the profession to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated profession,
     * or with status {@code 400 (Bad Request)} if the profession is not valid,
     * or with status {@code 404 (Not Found)} if the profession is not found,
     * or with status {@code 500 (Internal Server Error)} if the profession couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/professions/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Profession> partialUpdateProfession(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Profession profession
    ) throws URISyntaxException {
        log.debug("REST request to partial update Profession partially : {}, {}", id, profession);
        if (profession.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, profession.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Profession> result = professionService.partialUpdate(profession);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, profession.getId().toString())
        );
    }

    /**
     * {@code GET  /professions} : get all the professions.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of professions in body.
     */
    @GetMapping("/professions")
    public ResponseEntity<List<Profession>> getAllProfessions(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Professions");
        Page<Profession> page = professionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /professions/:id} : get the "id" profession.
     *
     * @param id the id of the profession to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the profession, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/professions/{id}")
    public ResponseEntity<Profession> getProfession(@PathVariable Long id) {
        log.debug("REST request to get Profession : {}", id);
        Optional<Profession> profession = professionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(profession);
    }

    /**
     * {@code DELETE  /professions/:id} : delete the "id" profession.
     *
     * @param id the id of the profession to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/professions/{id}")
    public ResponseEntity<Void> deleteProfession(@PathVariable Long id) {
        log.debug("REST request to delete Profession : {}", id);
        professionService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
