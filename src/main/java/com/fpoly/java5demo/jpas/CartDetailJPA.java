package com.fpoly.java5demo.jpas;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.fpoly.java5demo.entities.CartDetail;

public interface CartDetailJPA extends JpaRepository<CartDetail, Integer> {

	@Query(value = "SELECT * FROM cart_details WHERE user_id=?1", nativeQuery = true)
	List<CartDetail> findByUserId(int id);

	@Query(value = "SELECT * FROM cart_details WHERE user_id=?1 AND prod_id=?2", nativeQuery = true)
	Optional<CartDetail> findByUserIdAndProductId(int userId, int productId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM cart_details WHERE user_id=?1", nativeQuery = true)
	void deleteByUserId(int id);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM cart_details WHERE user_id=?1 AND prod_id=?2", nativeQuery = true)
	void deleteByUserIdAndProductId(int userId, int productId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM cart_details WHERE user_id=?1 AND prod_id IN (?2)", nativeQuery = true)
	void deleteByUserIdAndProductIds(int userId, List<Integer> productIds);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM cart_details WHERE prod_id=?1", nativeQuery = true)
	void deleteByProductId(int productId);
}

