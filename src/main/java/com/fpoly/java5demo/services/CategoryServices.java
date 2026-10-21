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
	CategoryJPA categoryJPA;

	public CategoryServices() {
	}

	public String addCategory(Category category) {
//		Kiểm tra tên danh mục đã tồn tại chưa? 
		try {
			if (category.getName() == null || category.getName().trim().isEmpty()) {
				return "Tên danh mục không được để trống";
			}
			category.setName(category.getName().trim());
			Optional<Category> catOptional = categoryJPA.checkNameExist(category.getName());
			if (catOptional.isPresent()) {
				return "Trùng tên";
			}
			categoryJPA.save(category);
		} catch (Exception e) {
			e.printStackTrace();
			return "Lỗi";
		}
		return null;
	}

	public String updateCategory(Category category) {
		try {
			if (category.getName() == null || category.getName().trim().isEmpty()) {
				return "Tên danh mục không được để trống";
			}
			category.setName(category.getName().trim());
			Optional<Category> catOptional = categoryJPA.checkNameExist(category.getName(), category.getId());
			if (catOptional.isPresent()) {
				return "Trùng tên";
			}
			categoryJPA.save(category);
		} catch (Exception e) {
			e.printStackTrace();
			return "Lỗi";
		}
		return null;
	}

	public List<Category> getList() {
		List<Category> categories = new ArrayList<Category>();
		try {
			categories = categoryJPA.findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return categories;
	}

	public Category getById(int id) {
		try {
			Optional<Category> catOptional = categoryJPA.findById(id);

			return catOptional.isPresent() ? catOptional.get() : null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean deleteCategory(int id) {
		try {
			categoryJPA.deleteById(id);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
