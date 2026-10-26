package com.fpoly.java5demo.jpas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpoly.java5demo.entities.Product;

public interface ProductJPA extends JpaRepository<Product, Integer> {

	@Query(value = "SELECT * FROM products WHERE status = 1", nativeQuery = true)
	List<Product> findByStatusTrue();

	@Query(value = "SELECT * FROM products WHERE cat_id = ?1 AND status = 1", nativeQuery = true)
	List<Product> findByCategoryIdAndStatusTrue(int categoryId);

	@Query(value = "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?1, '%')) AND status = 1", nativeQuery = true)
	List<Product> findByNameContainingAndStatusTrue(String name);

	@Query(value = "SELECT * FROM products WHERE status = 1 ORDER BY id DESC", nativeQuery = true)
	List<Product> findTopRecentProducts();

	@Query(value = "SELECT * FROM products WHERE LOWER(TRIM(name)) = LOWER(TRIM(?1))", nativeQuery = true)
	Optional<Product> checkNameExist(String name);

	@Query(value = "SELECT * FROM products WHERE LOWER(TRIM(name)) = LOWER(TRIM(?1)) AND id != ?2", nativeQuery = true)
	Optional<Product> checkNameExist(String name, int id);

	@Query(value = "SELECT * FROM products", nativeQuery = true)
	List<Product> findAllProducts();
}

