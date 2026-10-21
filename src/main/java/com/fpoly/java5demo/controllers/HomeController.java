package com.fpoly.java5demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.java5demo.entities.Category;
import com.fpoly.java5demo.entities.Product;
import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.CategoryServices;
import com.fpoly.java5demo.services.ProductServices;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private ProductServices productServices;

	@Autowired
	private CategoryServices categoryServices;

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserServices userServices;

	@ModelAttribute("categories")
	public List<Category> getCategories() {
		return categoryServices.getList();
	}

	@ModelAttribute("currentUser")
	public User getCurrentUser(HttpServletRequest request, HttpSession session) {
		return userServices.getLoggedInUser(request, session);
	}

	@ModelAttribute("cartCount")
	public int getCartCount() {
		return cartDetailServices.getTotalQuantity();
	}

	@GetMapping({ "/", "/home" })
	public String home(Model model, @RequestParam(name = "cat", required = false) Integer catId,
			@RequestParam(name = "search", required = false) String search) {
		List<Product> products;

		if (search != null && !search.trim().isEmpty()) {
			products = productServices.search(search);
			model.addAttribute("searchKeyword", search);
		} else if (catId != null && catId > 0) {
			products = productServices.getByCategory(catId);
			model.addAttribute("selectedCategory", catId);
		} else {
			products = productServices.getAvailableProducts();
		}

		model.addAttribute("products", products);
		return "home";
	}

	@GetMapping("/product/{id}")
	public String productDetail(@PathVariable("id") int id, Model model) {
		Product product = productServices.findById(id);
		if (product == null || !product.isStatus()) {
			return "redirect:/";
		}

		List<Product> relatedProducts = productServices.getByCategory(product.getCategory().getId());
		// Loại bỏ sản phẩm hiện tại khỏi danh sách gợi ý liên quan
		relatedProducts.removeIf(p -> p.getId() == id);

		model.addAttribute("product", product);
		model.addAttribute("relatedProducts", relatedProducts);
		return "product-detail";
	}

	@GetMapping("/category/{id}")
	public String productByCategory(@PathVariable("id") int id, Model model) {
		Category category = categoryServices.getById(id);
		if (category == null) {
			return "redirect:/";
		}
		List<Product> products = productServices.getByCategory(id);
		model.addAttribute("category", category);
		model.addAttribute("products", products);
		model.addAttribute("selectedCategory", id);
		return "home";
	}

	@GetMapping("/search")
	public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
		List<Product> products = productServices.search(keyword);
		model.addAttribute("products", products);
		model.addAttribute("searchKeyword", keyword);
		return "home";
	}
}
