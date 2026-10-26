package com.fpoly.java5demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.fpoly.java5demo.entities.Category;
import com.fpoly.java5demo.jpas.CategoryJPA;

@Service
public class CategoryServices {

	@Autowired
	private CategoryJPA categoryJPA;

	public CategoryServices() {
	}

	public Category addCategory(Category category) {
		if (category.getName() == null || category.getName().trim().isEmpty()) {
			throw new RuntimeException("Tên danh mục không được để trống!");
		}
		category.setName(category.getName().trim());

		Optional<Category> catOptional = categoryJPA.checkNameExist(category.getName());
		if (catOptional.isPresent()) {
			throw new RuntimeException("Tên danh mục đã tồn tại!");
		}

		return categoryJPA.save(category);
	}

	public Category updateCategory(Category category) {
		if (category.getName() == null || category.getName().trim().isEmpty()) {
			throw new RuntimeException("Tên danh mục không được để trống!");
		}
		category.setName(category.getName().trim());

		Optional<Category> existingOpt = categoryJPA.findById(category.getId());
		if (!existingOpt.isPresent()) {
			throw new RuntimeException("Danh mục không tồn tại trên hệ thống!");
		}

		Optional<Category> catOptional = categoryJPA.checkNameExist(category.getName(), category.getId());
		if (catOptional.isPresent()) {
			throw new RuntimeException("Tên danh mục đã tồn tại!");
		}

		return categoryJPA.save(category);
	}

	public List<Category> getList() {
		return categoryJPA.findAllCategories();
	}

	public Category getById(int id) {
		Optional<Category> catOptional = categoryJPA.findById(id);
		if (!catOptional.isPresent()) {
			throw new RuntimeException("Không tìm thấy danh mục có ID: " + id);
		}
		return catOptional.get();
	}

	public void deleteCategory(int id) {
		Optional<Category> catOptional = categoryJPA.findById(id);
		if (!catOptional.isPresent()) {
			throw new RuntimeException("Danh mục không tồn tại để xóa!");
		}

		Category category = catOptional.get();
		if (category.getProducts() != null && !category.getProducts().isEmpty()) {
			throw new RuntimeException("Không thể xóa danh mục đã chứa sản phẩm!");
		}

		categoryJPA.deleteById(id);
	}
}
