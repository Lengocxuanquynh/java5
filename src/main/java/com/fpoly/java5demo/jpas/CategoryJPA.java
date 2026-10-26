package com.fpoly.java5demo.jpas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpoly.java5demo.entities.Category;

public interface CategoryJPA extends JpaRepository<Category, Integer> {

	@Query(value = "SELECT * FROM categories WHERE LOWER(TRIM(name)) = LOWER(TRIM(?1))", nativeQuery = true)
	Optional<Category> checkNameExist(String name);

	@Query(value = "SELECT * FROM categories WHERE LOWER(TRIM(name)) = LOWER(TRIM(?1)) AND id != ?2", nativeQuery = true)
	Optional<Category> checkNameExist(String name, int id);

	@Query(value = "SELECT * FROM categories", nativeQuery = true)
	List<Category> findAllCategories();
}
