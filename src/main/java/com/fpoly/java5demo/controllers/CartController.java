package com.fpoly.java5demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.java5demo.entities.User;
import com.fpoly.java5demo.services.CartDetailServices;
import com.fpoly.java5demo.services.UserServices;

import jakarta.servlet.http.HttpServletRequest;

// [CartController] Quản lý giỏ hàng chuẩn phong cách thầy (Java5-SD21201)
// Bắt buộc đăng nhập khi xem giỏ hàng hoặc thêm sản phẩm, dùng Cookie & Database, không dùng Session
@Controller
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private CartDetailServices cartDetailServices;

	@Autowired
	private UserServices userServices;

	@GetMapping("")
	public String viewCart(@RequestParam(name = "error", required = false) String error,
			Model model, HttpServletRequest request) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		if ("admin_cannot_buy".equals(error) || currentUser.getRole() == 1) {
			model.addAttribute("errorMessage", "Tài khoản Quản Trị Viên (Admin) không được phép mua hàng!");
		}

		model.addAttribute("currentUser", currentUser);
		model.addAttribute("cartItems", cartDetailServices.getCartItems(currentUser.getId()));
		model.addAttribute("totalAmount", cartDetailServices.getTotalAmount(currentUser.getId()));
		model.addAttribute("cartCount", cartDetailServices.getTotalQuantity(currentUser.getId()));
		return "cart";
	}

	@PostMapping("/add")
	public String addToCart(@RequestParam("productId") int productId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity,
			@RequestParam(name = "buyNow", defaultValue = "false") boolean buyNow,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/product/" + productId;
		}

		if (currentUser.getRole() == 1) {
			redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản Quản Trị Viên (Admin) không được phép mua hàng!");
			return "redirect:/product/" + productId;
		}

		try {
			cartDetailServices.addToCart(currentUser.getId(), productId, quantity);
			if (buyNow) {
				return "redirect:/checkout?items=" + productId;
			}
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/product/" + productId;
		}
		return "redirect:/cart";
	}

	@GetMapping("/add/{id}")
	public String addToCartQuick(@PathVariable("id") int productId,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		if (currentUser.getRole() == 1) {
			redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản Quản Trị Viên (Admin) không được phép mua hàng!");
			return "redirect:/product/" + productId;
		}

		try {
			cartDetailServices.addToCart(currentUser.getId(), productId, 1);
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/cart";
	}

	@PostMapping("/update")
	public String updateQuantity(@RequestParam("productId") int productId,
			@RequestParam("quantity") int quantity,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		try {
			cartDetailServices.updateQuantity(currentUser.getId(), productId, quantity);
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/cart";
	}

	@GetMapping("/remove/{id}")
	public String removeFromCart(@PathVariable("id") int productId,
			HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		try {
			cartDetailServices.removeCart(currentUser.getId(), productId);
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/cart";
	}

	@GetMapping("/clear")
	public String clearCart(HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User currentUser = userServices.getLoggedInUser(request);
		if (currentUser == null) {
			return "redirect:/login?redirect=/cart";
		}

		try {
			cartDetailServices.clearCart(currentUser.getId());
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/cart";
	}
}
