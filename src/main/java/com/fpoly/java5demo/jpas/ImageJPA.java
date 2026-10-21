package com.fpoly.java5demo.jpas;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.java5demo.entities.Image;

public interface ImageJPA extends JpaRepository<Image, Integer> {
	List<Image> findByProductId(int productId);
}

