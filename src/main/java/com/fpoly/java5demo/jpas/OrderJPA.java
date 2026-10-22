package com.fpoly.java5demo.jpas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpoly.java5demo.entities.Order;

public interface OrderJPA extends JpaRepository<Order, Integer> {

	@Query(value = "SELECT * FROM orders WHERE user_id = ?1 ORDER BY date DESC, id DESC", nativeQuery = true)
	List<Order> findByUserIdOrderByDateDesc(int userId);

	@Query(value = "SELECT * FROM orders ORDER BY date DESC, id DESC", nativeQuery = true)
	List<Order> findAllByOrderByDateDesc();

	@Query(value = "SELECT * FROM orders ORDER BY date DESC, id DESC", nativeQuery = true)
	List<Order> findAllWithDetails();

	// [Vinh] Lọc đơn hàng theo trạng thái
	@Query(value = "SELECT * FROM orders WHERE status = ?1 ORDER BY date DESC, id DESC", nativeQuery = true)
	List<Order> findByStatusOrderByDateDesc(int status);

	// [Vinh] Lọc đơn hàng theo user và trạng thái
	@Query(value = "SELECT * FROM orders WHERE user_id = ?1 AND status = ?2 ORDER BY date DESC, id DESC", nativeQuery = true)
	List<Order> findByUserIdAndStatusOrderByDateDesc(int userId, int status);

	@Query(value = "SELECT COUNT(*) FROM orders WHERE status = ?1", nativeQuery = true)
	long countByStatus(int status);

	// Doanh thu thực thu: chỉ tính các đơn đã nhận hàng / hoàn thành (status = 5)
	@Query(value = "SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE status = 5", nativeQuery = true)
	Long calculateCompletedRevenue();
}


