package com.fpoly.java5demo.jpas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fpoly.java5demo.entities.Product;

public interface ProductJPA extends JpaRepository<Product, Integer> {
	List<Product> findByStatusTrue();

	List<Product> findByCategoryIdAndStatusTrue(int categoryId);

	List<Product> findByNameContainingIgnoreCaseAndStatusTrue(String name);

	List<Product> findByNameContainingIgnoreCase(String name);

	@Query(value = "SELECT * FROM products WHERE status = 1 ORDER BY id DESC", nativeQuery = true)
	List<Product> findTopRecentProducts();

	@Query(value = "SELECT * FROM products WHERE LOWER(TRIM(name)) = LOWER(TRIM(?1))", nativeQuery = true)
	Optional<Product> checkNameExist(String name);

	@Query(value = "SELECT * FROM products WHERE LOWER(TRIM(name)) = LOWER(TRIM(?1)) AND id != ?2", nativeQuery = true)
	Optional<Product> checkNameExist(String name, int id);

	Optional<Product> findByNameIgnoreCase(String name);

	Optional<Product> findByNameIgnoreCaseAndIdNot(String name, int id);
}

