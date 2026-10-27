package com.fpoly.java5demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.java5demo.beans.CategoryFormBean;
import com.fpoly.java5demo.entities.Category;
import com.fpoly.java5demo.services.CategoryServices;

import jakarta.validation.Valid;

@Controller
public class CategoryController {

	@Autowired
	private CategoryServices categoryServices;

	@GetMapping({ "/admin/categories", "/admin/category-form" })
	public String listCategories() {
		return "redirect:/admin?tab=categories";
	}

	@GetMapping("/admin/category-form/{id}")
	public String categoryUpdate(@PathVariable(name = "id") int id) {
		return "redirect:/admin?tab=categories&editCatId=" + id;
	}

	@PostMapping("/admin/category-form")
	public String categorySave(@Valid @ModelAttribute("bean") CategoryFormBean bean, Errors errors,
			Model model) {
		if (errors.hasErrors()) {
			return "redirect:/admin?tab=categories";
		}

		try {
			Category category = bean.convertBeanToEntity();
			if (bean.getId() != null && bean.getId().isPresent()) {
				categoryServices.updateCategory(category);
			} else {
				categoryServices.addCategory(category);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/admin?tab=categories";
	}

	@PostMapping({ "/admin/category-delete", "/admin/category/delete" })
	public String deleteCategory(@RequestParam("id") int id) {
		try {
			categoryServices.deleteCategory(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin?tab=categories";
	}

	@GetMapping("/admin/category/delete/{id}")
	public String deleteCategoryGet(@PathVariable("id") int id) {
		return deleteCategory(id);
	}

	@ModelAttribute("categories")
	public List<Category> getCategories() {
		return categoryServices.getList();
	}
}
