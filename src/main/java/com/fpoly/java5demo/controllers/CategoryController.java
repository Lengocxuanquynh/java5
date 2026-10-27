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

	@Autowired
	private com.fpoly.java5demo.services.ProductServices productServices;

	@Autowired
	private com.fpoly.java5demo.services.UserServices userServices;

	@Autowired
	private com.fpoly.java5demo.services.OrderServices orderServices;

	private void populateDashboardData(Model model) {
		model.addAttribute("productCount", productServices.getList().size());
		model.addAttribute("categoryCount", categoryServices.getList().size());
		model.addAttribute("userCount", userServices.getAllUsers().size());
		model.addAttribute("orderCount", orderServices.getOrderCount());
		model.addAttribute("pendingOrderCount", orderServices.getPendingOrderCount());
		model.addAttribute("completedRevenue", orderServices.getCompletedRevenue());
		model.addAttribute("recentOrders", orderServices.getRecentOrders(5));

		model.addAttribute("products", productServices.getList());
		model.addAttribute("categories", categoryServices.getList());
		model.addAttribute("orders", orderServices.getAllOrders());
		model.addAttribute("processingOrders", orderServices.getProcessingOrders());
		model.addAttribute("completedOrders", orderServices.getCompletedOrders());
		model.addAttribute("users", userServices.getAllUsers());
	}

	@GetMapping({ "/admin/categories", "/admin/category-form" })
	public String listCategories() {
		return "redirect:/admin?tab=categories";
	}

	@GetMapping("/admin/category-form/{id}")
	public String categoryUpdate(@PathVariable(name = "id") int id) {
		return "redirect:/admin?tab=categories&editCatId=" + id;
	}

	@PostMapping("/admin/category-form")
	public String categorySave(@Valid @ModelAttribute("categoryBean") CategoryFormBean bean, Errors errors,
			Model model) {
		if (errors.hasErrors()) {
			populateDashboardData(model);
			model.addAttribute("categoryBean", bean);
			model.addAttribute("bean", bean);
			model.addAttribute("productBean", new com.fpoly.java5demo.beans.ProductFormBean());
			model.addAttribute("activeTab", "categories");
			return "admin/dashboard";
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
			errors.rejectValue("name", "duplicate", e.getMessage() != null ? e.getMessage() : "Lỗi lưu danh mục!");
			populateDashboardData(model);
			model.addAttribute("categoryBean", bean);
			model.addAttribute("bean", bean);
			model.addAttribute("productBean", new com.fpoly.java5demo.beans.ProductFormBean());
			model.addAttribute("activeTab", "categories");
			return "admin/dashboard";
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
