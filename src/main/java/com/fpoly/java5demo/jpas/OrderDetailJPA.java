package com.fpoly.java5demo.jpas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java5demo.entities.OrderDetail;

public interface OrderDetailJPA extends JpaRepository<OrderDetail, Integer> {
	List<OrderDetail> findByOrderId(int orderId);
}

