package com.fpoly.java5demo.controllers;

import java.util.List;
import java.util.Optional;

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
import com.fpoly.java5demo.beans.ProductFormBean;
import com.fpoly.java5demo.entities.Category;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.services.CategoryServices;
import com.fpoly.java5demo.services.ProductServices;

import jakarta.validation.Valid;

@Controller
public class ProductController {

	@Autowired
	private ProductServices productServices;

	@Autowired
	private CategoryServices categoryServices;

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

	@GetMapping({ "/admin", "/admin/dashboard" })
	public String dashboard(Model model,
			@RequestParam(value = "tab", required = false) String tab,
			@RequestParam(value = "editProductId", required = false) Integer editProductId,
			@RequestParam(value = "editCatId", required = false) Integer editCatId) {
		populateDashboardData(model);

		// Product Form Bean
		ProductFormBean prodBean = new ProductFormBean();
		if (editProductId != null && editProductId > 0) {
			Product p = productServices.findById(editProductId);
			if (p != null) {
				prodBean.setId(p.getId());
				prodBean.setName(p.getName());
				prodBean.setPrice(p.getPrice());
				prodBean.setQuantity(p.getQuantity());
				prodBean.setStatus(p.isStatus() ? 1 : 2);
				prodBean.setDescription(p.getDescription());
				prodBean.setDetail(p.getDetail());
				if (p.getCategory() != null) {
					prodBean.setCategory(p.getCategory().getId());
				}
				model.addAttribute("editingProduct", p);
			}
		}
		model.addAttribute("productBean", prodBean);
		model.addAttribute("bean", prodBean);

		// Category Form Bean
		CategoryFormBean catBean = new CategoryFormBean();
		if (editCatId != null && editCatId > 0) {
			Category c = categoryServices.getById(editCatId);
			if (c != null) {
				catBean.setId(Optional.of(c.getId()));
				catBean.setName(c.getName());
			}
		}
		model.addAttribute("categoryBean", catBean);

		model.addAttribute("activeTab", (tab != null && !tab.isEmpty()) ? tab : "dashboard");
		return "admin/dashboard";
	}

	@GetMapping("/admin/products")
	public String listProducts() {
		return "redirect:/admin?tab=products";
	}

	@GetMapping("/admin/product-form")
	public String productForm() {
		return "redirect:/admin?tab=products";
	}

	@GetMapping("/admin/product-form/{id}")
	public String productEdit(@PathVariable("id") int id) {
		return "redirect:/admin?tab=products&editProductId=" + id;
	}

	@PostMapping("/admin/product-form")
	public String productSave(@Valid @ModelAttribute("bean") ProductFormBean bean, Errors errors,
			Model model) {
		if (errors.hasErrors()) {
			populateDashboardData(model);
			model.addAttribute("productBean", bean);
			model.addAttribute("activeTab", "products");
			return "admin/dashboard";
		}

		Category category = categoryServices.getById(bean.getCategory());
		if (category == null) {
			model.addAttribute("errorMessage", "Vui lòng chọn danh mục hợp lệ!");
			populateDashboardData(model);
			model.addAttribute("productBean", bean);
			model.addAttribute("activeTab", "products");
			return "admin/dashboard";
		}

		Product product;
		boolean isUpdate = (bean.getId() != null && bean.getId() > 0);

		if (isUpdate) {
			product = productServices.findById(bean.getId());
			if (product == null) {
				return "redirect:/admin?tab=products";
			}
		} else {
			product = new Product();
		}

		product.setName(bean.getName());
		product.setPrice(bean.getPrice());
		product.setQuantity(bean.getQuantity());
		if (bean.getQuantity() <= 0) {
			product.setQuantity(0);
			product.setStatus(false); // Số lượng = 0 thì tự động ẩn
		} else {
			product.setStatus(bean.getStatus() == 1);
		}
		product.setCategory(category);
		product.setDescription(bean.getDescription());
		product.setDetail(bean.getDetail());

		String error;
		if (isUpdate) {
			error = productServices.updateProduct(product, bean.getImages());
		} else {
			error = productServices.addProduct(product, bean.getImages());
		}

		if (error != null) {
			errors.rejectValue("name", "duplicate", error);
			model.addAttribute("errorMessage", error);
			populateDashboardData(model);
			model.addAttribute("productBean", bean);
			model.addAttribute("activeTab", "products");
			return "admin/dashboard";
		}

		return "redirect:/admin?tab=products";
	}

	@PostMapping({ "/admin/product-delete", "/admin/product/delete" })
	public String productDelete(@RequestParam("id") int id) {
		productServices.remove(id);
		return "redirect:/admin?tab=products";
	}

	@ModelAttribute("categories")
	public List<Category> getCategories() {
		return categoryServices.getList();
	}
}
