package com.fpoly.java5demo.jpas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpoly.java5demo.entities.Image;

public interface ImageJPA extends JpaRepository<Image, Integer> {

	@Query(value = "SELECT * FROM images WHERE product_id = ?1", nativeQuery = true)
	List<Image> findByProductId(int productId);

	@org.springframework.data.jpa.repository.Modifying
	@org.springframework.transaction.annotation.Transactional
	@Query(value = "DELETE FROM images WHERE product_id = ?1", nativeQuery = true)
	void deleteByProductId(int productId);
}

