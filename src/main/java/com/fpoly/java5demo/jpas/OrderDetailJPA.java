package com.fpoly.java5demo.jpas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.fpoly.java5demo.entities.OrderDetail;

public interface OrderDetailJPA extends JpaRepository<OrderDetail, Integer> {

	// [Vinh] Lấy chi tiết đơn hàng theo order_id - SQL thuần theo chuẩn thầy
	@Query(value = "SELECT * FROM order_details WHERE order_id = ?1", nativeQuery = true)
	List<OrderDetail> findByOrderId(int orderId);
}

