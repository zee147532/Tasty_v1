package com.tasty.app.repository;

import com.tasty.app.domain.DishType;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the DishType entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DishTypeRepository extends JpaRepository<DishType, Long> {}
