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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.java5demo.beans.CategoryFormBean;
import com.fpoly.java5demo.entities.Category;
import com.fpoly.java5demo.services.CategoryServices;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class CategoryController {

	@Autowired
	private CategoryServices categoryServices;

	@GetMapping({ "/categories", "/category-form" })
	public String listCategories() {
		return "redirect:/admin?tab=categories";
	}

	@GetMapping("/category-form/{id}")
	public String categoryUpdate(@PathVariable(name = "id") int id) {
		return "redirect:/admin?tab=categories&editCatId=" + id;
	}

	@PostMapping("/category-form")
	public String categorySave(@Valid @ModelAttribute("bean") CategoryFormBean bean, Errors errors,
			Model model, RedirectAttributes redirectAttributes) {
		if (errors.hasErrors()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Tên danh mục không được để trống!");
			return "redirect:/admin?tab=categories";
		}

		try {
			Category category = bean.convertBeanToEntity();
			if (bean.getId() != null && bean.getId().isPresent()) {
				categoryServices.updateCategory(category);
			} else {
				categoryServices.addCategory(category);
			}
			redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công!");
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}

		return "redirect:/admin?tab=categories";
	}

	@GetMapping("/category/delete/{id}")
	public String deleteCategory(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
		try {
			categoryServices.deleteCategory(id);
			redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/admin?tab=categories";
	}

	@ModelAttribute("categories")
	public List<Category> getCategories() {
		return categoryServices.getList();
	}
}
